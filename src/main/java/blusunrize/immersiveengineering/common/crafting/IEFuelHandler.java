package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.IFuelHandler;

public class IEFuelHandler implements IFuelHandler
{
	@Override
	public int getBurnTime(ItemStack fuel)
	{
		if(OreDictionary.itemMatches(new ItemStack(IEContent.itemMaterial,1,6), fuel, true))
			return 3200;
		if(OreDictionary.itemMatches(new ItemStack(IEContent.blockStoneDecoration,1,3), fuel, true))
			return 3200*10;
		return 0;
	}

}