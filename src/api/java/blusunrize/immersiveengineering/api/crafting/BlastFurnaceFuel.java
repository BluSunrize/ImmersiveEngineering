/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BlastFurnaceFuel extends IESerializableRecipe
{
	public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<BlastFurnaceFuel>> SERIALIZER;

	public static final CachedRecipeList<BlastFurnaceFuel> RECIPES = new CachedRecipeList<>(IERecipeTypes.BLAST_FUEL);

	public final Ingredient input;
	public final int burnTime;

	public BlastFurnaceFuel(Ingredient input, int burnTime)
	{
		super(TagOutput.EMPTY, IERecipeTypes.BLAST_FUEL);
		this.input = input;
		this.burnTime = burnTime;
	}

	public static int getBlastFuelTime(Level level, ItemStack stack)
	{
		for(RecipeHolder<BlastFurnaceFuel> e : RECIPES.getRecipes(level))
			if(e.value().input.test(stack))
				return e.value().burnTime;
		return 0;
	}

	public static boolean isValidBlastFuel(Level level, ItemStack stack)
	{
		return getBlastFuelTime(level, stack) > 0;
	}

	@Override
	protected IERecipeSerializer<BlastFurnaceFuel> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getResultItem(Provider access)
	{
		return ItemStack.EMPTY;
	}
}
