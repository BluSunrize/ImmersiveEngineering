/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

// RecipeProvider overrides name to "Recipes" with a final override, so we cannot have more than two "top-level"
// providers inheriting from it
public class RootRecipeProvider extends RecipeProvider
{
	private final PackOutput packOutput;

	public RootRecipeProvider(PackOutput packOutput)
	{
		super(packOutput);
		this.packOutput = packOutput;
	}

	@Override
	protected void buildRecipes(RecipeOutput out)
	{
		new OreRecipes(this.packOutput).buildRecipes(out);
		new ToolRecipes(this.packOutput).buildRecipes(out);
		new DecorationRecipes(this.packOutput).buildRecipes(out);
		new IngredientRecipes(this.packOutput).buildRecipes(out);
		new DeviceRecipes(this.packOutput).buildRecipes(out);
		new MultiblockRecipes(this.packOutput).buildRecipes(out);
		new MiscRecipes(this.packOutput).buildRecipes(out);
		new ClocheRecipes(this.packOutput).buildRecipes(out);
	}
}
