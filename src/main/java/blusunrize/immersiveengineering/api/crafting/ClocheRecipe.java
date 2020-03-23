package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ClocheRecipe
{
	public final List<ItemStack> outputs;
	public final IngredientStack seed;
	public final IngredientStack soil;
	public final int time;

	public static ArrayList<ClocheRecipe> recipeList = new ArrayList<>();

	public ClocheRecipe(List<ItemStack> outputs, Object seed, Object soil, int time)
	{
		this.outputs = outputs;
		this.seed = ApiUtils.createIngredientStack(seed);
		this.soil = ApiUtils.createIngredientStack(soil);
		this.time = time;
	}

	public ClocheRecipe(ItemStack output, Object seed, Object soil, int time)
	{
		this(ImmutableList.of(output), seed, soil, time);
	}

	public static void addRecipe(List<ItemStack> outputs, Object seed, Object soil, int time)
	{
		ClocheRecipe recipe = new ClocheRecipe(outputs, seed, soil, time);
		if(recipe.seed!=null && recipe.soil!=null)
			recipeList.add(recipe);
	}

	public static void addRecipe(ItemStack output, Object seed, Object soil, int time)
	{
		addRecipe(ImmutableList.of(output), seed, soil, time);
	}

	public static ClocheRecipe findRecipe(ItemStack seed, ItemStack soil)
	{
		for(ClocheRecipe recipe : recipeList)
		{
			if(ApiUtils.stackMatchesObject(seed, recipe.seed) && ApiUtils.stackMatchesObject(soil, recipe.soil))
				return recipe;
		}
		return null;
	}

}
