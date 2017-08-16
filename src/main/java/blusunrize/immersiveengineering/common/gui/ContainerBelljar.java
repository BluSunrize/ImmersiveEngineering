package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class ContainerBelljar extends ContainerIEBase
{
	public ContainerBelljar(InventoryPlayer inventoryPlayer, TileEntityBelljar tile)
	{
		super(inventoryPlayer, tile);
		this.addSlotToContainer(new IESlot.Belljar(0, this, this.inv, 0, 62, 54));
		this.addSlotToContainer(new IESlot.Belljar(1, this, this.inv, 1, 62, 34));
		this.addSlotToContainer(new IESlot.Belljar(2, this, this.inv, 2, 8, 59));

		for(int i=0; i<4; i++)
			this.addSlotToContainer(new IESlot.Output(this, this.inv, 3+i, 116+i%2*18, 34+i/2*18));

		this.slotCount=7;
		this.tile = tile;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}