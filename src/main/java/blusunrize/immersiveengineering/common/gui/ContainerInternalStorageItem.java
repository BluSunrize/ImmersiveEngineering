package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.tool.IInternalStorageItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class ContainerInternalStorageItem extends Container
{
	protected World worldObj;
	protected int blockedSlot;
	public IInventory input;
	protected EntityEquipmentSlot equipmentSlot = null;
	protected ItemStack heldItem = null;
	protected EntityPlayer player = null;
	public final int internalSlots;

	public ContainerInternalStorageItem(InventoryPlayer iinventory, World world, EntityEquipmentSlot entityEquipmentSlot, ItemStack heldItem)
	{
		this.worldObj = world;
		this.player = iinventory.player;
		this.equipmentSlot = entityEquipmentSlot;
		this.heldItem = heldItem;
		this.input = new InventoryStorageItem(this, heldItem);
		this.internalSlots = this.addSlots(iinventory);
		this.blockedSlot = (iinventory.currentItem + 27 + internalSlots);


		if (!world.isRemote)
			try {
				((InventoryStorageItem)this.input).stackList = ((IInternalStorageItem)this.heldItem.getItem()).getContainedItems(this.heldItem);
			}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		this.onCraftMatrixChanged(this.input);
	}

	abstract int addSlots(InventoryPlayer iinventory);

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = inventorySlots.get(slot);

		if(slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if(slot < internalSlots)
			{
				if(!this.mergeItemStack(stackInSlot, internalSlots, (internalSlots + 36), true))
					return null;
			}
			else if(stackInSlot!=null)
			{
				boolean b = true;
				for(int i=0; i<internalSlots; i++)
				{
					Slot s = inventorySlots.get(i);
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
			slotObject.onPickupFromSlot(player, stack);
			updatePlayerItem();
		}
		return stack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public ItemStack slotClick(int par1, int par2, ClickType par3, EntityPlayer par4EntityPlayer)
	{
		if(par1 == this.blockedSlot || (par3== ClickType.SWAP&&par2==par4EntityPlayer.inventory.currentItem))
			return null;
		ItemStack ret = super.slotClick(par1, par2, par3, par4EntityPlayer);
		updatePlayerItem();
		return ret;
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);
		if (!this.worldObj.isRemote)
			updatePlayerItem();
	}

	protected void updatePlayerItem()
	{
		((IInternalStorageItem)this.heldItem.getItem()).setContainedItems(this.heldItem, ((InventoryStorageItem)this.input).stackList);
		ItemStack hand = player.getItemStackFromSlot(this.equipmentSlot);
		if(hand != null && !hand.equals(heldItem))
			player.setItemStackToSlot(this.equipmentSlot, this.heldItem);
		player.inventory.markDirty();
	}
}