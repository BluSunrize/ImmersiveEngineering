/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager.SimpleComparatorValue;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FermenterLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.FermenterShapes;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraint;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraintGroup;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler.IntRange;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class FermenterLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	public static final BlockPos REDSTONE_POS = new BlockPos(2, 1, 2);
	private static final MultiblockFace FLUID_OUTPUT = new MultiblockFace(3, 0, 1, RelativeBlockFace.RIGHT);
	private static final CapabilityPosition FLUID_OUTPUT_CAP = CapabilityPosition.opposing(FLUID_OUTPUT);
	private static final CapabilityPosition ENERGY_POS = new CapabilityPosition(0, 1, 2, RelativeBlockFace.UP);

	public static final int TANK_CAPACITY = 24*FluidType.BUCKET_VOLUME;
	public static final int ENERGY_CAPACITY = 16000;
	public static final int NUM_INPUT_SLOTS = 8;
	public static final int OUTPUT_SLOT = NUM_INPUT_SLOTS;
	public static final int EMPTY_FLUID_SLOT = OUTPUT_SLOT+1;
	public static final int FILLED_FLUID_SLOT = EMPTY_FLUID_SLOT+1;
	public static final int NUM_SLOTS = FILLED_FLUID_SLOT+1;

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		final boolean rsEnabled = state.rsState.isEnabled(context);
		final boolean wasActive = state.active;
		state.active = state.processor.tickServer(state, context.getLevel(), rsEnabled);
		if(wasActive!=state.active)
			context.requestMasterBESync();
		if(!rsEnabled)
			return;
		boolean changed = false;
		if(state.energy.getEnergyStored() > 0&&state.processor.getQueueSize() < state.processor.getMaxQueueSize())
			changed = enqueueNewProcesses(context);

		changed |= FluidUtils.multiblockFluidOutput(
				state.fluidOutput, state.tank, EMPTY_FLUID_SLOT, FILLED_FLUID_SLOT, state.inventory
		);
		changed |= outputItem(context);

		if(changed)
			context.markMasterDirty();
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		if(!state.isPlayingSound.getAsBoolean())
		{
			final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(1.5, 1.5, 1.5));
			state.isPlayingSound = MultiblockSound.startSound(
					() -> state.active, context.isValid(), soundPos, IESounds.fermenter, 0.25f
			);
		}
	}

	private boolean enqueueNewProcesses(IMultiblockContext<State> ctx)
	{
		final State state = ctx.getState();
		final Level level = ctx.getLevel().getRawLevel();
		boolean addedAny = false;
		final int[] usedInvSlots = new int[NUM_INPUT_SLOTS];
		for(final MultiblockProcess<FermenterRecipe, ProcessContextInMachine<FermenterRecipe>> process : state.processor.getQueue())
			if(process instanceof MultiblockProcessInMachine)
				for(int i : ((MultiblockProcessInMachine<FermenterRecipe>)process).getInputSlots())
					usedInvSlots[i]++;

		Integer[] preferredSlots = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7};
		Arrays.sort(preferredSlots, 0, 8, Comparator.comparingInt(arg0 -> usedInvSlots[arg0]));
		for(int slot : preferredSlots)
		{
			ItemStack stack = state.inventory.getStackInSlot(slot);
			if(!stack.isEmpty())
			{
				stack = stack.copy();
				stack.shrink(usedInvSlots[slot]);
			}
			if(!stack.isEmpty()&&stack.getCount() > 0)
			{
				FermenterRecipe recipe = FermenterRecipe.findRecipe(level, stack);
				if(recipe!=null)
				{
					MultiblockProcessInMachine<FermenterRecipe> process = new MultiblockProcessInMachine<>(recipe, slot);
					if(state.processor.addProcessToQueue(process, level, false))
						addedAny = true;
				}
			}
		}
		return addedAny;
	}

	private boolean outputItem(IMultiblockContext<State> ctx)
	{
		final State state = ctx.getState();
		final ItemStack outputStack = state.inventory.getStackInSlot(OUTPUT_SLOT);
		if(outputStack.isEmpty()||!ctx.getLevel().shouldTickModulo(8))
			return false;
		IItemHandler outputHandler = state.itemOutput.getNullable();
		if(outputHandler==null)
			return false;
		ItemStack stack = ItemHandlerHelper.copyStackWithSize(outputStack, 1);
		stack = ItemHandlerHelper.insertItem(outputHandler, stack, false);
		if(stack.isEmpty())
		{
			outputStack.shrink(1);
			return true;
		}
		return false;
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public <T> LazyOptional<T> getCapability(
			IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap
	)
	{
		if(cap==ForgeCapabilities.ENERGY&&ENERGY_POS.equalsOrNullFace(position))
			return ctx.getState().energyHandler.cast(ctx);
		if(cap==ForgeCapabilities.FLUID_HANDLER&&FLUID_OUTPUT_CAP.equalsOrNullFace(position))
			return ctx.getState().fluidHandler.cast(ctx);
		if(cap==ForgeCapabilities.ITEM_HANDLER)
		{
			if(new BlockPos(0, 1, 0).equals(position.posInMultiblock()))
				return ctx.getState().insertionHandler.cast(ctx);
			else if(new BlockPos(1, 1, 1).equals(position.posInMultiblock()))
				return ctx.getState().extractionHandler.cast(ctx);
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
		return FermenterShapes.SHAPE_GETTER;
	}

	public static ComparatorManager<State> makeComparator()
	{
		return ComparatorManager.makeSimple(
				SimpleComparatorValue.inventory(State::getInventory, 0, 8), FermenterLogic.REDSTONE_POS
		);
	}

	public static class State implements IMultiblockState, ProcessContextInMachine<FermenterRecipe>
	{
		private final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);
		private final FluidTank tank = new FluidTank(TANK_CAPACITY);
		private final SlotwiseItemHandler inventory;
		private final MultiblockProcessor<FermenterRecipe, ProcessContextInMachine<FermenterRecipe>> processor;
		public final RSState rsState = RSState.enabledByDefault();

		private final CapabilityReference<IFluidHandler> fluidOutput;
		private final CapabilityReference<IItemHandler> itemOutput;
		private final StoredCapability<IItemHandler> insertionHandler;
		private final StoredCapability<IItemHandler> extractionHandler;
		private final StoredCapability<IFluidHandler> fluidHandler;
		private final StoredCapability<IEnergyStorage> energyHandler;

		// Client/sync field
		public boolean active;
		private BooleanSupplier isPlayingSound = () -> false;

		public State(IInitialMultiblockContext<State> ctx)
		{
			this.processor = new MultiblockProcessor<>(
					8, 0, 8, ctx.getMarkDirtyRunnable(), FermenterRecipe.RECIPES::getById
			);
			this.inventory = SlotwiseItemHandler.makeWithGroups(List.of(
					new IOConstraintGroup(IOConstraint.ANY_INPUT, NUM_INPUT_SLOTS),
					new IOConstraintGroup(IOConstraint.OUTPUT, 1),
					new IOConstraintGroup(IOConstraint.FLUID_INPUT, 1),
					new IOConstraintGroup(IOConstraint.OUTPUT, 1)
			), ctx.getMarkDirtyRunnable());
			this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, FLUID_OUTPUT);
			this.itemOutput = ctx.getCapabilityAt(
					ForgeCapabilities.ITEM_HANDLER, new BlockPos(2, 1, 1), RelativeBlockFace.LEFT
			);
			this.insertionHandler = new StoredCapability<>(new WrappingItemHandler(
					this.inventory, true, false, new IntRange(0, NUM_INPUT_SLOTS)
			));
			this.extractionHandler = new StoredCapability<>(new WrappingItemHandler(
					this.inventory, false, true, new IntRange(OUTPUT_SLOT, OUTPUT_SLOT+1)
			));
			this.fluidHandler = new StoredCapability<>(new ArrayFluidHandler(
					tank, true, false, ctx.getMarkDirtyRunnable()
			));
			this.energyHandler = new StoredCapability<>(energy);
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
		public int[] getOutputSlots()
		{
			return new int[]{OUTPUT_SLOT};
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

		@Override
		public IItemHandlerModifiable getInventory()
		{
			return inventory.getRawHandler();
		}

		public FluidTank getTank()
		{
			return tank;
		}
	}
}
