package blusunrize.immersiveengineering.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;

public class ContainerBlastFurnace extends Container
{
	TileEntityBlastFurnace tile;
	int slotCount;
	public ContainerBlastFurnace(InventoryPlayer inventoryPlayer, TileEntityBlastFurnace tile)
	{
		this.tile=tile;

		this.addSlotToContainer(new IESlot(this, tile, 0, 52, 17)
		{
			@Override
			public boolean isItemValid(ItemStack itemStack)
			{
				return BlastFurnaceRecipe.findRecipe(itemStack)!=null;
			}
		});
		this.addSlotToContainer(new IESlot.BlastFuel(this, tile, 1, 52, 53));
		this.addSlotToContainer(new IESlot.Output(this, tile, 2,112, 35));
		slotCount=3;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 142));
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
				int i = -1;
				if(BlastFurnaceRecipe.findRecipe(stackInSlot)!=null)
					i=0;
				else if(BlastFurnaceRecipe.isValidBlastFuel(stackInSlot))
					i=1;
				if(i!=-1)
					if(!this.mergeItemStack(stackInSlot, i,i+1, false))
						return null;

				//				for(int i=0;i<slotCount;i++)
				//					if(this.getSlot(i).isItemValid(stackInSlot))
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