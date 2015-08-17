package blusunrize.immersiveengineering.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import blusunrize.immersiveengineering.common.items.ItemUpgradeableTool;

public class ContainerModWorkbench extends Container
{
	public int slotCount;
	public InventoryStorageItem toolInv;
	public TileEntityModWorkbench tile;
	public InventoryPlayer inventoryPlayer;

	public ContainerModWorkbench(InventoryPlayer inventoryPlayer, TileEntityModWorkbench tile)
	{
		this.inventoryPlayer = inventoryPlayer;
		this.tile = tile;
		rebindSlots();
	}

	private void bindPlayerInv(InventoryPlayer inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 87+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 145));
	}

	public void rebindSlots()
	{
		this.inventorySlots.clear();
		this.addSlotToContainer(new IESlot.UpgradeableItem(this, tile, 0, 24, 22, 1));
		slotCount=1;

		ItemStack tool = this.getSlot(0).getStack();
		if(tool!=null && tool.getItem() instanceof ItemUpgradeableTool)
		{
			if(tool.getItem() instanceof ItemEngineersBlueprint)
				((ItemEngineersBlueprint)tool.getItem()).updateOutputs(tool);
			
			this.toolInv = new InventoryStorageItem(this, tool);
			Slot[] slots =  ((ItemUpgradeableTool)tool.getItem()).getWorkbenchSlots(this, tool, toolInv);
			if(slots!=null)
				for(Slot s : slots)
				{
					this.addSlotToContainer(s);
					slotCount++;
				}

			ItemStack[] cont = ((ItemUpgradeableTool)tool.getItem()).getContainedItems(tool);
			((InventoryStorageItem)this.toolInv).stackList = cont;
		}

		bindPlayerInv(inventoryPlayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_)
	{
		return true;
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
			else if(stackInSlot!=null)
			{
				if(stackInSlot.getItem() instanceof ItemUpgradeableTool && ((ItemUpgradeableTool)stackInSlot.getItem()).canModify(stackInSlot))
				{
					if(!this.mergeItemStack(stackInSlot, 0, 1, true))
						return null;
				}
				else if(slotCount>1)
				{
					boolean b = true;
					for(int i=1; i<slotCount; i++)
					{
						Slot s = this.getSlotFromInventory(toolInv, i-1);
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
			}

			if (stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();

			if (stackInSlot.stackSize == stack.stackSize)
				return null;
			slotObject.onPickupFromSlot(player, stack);
		}
		return stack;
	}
}