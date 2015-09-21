package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.event.FMLInterModComms;

public class TEHelper extends IECompatModule
{
	@Override
	public void init()
	{
		//Constantan
		addSmelterRecipe(new ItemStack(IEContent.itemMetal,2,5), new ItemStack(IEContent.itemMetal,1,0),new ItemStack(IEContent.itemMetal,1,4), 2400, null,0);
		addSmelterRecipe(new ItemStack(IEContent.itemMetal,2,5), new ItemStack(IEContent.itemMetal,1,10),new ItemStack(IEContent.itemMetal,1,14), 1600, null,0);
	}

	@Override
	public void postInit()
	{
	}

	public static void addSmelterRecipe(ItemStack output, ItemStack input0, ItemStack input1, int energy, ItemStack secondaryOuput, int secondaryChance)
	{
		if(input0==null || input1==null || output==null)
			return;
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("energy", energy);
		tag.setTag("primaryInput", new NBTTagCompound());
		tag.setTag("secondaryInput", new NBTTagCompound());
		tag.setTag("primaryOutput", new NBTTagCompound());
		if (secondaryOuput != null)
			tag.setTag("secondaryOutput", new NBTTagCompound());
		input0.writeToNBT(tag.getCompoundTag("primaryInput"));
		input1.writeToNBT(tag.getCompoundTag("secondaryInput"));
		output.writeToNBT(tag.getCompoundTag("primaryOutput"));
		if (secondaryOuput != null)
		{
			secondaryOuput.writeToNBT(tag.getCompoundTag("secondaryOutput"));
			tag.setInteger("secondaryChance", secondaryChance);
		}
		FMLInterModComms.sendMessage("ThermalExpansion", "SmelterRecipe", tag);
	}
}