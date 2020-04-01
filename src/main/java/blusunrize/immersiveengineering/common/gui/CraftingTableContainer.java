/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.CraftingTableTileEntity;
import invtweaks.api.container.ChestContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Consumer;

@ChestContainer
public class CraftingTableContainer extends IEBaseContainer<CraftingTableTileEntity>
{
	private final CraftingInventory craftingInventory = new CraftingInventory(this, 3, 3);
	private final CraftResultInventory craftResultInventory = new CraftResultInventory();
	private final PlayerEntity player;

	public CraftingTableContainer(int id, PlayerInventory inventoryPlayer, CraftingTableTileEntity tile)
	{
		super(inventoryPlayer, tile, id);
		this.player = inventoryPlayer.player;

		this.addSlot(new CraftingResultSlot(player, craftingInventory, craftResultInventory, 0, 124, 35));

		for(int i = 0; i < 9; i++)
		{
			Slot s = this.addSlot(new Slot(craftingInventory, i, 30+(i%3)*18, 17+(i/3)*18));
			s.putStack(this.inv.getStackInSlot(18+i));
		}

		int iSlot = 0;
		for(int i = 0; i < 18; i++)
			this.addSlot(new Slot(this.inv, iSlot++, 8+(i%9)*18, 79+(i/9)*18));

		this.slotCount = tile.getInventory().size();
		this.tile = tile;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 129+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 187));
	}

	private void doIfServerWorld(Consumer<World> consumer)
	{
		if(this.tile!=null&&this.tile.getWorld()!=null&&!this.tile.getWorld().isRemote)
		{
			consumer.accept(this.tile.getWorld());
		}
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		super.onContainerClosed(playerIn);
		doIfServerWorld(world -> {
			for(int i = 0; i < 9; i++)
				this.inv.setInventorySlotContents(18+i, this.getSlot(1+i).getStack());
		});
	}

	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn)
	{
		doIfServerWorld(world -> {
			ServerPlayerEntity serverplayerentity = (ServerPlayerEntity)this.player;
			ItemStack itemstack = ItemStack.EMPTY;
			Optional<ICraftingRecipe> optional = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftingInventory, world);
			if(optional.isPresent())
			{
				ICraftingRecipe icraftingrecipe = optional.get();
				if(craftResultInventory.canUseRecipe(world, serverplayerentity, icraftingrecipe))
				{
					itemstack = icraftingrecipe.getCraftingResult(craftingInventory);
				}
			}

			craftResultInventory.setInventorySlotContents(0, itemstack);
			serverplayerentity.connection.sendPacket(new SSetSlotPacket(this.windowId, 0, itemstack));
		});
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
	{
		if(index!=0)
			return super.transferStackInSlot(playerIn, index);

		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if(slot!=null&&slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			doIfServerWorld(world -> {
				itemstack1.getItem().onCreated(itemstack1, world, playerIn);
			});
			if(!this.mergeItemStack(itemstack1, 10, 46, true))
				return ItemStack.EMPTY;
			slot.onSlotChange(itemstack1, itemstack);

			if(itemstack1.isEmpty())
				slot.putStack(ItemStack.EMPTY);
			else
				slot.onSlotChanged();

			if(itemstack1.getCount()==itemstack.getCount())
				return ItemStack.EMPTY;

			ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);
			playerIn.dropItem(itemstack2, false);
		}
		return itemstack;
	}
}