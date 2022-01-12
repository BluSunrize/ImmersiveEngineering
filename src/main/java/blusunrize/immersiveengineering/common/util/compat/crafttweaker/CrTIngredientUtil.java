/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.tag.MCTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker.api.util.random.Percentaged;
import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

import java.util.Arrays;

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
	public static StackWithChance getStackWithChance(Percentaged<IItemStack> weightedStack)
	{
		final ItemStack stack = weightedStack.getData().getInternal();
		final float weight = Mth.clamp((float)weightedStack.getPercentage(), 0, 1);
		return new StackWithChance(stack, weight);
	}

	public static FluidTagInput getFluidTagInput(MCTag<Fluid> tag, int amount)
	{
		final Tag<Fluid> internal = tag.getInternal();
		Preconditions.checkNotNull(internal, "Invalid fluid tag used for recipe: "+tag);
		return new FluidTagInput(internal, amount, null);
	}

	public static FluidTagInput getFluidTagInput(Many<MCTag<Fluid>> tag)
	{
		return getFluidTagInput(tag.getData(), tag.getAmount());
	}

	public static ItemStack[] getItemStacks(IItemStack[] ctStacks)
	{
		return Arrays.stream(ctStacks)
				.map(IItemStack::getInternal)
				.toArray(ItemStack[]::new);
	}
}
