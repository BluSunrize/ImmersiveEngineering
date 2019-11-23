/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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

public class SqueezerTileEntity extends PoweredMultiblockTileEntity<SqueezerTileEntity, SqueezerRecipe> implements
		IAdvancedSelectionBounds, IAdvancedCollisionBounds, IInteractionObjectIE
{
	public static TileEntityType<SqueezerTileEntity> TYPE;
	
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000)};
	public NonNullList<ItemStack> inventory = NonNullList.withSize(11, ItemStack.EMPTY);
	public float animation_piston = 0;
	public boolean animation_down = true;
	private CapabilityReference<IItemHandler> outputCap = CapabilityReference.forTileEntity(this, this::getOutputPos,
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	public SqueezerTileEntity()
	{
		super(IEMultiblocks.SQUEEZER, 16000, true, TYPE);
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

	@Override
	public void tick()
	{
		super.tick();
		if(isDummy()||isRSDisabled())
			return;

		if(world.isRemote)
		{
			if(this.processQueue.isEmpty()&&animation_piston < .6875)
				animation_piston = Math.min(.6875f, animation_piston+.03125f);
			else if(shouldRenderAsActive())
			{
				if(animation_down)
					animation_piston = Math.max(0, animation_piston-.03125f);
				else
					animation_piston = Math.min(.6875f, animation_piston+.03125f);
				if(animation_piston <= 0&&animation_down)
					animation_down = false;
				else if(animation_piston >= .6875&&!animation_down)
					animation_down = true;
			}
		}
		else
		{
			boolean update = false;
			if(energyStorage.getEnergyStored() > 0&&processQueue.size() < this.getProcessQueueMaxLength())
			{
				final int[] usedInvSlots = new int[8];
				for(MultiblockProcess process : processQueue)
					if(process instanceof MultiblockProcessInMachine)
						for(int i : ((MultiblockProcessInMachine)process).getInputSlots())
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
						SqueezerRecipe recipe = this.findRecipeForInsertion(stack);
						if(recipe!=null)
						{
							MultiblockProcessInMachine<SqueezerRecipe> process = new MultiblockProcessInMachine<>(recipe, slot);
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
				update |= FluidUtil.getFluidHandler(world, outputPos, fw.getOpposite()).map(output -> {
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
				BlockPos outputPos = this.getPos();
				TileEntity outputTile = Utils.getExistingTileEntity(world, outputPos);
				if(outputTile!=null)
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

	private DirectionalBlockPos getOutputPos()
	{
		Direction fw = getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY();
		return new DirectionalBlockPos(pos.offset(fw), fw.getOpposite());
	}

	@Override
	public float[] getBlockBounds()
	{
		if(posInMultiblock.getY()==0&&!ImmutableSet.of(
				new BlockPos(0, 0, 0),
				new BlockPos(1, 0, 2)
		).contains(posInMultiblock))
			return new float[]{0, 0, 0, 1, .5f, 1};
		if(new BlockPos(0, 1, 2).equals(posInMultiblock))
			return new float[]{getFacing()==Direction.WEST?.5f: 0, 0, getFacing()==Direction.NORTH?.5f: 0, getFacing()==Direction.EAST?.5f: 1, 1, getFacing()==Direction.SOUTH?.5f: 1};

		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		Direction fl = getFacing();
		Direction fw = getFacing().rotateY();
		if(getIsMirrored())
			fw = fw.getOpposite();
		if(new BlockPos(0, 0, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			float minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .125f;
			float maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .25f;
			float minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .125f;
			float maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .25f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .75f;
			maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .875f;
			minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .75f;
			maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .875f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(posInMultiblock.getX() > 0&&posInMultiblock.getY()==0&&posInMultiblock.getZ() < 2)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock.getX()==2)
				fl = fl.getOpposite();
			if(posInMultiblock.getZ()==1)
				fw = fw.getOpposite();
			float minX = fl==Direction.WEST?.6875f: fl==Direction.EAST?.0625f: fw==Direction.EAST?.0625f: .6875f;
			float maxX = fl==Direction.EAST?.3125f: fl==Direction.WEST?.9375f: fw==Direction.EAST?.3125f: .9375f;
			float minZ = fl==Direction.NORTH?.6875f: fl==Direction.SOUTH?.0625f: fw==Direction.SOUTH?.0625f: .6875f;
			float maxZ = fl==Direction.SOUTH?.3125f: fl==Direction.NORTH?.9375f: fw==Direction.SOUTH?.3125f: .9375f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			if(new BlockPos(1, 0, 1).equals(posInMultiblock))
			{
				minX = fl==Direction.WEST?.375f: fl==Direction.EAST?.625f: fw==Direction.WEST?-.125f: 0;
				maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.625f: fw==Direction.EAST?1.125f: 1;
				minZ = fl==Direction.NORTH?.375f: fl==Direction.SOUTH?.625f: fw==Direction.NORTH?-.125f: 0;
				maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.625f: fw==Direction.SOUTH?1.125f: 1;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, .75f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				minX = fl==Direction.WEST?-.125f: fl==Direction.EAST?.625f: fw==Direction.WEST?-.125f: .875f;
				maxX = fl==Direction.EAST?1.125f: fl==Direction.WEST?.375f: fw==Direction.EAST?1.125f: .125f;
				minZ = fl==Direction.NORTH?-.125f: fl==Direction.SOUTH?.625f: fw==Direction.NORTH?-.125f: .875f;
				maxZ = fl==Direction.SOUTH?1.25f: fl==Direction.NORTH?.375f: fw==Direction.SOUTH?1.125f: .125f;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, .75f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				minX = fl==Direction.WEST?-.125f: fl==Direction.EAST?.875f: fw==Direction.WEST?-.125f: .875f;
				maxX = fl==Direction.EAST?1.125f: fl==Direction.WEST?.125f: fw==Direction.EAST?1.125f: .125f;
				minZ = fl==Direction.NORTH?-.125f: fl==Direction.SOUTH?.875f: fw==Direction.NORTH?-.125f: .875f;
				maxZ = fl==Direction.SOUTH?1.25f: fl==Direction.NORTH?.125f: fw==Direction.SOUTH?1.125f: .125f;
				list.add(new AxisAlignedBB(minX, .75f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}

			return list;
		}
		if(posInMultiblock.getX() > 0&&posInMultiblock.getY() > 0&&posInMultiblock.getZ() < 2)
		{
			List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>(2);
			if(posInMultiblock.getY()==1)
				list.add(new AxisAlignedBB(0, 0, 0, 1, .125f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock.getX()==2)
				fl = fl.getOpposite();
			if(posInMultiblock.getZ()==1)
				fw = fw.getOpposite();
			float minY = posInMultiblock.getY()==1?.125f: -.875f;
			float maxY = posInMultiblock.getY()==1?1.125f: .125f;

			float minX = fl==Direction.WEST?.84375f: fl==Direction.EAST?0f: fw==Direction.EAST?0f: .84375f;
			float maxX = fl==Direction.EAST?.15625f: fl==Direction.WEST?1f: fw==Direction.EAST?.15625f: 1;
			float minZ = fl==Direction.NORTH?.84375f: fl==Direction.SOUTH?0f: fw==Direction.SOUTH?0f: .84375f;
			float maxZ = fl==Direction.SOUTH?.15625f: fl==Direction.NORTH?1f: fw==Direction.SOUTH?.15625f: 1;
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==Direction.WEST?0f: fl==Direction.EAST?.15625f: fw==Direction.EAST?.0625f: .8125f;
			maxX = fl==Direction.EAST?1f: fl==Direction.WEST?.84375f: fw==Direction.EAST?.1875f: .9375f;
			minZ = fl==Direction.NORTH?0f: fl==Direction.SOUTH?.15625f: fw==Direction.SOUTH?.0625f: .8125f;
			maxZ = fl==Direction.SOUTH?1f: fl==Direction.NORTH?.84375f: fw==Direction.SOUTH?.1875f: .9375f;
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==Direction.WEST?.8125f: fl==Direction.EAST?.0625f: fw==Direction.EAST?.15625f: 0f;
			maxX = fl==Direction.EAST?.1875f: fl==Direction.WEST?.9375f: fw==Direction.EAST?1f: .84375f;
			minZ = fl==Direction.NORTH?.8125f: fl==Direction.SOUTH?.0625f: fw==Direction.SOUTH?.15625f: 0f;
			maxZ = fl==Direction.SOUTH?.1875f: fl==Direction.NORTH?.9375f: fw==Direction.SOUTH?1f: .84375f;
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			if(posInMultiblock.getY()==2)
			{
				minX = fl==Direction.WEST?-.25f: fl==Direction.EAST?1.25f: fw==Direction.EAST?.75f: -.25f;
				maxX = fl==Direction.EAST?.75f: fl==Direction.WEST?.25f: fw==Direction.EAST?1.25f: .25f;
				minZ = fl==Direction.NORTH?-.25f: fl==Direction.SOUTH?1.25f: fw==Direction.SOUTH?.75f: -.25f;
				maxZ = fl==Direction.SOUTH?.75f: fl==Direction.NORTH?.25f: fw==Direction.SOUTH?1.25f: .25f;
				list.add(new AxisAlignedBB(minX, .375f, minZ, maxX, .9375f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, PlayerEntity player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return getAdvancedSelectionBounds();
	}

	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 1, 0)
		);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 1, 2)
		);
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<SqueezerRecipe> process)
	{
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
			Utils.dropStackAtPos(world, getOutputPos(), output);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<SqueezerRecipe> process)
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
	public float getMinProcessDistance(MultiblockProcess<SqueezerRecipe> process)
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
		SqueezerTileEntity master = master();
		if(master!=null&&new BlockPos(1, 0, 2).equals(posInMultiblock)&&(side==null||side==(getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY())))
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

	private static final BlockPos inputOffset = new BlockPos(2, 1, 0);
	private static final BlockPos outputOffset = new BlockPos(1, 1, 1);
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if((inputOffset.equals(posInMultiblock)||outputOffset.equals(posInMultiblock))&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			SqueezerTileEntity master = master();
			if(master==null)
				return LazyOptional.empty();
			if(inputOffset.equals(posInMultiblock))
				return master.insertionHandler.cast();
			if(outputOffset.equals(posInMultiblock))
				return master.extractionHandler.cast();
			return LazyOptional.empty();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public SqueezerRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return SqueezerRecipe.findRecipe(inserting);
	}

	@Override
	protected SqueezerRecipe readRecipeFromNBT(CompoundNBT tag)
	{
		return SqueezerRecipe.loadFromNBT(tag);
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