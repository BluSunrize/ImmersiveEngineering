/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SqueezerLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor.InMachineProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.SqueezerShapes;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraint;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraintGroup;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler.IntRange;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class SqueezerLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	public static final BlockPos REDSTONE_POS = new BlockPos(2, 1, 2);
	private static final MultiblockFace ITEM_OUTPUT = new MultiblockFace(2, 1, 1, RelativeBlockFace.RIGHT);
	private static final MultiblockFace FLUID_OUTPUT = new MultiblockFace(3, 0, 1, RelativeBlockFace.RIGHT);
	private static final CapabilityPosition ITEM_OUTPUT_CAP = CapabilityPosition.opposing(ITEM_OUTPUT);
	private static final CapabilityPosition FLUID_OUTPUT_CAP = CapabilityPosition.opposing(FLUID_OUTPUT);
	private static final BlockPos ITEM_INPUT = new BlockPos(0, 1, 0);
	private static final CapabilityPosition ENERGY_POS = new CapabilityPosition(0, 1, 2, RelativeBlockFace.UP);

	public static final int NUM_SLOTS = 11;
	public static final int NUM_INPUT_SLOTS = 8;
	public static final int OUTPUT_SLOT = NUM_INPUT_SLOTS;
	public static final int TANK_CAPACITY = 24*FluidType.BUCKET_VOLUME;
	public static final int ENERGY_CAPACITY = 16000;

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		final boolean active = state.processor.tickServer(state, context.getLevel(), state.rsState.isEnabled(context));
		if(active!=state.active)
		{
			state.active = active;
			context.requestMasterBESync();
		}
		enqueueProcesses(state, context.getLevel().getRawLevel());
		if(context.getLevel().shouldTickModulo(8))
			handleItemOutput(context);
		FluidUtils.multiblockFluidOutput(state.fluidOutput, state.tank, 9, 10, state.inventory);
	}

	private void enqueueProcesses(State state, Level level)
	{
		// TODO deduplicate with fermenter
		if(state.energy.getEnergyStored() <= 0||state.processor.getQueueSize() >= state.processor.getMaxQueueSize())
			return;
		final int[] usedInvSlots = new int[NUM_INPUT_SLOTS];
		for(MultiblockProcess<?, ?> process : state.processor.getQueue())
			if(process instanceof MultiblockProcessInMachine)
				for(int i : ((MultiblockProcessInMachine<?>)process).getInputSlots())
					usedInvSlots[i]++;

		Integer[] preferredSlots = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7};
		Arrays.sort(preferredSlots, 0, NUM_INPUT_SLOTS, Comparator.comparingInt(arg0 -> usedInvSlots[arg0]));
		for(int slot : preferredSlots)
		{
			ItemStack stack = state.inventory.getStackInSlot(slot);
			if(stack.getCount() <= usedInvSlots[slot])
				continue;
			stack = stack.copy();
			stack.shrink(usedInvSlots[slot]);
			SqueezerRecipe recipe = SqueezerRecipe.findRecipe(level, stack);
			if(recipe!=null)
				state.processor.addProcessToQueue(new MultiblockProcessInMachine<>(recipe, slot), level, false);
		}
	}

	private void handleItemOutput(IMultiblockContext<State> ctx)
	{
		final State state = ctx.getState();
		final ItemStack fullOutputStack = state.inventory.getStackInSlot(OUTPUT_SLOT);
		if(fullOutputStack.isEmpty())
			return;
		ItemStack stack = ItemHandlerHelper.copyStackWithSize(fullOutputStack, 1);
		final ItemStack remaining = Utils.insertStackIntoInventory(state.itemOutput, stack, false);
		if(remaining.isEmpty())
		{
			fullOutputStack.shrink(1);
			ctx.markMasterDirty();
		}
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		if(!state.active&&state.animation_piston < .6875)
			state.animation_piston = Math.min(.6875f, state.animation_piston+.03125f);
		else if(state.active)
		{
			if(state.animation_down)
				state.animation_piston = Math.max(0, state.animation_piston-.03125f);
			else
				state.animation_piston = Math.min(.6875f, state.animation_piston+.03125f);
			if(state.animation_piston <= 0&&state.animation_down)
				state.animation_down = false;
			else if(state.animation_piston >= .6875&&!state.animation_down)
				state.animation_down = true;
		}
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public <T>
	LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		final State state = ctx.getState();
		if(cap==ForgeCapabilities.ENERGY&&ENERGY_POS.equalsOrNullFace(position))
			return state.energyCap.cast(ctx);

		else if(cap==ForgeCapabilities.FLUID_HANDLER&&FLUID_OUTPUT_CAP.equalsOrNullFace(position))
			return state.fluidOutputCap.cast(ctx);
		else if(cap==ForgeCapabilities.ITEM_HANDLER)
		{
			if(ITEM_INPUT.equals(position.posInMultiblock()))
				return state.itemInputCap.cast(ctx);
			else if(ITEM_OUTPUT_CAP.equals(position))
				return state.itemOutputCap.cast(ctx);
		}
		return LazyOptional.empty();
	}

	@Override
	public void dropExtraItems(State state, Consumer<ItemStack> drop)
	{
		MBInventoryUtils.dropItems(state.inventory, drop);
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return SqueezerShapes.SHAPE_GETTER;
	}

	public static ComparatorManager<SqueezerLogic.State> makeComparator()
	{
		return ComparatorManager.makeSimple(
				state -> Utils.calcRedstoneFromInventory(OUTPUT_SLOT, state.inventory), REDSTONE_POS
		);
	}

	public static class State implements IMultiblockState, ProcessContextInMachine<SqueezerRecipe>
	{
		private final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);
		private final FluidTank tank = new FluidTank(TANK_CAPACITY);
		private final SlotwiseItemHandler inventory;
		private final InMachineProcessor<SqueezerRecipe> processor;
		public final RSState rsState = RSState.enabledByDefault();

		// Only used on client
		public boolean active;
		public float animation_piston = 0;
		public boolean animation_down = true;

		// Utils
		private final CapabilityReference<IItemHandler> itemOutput;
		private final CapabilityReference<IFluidHandler> fluidOutput;
		private final StoredCapability<IEnergyStorage> energyCap;
		private final StoredCapability<IFluidHandler> fluidOutputCap;
		private final StoredCapability<IItemHandler> itemInputCap;
		private final StoredCapability<IItemHandler> itemOutputCap;

		public State(IInitialMultiblockContext<State> ctx)
		{
			final Runnable markDirty = ctx.getMarkDirtyRunnable();
			this.inventory = SlotwiseItemHandler.makeWithGroups(List.of(
					new IOConstraintGroup(IOConstraint.ANY_INPUT, NUM_INPUT_SLOTS),
					new IOConstraintGroup(IOConstraint.OUTPUT, 1),
					new IOConstraintGroup(IOConstraint.FLUID_INPUT, 1),
					new IOConstraintGroup(IOConstraint.OUTPUT, 1)
			), markDirty);
			this.processor = new InMachineProcessor<>(
					NUM_INPUT_SLOTS, 0, NUM_INPUT_SLOTS, markDirty, SqueezerRecipe.RECIPES::getById
			);
			this.itemOutput = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT);
			this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, FLUID_OUTPUT);
			this.energyCap = new StoredCapability<>(energy);
			this.fluidOutputCap = new StoredCapability<>(ArrayFluidHandler.drainOnly(tank, markDirty));
			this.itemInputCap = new StoredCapability<>(new WrappingItemHandler(
					inventory, true, false, new IntRange(0, NUM_INPUT_SLOTS)
			));
			this.itemOutputCap = new StoredCapability<>(new WrappingItemHandler(
					inventory, false, true, new IntRange(OUTPUT_SLOT, OUTPUT_SLOT+1)
			));
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.put("energy", energy.serializeNBT());
			nbt.put("tank", tank.writeToNBT(new CompoundTag()));
			nbt.put("inventory", inventory.serializeNBT());
			nbt.put("processor", processor.toNBT());
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			energy.deserializeNBT(nbt.get("energy"));
			tank.readFromNBT(nbt.getCompound("tank"));
			inventory.deserializeNBT(nbt.getCompound("inventory"));
			processor.fromNBT(nbt.get("processor"), MultiblockProcessInMachine::new);
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt)
		{
			nbt.putBoolean("active", active);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt)
		{
			active = nbt.getBoolean("active");
		}

		@Override
		public AveragingEnergyStorage getEnergy()
		{
			return energy;
		}

		@Override
		public IItemHandlerModifiable getInventory()
		{
			return inventory.getRawHandler();
		}

		@Override
		public int[] getOutputTanks()
		{
			return new int[]{0};
		}

		@Override
		public IFluidTank[] getInternalTanks()
		{
			return new IFluidTank[]{tank};
		}

		public FluidTank getTank()
		{
			return tank;
		}

		@Override
		public int[] getOutputSlots()
		{
			return new int[]{OUTPUT_SLOT};
		}
	}
}
