/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork.RedstoneBundleConnection;
import blusunrize.immersiveengineering.api.wires.redstone.WirelessRedstoneHandler;
import blusunrize.immersiveengineering.api.wires.redstone.WirelessRedstoneHandler.IWirelessRedstoneComponent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.RadioTowerLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.RadioTowerShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class RadioTowerLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	public static final int FREQUENCY_MIN = 128;
	public static final int FREQUENCY_MAX = 384;
	public static final int ENERGY_CAPACITY = 64000;

	public static final BlockPos REDSTONE_POS = new BlockPos(2, 1, 5);
	public static final BlockPos BROADCAST_POS = new BlockPos(2, 13, 2);

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		IMultiblockLevel level = context.getLevel();
		final Level rawLevel = context.getLevel().getRawLevel();
		State state = context.getState();
		if(rawLevel instanceof ServerLevel serverLevel)
		{
			BlockPos broadcastPos = level.toAbsolute(BROADCAST_POS);
			// update the maximum range if it's undefined or every ~ 7 minutes
			if(state.rangeInChunks < 0||serverLevel.getGameTime()%8192==((broadcastPos.getX()^broadcastPos.getZ())&8191))
			{
				int maxRange = calculateMaxRange(serverLevel, broadcastPos, 4);
				state.rangeInChunks = maxRange;
			}

			// fetch the wireless handler for this world, register with it if not yet done
			WirelessRedstoneHandler handler = WirelessRedstoneHandler.getHandler(serverLevel);
			if(!handler.isRegistered(broadcastPos))
				handler.register(broadcastPos, context.getState());

			// if there is a signal to be sent, do so
			if(state.sendingDirty)
			{
				handler.sendSignal(broadcastPos, state, state.sendingSignals);
				state.sendingDirty = false;
			}
		}

	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
	}

	private int calculateMaxRange(Level level, BlockPos from, int chunkRadius)
	{
		int chunkX = SectionPos.blockToSectionCoord(from.getX());
		int chunkZ = SectionPos.blockToSectionCoord(from.getZ());
		int countOfHigher = 0;
		for(int offsetZ = -chunkRadius; offsetZ <= chunkRadius; offsetZ++)
			for(int offsetX = -chunkRadius; offsetX <= chunkRadius; offsetX++)
			{
				LevelChunk chunk = level.getChunk(chunkX+offsetX, chunkZ+offsetZ);
				ChunkPos chunkPos = chunk.getPos();
				for(int iX = chunkPos.getMinBlockX(); iX <= chunkPos.getMaxBlockX(); iX++)
					for(int iZ = chunkPos.getMinBlockZ(); iZ <= chunkPos.getMaxBlockZ(); iZ++)
						if(chunk.getHeight(Types.WORLD_SURFACE, iX, iZ) > from.getY()+5)
							countOfHigher++;
			}
		double mod = 1.015-1/(1+Math.exp(-(countOfHigher/256d-6)));
		return (int)Mth.clamp(1024*mod, 16, 1024);
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public void registerCapabilities(CapabilityRegistrar<State> register)
	{
		register.registerAtBlockPos(CapabilityRedstoneNetwork.REDSTONE_BUNDLE_CONNECTION, REDSTONE_POS, state -> state.bundleConnection);
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

	public static class State implements IMultiblockState, IWirelessRedstoneComponent
	{
		public final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);
		private final RedstoneBundleConnection bundleConnection;
		public int frequency = 142;
		public int rangeInChunks = -1;

		private byte[] receivedSignals = new byte[16];
		private byte[] sendingSignals = new byte[16];
		private boolean sendingDirty = false;


		public State(IInitialMultiblockContext<State> ctx)
		{
//			this.mifHandler = () -> new MachineCheckImplementation[]{
//					new MachineCheckImplementation<>((BooleanSupplier)() -> this.active, MachineInterfaceHandler.BASIC_ACTIVE),
//					new MachineCheckImplementation<>(insertionHandler, MachineInterfaceHandler.BASIC_ITEM_IN),
//					new MachineCheckImplementation<>(extractionHandler, MachineInterfaceHandler.BASIC_ITEM_OUT),
//					new MachineCheckImplementation<>(fluidHandler, MachineInterfaceHandler.BASIC_FLUID_OUT),
//					new MachineCheckImplementation<>(energy, MachineInterfaceHandler.BASIC_ENERGY),
//			};
			bundleConnection = new RedstoneBundleConnection()
			{
				@Override
				public void onChange(byte[] externalInputs, Direction side)
				{
					if(!Arrays.equals(externalInputs, sendingSignals))
					{
						sendingSignals = externalInputs;
						sendingDirty = true;
					}
				}

				@Override
				public void updateInput(byte[] signals, Direction side)
				{
					for(int i = 0; i < signals.length; i++)
						signals[i] = (byte)Math.max(signals[i], receivedSignals[i]);
				}
			};
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.putInt("frequency", frequency);
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

		public int getChunkRange()
		{
			return rangeInChunks;
		}

		@Override
		public int getChunkRangeSq()
		{
			return rangeInChunks*rangeInChunks;
		}

		@Override
		public int getFrequency()
		{
			return frequency;
		}

		public void setFrequency(int frequency)
		{
			this.frequency = frequency;
		}

		@Override
		public void receiveSignal(byte[] signal)
		{
			this.receivedSignals = signal;
			this.bundleConnection.markDirty();
		}

		public List<Vec3> getRelativeComponentsInRange(IMultiblockContext<State> context)
		{
			IMultiblockLevel level = context.getLevel();
			final Level rawLevel = context.getLevel().getRawLevel();
			if(rawLevel instanceof ServerLevel serverLevel)
				return WirelessRedstoneHandler.getHandler(serverLevel).getRelativeComponentsInRange(
						level.toAbsolute(BROADCAST_POS),
						this
				);
			return List.of();
		}
	}
}
