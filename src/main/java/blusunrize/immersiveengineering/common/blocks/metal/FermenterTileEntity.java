/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.util.shapes.MultiblockCacheKey;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class FermenterTileEntity extends PoweredMultiblockTileEntity<FermenterTileEntity, FermenterRecipe> implements
		IBlockBounds, IInteractionObjectIE
{
	public static TileEntityType<FermenterTileEntity> TYPE;
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000)};
	public NonNullList<ItemStack> inventory = NonNullList.withSize(11, ItemStack.EMPTY);

	public FermenterTileEntity()
	{
		super(IEMultiblocks.FERMENTER, 16000, true, TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank"));
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getList("inventory", 10), 11);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		CompoundNBT tankTag = tanks[0].writeToNBT(new CompoundNBT());
		nbt.put("tank", tankTag);
		if(!descPacket)
			nbt.put("inventory", Utils.writeInventory(inventory));
	}

	private CapabilityReference<IItemHandler> outputCap = CapabilityReference.forTileEntity(this,
			() -> {
				Direction fw = getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY();
				return new DirectionalBlockPos(this.getPos().offset(fw), fw.getOpposite());
			}, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void tick()
	{
		super.tick();
		if(isDummy()||isRSDisabled())
			return;

		if(!world.isRemote)
		{
			boolean update = false;
			if(energyStorage.getEnergyStored() > 0&&processQueue.size() < this.getProcessQueueMaxLength())
			{
				final int[] usedInvSlots = new int[8];
				for(MultiblockProcess<FermenterRecipe> process : processQueue)
					if(process instanceof MultiblockProcessInMachine)
						for(int i : ((MultiblockProcessInMachine<FermenterRecipe>)process).getInputSlots())
							usedInvSlots[i]++;

				Integer[] preferredSlots = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7};
				Arrays.sort(preferredSlots, 0, 8, Comparator.comparingInt(arg0 -> usedInvSlots[arg0]));
				for(int slot : preferredSlots)
				{
					ItemStack stack = this.getInventory().get(slot);
					if(!stack.isEmpty())
					{
						stack = stack.copy();
						stack.shrink(usedInvSlots[slot]);
					}
					if(!stack.isEmpty()&&stack.getCount() > 0)
					{
						FermenterRecipe recipe = this.findRecipeForInsertion(stack);
						if(recipe!=null)
						{
							MultiblockProcessInMachine<FermenterRecipe> process = new MultiblockProcessInMachine<>(recipe, slot);
							if(this.addProcessToQueue(process, true))
							{
								this.addProcessToQueue(process, false);
								update = true;
							}
						}
					}
				}
			}

			Direction fw = getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY();
			if(this.tanks[0].getFluidAmount() > 0)
			{
				FluidStack out = Utils.copyFluidStackWithAmount(this.tanks[0].getFluid(), Math.min(this.tanks[0].getFluidAmount(), 80), false);
				BlockPos outputPos = this.getPos().add(0, -1, 0).offset(fw, 2);
				update |= FluidUtil.getFluidHandler(world, outputPos, fw.getOpposite())
						.map(output -> {
							int accepted = output.fill(out, FluidAction.SIMULATE);
					if(accepted > 0)
					{
						int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
						this.tanks[0].drain(drained, FluidAction.EXECUTE);
						return true;
					}
							return false;
						}).orElse(false);
				ItemStack empty = getInventory().get(9);
				if(!empty.isEmpty()&&tanks[0].getFluidAmount() > 0)
				{
					ItemStack full = Utils.fillFluidContainer(tanks[0], empty, getInventory().get(10), null);
					if(!full.isEmpty())
					{
						if(getInventory().get(9).getCount()==1&&!Utils.isFluidContainerFull(full))
							getInventory().set(9, full.copy());
						else
						{
							if(!getInventory().get(10).isEmpty()&&ItemHandlerHelper.canItemStacksStack(full, getInventory().get(10)))
								getInventory().get(10).grow(full.getCount());
							else
								getInventory().set(10, full);
							inventory.get(9).shrink(1);
							if(inventory.get(9).getCount() <= 0)
								inventory.set(9, ItemStack.EMPTY);
						}
						update = true;
					}
				}
			}
			if(!inventory.get(8).isEmpty()&&world.getGameTime()%8==0)
			{
				if(outputCap.isPresent())
				{
					ItemStack stack = Utils.copyStackWithAmount(inventory.get(8), 1);
					stack = Utils.insertStackIntoInventory(outputCap, stack, false);
					if(stack.isEmpty())
					{
						this.inventory.get(8).shrink(1);
						if(this.inventory.get(8).getCount() <= 0)
							this.inventory.set(8, ItemStack.EMPTY);
					}
				}
			}

			if(update)
			{
				this.markDirty();
				this.markContainingBlockForUpdate(null);
			}
		}
	}

	private static final CachedVoxelShapes<MultiblockCacheKey> SHAPES = new CachedVoxelShapes<>(FermenterTileEntity::getShape);

	@Override
	public VoxelShape getBlockBounds()
	{
		return SHAPES.get(new MultiblockCacheKey(this));
	}

	private static List<AxisAlignedBB> getShape(MultiblockCacheKey key)
	{
		BlockPos posInMultiblock = key.posInMultiblock;
		Direction fl = key.facing;
		Direction fw = key.facing.rotateY();
		if(key.mirrored)
			fw = fw.getOpposite();
		if(new BlockPos(2, 0, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1));
			float minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .125f;
			float maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .25f;
			float minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .125f;
			float maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .25f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ));

			minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .75f;
			maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .875f;
			minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .75f;
			maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .875f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ));
			return list;
		}
		if(new MutableBoundingBox(0, 0, 0, 1, 0, 1)
				.isVecInside(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1));
			if(posInMultiblock.getZ()==0)
				fl = fl.getOpposite();
			if(posInMultiblock.getX()==1)
				fw = fw.getOpposite();
			float minX = fl==Direction.WEST?.6875f: fl==Direction.EAST?.0625f: fw==Direction.EAST?.0625f: .6875f;
			float maxX = fl==Direction.EAST?.3125f: fl==Direction.WEST?.9375f: fw==Direction.EAST?.3125f: .9375f;
			float minZ = fl==Direction.NORTH?.6875f: fl==Direction.SOUTH?.0625f: fw==Direction.SOUTH?.0625f: .6875f;
			float maxZ = fl==Direction.SOUTH?.3125f: fl==Direction.NORTH?.9375f: fw==Direction.SOUTH?.3125f: .9375f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1.1875f, maxZ));

			if(new BlockPos(1, 0, 1).equals(posInMultiblock))
			{
				minX = fl==Direction.WEST?.375f: fl==Direction.EAST?.625f: fw==Direction.WEST?-.125f: 0;
				maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.625f: fw==Direction.EAST?1.125f: 1;
				minZ = fl==Direction.NORTH?.375f: fl==Direction.SOUTH?.625f: fw==Direction.NORTH?-.125f: 0;
				maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.625f: fw==Direction.SOUTH?1.125f: 1;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, .75f, maxZ));

				minX = fl==Direction.WEST?-.125f: fl==Direction.EAST?.625f: fw==Direction.WEST?-.125f: .875f;
				maxX = fl==Direction.EAST?1.125f: fl==Direction.WEST?.375f: fw==Direction.EAST?1.125f: .125f;
				minZ = fl==Direction.NORTH?-.125f: fl==Direction.SOUTH?.625f: fw==Direction.NORTH?-.125f: .875f;
				maxZ = fl==Direction.SOUTH?1.25f: fl==Direction.NORTH?.375f: fw==Direction.SOUTH?1.125f: .125f;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, .75f, maxZ));

				minX = fl==Direction.WEST?-.125f: fl==Direction.EAST?.875f: fw==Direction.WEST?-.125f: .875f;
				maxX = fl==Direction.EAST?1.125f: fl==Direction.WEST?.125f: fw==Direction.EAST?1.125f: .125f;
				minZ = fl==Direction.NORTH?-.125f: fl==Direction.SOUTH?.875f: fw==Direction.NORTH?-.125f: .875f;
				maxZ = fl==Direction.SOUTH?1.25f: fl==Direction.NORTH?.125f: fw==Direction.SOUTH?1.125f: .125f;
				list.add(new AxisAlignedBB(minX, .75f, minZ, maxX, 1, maxZ));
			}

			return list;
		}
		if(new MutableBoundingBox(0, 1, 0, 1, 2, 1).isVecInside(posInMultiblock))
		{
			List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>(2);
			if(posInMultiblock.getZ()==0)
				fl = fl.getOpposite();
			if(posInMultiblock.getX()==1)
				fw = fw.getOpposite();
			float minY = posInMultiblock.getY() < 2?.1875f: -.8125f;
			float maxY = posInMultiblock.getY() < 2?2: 1;

			float minX = fl==Direction.WEST?0: fl==Direction.EAST?.0625f: fw==Direction.EAST?.0625f: 0;
			float maxX = fl==Direction.EAST?1: fl==Direction.WEST?.9375f: fw==Direction.EAST?1: .9375f;
			float minZ = fl==Direction.NORTH?0: fl==Direction.SOUTH?.0625f: fw==Direction.SOUTH?.0625f: 0;
			float maxZ = fl==Direction.SOUTH?1: fl==Direction.NORTH?.9375f: fw==Direction.SOUTH?1: .9375f;
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
			return list;
		}
		return null;
	}

	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 1, 2)
		);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(2, 1, 2)
		);
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<FermenterRecipe> process)
	{
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
			Utils.dropStackAtPos(world, getPos().offset(getFacing(), 2), output, getFacing());
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<FermenterRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 8;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 8;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<FermenterRecipe> process)
	{
		return 0;
	}


	@Override
	public int getComparatedSize()
	{
		return 8;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public int[] getOutputSlots()
	{
		return new int[]{8};
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[]{0};
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return tanks;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		FermenterTileEntity master = this.master();
		if(master!=null&&new BlockPos(2, 0, 1).equals(posInMultiblock)&&(side==null||side==(getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY())))
			return master.tanks;
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return true;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	private LazyOptional<IItemHandler> insertionHandler = registerConstantCap(
			new IEInventoryHandler(8, this, 0, new boolean[]{true, true, true, true, true, true, true, true}, new boolean[8])
	);
	private LazyOptional<IItemHandler> extractionHandler = registerConstantCap(
			new IEInventoryHandler(1, this, 8, new boolean[1], new boolean[]{true})
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(ImmutableSet.of(
				new BlockPos(1, 1, 1),
				new BlockPos(0, 1, 0)
		).contains(posInMultiblock)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			FermenterTileEntity master = master();
			if(master!=null)
			{
				if(posInMultiblock.getX()==0)
					return master.insertionHandler.cast();
				else
					return master.extractionHandler.cast();
			}
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public FermenterRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return FermenterRecipe.findRecipe(inserting);
	}

	@Override
	protected FermenterRecipe getRecipeForId(ResourceLocation id)
	{
		return FermenterRecipe.recipeList.get(id);
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return formed;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return master();
	}
}