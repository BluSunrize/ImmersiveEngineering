package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.IInternalStorageItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

public abstract class ItemInternalStorage extends ItemIEBase implements IInternalStorageItem 
{
	public ItemInternalStorage(String name, int stackSize, String... subNames)
	{
		super(name, stackSize, subNames);
	}
	
	@Override
	public NonNullList<ItemStack> getContainedItems(ItemStack stack)
	{
		NonNullList<ItemStack> stackList = NonNullList.withSize(getInternalSlots(stack), ItemStack.EMPTY);
		if(stack.hasTagCompound())
		{
			NBTTagList inv = stack.getTagCompound().getTagList("Inv",10);
			for (int i=0; i<inv.tagCount(); i++)
			{
				NBTTagCompound tag = inv.getCompoundTagAt(i);
				int slot = tag.getByte("Slot") & 0xFF;
				if ((slot >= 0) && (slot < stackList.size()))
					stackList.set(slot, new ItemStack(tag));
			}
		}
		return stackList;
	}
	
	@Override
	public void setContainedItems(ItemStack stack, NonNullList<ItemStack> stackList)
	{
		NBTTagList inv = new NBTTagList();
		for (int i = 0; i < stackList.size(); i++)
			if (!stackList.get(i).isEmpty())
			{
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte)i);
				stackList.get(i).writeToNBT(tag);
				inv.appendTag(tag);
			}
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setTag("Inv",inv);
	}

	@Override
	public abstract int getInternalSlots(ItemStack stack);
}