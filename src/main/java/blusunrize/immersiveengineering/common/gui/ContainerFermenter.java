package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class ContainerFermenter extends ContainerIEBase<TileEntityFermenter>
{
	public ContainerFermenter(InventoryPlayer inventoryPlayer, TileEntityFermenter tile)
	{
		super(inventoryPlayer, tile);

		for(int i=0; i<8; i++)
			this.addSlotToContainer(new Slot(this.inv, i, 8+(i%4)*18,19+(i/4)*18));
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 8, 91,53));
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 9, 134,17, true));
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 10, 134,53));
		slotCount=11;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}