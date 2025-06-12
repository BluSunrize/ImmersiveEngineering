/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MBInventoryUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.ChunkLoaderMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ChunkLoaderLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.ChunkLoaderShapes;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraint;
import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler.IOConstraintGroup;
import blusunrize.immersiveengineering.common.util.inventory.WrappingItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketSet;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ChunkLoaderLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	public static final TicketController TICKET_CONTROLLER = new TicketController(
			ResourceLocation.fromNamespaceAndPath(Lib.MODID, "resonanz_observer"),
			(serverLevel, ticketHelper) -> {
				for(Entry<BlockPos, TicketSet> check : ticketHelper.getBlockTickets().entrySet())
				{
					boolean stillValid = false;
					if(serverLevel.getBlockEntity(check.getKey()) instanceof MultiblockBlockEntityMaster<?> mb)
						if(mb.getHelper().getState() instanceof State chunkLoaderState)
							stillValid = chunkLoaderState.refreshTimer > 0;
					if(!stillValid)
						ticketHelper.removeAllTickets(check.getKey());
				}
			}
	);

	public static final int ENERGY_CAPACITY = 32000;

	private static final CapabilityPosition ENERGY_INPUT = new CapabilityPosition(2, 1, 1, RelativeBlockFace.LEFT);
	public static final BlockPos REDSTONE_POS = new BlockPos(0, 1, 2);
	private static final CapabilityPosition INPUT_POS = new CapabilityPosition(0, 1, 1, RelativeBlockFace.RIGHT);

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		if(!(context.getLevel().getRawLevel() instanceof ServerLevel))
			return;
		final State state = context.getState();
		int energy_required = IEServerConfig.MACHINES.resonanz_observer_consumption.get();
		if(state.rsState.isEnabled(context)&&state.energy.extractEnergy(energy_required, true)==energy_required)
		{
			if(state.refreshTimer > 0)
				state.refreshTimer--;

			if(state.refreshTimer <= 0)
				if(!state.inventory.getStackInSlot(0).isEmpty())
				{
					// consume paper
					state.inventory.getStackInSlot(0).shrink(1);
					// consume energy
					state.energy.extractEnergy(energy_required, false);
					// set timer to the seconds configured in the config
					state.refreshTimer = 20*IEServerConfig.MACHINES.resonanz_observer_paper_duration.get();
					// mark chunks for loading
					forceChunks(context, true);
				}
				else
					forceChunks(context, false);
		}
		else if(state.refreshTimer > 0)
		{
			state.refreshTimer = 0;
			forceChunks(context, false);
		}

		// update client rendering
		final boolean wasActive = state.renderAsActive;
		state.renderAsActive = state.refreshTimer > 0;
		if(wasActive!=state.renderAsActive)
			context.requestMasterBESync();
	}

	private void forceChunks(IMultiblockContext<State> ctx, boolean add)
	{
		BlockPos masterPos = ctx.getLevel().toAbsolute(ChunkLoaderMultiblock.MASTER_OFFSET);
		if((ctx.getLevel().getRawLevel() instanceof ServerLevel serverLevel))
			getChunks(masterPos).forEach(chunk -> TICKET_CONTROLLER.forceChunk(serverLevel, masterPos, chunk.x, chunk.z, add, true));
	}

	private static Stream<ChunkPos> getChunks(BlockPos masterPos)
	{
		int blockRadius = IEServerConfig.MACHINES.resonanz_observer_radius.get();
		int minX = SectionPos.blockToSectionCoord(masterPos.getX()-blockRadius);
		int maxX = SectionPos.blockToSectionCoord(masterPos.getX()+blockRadius);
		int minZ = SectionPos.blockToSectionCoord(masterPos.getZ()-blockRadius);
		int maxZ = SectionPos.blockToSectionCoord(masterPos.getZ()+blockRadius);
		return IntStream.range(minX, maxX).boxed().flatMap(x -> IntStream.range(minZ, maxZ).mapToObj(z -> new ChunkPos(x, z)));
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
	}

	@Override
	public void onRemoved(IMultiblockContext<State> context)
	{
		forceChunks(context, false);
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public void registerCapabilities(CapabilityRegistrar<State> register)
	{
		register.registerAt(ItemHandler.BLOCK, INPUT_POS, state -> state.input);
		register.registerAt(EnergyStorage.BLOCK, ENERGY_INPUT, state -> state.energy);
	}

	@Override
	public void dropExtraItems(State state, Consumer<ItemStack> drop)
	{
		MBInventoryUtils.dropItems(state.inventory, drop);
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return ChunkLoaderShapes.SHAPE_GETTER;
	}

	public static class State implements IMultiblockState
	{
		public final SlotwiseItemHandler inventory;
		public final AveragingEnergyStorage energy = new AveragingEnergyStorage(ENERGY_CAPACITY);
		public final RSState rsState = RSState.enabledByDefault();
		public int refreshTimer = 0;
		public boolean renderAsActive;

		private final IItemHandler input;

		public State(IInitialMultiblockContext<State> ctx)
		{
			this.inventory = SlotwiseItemHandler.makeWithGroups(
					List.of(new IOConstraintGroup(new IOConstraint(true, i -> i.is(IETags.paper)), 1)),
					ctx.getMarkDirtyRunnable()
			);
			this.input = new WrappingItemHandler(inventory, true, false);
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt, Provider provider)
		{
			nbt.put("inventory", inventory.serializeNBT(provider));
			nbt.put("energy", energy.serializeNBT(provider));
			nbt.putInt("refreshTimer", refreshTimer);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt, Provider provider)
		{
			inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
			energy.deserializeNBT(provider, nbt.getCompound("energy"));
			refreshTimer = nbt.getInt("refreshTimer");
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt, Provider provider)
		{
			nbt.putBoolean("renderAsActive", renderAsActive);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt, Provider provider)
		{
			renderAsActive = nbt.getBoolean("renderAsActive");
		}

		public Stream<BlockEntity> getNearbyBlockEntities(IMultiblockContext<State> ctx)
		{
			BlockPos masterPos = ctx.getLevel().toAbsolute(ChunkLoaderMultiblock.MASTER_OFFSET);
			Level level = ctx.getLevel().getRawLevel();
			Stream<ChunkPos> chunks =ChunkLoaderLogic.getChunks(masterPos);
			return chunks
					// find all block entities in the area
					.flatMap(pos -> level.getChunk(pos.x, pos.z).getBlockEntities().values().stream())
					// filter to ticking ones
					.filter(blockEntity -> !masterPos.equals(blockEntity.getBlockPos())&&blockEntity.getBlockState().getTicker(level, blockEntity.getType())!=null);
		}
	}
}
