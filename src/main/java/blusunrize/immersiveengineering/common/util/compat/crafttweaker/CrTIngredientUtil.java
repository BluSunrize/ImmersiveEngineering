/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import com.blamejared.crafttweaker.api.item.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCWeightedItemStack;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.MCTagWithAmount;
import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

public class CrTIngredientUtil
{

	private CrTIngredientUtil()
	{
	}

	/**
	 * So far, only IItemStack supports setting an amount.
	 * So we can at least check for that, I guess?
	 */
	public static IngredientWithSize getIngredientWithSize(IIngredientWithAmount crafttweakerIngredient)
	{
		final Ingredient basePredicate = crafttweakerIngredient.getIngredient().asVanillaIngredient();
		return new IngredientWithSize(basePredicate, crafttweakerIngredient.getAmount());
	}

	public static IngredientWithSize[] getIngredientsWithSize(IIngredientWithAmount[] crafttweakerIngredients)
	{
		final IngredientWithSize[] result = new IngredientWithSize[crafttweakerIngredients.length];
		for(int i = 0; i < crafttweakerIngredients.length; i++)
			result[i] = new IngredientWithSize(crafttweakerIngredients[i].getIngredient().asVanillaIngredient(), crafttweakerIngredients[i].getAmount());
		return result;
	}

	/**
	 * {@link com.blamejared.crafttweaker.impl.helper.CraftTweakerHelper} only allows to get a List, not a NonNullList
	 */
	public static NonNullList<ItemStack> getNonNullList(IItemStack[] itemStacks)
	{
		final NonNullList<ItemStack> result = NonNullList.create();
		for(IItemStack itemStack : itemStacks)
			result.add(itemStack.getInternal());
		return result;
	}

	/**
	 * Creates a StackWithChance, and clamps the chance to [0..1]
	 */
	public static StackWithChance getStackWithChance(MCWeightedItemStack weightedStack)
	{
		final ItemStack stack = weightedStack.getItemStack().getInternal();
		final float weight = Mth.clamp((float)weightedStack.getWeight(), 0, 1);
		return new StackWithChance(stack, weight);
	}

	/**
	 * Allows us to be typesafe, since tag.getInternal() has unknown type
	 */
	public static FluidTagInput getFluidTagInput(MCTag<Fluid> tag, int amount)
	{
		//noinspection unchecked
		final Tag<Fluid> internal = (Tag<Fluid>)tag.getInternal();
		Preconditions.checkNotNull(internal, "Invalid fluid tag used for recipe: "+tag.toString());
		return new FluidTagInput(internal, amount, null);
	}

	/**
	 * Allows us to be typesafe, since tag.getInternal() has unknown type
	 */
	public static FluidTagInput getFluidTagInput(MCTagWithAmount<Fluid> tag)
	{
		//noinspection unchecked
		final Tag<Fluid> internal = (Tag<Fluid>)tag.getTag().getInternal();
		Preconditions.checkNotNull(internal, "Invalid fluid tag used for recipe: "+tag.toString());
		return new FluidTagInput(internal, tag.getAmount(), null);
	}
}
