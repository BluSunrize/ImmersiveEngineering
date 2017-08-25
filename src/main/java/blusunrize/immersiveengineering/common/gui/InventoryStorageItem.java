package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.tool.IInternalStorageItem;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class InventoryStorageItem implements IInventory
{
	private ItemStack itemStack = ItemStack.EMPTY;
	private Container container;
	public NonNullList<ItemStack> stackList;
	private String name;

	public InventoryStorageItem(Container par1Container, ItemStack stack)
	{
		this.container = par1Container;
		if(!stack.isEmpty() && stack.getItem() instanceof IInternalStorageItem)
		{
			this.itemStack=stack;
			syncItemToList();
			this.name = stack.getDisplayName();
		}
	}

	public void syncItemToList()
	{
		stackList = ((IInternalStorageItem)itemStack.getItem()).getContainedItems(itemStack);
	}

	@Override
	public int getSizeInventory()
	{
		return this.stackList.size();
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < stackList.size(); ++i) {
			if (!stackList.get(i).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		if(i >= this.getSizeInventory())return ItemStack.EMPTY;
		return this.stackList.get(i);
	}	

	@Override
	public ItemStack removeStackFromSlot(int i)
	{
		if (!this.stackList.get(i).isEmpty())
		{
			ItemStack itemstack = this.stackList.get(i);
			this.stackList.set(i, ItemStack.EMPTY);
			return itemstack;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{

		if (!this.stackList.get(i).isEmpty())
		{
			ItemStack itemstack;

			if (this.stackList.get(i).getCount() <= j)
			{
				itemstack = this.stackList.get(i);
				this.stackList.set(i, ItemStack.EMPTY);
				this.markDirty();
				this.container.onCraftMatrixChanged(this);
				return itemstack;
			}
			itemstack = this.stackList.get(i).splitStack(j);

			if (this.stackList.get(i).getCount() == 0)
			{
				this.stackList.set(i, ItemStack.EMPTY);
			}

			this.container.onCraftMatrixChanged(this);
			return itemstack;
		}
		return ItemStack.EMPTY;
	}


	@Override
	public void setInventorySlotContents(int i, ItemStack stack)
	{
		this.stackList.set(i, stack);

		if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit())
		{
			stack.setCount(this.getInventoryStackLimit());
		}

		this.container.onCraftMatrixChanged(this);
	}

	@Override
	public String getName()
	{
		return "container."+name;
	}
	@Override
	public boolean hasCustomName()
	{
		return false;
	}
	@Override
	public ITextComponent getDisplayName()
	{
		return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty()
	{
		if(!itemStack.isEmpty())
		{
			((IInternalStorageItem)this.itemStack.getItem()).setContainedItems(itemStack, stackList);
			if(this.itemStack.getItem() instanceof IUpgradeableTool)
				((IUpgradeableTool)this.itemStack.getItem()).recalculateUpgrades(itemStack);
		}
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}


	@Override
	public int getField(int id)
	{
		return 0;
	}
	@Override
	public void setField(int id, int value)
	{
	}
	@Override
	public int getFieldCount()
	{
		return 0;
	}
	@Override
	public void clear()
	{
	}
}