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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the blast furnace
 */
public class BlastFurnaceRecipe extends IESerializableRecipe
{
	public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<BlastFurnaceRecipe>> SERIALIZER;
	public static final CachedRecipeList<BlastFurnaceRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.BLAST_FURNACE);

	public final IngredientWithSize input;
	public final TagOutput output;
	@Nonnull
	public final TagOutput slag;
	public final int time;

	public BlastFurnaceRecipe(TagOutput output, IngredientWithSize input, int time, @Nonnull TagOutput slag)
	{
		super(output, IERecipeTypes.BLAST_FURNACE);
		this.output = output;
		this.input = input;
		this.time = time;
		this.slag = slag;
	}

	@Override
	protected IERecipeSerializer<BlastFurnaceRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getResultItem(Provider access)
	{
		return output.get();
	}

	public boolean matches(ItemStack input) {
		return this.input.test(input);
	}

	public static BlastFurnaceRecipe findRecipe(Level level, ItemStack input, @Nullable BlastFurnaceRecipe hint)
	{
		if (input.isEmpty())
			return null;
		if (hint != null && hint.matches(input))
			return hint;
		for(RecipeHolder<BlastFurnaceRecipe> recipe : RECIPES.getRecipes(level))
			if(recipe.value().matches(input))
				return recipe.value();
		return null;
	}
}