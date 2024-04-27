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
 * @author BluSunrize - 19.05.2017
 * <br>
 * The recipe for the alloy smelter
 */
public class AlloyRecipe extends IESerializableRecipe
{
	// TODO SetRestrictedField?
	public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<AlloyRecipe>> SERIALIZER;
	public static final CachedRecipeList<AlloyRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.ALLOY);

	public final IngredientWithSize input0;
	public final IngredientWithSize input1;
	public final TagOutput output;
	public final int time;

	public AlloyRecipe(TagOutput output, IngredientWithSize input0, IngredientWithSize input1, int time)
	{
		super(output, IERecipeTypes.ALLOY);
		this.output = output;
		this.input0 = input0;
		this.input1 = input1;
		this.time = time;
	}

	@Override
	protected IERecipeSerializer<AlloyRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getResultItem(Provider access)
	{
		return this.output.get();
	}

	public boolean matches(ItemStack input0, ItemStack input1) {
		if (this.input0.test(input0)&&this.input1.test(input1))
			return true;
		else if (this.input0.test(input1)&&this.input1.test(input0))
			return true;
		else
			return false;
	}

	public static AlloyRecipe findRecipe(
			Level level, ItemStack input0, ItemStack input1, @Nullable AlloyRecipe hint
	)
	{
		if (input0.isEmpty() || input1.isEmpty())
			return null;
		if (hint != null && hint.matches(input0, input1))
			return hint;
		for(RecipeHolder<AlloyRecipe> recipe : RECIPES.getRecipes(level))
			if(recipe.value().matches(input0, input1))
				return recipe.value();
		return null;
	}
}
