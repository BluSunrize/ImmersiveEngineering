package blusunrize.immersiveengineering.common.util.compat.jei.refinery;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import mezz.jei.api.IJeiHelpers;

import java.util.ArrayList;
import java.util.List;

public class RefineryRecipeWrapper extends MultiblockRecipeWrapper
{
	public RefineryRecipeWrapper(RefineryRecipe recipe)
	{
		super(recipe);
	}

	public static List<RefineryRecipeWrapper> getRecipes(IJeiHelpers jeiHelpers)
	{
		List<RefineryRecipeWrapper> recipes = new ArrayList<>();
		for(RefineryRecipe r : RefineryRecipe.recipeList)
			recipes.add(new RefineryRecipeWrapper(r));
		return recipes;
	}
}