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

public class BetterWithModsHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{

	}

	@Override
	public void init()
	{
		Item hempSeeds = Item.REGISTRY.getObject(new ResourceLocation("betterwithmods:hemp"));
		Item material = Item.REGISTRY.getObject(new ResourceLocation("betterwithmods:material"));
		Block hempBlock = Block.REGISTRY.getObject(new ResourceLocation("betterwithmods:hemp"));
		if(hempSeeds!=null&&material!=null&&hempBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(hempSeeds), new ItemStack[]{new ItemStack(material, 1, 2), new ItemStack(hempSeeds, 1)}, new ItemStack(Blocks.DIRT), hempBlock.getDefaultState());
		if(material!=null)
			BelljarHandler.registerBasicItemFertilizer(new ItemStack(material, 1, 5), 1.25f);
	}

	@Override
	public void postInit()
	{
	}
}