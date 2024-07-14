/*
 * BluSunrize
 * Copyright (c) 2024
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
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.RadioTowerLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.RadioTowerShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Consumer;
import java.util.function.Function;

public class RadioTowerLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	public static final BlockPos REDSTONE_POS = new BlockPos(2, 1, 2);

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public void registerCapabilities(CapabilityRegistrar<State> register)
	{
//		register.registerAtBlockPos(IMachineInterfaceConnection.CAPABILITY, REDSTONE_POS, state -> state.mifHandler);
	}

	@Override
	public void dropExtraItems(State state, Consumer<ItemStack> drop)
	{
//		MBInventoryUtils.dropItems(state.inventory, drop);
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return RadioTowerShapes.SHAPE_GETTER;
	}

	public static class State implements IMultiblockState
	{
//		private final IMachineInterfaceConnection mifHandler;
		public int frequency = 142;


		public State(IInitialMultiblockContext<State> ctx)
		{
//			this.mifHandler = () -> new MachineCheckImplementation[]{
//					new MachineCheckImplementation<>((BooleanSupplier)() -> this.active, MachineInterfaceHandler.BASIC_ACTIVE),
//					new MachineCheckImplementation<>(insertionHandler, MachineInterfaceHandler.BASIC_ITEM_IN),
//					new MachineCheckImplementation<>(extractionHandler, MachineInterfaceHandler.BASIC_ITEM_OUT),
//					new MachineCheckImplementation<>(fluidHandler, MachineInterfaceHandler.BASIC_FLUID_OUT),
//					new MachineCheckImplementation<>(energy, MachineInterfaceHandler.BASIC_ENERGY),
//			};
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.putInt("frequency",frequency);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			frequency = nbt.getInt("frequency");
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt)
		{
			// write a dummy value to prevent NPE exceptions
			nbt.putBoolean("npe_avoid", true);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt)
		{
		}
	}
}
