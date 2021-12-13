/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.orientation.RelativeBlockFace;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * WARNING: This inherits from PoweredMultiblockBlockEntity, but does not actually use the process queue system provided
 * by that class! This needs to be fixed at some point, but is far from trivial since we can't actually precompute the
 * recipe output even if we use a hacky recipe class to represent dynamic filling (due to endertank-style items)
 */
public class BottlingMachineBlockEntity extends PoweredMultiblockBlockEntity<BottlingMachineBlockEntity, MultiblockRecipe>
		implements IConveyorAttachable, IBlockBounds, IEClientTickableBE
{
	public static final float TRANSLATION_DISTANCE = 2.5f;
	private static final float STANDARD_TRANSPORT_TIME = 16f*(TRANSLATION_DISTANCE/2); //16 frames in conveyor animation, 1 frame/tick, 2.5 blocks of total translation distance, halved because transport time just affects half the distance
	private static final float STANDARD_LIFT_TIME = 3.75f;
	private static final float MIN_CYCLE_TIME = 60f; //set >= 2*(STANDARD_LIFT_TIME+STANDARD_TRANSPORT_TIME)
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(8*FluidAttributes.BUCKET_VOLUME)};
	public List<BottlingProcess> bottlingProcessQueue = new ArrayList<>();

	public BottlingMachineBlockEntity(BlockEntityType<BottlingMachineBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.BOTTLING_MACHINE, 16000, true, type, pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		ListTag processNBT = nbt.getList("bottlingQueue", 10);
		bottlingProcessQueue.clear();
		for(int i = 0; i < processNBT.size(); i++)
		{
			CompoundTag tag = processNBT.getCompound(i);
			BottlingProcess process = BottlingProcess.readFromNBT(tag);
			bottlingProcessQueue.add(process);
		}
		tanks[0].readFromNBT(nbt.getCompound("tank"));
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		ListTag processNBT = new ListTag();
		for(BottlingProcess process : this.bottlingProcessQueue)
			processNBT.add(process.writeToNBT());
		nbt.put("bottlingQueue", processNBT);
		nbt.put("tank", tanks[0].writeToNBT(new CompoundTag()));
	}

	@Override
	public void receiveMessageFromClient(CompoundTag message)
	{
	}

	private final CapabilityReference<IItemHandler> outputCap = CapabilityReference.forBlockEntityAt(this, () -> {
		Direction outDir = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
		return new DirectionalBlockPos(getBlockPosForPos(new BlockPos(2, 1, 1)).relative(outDir), outDir.getOpposite());
	}, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void tickClient()
	{
		if(!shouldRenderAsActive())
			return;
		for(BottlingProcess process : bottlingProcessQueue)
			++process.processTick;
	}

	@Override
	public void tickServer()
	{
		super.tickServer();
		if(isRSDisabled())
			return;

		int max = getMaxProcessPerTick();
		int i = 0;
		Iterator<BottlingProcess> processIterator = bottlingProcessQueue.iterator();
		tickedProcesses = 0;
		while(processIterator.hasNext()&&i++ < max)
		{
			BottlingProcess process = processIterator.next();
			if(process.processStep(this))
				tickedProcesses++;
			if(process.processFinished)
			{
				ItemStack output = !process.items.get(1).isEmpty()?process.items.get(1): process.items.get(0);
				doProcessOutput(output);
				processIterator.remove();
			}
		}
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		if(new BlockPos(1, 0, 0).equals(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .5f, 1);
		if(posInMultiblock.getY()==0||new BlockPos(2, 1, 0).equals(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, 1, 1);
		if(posInMultiblock.getZ()==1&&posInMultiblock.getY()==1)
			return Shapes.box(0, 0, 0, 1, .125f, 1);
		if(new BlockPos(1, 1, 0).equals(posInMultiblock))
			return Shapes.box(.0625f, 0, .0625f, .9375f, 1, .9375f);
		if(new BlockPos(1, 1, 0).equals(posInMultiblock))
		{
			Direction f = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
			float xMin = f==Direction.EAST?-.0625f: f==Direction.WEST?.25f: getFacing()==Direction.WEST?.125f: getFacing()==Direction.EAST?.25f: 0;
			float zMin = getFacing()==Direction.NORTH?.125f: getFacing()==Direction.SOUTH?.25f: f==Direction.SOUTH?-.0625f: f==Direction.NORTH?.25f: 0;
			float xMax = f==Direction.EAST?.75f: f==Direction.WEST?1.0625f: getFacing()==Direction.WEST?.75f: getFacing()==Direction.EAST?.875f: 1;
			float zMax = getFacing()==Direction.NORTH?.75f: getFacing()==Direction.SOUTH?.875f: f==Direction.SOUTH?.75f: f==Direction.NORTH?1.0625f: 1;
			return Shapes.box(xMin, .0625f, zMin, xMax, .6875f, zMax);
		}
		if(new BlockPos(1, 2, 1).equals(posInMultiblock))
		{
			float xMin = getFacing()==Direction.WEST?0: .21875f;
			float zMin = getFacing()==Direction.NORTH?0: .21875f;
			float xMax = getFacing()==Direction.EAST?1: .78125f;
			float zMax = getFacing()==Direction.SOUTH?1: .78125f;
			return Shapes.box(xMin, -.4375f, zMin, xMax, .5625f, zMax);
		}
		if(new BlockPos(1, 2, 0).equals(posInMultiblock))
		{
			float xMin = getFacing()==Direction.WEST?.8125f: getFacing()==Direction.EAST?0: .125f;
			float zMin = getFacing()==Direction.NORTH?.8125f: getFacing()==Direction.SOUTH?0: .125f;
			float xMax = getFacing()==Direction.WEST?1: getFacing()==Direction.EAST?.1875f: .875f;
			float zMax = getFacing()==Direction.NORTH?1: getFacing()==Direction.SOUTH?.1875f: .875f;
			return Shapes.box(xMin, -1, zMin, xMax, .25f, zMax);
		}
		return Shapes.box(0, 0, 0, 1, 1, 1);
	}

	@Override
	public Set<MultiblockFace> getEnergyPos()
	{
		return ImmutableSet.of(new MultiblockFace(2, 1, 0, RelativeBlockFace.UP));
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(new BlockPos(1, 0, 1));
	}

	@Override
	protected boolean shouldRenderAsActiveImpl()
	{
		// Use bottlingProcessQueue instead of the "real" processQueue
		return energyStorage.getEnergyStored() > 0&&!isRSDisabled()&&!bottlingProcessQueue.isEmpty();
	}

	@Override
	public void onEntityCollision(Level world, Entity entity)
	{
		if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&!world.isClientSide&&entity instanceof ItemEntity itemEntity&&entity.isAlive())
		{
			BottlingMachineBlockEntity master = master();
			if(master==null)
				return;
			ItemStack stack = itemEntity.getItem();
			if(stack.isEmpty())
				return;

			if(master.bottlingProcessQueue.size() < master.getProcessQueueMaxLength())
			{
				float dist = 1;
				BottlingProcess p = null;
				if(master.bottlingProcessQueue.size() > 0)
				{
					p = master.bottlingProcessQueue.get(master.bottlingProcessQueue.size()-1);
					if(p!=null)
						dist = p.processTick/(float)p.maxProcessTick;
				}
				if(p!=null&&dist < master.getMinProcessDistance(null))
					return;

				p = new BottlingProcess(ItemHandlerHelper.copyStackWithSize(stack, 1));
				master.bottlingProcessQueue.add(p);
				master.setChanged();
				master.markContainingBlockForUpdate(null);
				stack = stack.copy();
				stack.shrink(1);
				if(stack.getCount() <= 0)
					entity.discard();
				else
					itemEntity.setItem(stack);
			}
		}
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return true;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MultiblockRecipe> process)
	{
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
		{
			Direction outDir = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
			BlockPos pos = getBlockPos().relative(outDir, 2);
			Utils.dropStackAtPos(level, pos, output, outDir);
		}
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<MultiblockRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 2;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 2;
	}

	public static float getTransportTime(float processMaxTicks)
	{
		if(processMaxTicks >= MIN_CYCLE_TIME)
			return STANDARD_TRANSPORT_TIME;
		else
			return processMaxTicks*STANDARD_TRANSPORT_TIME/MIN_CYCLE_TIME;
	}

	public static float getLiftTime(float processMaxTicks)
	{
		if(processMaxTicks >= MIN_CYCLE_TIME)
			return STANDARD_LIFT_TIME;
		else
			return processMaxTicks*STANDARD_LIFT_TIME/MIN_CYCLE_TIME;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<MultiblockRecipe> process)
	{
		float maxTicks = BottlingProcess.getMaxProcessTick();
		return 1f-(getTransportTime(maxTicks)+getLiftTime(maxTicks))/maxTicks;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return null;
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
		return null;
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
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


	@Override
	public MultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected MultiblockRecipe getRecipeForId(ResourceLocation id)
	{
		return null;
	}

	private final MultiblockCapability<IItemHandler> insertionHandler = MultiblockCapability.make(
			this, be -> be.insertionHandler, BottlingMachineBlockEntity::master,
			registerCapability(new BottlingMachineInventoryHandler(this))
	);
	private final MultiblockCapability<IFluidHandler> fluidCap = MultiblockCapability.make(
			this, be -> be.fluidCap, BottlingMachineBlockEntity::master, registerFluidInput(tanks)
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			if(facing==null||(BlockPos.ZERO.equals(posInMultiblock)&&facing.getAxis().isHorizontal()))
				return fluidCap.getAndCast();
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&facing==(getIsMirrored()?this.getFacing().getClockWise(): this.getFacing().getCounterClockWise()))
				return insertionHandler.getAndCast();
		return super.getCapability(capability, facing);
	}

	@Override
	public Direction[] sigOutputDirections()
	{
		if(new BlockPos(2, 1, 1).equals(posInMultiblock))
			return new Direction[]{getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise()};
		return new Direction[0];
	}

	public static class BottlingProcess
	{
		public NonNullList<ItemStack> items;
		public int processTick;
		public int maxProcessTick = getMaxProcessTick();
		boolean processFinished = false;

		public BottlingProcess(ItemStack input)
		{
			this.items = NonNullList.withSize(2, ItemStack.EMPTY);
			this.items.set(0, input);
		}

		public boolean processStep(BottlingMachineBlockEntity tile)
		{
			int energyExtracted = (int)(8*IEServerConfig.MACHINES.bottlingMachineConfig.energyModifier.get());
			if(tile.energyStorage.extractEnergy(energyExtracted, true) >= energyExtracted)
			{
				tile.energyStorage.extractEnergy(energyExtracted, false);
				processTick++;
				float transformationPoint = getTransportTime(maxProcessTick)+getLiftTime(maxProcessTick);
				if(processTick >= transformationPoint&&processTick < 1+transformationPoint)
				{
					FluidStack fs = tile.tanks[0].getFluid();
					if(!fs.isEmpty())
					{
						BottlingMachineRecipe recipe = BottlingMachineRecipe.findRecipe(items.get(0), fs);
						if(recipe!=null)
						{
							if(recipe.fluidInput.test(tile.tanks[0].getFluid()))
							{
								items.set(1, recipe.getActualItemOutputs(tile).get(0));
								tile.tanks[0].drain(recipe.fluidInput.getAmount(), FluidAction.EXECUTE);
							}
						}
						else
						{
							ItemStack ret = Utils.fillFluidContainer(tile.tanks[0], items.get(0), ItemStack.EMPTY, null);
							if(!ret.isEmpty())
								items.set(1, ret);
						}
						if(items.get(1).isEmpty())
							items.set(1, items.get(0));
						tile.markContainingBlockForUpdate(null);
					}
				}
				if(processTick >= maxProcessTick)
					processFinished = true;
				return true;
			}
			return false;
		}

		public static int getMaxProcessTick()
		{
			return (int)(60*IEServerConfig.MACHINES.bottlingMachineConfig.timeModifier.get());
		}

		public CompoundTag writeToNBT()
		{
			CompoundTag nbt = new CompoundTag();
			if(!items.get(0).isEmpty())
				nbt.put("input", items.get(0).save(new CompoundTag()));
			if(!items.get(1).isEmpty())
				nbt.put("output", items.get(1).save(new CompoundTag()));
			nbt.putInt("processTick", processTick);
			return nbt;
		}

		public static BottlingProcess readFromNBT(CompoundTag nbt)
		{
			ItemStack input = ItemStack.of(nbt.getCompound("input"));
			BottlingProcess process = new BottlingProcess(input);
			if(nbt.contains("output", Tag.TAG_COMPOUND))
				process.items.set(1, ItemStack.of(nbt.getCompound("output")));
			process.processTick = nbt.getInt("processTick");
			return process;
		}
	}

	public static class BottlingMachineInventoryHandler implements IItemHandlerModifiable
	{
		BottlingMachineBlockEntity multiblock;

		public BottlingMachineInventoryHandler(BottlingMachineBlockEntity multiblock)
		{
			this.multiblock = multiblock;
		}

		@Override
		public int getSlots()
		{
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if(multiblock.bottlingProcessQueue.size() < multiblock.getProcessQueueMaxLength())
			{
				stack = stack.copy();
				float dist = 1;
				BottlingProcess p = null;
				if(multiblock.bottlingProcessQueue.size() > 0)
				{
					p = multiblock.bottlingProcessQueue.get(multiblock.bottlingProcessQueue.size()-1);
					if(p!=null)
						dist = p.processTick/(float)p.maxProcessTick;
				}
				if(p!=null&&dist < multiblock.getMinProcessDistance(null))
					return stack;
				if(!simulate)
				{
					p = new BottlingProcess(ItemHandlerHelper.copyStackWithSize(stack, 1));
					multiblock.bottlingProcessQueue.add(p);
					multiblock.setChanged();
					multiblock.markContainingBlockForUpdate(null);
				}
				stack.shrink(1);
				if(stack.getCount() <= 0)
					stack = ItemStack.EMPTY;
			}
			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
		{
			return true;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
		}
	}
}