package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.common.IEContent;
import cpw.mods.fml.common.event.FMLInterModComms;

public class FMPHelper extends IECompatModule
{
	@Override
	public void init()
	{
		for(int i=0; i<=2; i++)
			FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(IEContent.blockTreatedWood,1,i));
		FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(IEContent.blockWoodenDecoration,1,5));
		FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(IEContent.blockWoodenDevice,1,4));
		for(int i=0; i<=10; i++)
			FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(IEContent.blockStorage,1,i));
		for(int i=0; i<=4; i++)
			FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(IEContent.blockStoneDecoration,1,i));
		FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(IEContent.blockStoneDevice,1,4));
		FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(IEContent.blockMetalDecoration,1,1));
		for(int i=4; i<=7; i++)
			FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(IEContent.blockMetalDecoration,1,i));
	}

	@Override
	public void postInit()
	{
	}
}