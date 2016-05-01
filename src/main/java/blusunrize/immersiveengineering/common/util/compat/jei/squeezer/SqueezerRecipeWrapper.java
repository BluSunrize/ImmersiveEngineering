package blusunrize.immersiveengineering.common.util.compat.jei.squeezer;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import mezz.jei.api.IJeiHelpers;

public class SqueezerRecipeWrapper extends MultiblockRecipeWrapper
{
	public SqueezerRecipeWrapper(SqueezerRecipe recipe)
	{
		super(recipe);
	}


	public static List<SqueezerRecipeWrapper> getRecipes(IJeiHelpers jeiHelpers)
	{
		List<SqueezerRecipeWrapper> recipes = new ArrayList<>();
		for(SqueezerRecipe r : SqueezerRecipe.recipeList)
			recipes.add(new SqueezerRecipeWrapper(r));
		return recipes;
	}
}