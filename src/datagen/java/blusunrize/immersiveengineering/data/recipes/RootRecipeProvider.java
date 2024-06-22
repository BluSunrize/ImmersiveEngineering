/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

// RecipeProvider overrides name to "Recipes" with a final override, so we cannot have more than two "top-level"
// providers inheriting from it
public class RootRecipeProvider extends RecipeProvider
{
	private final PackOutput packOutput;
	private final CompletableFuture<Provider> providerFuture;

	public RootRecipeProvider(PackOutput p_248933_, CompletableFuture<Provider> p_323846_)
	{
		super(p_248933_, p_323846_);
		this.packOutput = p_248933_;
		this.providerFuture = p_323846_;
	}

	@Override
	protected void buildRecipes(RecipeOutput out)
	{
		new OreRecipes(this.packOutput, providerFuture).buildRecipes(out);
		new ToolRecipes(this.packOutput, providerFuture).buildRecipes(out);
		new DecorationRecipes(this.packOutput, providerFuture).buildRecipes(out);
		new IngredientRecipes(this.packOutput, providerFuture).buildRecipes(out);
		new DeviceRecipes(this.packOutput, providerFuture).buildRecipes(out);
		new MultiblockRecipes(this.packOutput, providerFuture).buildRecipes(out);
		new MiscRecipes(this.packOutput, providerFuture).buildRecipes(out);
		new ClocheRecipes(this.packOutput, providerFuture).buildRecipes(out);
	}
}
