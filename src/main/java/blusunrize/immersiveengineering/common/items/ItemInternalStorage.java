package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.IInternalStorageItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public abstract class ItemInternalStorage extends ItemIEBase implements IInternalStorageItem 
{
	public ItemInternalStorage(String name, int stackSize, String... subNames)
	{
		super(name, stackSize, subNames);
	}
	
	@Override
	public ItemStack[] getContainedItems(ItemStack stack)
	{
		ItemStack[] stackList = new ItemStack[getInternalSlots(stack)];
		if(stack.hasTagCompound())
		{
			NBTTagList inv = stack.getTagCompound().getTagList("Inv",10);
			for (int i=0; i<inv.tagCount(); i++)
			{
				NBTTagCompound tag = inv.getCompoundTagAt(i);
				int slot = tag.getByte("Slot") & 0xFF;
				if ((slot >= 0) && (slot < stackList.length))
					stackList[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
		return stackList;
	}
	
	@Override
	public void setContainedItems(ItemStack stack, ItemStack[] stackList)
	{
		NBTTagList inv = new NBTTagList();
		for (int i = 0; i < stackList.length; i++)
			if (stackList[i] != null)
			{
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte)i);
				stackList[i].writeToNBT(tag);
				inv.appendTag(tag);
			}
		if(stack.hasTagCompound())
		{
			NBTTagList invExisting = stack.getTagCompound().getTagList("Inv",10);
			for (int i=0; i<invExisting.tagCount(); i++)
			{
				NBTTagCompound tag = invExisting.getCompoundTagAt(i);
				int slot = tag.getByte("Slot") & 0xFF;
				if ((slot >= stackList.length) && (slot < stackList.length))
					inv.appendTag(tag);
			}
		}
		else
			stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setTag("Inv",inv);
	}

	@Override
	public abstract int getInternalSlots(ItemStack stack);
}