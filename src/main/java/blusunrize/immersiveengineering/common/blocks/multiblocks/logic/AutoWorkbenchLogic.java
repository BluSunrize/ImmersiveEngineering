/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager.SimpleComparatorValue;
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
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AutoWorkbenchLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor.InWorldProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.AutoWorkbenchShapes;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.DroppingMultiblockOutput;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraint;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraintGroup;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler.IntRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class AutoWorkbenchLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	public static final int BLUEPRINT_SLOT = 0;
	public static final int FIRST_INPUT_SLOT = 1;
	public static final int NUM_INPUT_SLOTS = 16;
	public static final int NUM_SLOTS = 1+NUM_INPUT_SLOTS;
	public static final int ENERGY_CAPACITY = 32000;

	private static final MultiblockFace OUTPUT_POS = new MultiblockFace(3, 1, 1, RelativeBlockFace.RIGHT);
	private static final BlockPos INPUT_POS = new BlockPos(0, 1, 2);
	private static final CapabilityPosition ENERGY_POS = new CapabilityPosition(0, 1, 2, RelativeBlockFace.UP);
	public static final BlockPos REDSTONE_POS = new BlockPos(1, 0, 2);
	public static final BlockPos PROCESS_1_POS = new BlockPos(1, 0, 0);
	public static final BlockPos PROCESS_2_POS = new BlockPos(2, 1, 1);

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		boolean isRSEnabled = state.rsState.isEnabled(context);
		boolean active = state.processor.tickServer(state, context.getLevel(), isRSEnabled);
		if(active!=state.active)
		{
			state.active = active;
			context.requestMasterBESync();
		}
		if(!context.getLevel().shouldTickModulo(16))
			return;
		if(!isRSEnabled||state.inventory.getStackInSlot(BLUEPRINT_SLOT).isEmpty())
			return;

		List<RecipeHolder<BlueprintCraftingRecipe>> recipes = getAvailableRecipes(context.getLevel().getRawLevel(), state);
		if(state.selectedRecipe >= 0&&state.selectedRecipe < recipes.size())
		{
			RecipeHolder<BlueprintCraftingRecipe> holder = recipes.get(state.selectedRecipe);
			BlueprintCraftingRecipe recipe = holder.value();
			if(!recipe.output.get().isEmpty())
			{
				NonNullList<ItemStack> query = NonNullList.withSize(NUM_INPUT_SLOTS, ItemStack.EMPTY);
				for(int i = 0; i < query.size(); i++)
					query.set(i, state.inventory.getStackInSlot(i+FIRST_INPUT_SLOT));
				int crafted = recipe.getMaxCrafted(query);
				if(crafted > 0&&state.processor.addProcessToQueue(
						new MultiblockProcessInWorld<>(holder, 0.78f, NonNullList.create()),
						context.getLevel().getRawLevel(),
						true
				))
				{
					state.processor.addProcessToQueue(
							new MultiblockProcessInWorld<>(holder, 0.78f, recipe.consumeInputs(query, 1)),
							context.getLevel().getRawLevel(),
							false
					);
					for(int i = 0; i < query.size(); i++)
						state.inventory.setStackInSlot(i+FIRST_INPUT_SLOT, query.get(i));
				}
			}
		}
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final IMultiblockLevel level = context.getLevel();
		final Level rawLevel = level.getRawLevel();
		if(context.getState().active)
			for(final MultiblockProcess<BlueprintCraftingRecipe, ProcessContextInWorld<BlueprintCraftingRecipe>> process : context.getState().processor.getQueue())
			{
				++process.processTick;
				Player localPlayer = ImmersiveEngineering.proxy.getClientPlayer();
				switch(process.processTick)
				{
					default: break;
					case 39:
						rawLevel.playSound(localPlayer, level.toAbsolute(PROCESS_1_POS), IESounds.process1.value(), SoundSource.BLOCKS, .25f, 1f);
						break;
					case 40:
					case 78:
						rawLevel.playSound(localPlayer, level.toAbsolute(PROCESS_1_POS), IESounds.process1Lift.value(), SoundSource.BLOCKS, .64f, 1f);
						break;
					case 136:
					case 163:
						rawLevel.playSound(localPlayer, level.toAbsolute(PROCESS_2_POS), IESounds.process2Lift.value(), SoundSource.BLOCKS, 1f, 1f);
						break;
					case 144:
						rawLevel.playSound(localPlayer, level.toAbsolute(PROCESS_2_POS), IESounds.process2.value(), SoundSource.BLOCKS, 1F, 1f);
						break;
				}
			}
	}

	public static List<RecipeHolder<BlueprintCraftingRecipe>> getAvailableRecipes(Level level, State state)
	{
		return EngineersBlueprintItem.getRecipes(level, state.inventory.getStackInSlot(BLUEPRINT_SLOT));
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public void registerCapabilities(CapabilityRegistrar<State> register)
	{
		register.register(ItemHandler.BLOCK, (state, pos) -> INPUT_POS.equals(pos.posInMultiblock())?state.input: null);
		register.registerAtOrNull(EnergyStorage.BLOCK, ENERGY_POS, state -> state.energy);
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
		return AutoWorkbenchShapes.SHAPE_GETTER;
	}

	public static ComparatorManager<State> makeComparator()
	{
		return ComparatorManager.makeSimple(
				SimpleComparatorValue.inventory(State::getInventory, FIRST_INPUT_SLOT, NUM_INPUT_SLOTS), REDSTONE_POS
		);
	}

	public static class State implements IMultiblockState, ProcessContextInWorld<BlueprintCraftingRecipe>
	{
		public final SlotwiseItemHandler inventory;
		public int selectedRecipe = -1;
		public final InWorldProcessor<BlueprintCraftingRecipe> processor;
		private final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);
		public final RSState rsState = RSState.enabledByDefault();

		// Only used on client
		public boolean active;

		private final DroppingMultiblockOutput output;
		private final IItemHandler input;
		private final IMachineInterfaceConnection mifHandler;

		public State(IInitialMultiblockContext<State> ctx)
		{
			this.processor = new InWorldProcessor<>(
					3, $ -> .4375f, 3,
					ctx.getMarkDirtyRunnable(), ctx.getSyncRunnable(),
					BlueprintCraftingRecipe.RECIPES::getById
			);
			this.output = new DroppingMultiblockOutput(OUTPUT_POS, ctx);
			this.inventory = SlotwiseItemHandler.makeWithGroups(
					List.of(
							new IOConstraintGroup(new IOConstraint(true, i -> i.is(Misc.BLUEPRINT.asItem())), 1),
							new IOConstraintGroup(IOConstraint.NO_CONSTRAINT, NUM_SLOTS)
					),
					ctx.getMarkDirtyRunnable()
			);
			this.input = new WrappingItemHandler(
					inventory, true, false, new IntRange(FIRST_INPUT_SLOT, NUM_SLOTS)
			);
			this.mifHandler = () -> new MachineCheckImplementation[]{
					new MachineCheckImplementation<>((BooleanSupplier)() -> this.active, MachineInterfaceHandler.BASIC_ACTIVE),
					new MachineCheckImplementation<>(input, MachineInterfaceHandler.BASIC_ITEM_IN),
					new MachineCheckImplementation<>(energy, MachineInterfaceHandler.BASIC_ENERGY),
			};
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt, Provider provider)
		{
			nbt.put("inventory", inventory.serializeNBT(provider));
			nbt.putInt("selectedRecipe", selectedRecipe);
			nbt.put("processor", processor.toNBT(provider));
			nbt.put("energy", energy.serializeNBT(provider));
		}

		@Override
		public void readSaveNBT(CompoundTag nbt, Provider provider)
		{
			inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
			selectedRecipe = nbt.getInt("selectedRecipe");
			processor.fromNBT(nbt.get("processor"), MultiblockProcessInWorld::new, provider);
			energy.deserializeNBT(provider, nbt.get("energy"));
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt, Provider provider)
		{
			nbt.put("processor", processor.toNBT(provider));
			nbt.putBoolean("active", active);
			nbt.put("blueprint", inventory.getStackInSlot(BLUEPRINT_SLOT).saveOptional(provider));
			nbt.putInt("selectedRecipe", selectedRecipe);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt, Provider provider)
		{
			processor.fromNBT(nbt.get("processor"), MultiblockProcessInWorld::new, provider);
			active = nbt.getBoolean("active");
			inventory.setStackInSlot(BLUEPRINT_SLOT, ItemStack.parseOptional(provider, nbt.getCompound("blueprint")));
			selectedRecipe = nbt.getInt("selectedRecipe");
		}

		@Override
		public AveragingEnergyStorage getEnergy()
		{
			return energy;
		}

		@Override
		public void doProcessOutput(ItemStack result, IMultiblockLevel level)
		{
			this.output.insertOrDrop(result, level);
		}
	}
}
