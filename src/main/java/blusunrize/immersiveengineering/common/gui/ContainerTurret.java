package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTurret;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class ContainerTurret extends ContainerIEBase<TileEntityTurret>
{
	public ContainerTurret(InventoryPlayer inventoryPlayer, TileEntityTurret tile)
	{
		super(inventoryPlayer, tile);
		this.tile=tile;
//		for(int i=0; i<18; i++)
//			this.addSlotToContainer(new Slot(this.inv, i, 13+(i%9)*18, 87+(i/9)*18));
//		slotCount=21;
//
		for(int i=0; i<3; i++)
			for(int j=0; j<9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 109+i*18));
		for(int i=0; i<9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 167));
	}
}