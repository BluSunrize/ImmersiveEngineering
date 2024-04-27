/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the coke oven
 */
public class CokeOvenRecipe extends IESerializableRecipe
{
	public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<CokeOvenRecipe>> SERIALIZER;
	public static final CachedRecipeList<CokeOvenRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.COKE_OVEN);

	public final IngredientWithSize input;
	public final TagOutput output;
	public final int time;
	public final int creosoteOutput;

	public CokeOvenRecipe(TagOutput output, IngredientWithSize input, int time, int creosoteOutput)
	{
		super(output, IERecipeTypes.COKE_OVEN);
		this.output = output;
		this.input = input;
		this.time = time;
		this.creosoteOutput = creosoteOutput;
	}

	public boolean matches(ItemStack stack) {
		return input.test(stack);
	}

	@Override
	protected IERecipeSerializer getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getResultItem(Provider access)
	{
		return this.output.get();
	}

	public static CokeOvenRecipe findRecipe(Level level, ItemStack input)
	{
		return findRecipe(level, input, null);
	}

	public static CokeOvenRecipe findRecipe(Level level, ItemStack input, @Nullable CokeOvenRecipe hint)
	{
		if (input.isEmpty())
			return null;
		if (hint != null && hint.matches(input))
			return hint;
		for(RecipeHolder<CokeOvenRecipe> recipe : RECIPES.getRecipes(level))
			if(recipe.value().matches(input))
				return recipe.value();
		return null;
	}

}
