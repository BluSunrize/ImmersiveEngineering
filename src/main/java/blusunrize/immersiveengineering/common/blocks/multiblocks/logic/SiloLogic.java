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
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SiloLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.SiloShapes;
import blusunrize.immersiveengineering.common.util.LayeredComparatorOutput;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class SiloLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>
{
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
		for(CapabilityReference<IItemHandler> output : state.outputs)
		{
			ItemStack stack = ItemHandlerHelper.copyStackWithSize(state.identStack, 1);
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
	public <T>
	LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		if(cap==ForgeCapabilities.ITEM_HANDLER&&IO_OFFSETS.contains(position.posInMultiblock()))
			return ctx.getState().inputHandler.cast(ctx);
		return LazyOptional.empty();
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return SiloShapes.SHAPE_GETTER;
	}

	public static class State implements IMultiblockState
	{
		public ItemStack identStack = ItemStack.EMPTY;
		public int storageAmount = 0;
		public final RSState rsState = RSState.disabledByDefault();

		// TODO integrate into component system?
		private final LayeredComparatorOutput<IMultiblockContext<?>> comparatorHelper;
		private final List<CapabilityReference<IItemHandler>> outputs;
		private final StoredCapability<IItemHandler> inputHandler;

		public State(IInitialMultiblockContext<State> capabilitySource)
		{
			this.comparatorHelper = LayeredComparatorOutput.makeForSiloLike(MAX_STORAGE, 6);
			ImmutableList.Builder<CapabilityReference<IItemHandler>> outputBuilder = ImmutableList.builder();
			for(RelativeBlockFace face : RelativeBlockFace.values())
				if(face!=RelativeBlockFace.DOWN)
				{
					final BlockPos neighbor = face.offsetRelative(OUTPUT_POS, -1);
					outputBuilder.add(capabilitySource.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, neighbor, face));
				}
			this.outputs = outputBuilder.build();
			this.inputHandler = new StoredCapability<>(new InventoryHandler(this, () -> {
				capabilitySource.getMarkDirtyRunnable().run();
				capabilitySource.getSyncRunnable().run();
			}));
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.put("identStack", identStack.save(new CompoundTag()));
			nbt.putInt("count", storageAmount);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			identStack = ItemStack.of(nbt.getCompound("identStack"));
			storageAmount = nbt.getInt("count");
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt)
		{
			writeSaveNBT(nbt);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt)
		{
			readSaveNBT(nbt);
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
				return ItemHandlerHelper.copyStackWithSize(state.identStack, state.storageAmount);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			int space = MAX_STORAGE-state.storageAmount;
			if(slot!=0||space < 1||stack.isEmpty()||(!state.identStack.isEmpty()&&!ItemHandlerHelper.canItemStacksStack(state.identStack, stack)))
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
			ItemStack out = ItemHandlerHelper.copyStackWithSize(state.identStack, returned);
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
