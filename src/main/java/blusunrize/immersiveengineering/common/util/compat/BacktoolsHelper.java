package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.common.IEContent;
import cpw.mods.fml.common.event.FMLInterModComms;

public class BacktoolsHelper extends IECompatModule
{
	@Override
	public void init()
	{
		FMLInterModComms.sendMessage("BackTools", "blacklist", new ItemStack(IEContent.itemDrill,1,OreDictionary.WILDCARD_VALUE));
		FMLInterModComms.sendMessage("BackTools", "blacklist", new ItemStack(IEContent.itemRevolver,1,OreDictionary.WILDCARD_VALUE));
	}

	@Override
	public void postInit()
	{
	}
}