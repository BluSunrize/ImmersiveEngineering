package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.IERecipes;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class AppliedEnergisticsHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}
	@Override
	public void init()
	{
	}
	@Override
	public void postInit()
	{
		IERecipes.addItemToOreDictCrusherRecipe("dustEnderPearl", 1, Items.ender_pearl, 1600);
		Item ae2Material = GameRegistry.findItem("appliedenergistics2", "item.ItemMultiMaterial");
		if(ae2Material!=null)
		{
			IERecipes.addCrusherRecipe(new ItemStack(ae2Material,1,2), "crystalCertusQuartz", 1600);
			IERecipes.addCrusherRecipe(new ItemStack(ae2Material,1,2), new ItemStack(ae2Material,1,1), 1600);
			IERecipes.addCrusherRecipe(new ItemStack(ae2Material,1,4), "cropWheat", 1600);
			IERecipes.addCrusherRecipe(new ItemStack(ae2Material,1,8), "crystalFluix", 1600);
			Block ae2Skystone = GameRegistry.findBlock("appliedenergistics2", "tile.BlockSkyStone");
			if(ae2Skystone!=null)
				IERecipes.addCrusherRecipe(new ItemStack(ae2Material,1,45), new ItemStack(ae2Skystone,1,0), 1600);
		}
	}
}