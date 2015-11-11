package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import cpw.mods.fml.common.registry.GameRegistry;

public class ExtraUtilsHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		Item unstable = GameRegistry.findItem("ExtraUtilities", "unstableingot");
		if(unstable!=null)
			ArcFurnaceRecipe.makeItemInvalidRecyclingOutput(new ItemStack(unstable,1,OreDictionary.WILDCARD_VALUE));
	}

	@Override
	public void postInit()
	{

	}
}