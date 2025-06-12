/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ChunkLoaderLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ChunkLoaderLogic.State;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

public class ChunkLoaderMenu extends IEContainerMenu
{
	private static final BlockPos CRYSTAL_POS = new BlockPos(1, 3, 1);

	public static ChunkLoaderMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<State> ctx
	)
	{
		final State state = ctx.mbContext().getState();
		return new ChunkLoaderMenu(
				multiblockCtx(type, id, ctx),
				invPlayer,
				state.inventory,
				state.energy,
				GetterAndSetter.getterOnly(() -> state.getNearbyBlockEntities(ctx.mbContext())
						.collect(Collector.of(
								ArrayListMultimap::create,
								(blockListMap, blockEntity) -> blockListMap.put(blockEntity.getBlockState().getBlock(), blockEntity.getBlockPos()),
								(m1, m2) -> {
									for(Block key : m2.keySet())
										m1.get(key).addAll(m2.get(key));
									return m1;
								},
								(Function<Multimap<Block, BlockPos>, List<NearbyBlockEntity>>)multimap -> multimap.keySet().stream()
										.sorted(Comparator.comparing(block -> block.getName().getString()))
										.map(block -> new NearbyBlockEntity(block.getName(), List.copyOf(multimap.get(block))))
										.toList()
						))
				),
				GetterAndSetter.getterOnly(() -> state.refreshTimer),
				GetterAndSetter.constant(ctx.mbContext().getLevel().toAbsolute(CRYSTAL_POS))
		);
	}

	public static ChunkLoaderMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new ChunkLoaderMenu(
				clientCtx(type, id),
				invPlayer,
				new ItemStackHandler(1),
				new MutableEnergyStorage(ChunkLoaderLogic.ENERGY_CAPACITY),
				GetterAndSetter.standalone(List.of()),
				GetterAndSetter.standalone(0),
				GetterAndSetter.standalone(BlockPos.ZERO)
		);
	}

	public final IEnergyStorage energy;
	public final GetterAndSetter<List<NearbyBlockEntity>> blockEntityList;
	public final GetterAndSetter<Integer> refreshTimer;
	public final GetterAndSetter<BlockPos> crystalPos;

	public ChunkLoaderMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv,
			IMutableEnergyStorage energy, GetterAndSetter<List<NearbyBlockEntity>> blockEntityList,
			GetterAndSetter<Integer> refreshTimer, GetterAndSetter<BlockPos> crystalPos
	)
	{
		super(ctx);
		this.energy = energy;
		this.blockEntityList = blockEntityList;
		this.refreshTimer = refreshTimer;
		this.crystalPos = crystalPos;

		this.addSlot(new IESlot.Tagged(inv, ownSlotCount++, 124, 94, IETags.paper));

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 160+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 218));
		addGenericData(GenericContainerData.energy(energy));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.NEARBY_BLOCK_ENTITIES, blockEntityList));
		addGenericData(GenericContainerData.int32(refreshTimer.getter(), refreshTimer.setter()));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.BLOCK_POS, crystalPos));
	}

	public record NearbyBlockEntity(Component name, List<BlockPos> pos)
	{
		public static final StreamCodec<RegistryFriendlyByteBuf, NearbyBlockEntity> STREAM_CODEC = StreamCodec.composite(
				ComponentSerialization.STREAM_CODEC, NearbyBlockEntity::name,
				BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), NearbyBlockEntity::pos,
				NearbyBlockEntity::new
		);

		public String getDisplayString()
		{
			return pos.size()+"x "+name.getString();
		}
	}
}