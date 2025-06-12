/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.SorterBlockEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.SorterBlockEntity.FilterConfig;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SorterMenu extends IEContainerMenu
{
	public static SorterMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, SorterBlockEntity be
	)
	{
		final Map<Direction, GetterAndSetter<FilterConfig>> filters = Arrays.stream(Direction.values())
				.map(d -> Pair.of(d, new GetterAndSetter<>(() -> be.sideFilter.get(d), f -> be.sideFilter.put(d, f))))
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
		List<GetterAndSetter<Optional<ResourceLocation>>> selectedTag = IntStream.range(0, SorterBlockEntity.TOTAL_SLOTS).mapToObj(slot -> new GetterAndSetter<>(
				() -> Optional.ofNullable(be.filter.getSelectedTag(slot)),
				resourceLocation -> be.filter.setSelectedTag(slot, resourceLocation.orElse(null))
		)).toList();
		return new SorterMenu(blockCtx(type, id, be), invPlayer, be.filter, filters, selectedTag);
	}

	public static SorterMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		final Map<Direction, GetterAndSetter<FilterConfig>> filters = Arrays.stream(Direction.values())
				.map(d -> Pair.of(d, GetterAndSetter.standalone(FilterConfig.DEFAULT)))
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
		List<GetterAndSetter<Optional<ResourceLocation>>> selectedTag = IntStream.range(0, SorterBlockEntity.TOTAL_SLOTS)
				.mapToObj(slot -> GetterAndSetter.standalone(Optional.ofNullable((ResourceLocation)null)))
				.toList();
		return new SorterMenu(clientCtx(type, id), invPlayer, new ItemStackHandler(SorterBlockEntity.TOTAL_SLOTS), filters, selectedTag);
	}

	public final Map<Direction, GetterAndSetter<FilterConfig>> filterMasks;
	public final List<GetterAndSetter<Optional<ResourceLocation>>> selectedTags;

	private SorterMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler filter, Map<Direction, GetterAndSetter<FilterConfig>> filterMasks,
			List<GetterAndSetter<Optional<ResourceLocation>>> selectedTags
	)
	{
		super(ctx);
		this.filterMasks = filterMasks;
		this.selectedTags = selectedTags;
		for(int side = 0; side < 6; side++)
			for(int i = 0; i < SorterBlockEntity.FILTER_SLOTS_PER_SIDE; i++)
			{
				int x = 4+(side/2)*58+(i < 3?i*18: i > 4?(i-5)*18: i==3?0: 36);
				int y = 22+(side%2)*76+(i < 3?0: i > 4?36: 18);
				int id = side*SorterBlockEntity.FILTER_SLOTS_PER_SIDE+i;
				this.addSlot(new IESlot.ItemHandlerGhost(filter, id, x, y));
			}

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 163+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 221));
		for(Direction d : Direction.values())
			addGenericData(new GenericContainerData<>(GenericDataSerializers.FILTER_CONFIG, filterMasks.get(d)));
		for(GetterAndSetter<Optional<ResourceLocation>> selTag : selectedTags)
			addGenericData(new GenericContainerData<>(GenericDataSerializers.OPTIONAL_RESOURCE_LOCATION, selTag));
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player player, int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public void receiveMessageFromScreen(CompoundTag message)
	{
		if(message.contains("sideConfigId", Tag.TAG_INT))
		{
			var filter = FilterConfig.CODEC.fromNBT(message.get("sideConfigVal"));
			filterMasks.get(Direction.values()[message.getInt("sideConfigId")]).set(filter);
		}
		else if(message.contains("tagSlot", Tag.TAG_INT))
		{
			var selected = ResourceLocation.parse(message.getString("selectedTag"));
			selectedTags.get(message.getInt("tagSlot")).set(Optional.of(selected));
		}
	}
}