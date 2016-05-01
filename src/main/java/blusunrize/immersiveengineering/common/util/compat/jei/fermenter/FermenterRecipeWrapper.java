package blusunrize.immersiveengineering.common.util.compat.jei.fermenter;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import mezz.jei.api.IJeiHelpers;

public class FermenterRecipeWrapper extends MultiblockRecipeWrapper
{
	public FermenterRecipeWrapper(FermenterRecipe recipe)
	{
		super(recipe);
	}

	public static List<FermenterRecipeWrapper> getRecipes(IJeiHelpers jeiHelpers)
	{
		List<FermenterRecipeWrapper> recipes = new ArrayList<>();
		for(FermenterRecipe r : FermenterRecipe.recipeList)
			recipes.add(new FermenterRecipeWrapper(r));
		return recipes;
	}
}