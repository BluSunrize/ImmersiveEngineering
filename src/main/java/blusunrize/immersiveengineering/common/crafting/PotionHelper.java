/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.mixin.accessors.MixPredicateAccess;
import blusunrize.immersiveengineering.mixin.accessors.PotionBrewingAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing.MixPredicate;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public class PotionHelper
{
	public static FluidTagInput getFluidTagForType(Potion type, int amount)
	{
		if(type==Potions.WATER||type==null)
			return new FluidTagInput(FluidTags.WATER.getName(), amount);
		else
		{
			CompoundNBT nbt = new CompoundNBT();
			nbt.putString("Potion", type.getRegistryName().toString());
			return new FluidTagInput(IETags.fluidPotion.getName(), amount, nbt);
		}
	}

	public static void applyToAllPotionRecipes(PotionRecipeProcessor out)
	{
		// Vanilla
		for(MixPredicate<Potion> mixPredicate : PotionBrewingAccess.getConversions())
			out.apply(
					mixPredicate.output.get(), mixPredicate.input.get(),
					new IngredientWithSize(((MixPredicateAccess)mixPredicate).getReagent())
			);

		// Modded
		for(IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes())
			if(recipe instanceof BrewingRecipe)
			{
				IngredientWithSize ingredient = new IngredientWithSize(((BrewingRecipe)recipe).getIngredient());
				Ingredient input = ((BrewingRecipe)recipe).getInput();
				ItemStack output = ((BrewingRecipe)recipe).getOutput();
				if(output.getItem()==Items.POTION)
					out.apply(PotionUtils.getPotionFromItem(output),
							PotionUtils.getPotionFromItem(input.getMatchingStacks()[0]), ingredient);
			}
	}

	public interface PotionRecipeProcessor
	{
		void apply(Potion output, Potion input, IngredientWithSize reagent);
	}
}
