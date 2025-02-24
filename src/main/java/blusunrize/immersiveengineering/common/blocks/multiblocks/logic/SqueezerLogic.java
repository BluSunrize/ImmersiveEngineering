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
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.IMachineInterfaceConnection;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.MachineCheckImplementation;
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
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
		FluidUtils.multiblockFluidOutput(state.fluidOutput.get(), state.tank, 9, 10, state.inventory);
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
			RecipeHolder<SqueezerRecipe> recipe = SqueezerRecipe.findRecipe(level, stack);
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
		ItemStack stack = fullOutputStack.copyWithCount(1);
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
	public void registerCapabilities(CapabilityRegistrar<State> register)
	{
		register.registerAtOrNull(EnergyStorage.BLOCK, ENERGY_POS, state -> state.energy);
		register.registerAtOrNull(FluidHandler.BLOCK, FLUID_OUTPUT_CAP, state -> state.fluidOutputCap);
		register.register(ItemHandler.BLOCK, (state, position) -> {
			if(ITEM_INPUT.equals(position.posInMultiblock()))
				return state.itemInputCap;
			else if(ITEM_OUTPUT_CAP.equals(position))
				return state.itemOutputCap;
			else
				return null;
		});
		register.registerAtBlockPos(IMachineInterfaceConnection.CAPABILITY, REDSTONE_POS, state -> state.mifHandler);

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
		private final Supplier<@Nullable IItemHandler> itemOutput;
		private final Supplier<@Nullable IFluidHandler> fluidOutput;
		private final IFluidHandler fluidOutputCap;
		private final IItemHandler itemInputCap;
		private final IItemHandler itemOutputCap;
		private final IMachineInterfaceConnection mifHandler;

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
			this.itemOutput = ctx.getCapabilityAt(ItemHandler.BLOCK, ITEM_OUTPUT);
			this.fluidOutput = ctx.getCapabilityAt(FluidHandler.BLOCK, FLUID_OUTPUT);
			this.fluidOutputCap = ArrayFluidHandler.drainOnly(tank, markDirty);
			this.itemInputCap = new WrappingItemHandler(
					inventory, true, false, new IntRange(0, NUM_INPUT_SLOTS)
			);
			this.itemOutputCap = new WrappingItemHandler(
					inventory, false, true, new IntRange(OUTPUT_SLOT, OUTPUT_SLOT+1)
			);
			this.mifHandler = () -> new MachineCheckImplementation[]{
					new MachineCheckImplementation<>((BooleanSupplier)() -> this.active, MachineInterfaceHandler.BASIC_ACTIVE),
					new MachineCheckImplementation<>(itemInputCap, MachineInterfaceHandler.BASIC_ITEM_IN),
					new MachineCheckImplementation<>(itemOutputCap, MachineInterfaceHandler.BASIC_ITEM_OUT),
					new MachineCheckImplementation<>(fluidOutputCap, MachineInterfaceHandler.BASIC_FLUID_OUT),
					new MachineCheckImplementation<>(energy, MachineInterfaceHandler.BASIC_ENERGY),
			};
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt, Provider provider)
		{
			nbt.put("energy", energy.serializeNBT(provider));
			nbt.put("tank", tank.writeToNBT(provider, new CompoundTag()));
			nbt.put("inventory", inventory.serializeNBT(provider));
			nbt.put("processor", processor.toNBT(provider));
		}

		@Override
		public void readSaveNBT(CompoundTag nbt, Provider provider)
		{
			energy.deserializeNBT(provider, nbt.get("energy"));
			tank.readFromNBT(provider, nbt.getCompound("tank"));
			inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
			processor.fromNBT(
					nbt.get("processor"),
					(getRecipe, data, p) -> new MultiblockProcessInMachine<>(getRecipe, data),
					provider
			);
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt, Provider provider)
		{
			nbt.putBoolean("active", active);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt, Provider provider)
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
			return inventory;
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
