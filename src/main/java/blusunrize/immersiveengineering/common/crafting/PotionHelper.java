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
import blusunrize.immersiveengineering.mixin.accessors.PotionBrewingAccess;
import blusunrize.immersiveengineering.mixin.accessors.PotionMixAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing.Mix;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.BrewingRecipeRegistry;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;

public class PotionHelper
{
	public static FluidTagInput getFluidTagForType(Potion type, int amount)
	{
		if(type==Potions.WATER||type==null)
			return new FluidTagInput(FluidTags.WATER, amount);
		else
		{
			CompoundTag nbt = new CompoundTag();
			nbt.putString("Potion", BuiltInRegistries.POTION.getKey(type).toString());
			return new FluidTagInput(IETags.fluidPotion, amount, nbt);
		}
	}

	public static void applyToAllPotionRecipes(PotionRecipeProcessor out)
	{
		// Vanilla
		for(Mix<Potion> mixPredicate : PotionBrewingAccess.getConversions())
			out.apply(
					mixPredicate.to, mixPredicate.from,
					new IngredientWithSize(((PotionMixAccessor)mixPredicate).getIngredient())
			);

		// Modded
		for(IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes())
			if(recipe instanceof BrewingRecipe)
			{
				IngredientWithSize ingredient = new IngredientWithSize(((BrewingRecipe)recipe).getIngredient());
				Ingredient input = ((BrewingRecipe)recipe).getInput();
				ItemStack output = ((BrewingRecipe)recipe).getOutput();
				if(output.getItem()==Items.POTION&&input.getItems().length > 0)
					out.apply(PotionUtils.getPotion(output),
							PotionUtils.getPotion(input.getItems()[0]), ingredient);
			}
	}

	public interface PotionRecipeProcessor
	{
		void apply(Potion output, Potion input, IngredientWithSize reagent);
	}
}
