/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.RefineryLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor.InMachineProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.RefineryShapes;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraint;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraintGroup;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RefineryLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	public static final BlockPos REDSTONE_POS = new BlockPos(4, 1, 2);
	private static final CapabilityPosition ENERGY_POS = new CapabilityPosition(2, 1, 0, RelativeBlockFace.UP);
	private static final MultiblockFace FLUID_OUTPUT = new MultiblockFace(2, 0, 3, RelativeBlockFace.FRONT);
	private static final CapabilityPosition FLUID_OUTPUT_CAP = CapabilityPosition.opposing(FLUID_OUTPUT);
	private static final Set<CapabilityPosition> FLUID_INPUT_CAPS = Set.of(
			new CapabilityPosition(0, 0, 1, RelativeBlockFace.RIGHT),
			new CapabilityPosition(4, 0, 1, RelativeBlockFace.LEFT)
	);
	private static final Set<BlockPos> FLUID_INPUTS = FLUID_INPUT_CAPS.stream()
			.map(CapabilityPosition::posInMultiblock)
			.collect(Collectors.toSet());

	private static final int SLOT_CATALYST = 0;
	private static final int SLOT_CONTAINER_IN = 1;
	private static final int SLOT_CONTAINER_OUT = 2;
	public static final int NUM_SLOTS = 3;
	public static final int ENERGY_CAPACITY = 16000;

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		final boolean wasActive = state.active;
		state.active = state.processor.tickServer(state, context.getLevel(), state.rsState.isEnabled(context));
		if(wasActive!=state.active)
			context.requestMasterBESync();
		tryEnqueueProcess(state, context.getLevel().getRawLevel());
		FluidUtils.multiblockFluidOutput(
				state.fluidOutput, state.tanks.output(), SLOT_CONTAINER_IN, SLOT_CONTAINER_OUT, state.inventory
		);
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		if(!state.isSoundPlaying.getAsBoolean())
		{
			final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(1.5, 1.5, 1.5));
			state.isSoundPlaying = MultiblockSound.startSound(
					() -> state.active, context.isValid(), soundPos, IESounds.refinery, 0.25f
			);
		}
	}

	private void tryEnqueueProcess(State state, Level level)
	{
		if(state.energy.getEnergyStored() <= 0||state.processor.getQueueSize() >= state.processor.getMaxQueueSize())
			return;
		final FluidStack leftInput = state.tanks.leftInput.getFluid();
		final FluidStack rightInput = state.tanks.rightInput.getFluid();
		if(leftInput.isEmpty()&&rightInput.isEmpty())
			return;
		final ItemStack catalyst = state.inventory.getStackInSlot(SLOT_CATALYST);
		RefineryRecipe recipe = RefineryRecipe.findRecipe(level, leftInput, rightInput, catalyst);
		if(recipe==null)
			return;
		MultiblockProcessInMachine<RefineryRecipe> process = new MultiblockProcessInMachine<>(recipe);
		if(!leftInput.isEmpty()&&!rightInput.isEmpty())
			process.setInputTanks(0, 1);
		else if(!leftInput.isEmpty())
			process.setInputTanks(0);
		else
			process.setInputTanks(1);
		state.processor.addProcessToQueue(process, level, false);
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
		else if(cap==ForgeCapabilities.FLUID_HANDLER)
		{
			if(FLUID_OUTPUT_CAP.equals(position))
				return state.outputCap.cast(ctx);
			else if(FLUID_INPUT_CAPS.contains(position))
				return state.inputCap.cast(ctx);
		}
		return LazyOptional.empty();
	}

	@Override
	public InteractionResult click(
			IMultiblockContext<State> ctx, BlockPos posInMultiblock,
			Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient
	)
	{
		if(isClient)
			return InteractionResult.SUCCESS;
		final State state = ctx.getState();
		IFluidHandler tank = null;
		if(FLUID_INPUTS.contains(posInMultiblock))
			tank = posInMultiblock.getX() < 2?state.tanks.leftInput: state.tanks.rightInput;
		else if(FLUID_OUTPUT_CAP.posInMultiblock().equals(posInMultiblock))
			tank = state.tanks.output;
		if(tank!=null)
		{
			FluidUtils.interactWithFluidHandler(player, hand, tank);
			ctx.markMasterDirty();
		}
		else
			player.openMenu(IEMenuTypes.REFINERY.provide(ctx, posInMultiblock));
		return InteractionResult.SUCCESS;
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return RefineryShapes.SHAPE_GETTER;
	}

	public static class State implements IMultiblockState, ProcessContextInMachine<RefineryRecipe>
	{
		private final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);
		public final RefineryTanks tanks = new RefineryTanks();
		private final InMachineProcessor<RefineryRecipe> processor;
		public final SlotwiseItemHandler inventory;
		public final RSState rsState = RSState.enabledByDefault();

		// Utils
		private final IFluidTank[] tankArray = {tanks.leftInput, tanks.rightInput, tanks.output};
		private final CapabilityReference<IFluidHandler> fluidOutput;
		private final StoredCapability<IEnergyStorage> energyCap;
		private final StoredCapability<IFluidHandler> inputCap;
		private final StoredCapability<IFluidHandler> outputCap;

		// Sync/client fields
		public boolean active;
		private BooleanSupplier isSoundPlaying = () -> false;

		public State(IInitialMultiblockContext<State> ctx)
		{
			final Runnable markDirty = ctx.getMarkDirtyRunnable();
			this.processor = new InMachineProcessor<>(1, 0, 1, markDirty, RefineryRecipe.RECIPES::getById);
			this.inventory = SlotwiseItemHandler.makeWithGroups(
					List.of(new IOConstraintGroup(IOConstraint.NO_CONSTRAINT, NUM_SLOTS)), ctx.getMarkDirtyRunnable()
			);
			this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, FLUID_OUTPUT);
			this.energyCap = new StoredCapability<>(this.energy);
			this.inputCap = new StoredCapability<>(new ArrayFluidHandler(
					false, true, markDirty, tanks.leftInput, tanks.rightInput
			));
			this.outputCap = new StoredCapability<>(ArrayFluidHandler.drainOnly(this.tanks.output, markDirty));
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.put("energy", energy.serializeNBT());
			nbt.put("tanks", tanks.toNBT());
			nbt.put("processor", processor.toNBT());
			nbt.put("inventory", inventory.serializeNBT());
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			energy.deserializeNBT(nbt.get("energy"));
			tanks.readNBT(nbt.getCompound("tanks"));
			processor.fromNBT(nbt.get("processor"), MultiblockProcessInMachine::new);
			inventory.deserializeNBT(nbt.getCompound("inventory"));
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
		public IFluidTank[] getInternalTanks()
		{
			return tankArray;
		}

		@Override
		public int[] getOutputTanks()
		{
			return new int[]{2};
		}
	}

	public record RefineryTanks(FluidTank leftInput, FluidTank rightInput, FluidTank output)
	{
		public static final int VOLUME = 24*FluidType.BUCKET_VOLUME;

		public RefineryTanks()
		{
			this(new FluidTank(VOLUME), new FluidTank(VOLUME), new FluidTank(VOLUME));
		}

		public Tag toNBT()
		{
			CompoundTag tag = new CompoundTag();
			tag.put("leftIn", leftInput.writeToNBT(new CompoundTag()));
			tag.put("rightIn", rightInput.writeToNBT(new CompoundTag()));
			tag.put("out", output.writeToNBT(new CompoundTag()));
			return tag;
		}

		public void readNBT(CompoundTag tag)
		{
			leftInput.readFromNBT(tag.getCompound("leftIn"));
			rightInput.readFromNBT(tag.getCompound("rightIn"));
			output.readFromNBT(tag.getCompound("out"));
		}
	}
}
