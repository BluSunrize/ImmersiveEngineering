/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.common.crafting.MixerRecipePotion;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

public class InspirationsHelper extends IECompatModule
{
	@ObjectHolder("inspirations:materials")
	public static final Item ITEM_MATERIAL = null;

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
		if(ITEM_MATERIAL!=null)
		{
			CreativeTabs[] tabs = ITEM_MATERIAL.getCreativeTabs();
			//Check if Potion bottles are enabled by checking if they have a creative tab
			if(tabs.length > 2&&tabs[2]==CreativeTabs.BREWING)
			{
				ItemStack splashBottle = new ItemStack(ITEM_MATERIAL, 1, 2);
				ItemStack lingeringBottle = new ItemStack(ITEM_MATERIAL, 1, 3);
				for(PotionType potionType : MixerRecipePotion.REGISTERED.keySet())
				{
					BottlingMachineRecipe.addRecipe(PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), potionType),
							splashBottle, MixerRecipePotion.getFluidStackForType(potionType, 250));
					BottlingMachineRecipe.addRecipe(PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), potionType),
							lingeringBottle, MixerRecipePotion.getFluidStackForType(potionType, 250));
				}
			}
		}
	}
}