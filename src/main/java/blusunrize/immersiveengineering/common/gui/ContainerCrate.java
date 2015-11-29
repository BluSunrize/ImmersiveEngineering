package blusunrize.immersiveengineering.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;

public class ContainerCrate extends Container
{
	int slotCount;
	TileEntityWoodenCrate tile;
	public ContainerCrate(InventoryPlayer inventoryPlayer, TileEntityWoodenCrate tile)
	{
		for(int i=0; i<tile.getSizeInventory(); i++)
			this.addSlotToContainer(new Slot(tile, i, 8+(i%9)*18, 18+(i/9)*18){
				@Override
				public boolean isItemValid(ItemStack stack)
				{
					return !OreDictionary.itemMatches(new ItemStack(IEContent.blockWoodenDevice,1,4), stack, true);
				}
			});
		this.slotCount=tile.getSizeInventory();
		this.tile = tile;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 87+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 145));
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_)
	{
		return tile.isUseableByPlayer(p_75145_1_);
	}


	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);

		if (slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if (slot < slotCount)
			{
				if(!this.mergeItemStack(stackInSlot, slotCount, (slotCount + 36), true))
					return null;
			}
			else
			{
				boolean b = true;
				for(int i=0; i<slotCount; i++)
				{
					Slot s = (Slot)inventorySlots.get(i);
					if(s!=null && s.isItemValid(stackInSlot))
						if(this.mergeItemStack(stackInSlot, i, i+1, true))
						{
							b = false;
							break;
						}
						else
							continue;
				}
				if(b)
					return null;
			}

			if (stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();

			if (stackInSlot.stackSize == stack.stackSize)
				return null;
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		return stack;
	}
}