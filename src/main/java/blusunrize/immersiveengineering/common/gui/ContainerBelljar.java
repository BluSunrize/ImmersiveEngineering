package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class ContainerBelljar extends ContainerIEBase
{
	public ContainerBelljar(InventoryPlayer inventoryPlayer, TileEntityBelljar tile)
	{
		super(inventoryPlayer, tile);
//		for(int i=0; i<tile.getInventory().length; i++)
//			this.addSlotToContainer(new Slot(this.inv, i, 8+(i%9)*18, 18+(i/9)*18){
//				@Override
//				public boolean isItemValid(ItemStack stack)
//				{
//					if(OreDictionary.itemMatches(new ItemStack(IEContent.blockWoodenDevice0,1,0), stack, true))
//						return false;
//					return !OreDictionary.itemMatches(new ItemStack(IEContent.blockWoodenDevice0, 1, 5), stack, true);
//				}
//			});
		this.addSlotToContainer(new Slot(this.inv, 0, 44, 54){
			@Override
			public int getSlotStackLimit()
			{
				return 1;
			}
		});
		this.addSlotToContainer(new Slot(this.inv, 1, 44, 34){
			@Override
			public int getSlotStackLimit()
			{
				return 1;
			}
		});
		for(int i=0; i<4; i++)
			this.addSlotToContainer(new IESlot.Output(this, this.inv, 2+i, 116+i%2*18, 34+i/2*18));

		this.slotCount=6;
		this.tile = tile;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}