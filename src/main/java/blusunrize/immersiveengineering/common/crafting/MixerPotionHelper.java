/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashSet;
import java.util.Set;

/**
 * @author BluSunrize - 22.02.2017
 */
public class MixerPotionHelper
{
	public static final Set<String> BLACKLIST = new HashSet<>();

	public static void registerPotionRecipe(PotionType output, PotionType input, IngredientStack reagent)
	{
		if(!BLACKLIST.contains(PotionType.REGISTRY.getNameForObject(output).toString()))
		{
			MixerRecipe recipe = new MixerRecipe(
					getFluidStackForType(output, 1000),
					getFluidStackForType(input, 1000),
					new IngredientStack[]{reagent},
					6400);
			MixerRecipe.recipeList.add(recipe);

			BottlingMachineRecipe.addRecipe(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), output),
					new ItemStack(Items.GLASS_BOTTLE), getFluidStackForType(output, 250));
		}
	}

	public static FluidStack getFluidStackForType(PotionType type, int amount)
	{
		if(type==PotionTypes.WATER||type==null)
			return new FluidStack(FluidRegistry.WATER, amount);
		FluidStack stack = new FluidStack(IEContent.fluidPotion, amount);
		stack.tag = new NBTTagCompound();
		stack.tag.setString("Potion", PotionType.REGISTRY.getNameForObject(type).toString());
		return stack;
	}
}
