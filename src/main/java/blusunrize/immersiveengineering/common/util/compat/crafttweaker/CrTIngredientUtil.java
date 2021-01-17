/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCWeightedItemStack;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

public class CrTIngredientUtil
{

	private CrTIngredientUtil()
	{
	}

	/**
	 * So far, only IItemStack supports setting an amount.
	 * So we can at least check for that, I guess?
	 */
	public static IngredientWithSize getIngredientWithSize(IIngredient crafttweakerIngredient)
	{
		final Ingredient basePredicate = crafttweakerIngredient.asVanillaIngredient();
		if(crafttweakerIngredient instanceof IItemStack)
		{
			return IngredientWithSize.of(((IItemStack)crafttweakerIngredient).getInternal());
		}
		return new IngredientWithSize(basePredicate);
	}

	/**
	 * Same as {@link #getIngredientWithSize(IIngredient)} but for an array
	 */
	public static IngredientWithSize[] getIngredientsWithSize(IIngredient[] crafttweakerIngredients)
	{
		final IngredientWithSize[] result = new IngredientWithSize[crafttweakerIngredients.length];
		for(int i = 0; i < crafttweakerIngredients.length; i++)
		{
			result[i] = getIngredientWithSize(crafttweakerIngredients[i]);
		}
		return result;
	}

	/**
	 * {@link com.blamejared.crafttweaker.impl.helper.CraftTweakerHelper} only allows to get a List, not a NonNullList
	 */
	public static NonNullList<ItemStack> getNonNullList(IItemStack[] itemStacks)
	{
		final NonNullList<ItemStack> result = NonNullList.create();
		for(IItemStack itemStack : itemStacks)
		{
			result.add(itemStack.getInternal());
		}
		return result;
	}

	/**
	 * Creates a StackWithChance, and clamps the chance to [0..1]
	 */
	public static StackWithChance getStackWithChance(MCWeightedItemStack weightedStack)
	{
		final ItemStack stack = weightedStack.getItemStack().getInternal();
		final float weight = MathHelper.clamp((float)weightedStack.getWeight(), 0, 1);
		return new StackWithChance(stack, weight);
	}

	/**
	 * Allows us to be typesafe, since tag.getInternal() has unknown type
	 */
	public static FluidTagInput getFluidTagInput(MCTag<Fluid> tag, int amount)
	{
		//noinspection unchecked
		final ITag<Fluid> internal = (ITag<Fluid>)tag.getInternal();
		return new FluidTagInput(internal, amount, null);
	}
}
