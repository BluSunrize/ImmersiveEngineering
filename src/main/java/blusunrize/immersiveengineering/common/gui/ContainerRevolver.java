package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ContainerRevolver extends ContainerInternalStorageItem
{
	public static int[][][] slotPositions = {
			{
				{ 48, 11},
				{ 56, 30}
			},
			{
				{ 48, 11},
				{ 68,  3},
				{ 78, 22},
				{ 88,  3},
				{ 98, 22},
				{108,  3},
				{118, 22},
				{ 56, 30}
			},
			{
				{ 48,  3},
				{ 67,  3},
				{ 86,  3},
				{105,  3},
				{124, 11},
				{132, 30},
				{124, 49},
				{105, 57},
				{ 86, 49},
				{ 86, 30},
				{ 67, 30},
				{ 48, 30},
			}
	};

	int revolverSlots;

	public ContainerRevolver(InventoryPlayer iinventory, World world, EntityEquipmentSlot slot, ItemStack revolver)
	{
		super(iinventory, world, slot, revolver);
		this.revolverSlots = ((ItemRevolver)revolver.getItem()).getBulletSlotAmount(revolver);
	}

	@Override
	void addSlots(InventoryPlayer iinventory)
	{
		int i = 0;
		int w = revolverSlots >= 18 ? 150 : revolverSlots > 8 ? 136 : 74;
		int off = (176 - w) / 2;

		this.addSlotToContainer(new IESlot.Bullet(this, this.input, i++, off + 29, 3, 1));
		int slots = revolverSlots >= 18 ? 2 : revolverSlots > 8 ? 1 : 0;
		for(int[] slot : slotPositions[slots])
			this.addSlotToContainer(new IESlot.Bullet(this, this.input, i++, off + slot[0], slot[1], 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input, i++, off + 48, 49, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input, i++, off + 29, 57, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input, i++, off + 10, 49, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input, i++, off + 2, 30, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input, i++, off + 10, 11, 1));
		this.bindPlayerInventory(iinventory);
	}
	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				this.addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));

		for (int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}