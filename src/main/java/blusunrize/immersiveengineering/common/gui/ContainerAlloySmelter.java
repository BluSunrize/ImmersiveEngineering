package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.stone.TileEntityAlloySmelter;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceFuel;

public class ContainerAlloySmelter extends ContainerIEBase<TileEntityAlloySmelter>
{
	public ContainerAlloySmelter(InventoryPlayer inventoryPlayer, TileEntityAlloySmelter tile)
	{
		super(inventoryPlayer, tile);

		this.addSlotToContainer(new Slot(this.inv, 0, 38, 17));
		this.addSlotToContainer(new Slot(this.inv, 1, 66, 17));
		this.addSlotToContainer(new SlotFurnaceFuel(this.inv, 2, 52, 53));
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 3,120, 35));
		slotCount=4;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 142));
	}
}