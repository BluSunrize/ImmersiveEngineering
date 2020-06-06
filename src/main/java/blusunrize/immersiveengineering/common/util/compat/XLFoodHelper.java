
/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class XLFoodHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{
	}

	String[] item = {"xlfoodmod:rice", "xlfoodmod:pepper", "xlfoodmod:corn", "xlfoodmod:cucumber",
			 "xlfoodmod:lettuce", "xlfoodmod:onion", "xlfoodmod:tomato", "xlfoodmod:strawberry"};
	@Override
	public void init()
	{
		for (String item:item) {
			Item seed = Item.REGISTRY.getObject(new ResourceLocation(item+"_seeds"));
			if (item.equals("xlfoodmod:onion"))
				seed = Item.REGISTRY.getObject(new ResourceLocation(item));
			Item material = Item.REGISTRY.getObject(new ResourceLocation(item));
			Block plant = Block.REGISTRY.getObject(new ResourceLocation(item+"_plant"));
			if (seed != null && material != null && plant != null)
				BelljarHandler.cropHandler.register(new ItemStack(seed), new ItemStack[]{new ItemStack(material, 1), new ItemStack(seed, 1)}, new ItemStack(Blocks.DIRT), plant.getDefaultState());
			if (material != null)
				BelljarHandler.registerBasicItemFertilizer(new ItemStack(material, 1, 5), 1.25f);
		}
	}

	@Override
	public void postInit()
	{
	}

}
