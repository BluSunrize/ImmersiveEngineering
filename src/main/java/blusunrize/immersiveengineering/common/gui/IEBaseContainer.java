/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GenericDataSerializers.DataPair;
import blusunrize.immersiveengineering.common.network.MessageContainerData;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class IEBaseContainer<T extends BlockEntity> extends AbstractContainerMenu
{
	public T tile;
	@Nullable
	public Container inv;
	public int slotCount;
	private final List<GenericContainerData<?>> genericData = new ArrayList<>();
	private final List<ServerPlayer> usingPlayers = new ArrayList<>();

	public IEBaseContainer(T tile, int id)
	{
		super(GuiHandler.getContainerTypeFor(tile), id);
		this.tile = tile;
		if(tile instanceof IIEInventory)
			this.inv = new TileInventory(tile, this);
	}

	@Override
	public boolean stillValid(@Nonnull Player player)
	{
		return inv!=null&&inv.stillValid(player);//Override for TE's that don't implement IIEInventory
	}

	public void addGenericData(GenericContainerData<?> newData)
	{
		genericData.add(newData);
	}

	@Override
	public void broadcastChanges()
	{
		super.broadcastChanges();
		List<Pair<Integer, DataPair<?>>> toSync = new ArrayList<>();
		for(int i = 0; i < genericData.size(); i++)
		{
			GenericContainerData<?> data = genericData.get(i);
			if(data.needsUpdate())
				toSync.add(Pair.of(i, data.dataPair()));
		}
		if(!toSync.isEmpty())
			for(ServerPlayer player : usingPlayers)
				ImmersiveEngineering.packetHandler.sendTo(
						new MessageContainerData(toSync), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT
				);
	}

	public void receiveSync(List<Pair<Integer, DataPair<?>>> synced)
	{
		for(Pair<Integer, DataPair<?>> syncElement : synced)
			genericData.get(syncElement.getFirst()).processSync(syncElement.getSecond().data());
	}

	@Override
	public ItemStack clicked(int id, int dragType, ClickType clickType, Player player)
	{
		Slot slot = id < 0?null: this.slots.get(id);
		if(!(slot instanceof IESlot.ItemHandlerGhost))
			return super.clicked(id, dragType, clickType, player);
		//Spooky Ghost Slots!!!!
		ItemStack stack = ItemStack.EMPTY;
		ItemStack stackSlot = slot.getItem();
		if(!stackSlot.isEmpty())
			stack = stackSlot.copy();

		if(dragType==2)
			slot.set(ItemStack.EMPTY);
		else if(dragType==0||dragType==1)
		{
			Inventory playerInv = player.inventory;
			ItemStack stackHeld = playerInv.getCarried();
			int amount = Math.min(slot.getMaxStackSize(), stackHeld.getCount());
			if(dragType==1)
				amount = 1;
			if(stackSlot.isEmpty())
			{
				if(!stackHeld.isEmpty()&&slot.mayPlace(stackHeld))
					slot.set(ItemHandlerHelper.copyStackWithSize(stackHeld, amount));
			}
			else if(stackHeld.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else if(slot.mayPlace(stackHeld))
			{
				if(ItemStack.isSame(stackSlot, stackHeld))
					stackSlot.grow(amount);
				else
					slot.set(ItemHandlerHelper.copyStackWithSize(stackHeld, amount));
			}
			if(stackSlot.getCount() > slot.getMaxStackSize())
				stackSlot.setCount(slot.getMaxStackSize());
		}
		else if(dragType==5)
		{
			Inventory playerInv = player.inventory;
			ItemStack stackHeld = playerInv.getCarried();
			int amount = Math.min(slot.getMaxStackSize(), stackHeld.getCount());
			if(!slot.hasItem())
			{
				slot.set(ItemHandlerHelper.copyStackWithSize(stackHeld, amount));
			}
		}
		return stack;
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
			if(slot < slotCount)
			{
				if(!this.moveItemStackTo(itemstack1, slotCount, this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.moveItemStackToWithMayPlace(itemstack1, 0, slotCount))
			{
				return ItemStack.EMPTY;
			}

			if(itemstack1.isEmpty())
			{
				slotObject.set(ItemStack.EMPTY);
			}
			else
			{
				slotObject.setChanged();
			}
		}

		return itemstack;
	}

	protected boolean moveItemStackToWithMayPlace(ItemStack pStack, int pStartIndex, int pEndIndex)
	{
		boolean inAllowedRange = true;
		int allowedStart = pStartIndex;
		for(int i = pStartIndex; i < pEndIndex; i++)
		{
			boolean mayplace = this.slots.get(i).mayPlace(pStack);
			if(inAllowedRange&&(!mayplace||i==pEndIndex-1))
			{
				if(moveItemStackTo(pStack, allowedStart, i, false))
					return true;
				inAllowedRange = false;
			}
			else if(!inAllowedRange&&mayplace)
			{
				allowedStart = i;
				inAllowedRange = true;
			}
		}
		return false;
	}

	@Override
	protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection)
	{
		return super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
	}

	@Override
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
		if(inv!=null)
			this.inv.stopOpen(playerIn);
	}

	public void receiveMessageFromScreen(CompoundTag nbt)
	{
	}

	@Override
	public void addSlotListener(ContainerListener listener)
	{
		super.addSlotListener(listener);
		if(!(listener instanceof ServerPlayer))
			return;
		final ServerPlayer serverPlayer = (ServerPlayer)listener;
		usingPlayers.add(serverPlayer);
		List<Pair<Integer, DataPair<?>>> list = new ArrayList<>();
		for(int i = 0; i < genericData.size(); i++)
			list.add(Pair.of(i, genericData.get(i).dataPair()));
		ImmersiveEngineering.packetHandler.sendTo(
				new MessageContainerData(list), serverPlayer.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT
		);
	}

	@Override
	public void removeSlotListener(ContainerListener listener)
	{
		super.removeSlotListener(listener);
		if(listener instanceof ServerPlayer)
			usingPlayers.remove((ServerPlayer)listener);
	}
}