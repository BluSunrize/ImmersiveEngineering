/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public class ActuallyAdditionsHelper extends IECompatModule
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
		Fluid canolaOil = FluidRegistry.getFluid("canolaoil");
		System.out.println("Attempting to register canolaoil to the squeezer: "+canolaOil+", "+OreDictionary.getOres("cropCanola"));
		if(canolaOil!=null)
		{
			SqueezerRecipe.addRecipe(new FluidStack(canolaOil,80), ItemStack.EMPTY, "cropCanola", 6400);
		}

		Item coffeeSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions","item_coffee_seed"));
		Item coffeeBeans = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions","item_coffee_beans"));
		Block coffeeBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions","block_coffee"));
		if(coffeeSeeds!=null && coffeeBeans!=null && coffeeBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(coffeeSeeds), new ItemStack[]{new ItemStack(coffeeBeans,3), new ItemStack(coffeeSeeds)}, new ItemStack(Blocks.DIRT), coffeeBlock.getDefaultState());

		Item riceSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions","item_rice_seed"));
		Item food = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions","item_food"));
		Block riceBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions","block_rice"));
		if(riceSeeds!=null && food!=null && riceBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(riceSeeds), new ItemStack[]{new ItemStack(food,2,16), new ItemStack(riceSeeds)}, new ItemStack(Blocks.DIRT), riceBlock.getDefaultState());

		Item canolaSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions","item_canola_seed"));
		Item misc = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions","item_misc"));
		Block canolaBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions","block_canola"));
		if(canolaSeeds!=null && misc!=null && canolaBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(canolaSeeds), new ItemStack[]{new ItemStack(misc,3,13), new ItemStack(canolaSeeds)}, new ItemStack(Blocks.DIRT), canolaBlock.getDefaultState());

		Item flaxSeeds = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions","item_flax_seed"));
		Block flaxBlock = Block.REGISTRY.getObject(new ResourceLocation("actuallyadditions","block_flax"));
		if(flaxSeeds!=null && flaxBlock!=null)
			BelljarHandler.cropHandler.register(new ItemStack(flaxSeeds), new ItemStack[]{new ItemStack(Items.STRING,4), new ItemStack(flaxSeeds)}, new ItemStack(Blocks.DIRT), flaxBlock.getDefaultState());

		Item fertilizer = Item.REGISTRY.getObject(new ResourceLocation("actuallyadditions","item_fertilizer"));
		if(fertilizer!=null)
			BelljarHandler.registerBasicItemFertilizer(new ItemStack(fertilizer), 1.25f);
	}
}