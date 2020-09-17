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
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.IRegistryDelegate;

import java.lang.reflect.Field;

public class PotionHelper
{
	public static FluidStack getFluidStackForType(Potion type, int amount)
	{
		if(type==Potions.WATER||type==null)
			return new FluidStack(Fluids.WATER, amount);
		FluidStack stack = new FluidStack(IEContent.fluidPotion, amount);
		stack.getOrCreateTag().putString("Potion", type.getRegistryName().toString());
		return stack;
	}

	public static FluidTagInput getFluidTagForType(Potion type, int amount)
	{
		if(type==Potions.WATER||type==null)
			return new FluidTagInput(FluidTags.WATER.getId(), amount);
		else
		{
			CompoundNBT nbt = new CompoundNBT();
			nbt.putString("Potion", type.getRegistryName().toString());
			return new FluidTagInput(IETags.fluidPotion.getId(), amount, nbt);
		}
	}

	public static void applyToAllPotionRecipes(PotionRecipeProcessor out)
	{
		// Vanilla
		try
		{
			String mixPredicateName = "net.minecraft.potion.PotionBrewing$MixPredicate";
			Class<PotionBrewing> mixPredicateClass = (Class<PotionBrewing>)Class.forName(mixPredicateName);
			Field f_input = ObfuscationReflectionHelper.findField(mixPredicateClass, "field_185198_a");
			Field f_reagent = ObfuscationReflectionHelper.findField(mixPredicateClass, "field_185199_b");
			Field f_output = ObfuscationReflectionHelper.findField(mixPredicateClass, "field_185200_c");
			f_input.setAccessible(true);
			f_reagent.setAccessible(true);
			f_output.setAccessible(true);
			for(Object mixPredicate : PotionBrewing.POTION_TYPE_CONVERSIONS)
			{
				Ingredient reagent = (Ingredient)f_reagent.get(mixPredicate);
				IRegistryDelegate<Potion> input = (IRegistryDelegate<Potion>)f_input.get(mixPredicate);
				IRegistryDelegate<Potion> output = (IRegistryDelegate<Potion>)f_output.get(mixPredicate);
				out.apply(output.get(), input.get(), new IngredientWithSize(reagent));
			}
		} catch(Exception e)
		{
			IELogger.error("Error when trying to figure out vanilla potion recipes", e);
		}

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
