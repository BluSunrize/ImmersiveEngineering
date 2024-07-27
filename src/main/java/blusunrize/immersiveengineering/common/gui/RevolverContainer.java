/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

public class RevolverContainer extends InternalStorageItemContainer
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

	private EquipmentSlot secondHand;
	public ItemStack secondRevolver;//NonNull after addSlots is called in the super constructor
	private int offset = 0;

	public RevolverContainer(
			MenuType<?> type, int id, Inventory iinventory, Level world, EquipmentSlot slot, ItemStack revolver
	)
	{
		super(type, id, iinventory, world, slot, revolver);
	}

	@Override
	int addSlots()
	{
		if(this.equipmentSlot==EquipmentSlot.MAINHAND||this.equipmentSlot==EquipmentSlot.OFFHAND)
		{
			int bullets0 = ((IBulletContainer)(heldItem).getItem()).getBulletCount(heldItem);

			this.secondHand = this.equipmentSlot==EquipmentSlot.MAINHAND?EquipmentSlot.OFFHAND: EquipmentSlot.MAINHAND;
			this.secondRevolver = this.player.getItemBySlot(this.secondHand);
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
			ItemStack held = this.secondHand==null?heldItem: (hand==0)==(player.getMainArm()==HumanoidArm.RIGHT)?secondRevolver: heldItem;
			IItemHandler secondRevolverInventory = secondRevolver.getCapability(ItemHandler.ITEM);
			IItemHandler inv = this.secondHand==null?this.inv: (hand==0)==(player.getMainArm()==HumanoidArm.RIGHT)?secondRevolverInventory: this.inv;
			int revolverSlots = ((IBulletContainer)(held).getItem()).getBulletCount(held);

			this.addSlot(new IESlot.Bullet(inv, i++, off+29, 3, 1));
			int slots = revolverSlots >= 18?2: revolverSlots > 8?1: 0;
			for(int[] slot : slotPositions[slots])
				this.addSlot(new IESlot.Bullet(inv, i++, off+slot[0], slot[1], 1));
			this.addSlot(new IESlot.Bullet(inv, i++, off+48, 49, 1));
			this.addSlot(new IESlot.Bullet(inv, i++, off+29, 57, 1));
			this.addSlot(new IESlot.Bullet(inv, i++, off+10, 49, 1));
			this.addSlot(new IESlot.Bullet(inv, i++, off+2, 30, 1));
			this.addSlot(new IESlot.Bullet(inv, i++, off+10, 11, 1));
			off += (revolverSlots >= 18?150: revolverSlots > 8?136: 74)+4;
			total += i;
		}

		this.bindPlayerInventory(this.inventoryPlayer);
		return total;
	}

	protected void bindPlayerInventory(Inventory inventoryPlayer)
	{
		int off = (offset > 0?offset: 0);
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				this.addSlot(new Slot(inventoryPlayer, j+i*9+9, off+8+j*18, 85+i*18));

		for(int i = 0; i < 9; i++)
			this.addSlot(new Slot(inventoryPlayer, i, off+8+i*18, 143));
	}


	@Override
	protected boolean allowShiftclicking()
	{
		return false;
	}
}