/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager.SimpleComparatorValue;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MBInventoryUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.IMachineInterfaceConnection;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.MachineCheckImplementation;
import blusunrize.immersiveengineering.api.tool.assembler.RecipeQuery;
import blusunrize.immersiveengineering.common.blocks.metal.CrafterPatternInventory;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AssemblerLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.AssemblerShapes;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.fluids.ArrayFluidHandler;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.InventoryCraftingFalse;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraint;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraintGroup;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class AssemblerLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	public static final int NUM_PATTERNS = 3;
	public static final int NUM_TANKS = 3;
	public static final int TANK_CAPACITY = 8*FluidType.BUCKET_VOLUME;
	public static final int ENERGY_CAPACITY = 32000;
	public static final int INVENTORY_SIZE = 18+NUM_PATTERNS;

	private static final CapabilityPosition ITEM_INPUT = new CapabilityPosition(1, 1, 2, RelativeBlockFace.BACK);
	private static final CapabilityPosition FLUID_INPUT = new CapabilityPosition(1, 0, 2, RelativeBlockFace.BACK);
	private static final CapabilityPosition ENERGY_INPUT = new CapabilityPosition(1, 2, 1, RelativeBlockFace.UP);
	public static final BlockPos[] REDSTONE_PORTS = {
			new BlockPos(0, 0, 1), new BlockPos(2, 0, 1)
	};

	// register additional conditions for machine interface
	public static ResourceLocation[] MIF_CONDITION_TANKS = IntStream.range(0, NUM_TANKS)
			.mapToObj(i -> ieLoc("assembler/tank_"+i))
			.toArray(ResourceLocation[]::new);

	static
	{
		for(ResourceLocation tank_cond : MIF_CONDITION_TANKS)
			MachineInterfaceHandler.copyOptions(tank_cond, MachineInterfaceHandler.BASIC_FLUID_IN);
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		if(!state.isPlayingSound.getAsBoolean())
		{
			final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(1.5, 1.5, 1.5));
			state.isPlayingSound = MultiblockSound.startSound(() -> state.shouldPlaySound, context.isValid(), soundPos, IESounds.assembler, 0.625f
			);
		}
	}

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		final boolean wasPlaying = state.shouldPlaySound;
		if(!state.rsState.isEnabled(context)&&wasPlaying!=state.rsState.isEnabled(context))
		{
			state.shouldPlaySound = false;
			context.requestMasterBESync();
		}
		if(!context.getLevel().shouldTickModulo(16)||!state.rsState.isEnabled(context))
			return;
		final List<OutputBuffer> outputs = craftRecipes(context);
		for(OutputBuffer buffer : outputs)
			for(int i = 0; i < buffer.results.size(); ++i)
				outputStack(state, buffer.results.get(i), buffer.id, i==0);
		for(int i = 0; i < 3; i++)
			if(!isRecipeIngredient(state, state.inventory.getStackInSlot(18+i), i))
				state.inventory.setStackInSlot(
						18+i, Utils.insertStackIntoInventory(state.output, state.inventory.getStackInSlot(18+i), false)
				);
		state.shouldPlaySound = state.rsState.isEnabled(context)&&!outputs.isEmpty();
		if(wasPlaying!=state.shouldPlaySound)
			context.requestMasterBESync();
	}

	private List<OutputBuffer> craftRecipes(IMultiblockContext<State> ctx)
	{
		final State state = ctx.getState();
		List<OutputBuffer> outputBuffer = new ArrayList<>();
		for(int patternId = 0; patternId < state.patterns.length; patternId++)
		{
			CrafterPatternInventory pattern = state.patterns[patternId];
			ItemStack output = pattern.inv.get(9).copy();
			if(output.isEmpty()||!canOutput(state, output, patternId))
				continue;
			ArrayList<ItemStack> availableStacks = new ArrayList<>();//List of all available inputs in the inventory
			for(OutputBuffer bufferedStacks : outputBuffer)
				availableStacks.addAll(bufferedStacks.results);
			for(ItemStack stack : state.inventory)
				if(!stack.isEmpty())
					availableStacks.add(stack);
			List<RecipeQuery> queries = pattern.getQueries(ctx.getLevel().getRawLevel());
			if(queries==null)
				continue;

			int consumed = IEServerConfig.MACHINES.assembler_consumption.get();
			if(!this.consumeIngredients(state, queries, availableStacks, false, null))
				continue;
			if(state.energy.extractEnergy(consumed, false)!=consumed)
				continue;
			NonNullList<ItemStack> outputList = NonNullList.create();//List of all outputs for the current recipe. This includes discarded containers
			outputList.add(output);

			RecipeInputSources sources = new RecipeInputSources(pattern);
			this.consumeIngredients(state, queries, availableStacks, true, sources);

			NonNullList<ItemStack> remainingItems = pattern.recipe.getRemainingItems(
					InventoryCraftingFalse.createFilledCraftingInventory(3, 3, sources.gridItems)
			);
			for(int i = 0; i < remainingItems.size(); i++)
			{
				ItemStack rem = remainingItems.get(i);
				if(!sources.providedByNonItem.getBoolean(i)&&!rem.isEmpty())
					outputList.add(rem);
			}

			outputBuffer.add(new OutputBuffer(outputList, patternId));
			ctx.markMasterDirty();
		}
		return outputBuffer;
	}

	private void outputStack(State state, ItemStack output, int patternId, boolean isMainOutput)
	{
		if(!isRecipeIngredient(state, output, patternId))
		{
			output = Utils.insertStackIntoInventory(state.output, output, false);
			if(output.isEmpty()||output.getCount() <= 0)
				return;
		}
		if(isMainOutput)
			tryInsertOnto(state, 18+patternId, output);
		else
		{
			boolean inserted = false;
			for(int i = 0; i < state.inventory.getSlots(); i++)
				if(tryInsertOnto(state, i, output))
				{
					inserted = true;
					break;
				}
			if(!inserted)
				for(int i = 0; i < state.inventory.getSlots(); i++)
					if(state.inventory.getStackInSlot(i).isEmpty())
						state.inventory.setStackInSlot(i, output.copy());
		}
	}

	public boolean consumeIngredients(
			State state,
			List<RecipeQuery> queries, ArrayList<ItemStack> itemStacks, boolean doConsume,
			@Nullable RecipeInputSources sources
	)
	{
		if(!doConsume)
		{
			ArrayList<ItemStack> dupeList = new ArrayList<>(itemStacks.size());
			for(ItemStack stack : itemStacks)
				dupeList.add(stack.copy());
			itemStacks = dupeList;
		}
		List<FluidStack> tankFluids = Arrays.stream(state.tanks)
				.map(tank -> doConsume?tank.getFluid(): tank.getFluid().copy())
				.toList();
		for(int i = 0; i < queries.size(); i++)
		{
			RecipeQuery recipeQuery = queries.get(i);
			int querySize = recipeQuery.getItemCount();
			if(recipeQuery.isFluid())
			{
				if(consumeFluid(tankFluids, i, recipeQuery, sources))
					continue;
				else
					querySize = 1;
			}
			for(ItemStack next : itemStacks)
				querySize -= consumeItem(querySize, i, next, recipeQuery, sources);
			if(querySize > 0)
				return false;
		}
		return true;
	}

	public boolean isRecipeIngredient(State state, ItemStack stack, int slot)
	{
		if(stack.isEmpty())
			return false;
		if(slot-1 < state.patterns.length||state.recursiveIngredients)
			for(int p = state.recursiveIngredients?0: slot; p < state.patterns.length; p++)
			{
				CrafterPatternInventory pattern = state.patterns[p];
				for(int i = 0; i < 9; i++)
					if(!pattern.inv.get(i).isEmpty()&&ItemStack.isSameItem(pattern.inv.get(i), stack))
						return true;
			}
		return false;
	}

	private boolean consumeFluid(
			List<FluidStack> tankFluids, int queryIndex, RecipeQuery query, @Nullable RecipeInputSources sources
	)
	{
		for(FluidStack tankFluid : tankFluids)
			if(query.matchesFluid(tankFluid))
			{
				tankFluid.shrink(query.getFluidSize());
				if(sources!=null)
					sources.providedByNonItem.set(sources.getSlotForQueryIndex(queryIndex), true);
				return true;
			}
		return false;
	}

	private int consumeItem(
			int maxConsume, int queryIndex, ItemStack next, RecipeQuery query, @Nullable RecipeInputSources sources
	)
	{
		if(maxConsume <= 0||next.isEmpty()||!query.matchesIgnoringSize(next))
			return 0;
		int taken = Math.min(maxConsume, next.getCount());
		ItemStack forGrid = next.split(taken);
		if(sources!=null)
			sources.gridItems.set(sources.getSlotForQueryIndex(queryIndex), forGrid);
		return taken;
	}

	private boolean tryInsertOnto(State state, int slot, ItemStack toAdd)
	{
		if(!canInsertOnto(state, slot, toAdd))
			return false;
		final ItemStack present = state.inventory.getStackInSlot(slot);
		if(present.isEmpty())
			state.inventory.setStackInSlot(slot, toAdd);
		else
			present.grow(toAdd.getCount());
		return true;
	}

	public boolean canInsertOnto(State state, int slot, ItemStack output)
	{
		final ItemStack existing = state.inventory.getStackInSlot(slot);
		if(existing.isEmpty())
			return true;
		else if(!ItemStack.isSameItemSameComponents(output, existing))
			return false;
		else
			return existing.getCount()+output.getCount() <= existing.getMaxStackSize();
	}

	public boolean canOutput(State state, ItemStack output, int iPattern)
	{
		return canInsertOnto(state, 18+iPattern, output);
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public void registerCapabilities(CapabilityRegistrar<State> register)
	{
		register.registerAt(ItemHandler.BLOCK, ITEM_INPUT, state -> state.itemInput);
		register.registerAt(FluidHandler.BLOCK, FLUID_INPUT, state -> state.fluidInput);
		register.registerAt(EnergyStorage.BLOCK, ENERGY_INPUT, state -> state.energy);
		for(BlockPos bp : REDSTONE_PORTS)
			register.registerAtBlockPos(IMachineInterfaceConnection.CAPABILITY, bp, state -> state.mifHandler);
	}

	@Override
	public void dropExtraItems(State state, Consumer<ItemStack> drop)
	{
		MBInventoryUtils.dropItems(state.inventory, drop);
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return AssemblerShapes.SHAPE_GETTER;
	}

	public static ComparatorManager<State> makeComparator()
	{
		return ComparatorManager.makeSimple(
				SimpleComparatorValue.inventory(State::getInventory, 0, 18), REDSTONE_PORTS
		);
	}

	public static class State implements IMultiblockState
	{
		public final FluidTank[] tanks = IntStream.range(0, NUM_TANKS)
				.mapToObj($ -> new FluidTank(TANK_CAPACITY))
				.toArray(FluidTank[]::new);
		public final SlotwiseItemHandler inventory;
		public final CrafterPatternInventory[] patterns = IntStream.range(0, NUM_TANKS)
				.mapToObj($ -> new CrafterPatternInventory())
				.toArray(CrafterPatternInventory[]::new);
		public boolean recursiveIngredients = false;
		public final MutableEnergyStorage energy = new MutableEnergyStorage(ENERGY_CAPACITY);
		public final RSState rsState = RSState.enabledByDefault();

		private final Supplier<@Nullable IItemHandler> output;
		private final IItemHandler itemInput;
		private final IFluidHandler fluidInput;
		private final IMachineInterfaceConnection mifHandler;
		private BooleanSupplier isPlayingSound = () -> false;
		private boolean shouldPlaySound;

		public State(IInitialMultiblockContext<State> ctx)
		{
			output = ctx.getCapabilityAt(ItemHandler.BLOCK, new BlockPos(1, 1, -1), RelativeBlockFace.FRONT);
			inventory = SlotwiseItemHandler.makeWithGroups(
					List.of(new IOConstraintGroup(IOConstraint.NO_CONSTRAINT, INVENTORY_SIZE)),
					ctx.getMarkDirtyRunnable()
			);
			itemInput = new WrappingItemHandler(inventory, true, false);
			fluidInput = new ArrayFluidHandler(tanks, false, true, ctx.getMarkDirtyRunnable());
			mifHandler = () -> new MachineCheckImplementation[]{
					new MachineCheckImplementation<>(itemInput, MachineInterfaceHandler.BASIC_ITEM_IN),
					new MachineCheckImplementation<>(tanks[0], MIF_CONDITION_TANKS[0]),
					new MachineCheckImplementation<>(tanks[1], MIF_CONDITION_TANKS[1]),
					new MachineCheckImplementation<>(tanks[2], MIF_CONDITION_TANKS[2]),
					new MachineCheckImplementation<>(output, MachineInterfaceHandler.BASIC_ITEM_OUT),
					new MachineCheckImplementation<>(energy, MachineInterfaceHandler.BASIC_ENERGY)
			};
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt, Provider provider)
		{
			ListTag tanks = new ListTag();
			for(FluidTank tank : this.tanks)
				tanks.add(tank.writeToNBT(provider, new CompoundTag()));
			ListTag patterns = new ListTag();
			for(CrafterPatternInventory pattern : this.patterns)
				patterns.add(pattern.writeToNBT(provider));
			nbt.put("tanks", tanks);
			nbt.put("patterns", patterns);
			nbt.putBoolean("recursiveIngredients", recursiveIngredients);
			nbt.put("inventory", inventory.serializeNBT(provider));
			nbt.put("energy", energy.serializeNBT(provider));
		}

		@Override
		public void readSaveNBT(CompoundTag nbt, Provider provider)
		{
			ListTag tanks = nbt.getList("tanks", Tag.TAG_COMPOUND);
			for(int i = 0; i < NUM_TANKS; ++i)
				this.tanks[i].readFromNBT(provider, tanks.getCompound(i));
			ListTag patterns = nbt.getList("patterns", Tag.TAG_LIST);
			for(int i = 0; i < NUM_PATTERNS; ++i)
				this.patterns[i].readFromNBT(patterns.getList(i), provider);
			recursiveIngredients = nbt.getBoolean("recursiveIngredients");
			inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
			energy.deserializeNBT(provider, nbt.get("energy"));
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt, Provider provider)
		{
			nbt.putBoolean("shouldPlaySound", shouldPlaySound);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt, Provider provider)
		{
			shouldPlaySound = nbt.getBoolean("shouldPlaySound");
		}

		public IItemHandler getInventory()
		{
			return inventory;
		}
	}

	private record OutputBuffer(NonNullList<ItemStack> results, int id)
	{
	}

	private record RecipeInputSources(List<ItemStack> gridItems, BooleanList providedByNonItem)
	{
		public RecipeInputSources(CrafterPatternInventory pattern)
		{
			this(new ArrayList<>(pattern.inv), new BooleanArrayList(new boolean[9]));
		}

		public int getSlotForQueryIndex(int queryIdx)
		{
			int slot = 0;
			for(; slot < gridItems.size(); slot++)
				if(!this.gridItems.get(slot).isEmpty()&&--queryIdx < 0)
					return slot;
			return 0;
		}
	}
}
