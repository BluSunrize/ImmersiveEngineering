package blusunrize.immersiveengineering.common.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ContainerSpeedloader extends ContainerInternalStorageItem
{
	public ContainerSpeedloader(InventoryPlayer iinventory, World world, EntityEquipmentSlot slot, ItemStack revolver)
	{
		super(iinventory, world, slot, revolver);
	}

	@Override
	int addSlots(InventoryPlayer iinventory)
	{
		int i = 0;
		this.addSlotToContainer(new IESlot.Bullet(this.inv, i++, 80, 3, 1));
		this.addSlotToContainer(new IESlot.Bullet(this.inv, i++, 99, 11, 1));
		this.addSlotToContainer(new IESlot.Bullet(this.inv, i++, 107, 30, 1));
		this.addSlotToContainer(new IESlot.Bullet(this.inv, i++, 99, 49, 1));
		this.addSlotToContainer(new IESlot.Bullet(this.inv, i++, 80, 57, 1));
		this.addSlotToContainer(new IESlot.Bullet(this.inv, i++, 61, 49, 1));
		this.addSlotToContainer(new IESlot.Bullet(this.inv, i++, 53, 30, 1));
		this.addSlotToContainer(new IESlot.Bullet(this.inv, i++, 61, 11, 1));
		this.bindPlayerInventory(iinventory);
		return i;
	}
	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				this.addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));

		for (int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}