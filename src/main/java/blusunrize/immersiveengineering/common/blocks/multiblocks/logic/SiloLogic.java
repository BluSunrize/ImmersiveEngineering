/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SiloLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces.MBMemorizeStructure;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.SiloTankShapes;
import blusunrize.immersiveengineering.common.util.LayeredComparatorOutput;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class SiloLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>, MBMemorizeStructure<State>
{
	private static final SiloTankShapes SHAPE_GETTER = new SiloTankShapes(6, true);
	private static final int MAX_STORAGE = 41472;
	public static final BlockPos OUTPUT_POS = new BlockPos(1, 0, 1);
	private static final Set<BlockPos> IO_OFFSETS = Set.of(OUTPUT_POS, new BlockPos(1, 6, 1));

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		state.comparatorHelper.update(context, state.storageAmount);
		if(state.identStack.isEmpty()||state.storageAmount <= 0)
			return;
		final IMultiblockLevel level = context.getLevel();
		if(!level.shouldTickModulo(8)||!state.rsState.isEnabled(context))
			return;
		for(Supplier<@Nullable IItemHandler> output : state.outputs)
		{
			ItemStack stack = state.identStack.copyWithCount(1);
			stack = Utils.insertStackIntoInventory(output, stack, false);
			if(stack.isEmpty())
			{
				state.storageAmount--;
				if(state.storageAmount <= 0)
					state.identStack = ItemStack.EMPTY;
				context.markMasterDirty();
				context.requestMasterBESync();
				if(state.storageAmount <= 0)
					break;
			}
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
		register.register(ItemHandler.BLOCK, (state, position) -> {
			if(IO_OFFSETS.contains(position.posInMultiblock()))
				return state.inputHandler;
			else
				return null;
		});
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return SHAPE_GETTER;
	}

	@Override
	public void setMemorizedBlockState(State state, BlockPos pos, BlockState blockState)
	{
		state.structureMemo.put(pos, blockState);
	}

	@Override
	public BlockState getMemorizedBlockState(State state, BlockPos pos)
	{
		return state.structureMemo.get(pos);
	}

	public static class State implements IMultiblockState
	{
		public ItemStack identStack = ItemStack.EMPTY;
		public int storageAmount = 0;
		public final RSState rsState = RSState.disabledByDefault();

		// TODO integrate into component system?
		private final LayeredComparatorOutput<IMultiblockContext<?>> comparatorHelper;
		private final List<Supplier<@Nullable IItemHandler>> outputs;
		private final IItemHandler inputHandler;
		private final StructureMemo structureMemo = new StructureMemo();

		public State(IInitialMultiblockContext<State> capabilitySource)
		{
			this.comparatorHelper = LayeredComparatorOutput.makeForSiloLike(MAX_STORAGE, 6);
			ImmutableList.Builder<Supplier<@Nullable IItemHandler>> outputBuilder = ImmutableList.builder();
			for(RelativeBlockFace face : RelativeBlockFace.values())
				if(face!=RelativeBlockFace.DOWN)
				{
					final BlockPos neighbor = face.offsetRelative(OUTPUT_POS, -1);
					outputBuilder.add(capabilitySource.getCapabilityAt(ItemHandler.BLOCK, neighbor, face));
				}
			this.outputs = outputBuilder.build();
			this.inputHandler = new InventoryHandler(this, () -> {
				capabilitySource.getMarkDirtyRunnable().run();
				capabilitySource.getSyncRunnable().run();
			});
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt, Provider provider)
		{
			nbt.put("identStack", identStack.saveOptional(provider));
			nbt.putInt("count", storageAmount);
			structureMemo.writeSaveNBT(nbt, provider);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt, Provider provider)
		{
			identStack = ItemStack.parseOptional(provider, nbt.getCompound("identStack"));
			storageAmount = nbt.getInt("count");
			structureMemo.readSaveNBT(nbt, provider);
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt, Provider provider)
		{
			writeSaveNBT(nbt, provider);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt, Provider provider)
		{
			readSaveNBT(nbt, provider);
		}
	}

	private record InventoryHandler(State state, Runnable onChange) implements IItemHandler
	{

		@Override
		public int getSlots()
		{
			return 2;
		}

		@Override
		public ItemStack getStackInSlot(int slot)
		{
			if(slot==0)
				return ItemStack.EMPTY;
			else
				return state.identStack.copyWithCount(state.storageAmount);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			int space = MAX_STORAGE-state.storageAmount;
			if(slot!=0||space < 1||stack.isEmpty()||(!state.identStack.isEmpty()&&!ItemStack.isSameItemSameComponents(state.identStack, stack)))
				return stack;
			int accepted = Math.min(space, stack.getCount());
			if(!simulate)
			{
				state.storageAmount += accepted;
				if(state.identStack.isEmpty())
					state.identStack = stack.copy();
				onChange.run();
			}
			stack = stack.copy();
			stack.shrink(accepted);
			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			if(slot!=1||state.storageAmount < 1||amount < 1||state.identStack.isEmpty())
				return ItemStack.EMPTY;
			int returned = Math.min(Math.min(state.storageAmount, amount), state.identStack.getMaxStackSize());
			ItemStack out = state.identStack.copyWithCount(returned);
			if(!simulate)
			{
				state.storageAmount -= out.getCount();
				if(state.storageAmount <= 0)
					state.identStack = ItemStack.EMPTY;
				onChange.run();
			}
			return out;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return MAX_STORAGE;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
		{
			return slot==0;
		}
	}
}
