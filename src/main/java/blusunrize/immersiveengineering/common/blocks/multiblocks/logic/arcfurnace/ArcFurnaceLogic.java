/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.IMachineInterfaceConnection;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.MachineCheckImplementation;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor.InMachineProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.ArcFurnaceSelectionShapes;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.ArcFurnaceShapes;
import blusunrize.immersiveengineering.common.register.IEParticles;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler.IntRange;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class ArcFurnaceLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	private static final Set<CapabilityPosition> ENERGY_INPUTS = Set.of(
			new CapabilityPosition(1, 1, 0, RelativeBlockFace.FRONT),
			new CapabilityPosition(2, 1, 0, RelativeBlockFace.FRONT),
			new CapabilityPosition(3, 1, 0, RelativeBlockFace.FRONT)
	);
	private static final List<Vec3> ELECTRODE_OFFSETS = List.of(
			new Vec3(2.5, 3.9, 2.75),
			new Vec3(2.125, 3.9, 2.25),
			new Vec3(2.875, 3.9, 2.25)
	);
	private static final Vec3 SMOKE_OFFSET = new Vec3(2.5, 3.9, 2.5);
	private static final double[] PARTICLE_Y_SPEEDS = {0.025, 0.05};
	public static final BlockPos REDSTONE_POS = new BlockPos(0, 1, 4);
	private static final BlockPos ELECTRODE_COMPARATOR_POS = new BlockPos(2, 4, 2);

	public static final int FIRST_IN_SLOT = 0;
	public static final int IN_SLOT_COUNT = 12;
	public static final int FIRST_ADDITIVE_SLOT = 12;
	public static final int ADDITIVE_SLOT_COUNT = 4;
	public static final int FIRST_OUT_SLOT = 16;
	public static final int OUT_SLOT_COUNT = 6;
	public static final int SLAG_SLOT = 22;
	public static final int FIRST_ELECTRODE_SLOT = 23;
	public static final int ELECTRODE_COUNT = 3;
	private static final MultiblockFace SLAG_OUT_POS = new MultiblockFace(2, 0, -1, RelativeBlockFace.BACK);
	private static final MultiblockFace MAIN_OUT_POS = new MultiblockFace(2, 0, 5, RelativeBlockFace.FRONT);
	private static final CapabilityPosition SLAG_CAP_POS = CapabilityPosition.opposing(SLAG_OUT_POS);
	private static final CapabilityPosition MAIN_CAP_POS = CapabilityPosition.opposing(MAIN_OUT_POS);
	private static final int[] OUTPUT_SLOTS = Util.make(new int[OUT_SLOT_COUNT], slots -> {
		for(int i = 0; i < OUT_SLOT_COUNT; ++i)
			slots[i] = FIRST_OUT_SLOT+i;
	});
	public static final int NUM_SLOTS = FIRST_ELECTRODE_SLOT+ELECTRODE_COUNT;
	public static final int ENERGY_CAPACITY = 64000;

	// register additional conditions for machine interface
	public static ResourceLocation MIF_CONDITION_ADDITIVES = ieLoc("arc_furnace/additives");
	public static ResourceLocation MIF_CONDITION_SLAG = ieLoc("arc_furnace/slag");
	public static ResourceLocation MIF_CONDITION_ELECTRODES = ieLoc("arc_furnace/electrodes");

	static
	{
		MachineInterfaceHandler.copyOptions(MIF_CONDITION_ADDITIVES, MachineInterfaceHandler.BASIC_ITEM_IN);
		MachineInterfaceHandler.copyOptions(MIF_CONDITION_SLAG, MachineInterfaceHandler.BASIC_ITEM_OUT);
		MachineInterfaceHandler.register(MIF_CONDITION_ELECTRODES, MachineInterfaceHandler.buildComparativeConditions(State::getElectrodeComparatorValue));
	}

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		final IMultiblockLevel level = context.getLevel();
		final boolean canWork = state.rsControl.isEnabled(context);
		final boolean tickedAny = state.processor.tickServer(state, level, canWork);
		if(state.active!=tickedAny||state.updateElectrodePresence())
		{
			state.active = tickedAny;
			context.requestMasterBESync();
		}
		if(!canWork||state.energy.getEnergyStored() <= 0)
			return;
		if(tickedAny)
			for(int i = FIRST_ELECTRODE_SLOT; i < FIRST_ELECTRODE_SLOT+ELECTRODE_COUNT; i++)
				ItemUtils.damageStackableItem(state.inventory.getStackInSlot(i), level.getRawLevel(), 1);

		if(state.processor.getQueueSize() < state.processor.getMaxQueueSize())
			enqueueProcesses(state, level.getRawLevel());

		if(level.shouldTickModulo(8))
			outputItems(state);

		if(tickedAny&&ApiUtils.RANDOM.nextInt(10)==0)
		{
			final Level rawLevel = level.getRawLevel();
			final Vec3 soundPos = level.toAbsolute(new Vec3(1.5, 1.5, 1.5));
			rawLevel.playSound(
					null,
					soundPos.x, soundPos.y, soundPos.z,
					SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS,
					0.6F+ApiUtils.RANDOM.nextFloat()*0.4F, 1.0f
			);
		}
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		if(state.pouringMetal > 0)
			state.pouringMetal--;
		if(!state.isPlayingSound.getAsBoolean())
		{
			final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(2.5, 3, 2.5));
			state.isPlayingSound = MultiblockSound.startSound(
					() -> state.active, context.isValid(), soundPos, IESounds.arcFurnace, 0.375f
			);
		}
		if(!state.active)
			return;
		final IMultiblockLevel level = context.getLevel();
		final Level rawLevel = level.getRawLevel();
		for(int i = 0; i < Math.max(1, state.queueSize*0.51); i++)
		{
			if(ApiUtils.RANDOM.nextInt(6)==0)
				for(final Vec3 offset : ELECTRODE_OFFSETS)
				{
					final Vec3 absPos = level.toAbsolute(offset);
					for(final double ySpeed : PARTICLE_Y_SPEEDS)
						rawLevel.addAlwaysVisibleParticle(
								IEParticles.SPARKS.get(),
								absPos.x, absPos.y, absPos.z,
								particleSpeed(0.025), ySpeed, particleSpeed(0.025)
						);
				}


			final Vec3 smokePos = level.toAbsolute(SMOKE_OFFSET);
			rawLevel.addAlwaysVisibleParticle(
					ParticleTypes.CAMPFIRE_COSY_SMOKE,
					smokePos.x, smokePos.y, smokePos.z,
					particleSpeed(0.009375), .0625, particleSpeed(0.009375)
			);
		}
	}

	private static double particleSpeed(double max)
	{
		return ApiUtils.RANDOM.nextDouble(-max, max);
	}

	private void enqueueProcesses(State state, Level level)
	{
		Int2IntOpenHashMap usedInvSlots = new Int2IntOpenHashMap();
		for(MultiblockProcess<ArcFurnaceRecipe, ProcessContextInMachine<ArcFurnaceRecipe>> process : state.processor.getQueue())
			if(process instanceof ArcFurnaceProcess arcProcess)
			{
				int[] inputSlots = arcProcess.getInputSlots();
				int[] inputAmounts = arcProcess.getInputAmounts();
				if(inputAmounts==null)
					continue;
				for(int i = 0; i < inputSlots.length; i++)
					if(inputAmounts[i] > 0)
						usedInvSlots.addTo(inputSlots[i], inputAmounts[i]);
			}

		NonNullList<ItemStack> additives = NonNullList.withSize(ADDITIVE_SLOT_COUNT, ItemStack.EMPTY);
		for(int i = 0; i < ADDITIVE_SLOT_COUNT; i++)
		{
			final ItemStack additive = state.inventory.getStackInSlot(FIRST_ADDITIVE_SLOT+i);
			if(additive.isEmpty())
				continue;
			additives.set(i, additive.copy());
			if(usedInvSlots.containsKey(FIRST_ADDITIVE_SLOT+i))
				additives.get(i).shrink(usedInvSlots.get(FIRST_ADDITIVE_SLOT+i));
		}

		for(int slot = FIRST_IN_SLOT; slot < IN_SLOT_COUNT; slot++)
		{
			if(usedInvSlots.containsKey(slot))
				continue;
			ItemStack stack = state.inventory.getStackInSlot(slot);
			if(stack.isEmpty())
				continue;
			RecipeHolder<ArcFurnaceRecipe> recipe = ArcFurnaceRecipe.findRecipe(level, stack, additives);
			if(recipe==null)
				continue;
			ArcFurnaceProcess process = new ArcFurnaceProcess(
					recipe, ApiUtils.RANDOM.nextLong(), slot, 12, 13, 14, 15
			);
			if(state.processor.addProcessToQueue(process, level, false))
			{
				int[] consumedAdditives = recipe.value().getConsumedAdditives(additives, true);
				if(consumedAdditives!=null)
					process.setInputAmounts(
							recipe.value().input.getCount(),
							consumedAdditives[0],
							consumedAdditives[1],
							consumedAdditives[2],
							consumedAdditives[3]
					);
			}
		}
	}

	private void outputItems(State state)
	{
		IItemHandler outputHandler = state.output.get();
		if(outputHandler!=null)
			for(int j : OUTPUT_SLOTS)
			{
				final ItemStack nextStack = state.inventory.getStackInSlot(j);
				if(nextStack.isEmpty())
					continue;
				ItemStack stack = nextStack.copyWithCount(1);
				stack = ItemHandlerHelper.insertItem(outputHandler, stack, false);
				if(stack.isEmpty())
					nextStack.shrink(1);
			}
		final ItemStack slagStack = state.inventory.getStackInSlot(SLAG_SLOT);
		if(slagStack.isEmpty())
			return;
		IItemHandler slagOutputHandler = state.slagOutput.get();
		if(slagOutputHandler!=null)
		{
			int out = Math.min(slagStack.getCount(), 16);
			ItemStack stack = slagStack.copyWithCount(out);
			stack = ItemHandlerHelper.insertItem(slagOutputHandler, stack, false);
			out -= stack.getCount();
			slagStack.shrink(out);
		}
	}

	@Override
	public void registerCapabilities(CapabilityRegistrar<State> register)
	{
		register.register(
				EnergyStorage.BLOCK,
				(state, pos) -> pos.side()==null||ENERGY_INPUTS.contains(pos)?state.energy: null
		);
		register.register(ItemHandler.BLOCK, (state, pos) -> {
			if(MAIN_CAP_POS.equals(pos))
				return state.outputHandler;
			else if(SLAG_CAP_POS.equals(pos))
				return state.slagHandler;
				//TODO are these swapped?
			else if(new BlockPos(1, 3, 2).equals(pos.posInMultiblock()))
				return state.insertionHandler;
			else if(new BlockPos(3, 3, 2).equals(pos.posInMultiblock()))
				return state.additiveHandler;
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
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		if(forType==ShapeType.SELECTION)
			return ArcFurnaceSelectionShapes.SHAPE_GETTER;
		else
			return ArcFurnaceShapes.SHAPE_GETTER;
	}

	public static ComparatorManager<State> makeInventoryComparator()
	{
		return ComparatorManager.makeSimple(
				state -> Utils.calcRedstoneFromInventory(IN_SLOT_COUNT, state.inventory), REDSTONE_POS
		);
	}

	public static ComparatorManager<State> makeElectrodeComparator()
	{
		return ComparatorManager.makeSimple(
				state -> Mth.ceil(Math.max(state.getElectrodeComparatorValue(), 0)*15), ELECTRODE_COMPARATOR_POS
		);
	}

	public static class State implements IMultiblockState, ProcessContextInMachine<ArcFurnaceRecipe>
	{
		private final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);
		public ItemStackHandler inventory = new ItemStackHandler(NUM_SLOTS){
			@Override
			public int getSlotLimit(int slot)
			{
				return 64;
			}
		};
		private final InMachineProcessor<ArcFurnaceRecipe> processor;

		// Utilities
		private final Supplier<@Nullable IItemHandler> output;
		private final Supplier<@Nullable IItemHandler> slagOutput;
		private final IItemHandler insertionHandler;
		private final IItemHandler additiveHandler;
		private final IItemHandler outputHandler;
		private final IItemHandler slagHandler;
		private final IMachineInterfaceConnection mifHandler;
		public final RSState rsControl = RSState.enabledByDefault();

		// Client/sync fields
		private boolean active;
		public byte electrodePresence;
		private int queueSize;
		public int pouringMetal = 0;
		private BooleanSupplier isPlayingSound = () -> false;

		public State(IInitialMultiblockContext<State> ctx)
		{
			this.processor = new InMachineProcessor<>(
					12, $ -> 0, 12, ctx.getMarkDirtyRunnable(), ctx.getSyncRunnable(), ArcFurnaceRecipe.RECIPES::getById
			);
			this.output = ctx.getCapabilityAt(Capabilities.ItemHandler.BLOCK, MAIN_OUT_POS);
			this.slagOutput = ctx.getCapabilityAt(Capabilities.ItemHandler.BLOCK, SLAG_OUT_POS);
			this.insertionHandler = new ArcFurnaceInputHandler(
					this.inventory, ctx.getMarkDirtyRunnable()
			);
			this.additiveHandler = new WrappingItemHandler(
					inventory, true, false, new IntRange(FIRST_ADDITIVE_SLOT, FIRST_ADDITIVE_SLOT+ADDITIVE_SLOT_COUNT)
			);
			this.outputHandler = new WrappingItemHandler(
					inventory, false, true, new IntRange(FIRST_OUT_SLOT, FIRST_OUT_SLOT+OUT_SLOT_COUNT)
			);
			this.slagHandler = new WrappingItemHandler(
					inventory, false, true, new IntRange(SLAG_SLOT, SLAG_SLOT+1)
			);
			this.mifHandler = () -> new MachineCheckImplementation[]{
					new MachineCheckImplementation<>((BooleanSupplier)() -> this.active, MachineInterfaceHandler.BASIC_ACTIVE),
					new MachineCheckImplementation<>(insertionHandler, MachineInterfaceHandler.BASIC_ITEM_IN),
					new MachineCheckImplementation<>(additiveHandler, MIF_CONDITION_ADDITIVES),
					new MachineCheckImplementation<>(outputHandler, MachineInterfaceHandler.BASIC_ITEM_OUT),
					new MachineCheckImplementation<>(slagHandler, MIF_CONDITION_SLAG),
					new MachineCheckImplementation<>(energy, MachineInterfaceHandler.BASIC_ENERGY),
					new MachineCheckImplementation<>(this, MIF_CONDITION_ELECTRODES)
			};
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt, Provider provider)
		{
			nbt.put("energy", energy.serializeNBT(provider));
			nbt.put("inventory", inventory.serializeNBT(provider));
			nbt.put("processor", processor.toNBT(provider));
		}

		@Override
		public void readSaveNBT(CompoundTag nbt, Provider provider)
		{
			energy.deserializeNBT(provider, nbt.get("energy"));
			inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
			processor.fromNBT(nbt.get("processor"), (getRecipe, data, p) -> new ArcFurnaceProcess(getRecipe, data), provider);
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt, Provider provider)
		{
			nbt.putByte("electrodeMask", electrodePresence);
			nbt.putBoolean("active", active);
			nbt.putInt("pouringMetal", pouringMetal);
			nbt.putInt("queueSize", processor.getQueueSize());
		}

		@Override
		public void readSyncNBT(CompoundTag nbt, Provider provider)
		{
			electrodePresence = nbt.getByte("electrodeMask");
			active = nbt.getBoolean("active");
			pouringMetal = nbt.getInt("pouringMetal");
			queueSize = nbt.getInt("queueSize");
		}

		private boolean updateElectrodePresence()
		{
			byte electrodePresence = 0;
			for(int i = 0; i < ELECTRODE_COUNT; i++)
				if(!inventory.getStackInSlot(i+FIRST_ELECTRODE_SLOT).isEmpty())
					electrodePresence |= (byte)(1<<i);
			if(electrodePresence!=this.electrodePresence)
			{
				this.electrodePresence = electrodePresence;
				return true;
			}
			return false;
		}

		@Override
		public AveragingEnergyStorage getEnergy()
		{
			return energy;
		}

		@Override
		public ItemStackHandler getInventory()
		{
			return inventory;
		}

		@Override
		public int[] getOutputSlots()
		{
			return OUTPUT_SLOTS;
		}

		public boolean isClientActive()
		{
			return active;
		}

		public List<MultiblockProcess<ArcFurnaceRecipe, ProcessContextInMachine<ArcFurnaceRecipe>>> getProcessQueue()
		{
			return processor.getQueue();
		}

		private float getElectrodeComparatorValue()
		{
			return (float)IntStream.range(0, ELECTRODE_COUNT).mapToDouble(i -> {
				final ItemStack electrode = inventory.getStackInSlot(FIRST_ELECTRODE_SLOT+i);
				if(electrode.isEmpty())
					return 0;
				return 1-(electrode.getDamageValue()/(float)electrode.getMaxDamage());
			}).min().orElse(0);
		}

		public boolean hasElectrodes()
		{
			for(int i = FIRST_ELECTRODE_SLOT; i < FIRST_ELECTRODE_SLOT+ELECTRODE_COUNT; i++)
				if(inventory.getStackInSlot(i).isEmpty())
					return false;
			return true;
		}

		@Override
		public boolean additionalCanProcessCheck(MultiblockProcess<ArcFurnaceRecipe, ?> process, Level level)
		{
			if(!hasElectrodes())
				return false;
			ArcFurnaceRecipe recipe = process.getRecipe(level);
			if(recipe==null||recipe.slag.get().isEmpty())
				return true;
			final ItemStack slag = this.inventory.getStackInSlot(SLAG_SLOT);
			if(slag.isEmpty())
				return true;
			return ItemStack.isSameItemSameComponents(slag, recipe.slag.get())&&slag.getCount()+recipe.slag.get().getCount() <= 64;
		}

		@Override
		public void onProcessFinish(MultiblockProcess<ArcFurnaceRecipe, ?> process, Level level)
		{
			ArcFurnaceRecipe recipe = process.getRecipe(level);
			if(recipe==null||recipe.slag.get().isEmpty())
				return;
			final ItemStack slag = this.inventory.getStackInSlot(SLAG_SLOT);
			if(slag.isEmpty())
				this.inventory.setStackInSlot(SLAG_SLOT, recipe.slag.get().copy());
			else if(ItemStack.isSameItemSameComponents(slag, recipe.slag.get()))
				slag.grow(recipe.slag.get().getCount());
		}
	}
}
