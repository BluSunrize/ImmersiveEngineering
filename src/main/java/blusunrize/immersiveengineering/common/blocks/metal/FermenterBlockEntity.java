/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.BEContainer;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.orientation.RelativeBlockFace;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class FermenterBlockEntity extends PoweredMultiblockBlockEntity<FermenterBlockEntity, FermenterRecipe> implements
		IBlockBounds, IInteractionObjectIE<FermenterBlockEntity>
{
	public final FluidTank[] tanks = new FluidTank[]{new FluidTank(24*FluidAttributes.BUCKET_VOLUME)};
	public final NonNullList<ItemStack> inventory = NonNullList.withSize(11, ItemStack.EMPTY);

	public FermenterBlockEntity(BlockEntityType<FermenterBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.FERMENTER, 16000, true, type, pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank"));
		if(!descPacket)
			ContainerHelper.loadAllItems(nbt, inventory);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		CompoundTag tankTag = tanks[0].writeToNBT(new CompoundTag());
		nbt.put("tank", tankTag);
		if(!descPacket)
			ContainerHelper.saveAllItems(nbt, inventory);
	}

	private final CapabilityReference<IItemHandler> outputCap = CapabilityReference.forBlockEntityAt(this,
			() -> {
				Direction fw = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
				return new DirectionalBlockPos(this.getBlockPos().relative(fw), fw.getOpposite());
			}, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void tickServer()
	{
		super.tickServer();
		if(isRSDisabled())
			return;
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

		Direction fw = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
		if(this.tanks[0].getFluidAmount() > 0)
		{
			FluidStack out = Utils.copyFluidStackWithAmount(this.tanks[0].getFluid(), Math.min(this.tanks[0].getFluidAmount(), 80), false);
			BlockPos outputPos = this.getBlockPos().offset(0, -1, 0).relative(fw, 2);
			update |= FluidUtil.getFluidHandler(level, outputPos, fw.getOpposite())
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
		if(!inventory.get(8).isEmpty()&&level.getGameTime()%8==0)
		{
			if(outputCap.isPresent())
			{
				ItemStack stack = ItemHandlerHelper.copyStackWithSize(inventory.get(8), 1);
				stack = Utils.insertStackIntoInventory(outputCap, stack, false);
				if(stack.isEmpty())
					this.inventory.get(8).shrink(1);
			}
		}

		if(update)
		{
			this.setChanged();
			this.markContainingBlockForUpdate(null);
		}
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES =
			CachedShapesWithTransform.createForMultiblock(FermenterBlockEntity::getShape);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return getShape(SHAPES);
	}

	private static List<AABB> getShape(BlockPos posInMultiblock)
	{
		if(new BlockPos(2, 0, 2).equals(posInMultiblock))
			return ImmutableList.of(
					new AABB(0, 0, 0, 1, .5f, 1),
					new AABB(0.125, .5f, 0.625, 0.25, 1, 0.875),
					new AABB(0.75, .5f, 0.625, 0.875, 1, 0.875)
			);
		if(new BoundingBox(0, 0, 0, 1, 0, 1)
				.isInside(posInMultiblock))
		{
			List<AABB> list = Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==1,
					new AABB(0, 0, 0, 1, .5f, 1),
					new AABB(0.0625, .5f, 0.6875, 0.3125, 1.1875f, 0.9375)
			);

			if(new BlockPos(1, 0, 1).equals(posInMultiblock))
			{
				list.add(new AABB(0, .5f, 0.375, 1.125, .75f, 0.625));
				list.add(new AABB(0.875, .5f, -0.125, 1.125, .75f, 0.375));
				list.add(new AABB(0.875, .75f, -0.125, 1.125, 1, 0.125));
			}

			return list;
		}
		if(new BoundingBox(0, 1, 0, 1, 2, 1).isInside(posInMultiblock))
		{
			float minY = posInMultiblock.getY() < 2?.1875f: -.8125f;
			float maxY = posInMultiblock.getY() < 2?2: 1;
			return Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==1,
					new AABB(0.0625, minY, 0, 1, maxY, 0.9375));
		}
		AABB ret;
		if(posInMultiblock.getY()==0&&!ImmutableSet.of(
				new BlockPos(2, 0, 1),
				new BlockPos(0, 0, 2)
		).contains(posInMultiblock))
			ret = new AABB(0, 0, 0, 1, .5f, 1);
		else if(new BlockPos(2, 1, 2).equals(posInMultiblock))
			ret = new AABB(0, 0, 0.5, 1, 1, 1);
		else
			ret = new AABB(0, 0, 0, 1, 1, 1);
		return ImmutableList.of(ret);
	}

	@Override
	public Set<MultiblockFace> getEnergyPos()
	{
		return ImmutableSet.of(new MultiblockFace(0, 1, 2, RelativeBlockFace.UP));
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
			Utils.dropStackAtPos(level, getBlockPos().relative(getFacing(), 2), output, getFacing());
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
	public void doGraphicalUpdates()
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}

	private final ResettableCapability<IItemHandler> insertionHandler = registerCapability(
			new IEInventoryHandler(8, this, 0, new boolean[]{true, true, true, true, true, true, true, true}, new boolean[8])
	);
	private final ResettableCapability<IItemHandler> extractionHandler = registerCapability(
			new IEInventoryHandler(1, this, 8, new boolean[1], new boolean[]{true})
	);
	private final MultiblockCapability<IFluidHandler> fluidCap = MultiblockCapability.make(
			be -> be.fluidCap, FermenterBlockEntity::master, this, registerFluidOutput(tanks)
	);
	private static final MultiblockFace FLUID_OUTPUT = new MultiblockFace(2, 0, 1, RelativeBlockFace.RIGHT);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&(facing==null||FLUID_OUTPUT.equals(asRelativeFace(facing))))
			return fluidCap.getAndCast();
		if(ImmutableSet.of(
				new BlockPos(1, 1, 1),
				new BlockPos(0, 1, 0)
		).contains(posInMultiblock)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			FermenterBlockEntity master = master();
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
	public boolean canUseGui(Player player)
	{
		return formed;
	}

	@Override
	public FermenterBlockEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public BEContainer<FermenterBlockEntity, ?> getContainerType()
	{
		return IEContainerTypes.FERMENTER;
	}
}