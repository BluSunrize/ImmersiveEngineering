/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.BucketWheelLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.BucketWheelShapes;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class BucketWheelLogic
		implements IMultiblockLogic<State>, IClientTickableComponent<State>, IServerTickableComponent<State>
{
	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		tickClient(context);
		if(context.getState().active&&context.getLevel().shouldTickModulo(20))
			context.requestMasterBESync();
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		if(state.active)
		{
			state.rotation += IEServerConfig.MACHINES.excavator_speed.get();
			state.rotation %= 360;
		}
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> ctx)
	{
		return new State();
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return BucketWheelShapes.SHAPE_GETTER;
	}

	public static class State implements IMultiblockState
	{
		public float rotation = 0;
		public final NonNullList<ItemStack> digStacks = NonNullList.withSize(8, ItemStack.EMPTY);
		public boolean active = false;
		// Used to adjust the direction of the wheel when installed in the excavator
		public boolean reverseRotation = false;
		public boolean outputLeft = false;

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.putFloat("rotation", rotation);
			ListTag stacksNBT = new ListTag();
			for(final ItemStack stack : digStacks)
				stacksNBT.add(stack.save(new CompoundTag()));
			nbt.put("stacks", stacksNBT);
			nbt.putBoolean("active", active);
			nbt.putBoolean("renderReverse", reverseRotation);
			nbt.putBoolean("outputLeft", outputLeft);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			rotation = nbt.getFloat("rotation");
			final ListTag stacksNBT = nbt.getList("stacks", Tag.TAG_COMPOUND);
			for(int i = 0; i < stacksNBT.size(); ++i)
				digStacks.set(i, ItemStack.of(stacksNBT.getCompound(i)));
			active = nbt.getBoolean("active");
			reverseRotation = nbt.getBoolean("renderReverse");
			outputLeft = nbt.getBoolean("outputLeft");
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
}
