/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherBlockEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherBlockEntity.BatchMode;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.IntStream;

import static blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherBlockEntity.NUM_SLOTS;

public class ItemBatcherMenu extends IEContainerMenu
{
	public static ItemBatcherMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, ItemBatcherBlockEntity be
	)
	{
		List<GetterAndSetter<Integer>> colors = IntStream.range(0, NUM_SLOTS)
				.mapToObj(i -> new GetterAndSetter<>(
						() -> be.redstoneColors.get(i).getId(), c -> be.redstoneColors.set(i, DyeColor.byId(c))
				))
				.toList();
		return new ItemBatcherMenu(
				blockCtx(type, id, be),
				invPlayer,
				new ItemStackHandler(be.getInventory()),
				new ItemStackHandler(be.getFilters()),
				new GetterAndSetter<>(() -> be.batchMode.ordinal(), b -> be.batchMode = BatchMode.values()[b]),
				colors
		);
	}

	public static ItemBatcherMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		List<GetterAndSetter<Integer>> colors = IntStream.range(0, NUM_SLOTS)
				.mapToObj($ -> GetterAndSetter.standalone(0))
				.toList();
		return new ItemBatcherMenu(
				clientCtx(type, id),
				invPlayer,
				new ItemStackHandler(NUM_SLOTS),
				new ItemStackHandler(NUM_SLOTS),
				GetterAndSetter.standalone(0),
				colors
		);
	}

	public final GetterAndSetter<Integer> batchMode;
	public final List<GetterAndSetter<Integer>> colors;

	public ItemBatcherMenu(
			MenuContext ctx,
			Inventory inventoryPlayer,
			IItemHandler filters,
			IItemHandler buffers,
			GetterAndSetter<Integer> batchMode,
			List<GetterAndSetter<Integer>> colors
	)
	{
		super(ctx);
		this.batchMode = batchMode;
		this.colors = colors;
		for(int i = 0; i < NUM_SLOTS; i++)
			this.addSlot(new IESlot.ItemHandlerGhost(filters, i, 8+i*18, 30));
		for(int i = 0; i < NUM_SLOTS; i++)
			this.addSlot(new SlotItemHandler(buffers, i, 8+i*18, 59));

		this.ownSlotCount = 2*NUM_SLOTS;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 118+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 176));
		addGenericData(new GenericContainerData<>(GenericDataSerializers.INT32, batchMode));
		for(GetterAndSetter<Integer> color : colors)
			addGenericData(new GenericContainerData<>(GenericDataSerializers.INT32, color));
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player player, int slot)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slotObject = this.slots.get(slot);
		if(slotObject!=null&&slotObject.hasItem())
		{
			ItemStack itemstack1 = slotObject.getItem();
			itemstack = itemstack1.copy();
			if(slot < ownSlotCount)
			{
				if(!this.moveItemStackTo(itemstack1, ownSlotCount, this.slots.size(), true))
					return ItemStack.EMPTY;
			}
			// exclude ghost slots from shiftclick
			else if(!this.moveItemStackTo(itemstack1, 9, ownSlotCount, false))
			{
				return ItemStack.EMPTY;
			}

			if(itemstack1.isEmpty())
				slotObject.set(ItemStack.EMPTY);
			else
				slotObject.setChanged();
		}
		return itemstack;
	}

	@Override
	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		super.receiveMessageFromScreen(nbt);
		if(nbt.contains("batchMode", Tag.TAG_BYTE))
			batchMode.set((int)nbt.getByte("batchMode"));
		if(nbt.contains("redstoneColor_slot", Tag.TAG_INT))
		{
			final int slot = nbt.getInt("redstoneColor_slot");
			final int newValue = nbt.getInt("redstoneColor_val");
			colors.get(slot).set(newValue);
		}
	}
}