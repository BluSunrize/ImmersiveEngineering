/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.client.fx.FluidSplashOptions;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor.InMachineProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.MixerShapes;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.register.IEParticles;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraint;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraintGroup;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class MixerLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	private static final MultiblockFace OUTPUT_POS = new MultiblockFace(1, 0, 3, RelativeBlockFace.FRONT);
	public static final BlockPos REDSTONE_POS = new BlockPos(2, 1, 2);
	private static final CapabilityPosition FLUID_OUTPUT = new CapabilityPosition(1, 0, 2, RelativeBlockFace.BACK);
	private static final CapabilityPosition FLUID_INPUT = new CapabilityPosition(0, 0, 1, RelativeBlockFace.RIGHT);
	private static final CapabilityPosition ENERGY_INPUT = new CapabilityPosition(0, 1, 2, RelativeBlockFace.UP);
	private static final BlockPos ITEM_INPUT = new BlockPos(1, 1, 0);

	public static final int NUM_SLOTS = 8;
	public static final int ENERGY_CAPACITY = 16000;
	public static final int TANK_VOLUME = 8*FluidType.BUCKET_VOLUME;

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		final IMultiblockLevel level = context.getLevel();
		final boolean rsEnabled = state.rsState.isEnabled(context);

		final boolean active = state.processor.tickServer(state, level, rsEnabled);
		final RecipeEnqueueState enqueueState = enqueueNewRecipes(state, level.getRawLevel());
		final boolean updateFromOutput = outputFluids(state, enqueueState.foundRecipe);

		if(updateFromOutput||enqueueState.update||active!=state.isActive)
		{
			state.isActive = active;
			context.markMasterDirty();
			// TODO do less often if possible
			context.requestMasterBESync();
		}
	}

	private RecipeEnqueueState enqueueNewRecipes(State state, Level rawLevel)
	{
		final List<MultiblockProcess<MixerRecipe, ProcessContextInMachine<MixerRecipe>>> processQueue = state.processor.getQueue();
		if(state.energy.getEnergyStored() <= 0||processQueue.size() >= state.processor.getMaxQueueSize())
			return RecipeEnqueueState.NOP;
		if(state.tank.getFluidAmount() <= 0)
			return RecipeEnqueueState.NOP;
		IntSet usedInvSlots = new IntOpenHashSet();
		for(MultiblockProcess<MixerRecipe, ?> process : processQueue)
			if(process instanceof MixingProcess mixingProcess)
				for(int i : mixingProcess.getInputSlots())
					usedInvSlots.add(i);
		NonNullList<ItemStack> components = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
		for(int i = 0; i < components.size(); i++)
			if(!usedInvSlots.contains(i))
				components.set(i, state.inventory.getStackInSlot(i));

		boolean foundRecipe = false;
		boolean update = false;
		for(FluidStack fs : state.tank.fluids)
		{
			MixerRecipe recipe = MixerRecipe.findRecipe(rawLevel, fs, components);
			if(recipe==null)
				continue;
			foundRecipe = true;
			MultiblockProcessInMachine<MixerRecipe> process = new MixingProcess(
					recipe, state.tank, recipe.getUsedSlots(fs, components)
			).setInputTanks(0);
			if(state.processor.addProcessToQueue(process, rawLevel, false))
				update = true;
		}
		return new RecipeEnqueueState(update, foundRecipe);
	}

	private boolean outputFluids(State state, boolean foundRecipe)
	{
		int fluidTypes = state.tank.getFluidTypes();
		if(fluidTypes <= 0||(fluidTypes <= 1&&foundRecipe&&!state.outputAll))
			return false;
		final IFluidHandler output = state.outputRef.getNullable();
		if(output==null)
			return false;
		if(!state.outputAll)
		{
			FluidStack inTank = state.tank.getFluid();
			final int maxAmount = Math.min(inTank.getAmount(), FluidType.BUCKET_VOLUME);
			FluidStack out = Utils.copyFluidStackWithAmount(inTank, maxAmount, false);
			int drained = output.fill(out, FluidAction.EXECUTE);
			state.tank.drain(drained, FluidAction.EXECUTE);
			return drained > 0;
		}
		else
		{
			int totalOut = 0;
			Iterator<FluidStack> it = state.tank.fluids.iterator();
			while(it.hasNext())
			{
				FluidStack fs = it.next();
				if(fs==null)
					continue;
				final int maxAmount = Math.min(fs.getAmount(), FluidType.BUCKET_VOLUME-totalOut);
				FluidStack out = Utils.copyFluidStackWithAmount(fs, maxAmount, false);
				int drained = output.fill(out, FluidAction.EXECUTE);
				MultiFluidTank.drain(drained, fs, it, FluidAction.EXECUTE);
				totalOut += drained;
				if(totalOut >= FluidType.BUCKET_VOLUME)
					break;
			}
			return totalOut > 0;
		}
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		if(!state.isActive)
			return;
		state.animation_agitator = (state.animation_agitator+9)%360;
		final IMultiblockLevel level = context.getLevel();
		if(!state.isSoundPlaying.getAsBoolean())
		{
			final Vec3 soundPos = level.toAbsolute(new Vec3(1.5, 1.5, 1.5));
			state.isSoundPlaying = MultiblockSound.startSound(
					() -> state.isActive, context.isValid(), soundPos, IESounds.mixer, 0.075f
			);
		}
		if(state.tank.fluids.isEmpty())
			return;
		FluidStack fs = state.tank.fluids.get(0);
		float amount = fs.getAmount()/(float)state.tank.getCapacity()*1.125f;
		final Vec3 relativePos = new Vec3(2, 0.9375+amount, 1);
		Vec3 partPos = level.toAbsolute(relativePos);
		float r = ApiUtils.RANDOM.nextFloat()*.8125f;
		float angleRad = (float)Math.toRadians(state.animation_agitator);
		partPos = partPos.add(r*Math.cos(angleRad), 0, r*Math.sin(angleRad));
		final Level rawLevel = level.getRawLevel();
		for(int i = 0; i < 2; ++i)
			if(ApiUtils.RANDOM.nextBoolean())
				rawLevel.addParticle(IEParticles.IE_BUBBLE.get(), partPos.x, partPos.y, partPos.z, 0, 0, 0);
			else
				rawLevel.addParticle(new FluidSplashOptions(fs.getFluid()), partPos.x, partPos.y, partPos.z, 0, 0, 0);
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		final State state = ctx.getState();
		if(cap==ForgeCapabilities.ENERGY&&ENERGY_INPUT.equalsOrNullFace(position))
			return state.energyCap.cast(ctx);
		else if(cap==ForgeCapabilities.FLUID_HANDLER)
		{
			if(FLUID_INPUT.equalsOrNullFace(position))
				return state.fluidInput.cast(ctx);
			else if(FLUID_OUTPUT.equals(position))
				return state.fluidOutput.cast(ctx);
		}
		else if(cap==ForgeCapabilities.ITEM_HANDLER&&ITEM_INPUT.equals(position.posInMultiblock()))
			return state.itemInput.cast(ctx);
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
		return MixerShapes.SHAPE_GETTER;
	}

	public static class State implements IMultiblockState, ProcessContextInMachine<MixerRecipe>
	{
		public final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);
		public final MultiFluidTank tank = new MultiFluidTank(TANK_VOLUME);
		public final SlotwiseItemHandler inventory;
		public boolean outputAll;
		public final InMachineProcessor<MixerRecipe> processor;
		public final RSState rsState = RSState.enabledByDefault();

		// Client only
		public boolean isActive;
		public float animation_agitator = 0;
		private BooleanSupplier isSoundPlaying = () -> false;

		// Util
		private final CapabilityReference<IFluidHandler> outputRef;
		private final StoredCapability<IFluidHandler> fluidInput;
		private final StoredCapability<IFluidHandler> fluidOutput;
		private final StoredCapability<IItemHandler> itemInput;
		private final StoredCapability<IEnergyStorage> energyCap;

		public State(IInitialMultiblockContext<State> ctx)
		{
			this.inventory = SlotwiseItemHandler.makeWithGroups(
					List.of(new IOConstraintGroup(IOConstraint.ANY_INPUT, NUM_SLOTS)), ctx.getMarkDirtyRunnable()
			);
			this.processor = new InMachineProcessor<>(
					8, 0, 8, ctx.getMarkDirtyRunnable(), MixerRecipe.RECIPES::getById
			);
			this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, OUTPUT_POS);
			this.fluidInput = new StoredCapability<>(ArrayFluidHandler.fillOnly(tank, ctx.getMarkDirtyRunnable()));
			this.fluidOutput = new StoredCapability<>(ArrayFluidHandler.drainOnly(tank, ctx.getMarkDirtyRunnable()));
			this.itemInput = new StoredCapability<>(this.inventory);
			this.energyCap = new StoredCapability<>(this.energy);
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.put("tank", tank.writeToNBT(new CompoundTag()));
			nbt.put("inventory", inventory.serializeNBT());
			nbt.putBoolean("outputAll", outputAll);
			nbt.put("processor", processor.toNBT());
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			tank.readFromNBT(nbt.getCompound("tank"));
			inventory.deserializeNBT(nbt.getCompound("inventory"));
			outputAll = nbt.getBoolean("outputAll");
			processor.fromNBT(nbt.get("processor"), (getRecipe, data) -> new MixingProcess(getRecipe, data, tank));
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt)
		{
			nbt.put("tank", tank.writeToNBT(new CompoundTag()));
			nbt.putBoolean("isActive", isActive);
			nbt.putFloat("animation_agitator", animation_agitator);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt)
		{
			tank.readFromNBT(nbt.getCompound("tank"));
			isActive = nbt.getBoolean("isActive");
			animation_agitator = nbt.getFloat("animation_agitator");
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
	}

	private record RecipeEnqueueState(boolean update, boolean foundRecipe)
	{
		private static final RecipeEnqueueState NOP = new RecipeEnqueueState(false, false);
	}
}
