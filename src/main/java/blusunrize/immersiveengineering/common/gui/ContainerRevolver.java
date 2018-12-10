/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ContainerRevolver extends ContainerInternalStorageItem
{
	public static int[][][] slotPositions = {
			{
					{48, 11},
					{56, 30}
			},
			{
					{48, 11},
					{68, 3},
					{78, 22},
					{88, 3},
					{98, 22},
					{108, 3},
					{118, 22},
					{56, 30}
			},
			{
					{48, 3},
					{67, 3},
					{86, 3},
					{105, 3},
					{124, 11},
					{132, 30},
					{124, 49},
					{105, 57},
					{86, 49},
					{86, 30},
					{67, 30},
					{48, 30},
			}
	};

	private EntityEquipmentSlot secondHand;
	public ItemStack secondRevolver;//NonNull after addSlots is called in the super constructor
	private int offset = 0;

	public ContainerRevolver(InventoryPlayer iinventory, World world, EntityEquipmentSlot slot, ItemStack revolver)
	{
		super(iinventory, world, slot, revolver);
	}

	@Override
	int addSlots()
	{
		if(this.equipmentSlot==EntityEquipmentSlot.MAINHAND||this.equipmentSlot==EntityEquipmentSlot.OFFHAND)
		{
			int bullets0 = ((IBulletContainer)(heldItem).getItem()).getBulletCount(heldItem);

			this.secondHand = this.equipmentSlot==EntityEquipmentSlot.MAINHAND?EntityEquipmentSlot.OFFHAND: EntityEquipmentSlot.MAINHAND;
			this.secondRevolver = this.player.getItemStackFromSlot(this.secondHand);
			if(!secondRevolver.isEmpty()&&secondRevolver.getItem() instanceof IBulletContainer)
			{
				int bullets1 = ((IBulletContainer)secondRevolver.getItem()).getBulletCount(secondRevolver);
				this.offset = ((bullets0 >= 18?150: bullets0 > 8?136: 74)+(bullets1 >= 18?150: bullets1 > 8?136: 74)+4-176)/2;
			}
			else
			{
				this.secondRevolver = ItemStack.EMPTY;
				this.secondHand = null;
				this.offset = ((bullets0 >= 18?150: bullets0 > 8?136: 74)-176)/2;
			}
		}

		int total = 0;
		int off = (offset < 0?-offset: 0);
		for(int hand = 0; hand < (this.secondHand!=null?2: 1); hand++)
		{
			int i = 0;
			ItemStack held = this.secondHand==null?heldItem: (hand==0)==(player.getPrimaryHand()==EnumHandSide.RIGHT)?secondRevolver: heldItem;
			IItemHandler secondRevolverInventory = secondRevolver.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			IItemHandler inv = this.secondHand==null?this.inv: (hand==0)==(player.getPrimaryHand()==EnumHandSide.RIGHT)?secondRevolverInventory: this.inv;
			int revolverSlots = ((IBulletContainer)(held).getItem()).getBulletCount(held);

			if(inv instanceof IEItemStackHandler)
				((IEItemStackHandler)inv).setInventoryForUpdate(this.inventoryPlayer);
			this.addSlotToContainer(new IESlot.Bullet(inv, i++, off+29, 3, 1));
			int slots = revolverSlots >= 18?2: revolverSlots > 8?1: 0;
			for(int[] slot : slotPositions[slots])
				this.addSlotToContainer(new IESlot.Bullet(inv, i++, off+slot[0], slot[1], 1));
			this.addSlotToContainer(new IESlot.Bullet(inv, i++, off+48, 49, 1));
			this.addSlotToContainer(new IESlot.Bullet(inv, i++, off+29, 57, 1));
			this.addSlotToContainer(new IESlot.Bullet(inv, i++, off+10, 49, 1));
			this.addSlotToContainer(new IESlot.Bullet(inv, i++, off+2, 30, 1));
			this.addSlotToContainer(new IESlot.Bullet(inv, i++, off+10, 11, 1));
			off += (revolverSlots >= 18?150: revolverSlots > 8?136: 74)+4;
			total += i;
		}

		this.bindPlayerInventory(this.inventoryPlayer);
		return total;
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
	{
		int off = (offset > 0?offset: 0);
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				this.addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, off+8+j*18, 85+i*18));

		for(int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(inventoryPlayer, i, off+8+i*18, 143));
	}


	@Override
	protected boolean allowShiftclicking()
	{
		return false;
	}


	@Override
	protected void updatePlayerItem()
	{
		super.updatePlayerItem();
		/*if(this.secondRevolver!=null)
		{
			((IInternalStorageItem)this.secondRevolver.getItem()).setContainedItems(this.secondRevolver, this.secondRevolverInventory.stackList);
			ItemStack hand = player.getItemStackFromSlot(this.secondHand);
			if(!hand.isEmpty()&&!hand.equals(secondRevolver))
				player.setItemStackToSlot(this.secondHand, this.secondRevolver);
		}*/
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);

		for(int hand = 0; hand < (this.secondHand!=null?2: 1); hand++)
		{
			IItemHandler secondRevolverInventory = secondRevolver.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			IItemHandler inv = this.secondHand==null?this.inv: (hand==0)==(player.getPrimaryHand()==EnumHandSide.RIGHT)?secondRevolverInventory: this.inv;
			if(inv instanceof IEItemStackHandler)
				((IEItemStackHandler)inv).setInventoryForUpdate(null);
		}
	}
}