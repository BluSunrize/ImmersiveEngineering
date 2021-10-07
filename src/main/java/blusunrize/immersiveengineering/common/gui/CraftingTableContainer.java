/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.CraftingTableBlockEntity;
import invtweaks.api.container.ChestContainer;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Consumer;

@ChestContainer
public class CraftingTableContainer extends IEBaseContainer<CraftingTableBlockEntity>
{
	private final CraftingContainer craftingInventory = new CraftingContainer(this, 3, 3);
	private final ResultContainer craftResultInventory = new ResultContainer();
	private final Player player;

	public CraftingTableContainer(MenuType<?> type, int id, Inventory inventoryPlayer, CraftingTableBlockEntity tile)
	{
		super(type, inventoryPlayer, tile, id);
		this.player = inventoryPlayer.player;

		this.addSlot(new ResultSlot(player, craftingInventory, craftResultInventory, 0, 124, 35));

		for(int i = 0; i < 9; i++)
		{
			Slot s = this.addSlot(new Slot(craftingInventory, i, 30+(i%3)*18, 17+(i/3)*18));
			s.set(this.inv.getItem(18+i));
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

	private void doIfServerWorld(Consumer<Level> consumer)
	{
		if(this.tile!=null&&this.tile.getLevel()!=null&&!this.tile.getLevel().isClientSide)
		{
			consumer.accept(this.tile.getLevel());
		}
	}

	@Override
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
		doIfServerWorld(world -> {
			for(int i = 0; i < 9; i++)
				this.inv.setItem(18+i, this.getSlot(1+i).getItem());
		});
	}

	@Override
	public void slotsChanged(Container inventoryIn)
	{
		doIfServerWorld(world -> {
			ServerPlayer serverplayerentity = (ServerPlayer)this.player;
			ItemStack itemstack = ItemStack.EMPTY;
			Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInventory, world);
			if(optional.isPresent())
			{
				CraftingRecipe icraftingrecipe = optional.get();
				if(craftResultInventory.setRecipeUsed(world, serverplayerentity, icraftingrecipe))
				{
					itemstack = icraftingrecipe.assemble(craftingInventory);
				}
			}

			craftResultInventory.setItem(0, itemstack);
			serverplayerentity.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, incrementStateId(), 0, itemstack));
		});
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player playerIn, int index)
	{
		if(index!=0)
			return super.quickMoveStack(playerIn, index);

		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if(slot!=null&&slot.hasItem())
		{
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			doIfServerWorld(world -> {
				itemstack1.getItem().onCraftedBy(itemstack1, world, playerIn);
			});
			if(!this.moveItemStackTo(itemstack1, 10, 46, true))
				return ItemStack.EMPTY;
			slot.onQuickCraft(itemstack1, itemstack);

			if(itemstack1.isEmpty())
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();

			if(itemstack1.getCount()==itemstack.getCount())
				return ItemStack.EMPTY;

			slot.onTake(playerIn, itemstack1);
			//TODO
			//playerIn.drop(itemstack2, false);
		}
		return itemstack;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot)
	{
		return pSlot.container!=this.craftResultInventory&&super.canTakeItemForPickAll(pStack, pSlot);
	}
}