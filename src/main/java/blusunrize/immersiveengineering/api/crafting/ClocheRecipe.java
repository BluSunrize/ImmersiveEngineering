package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClocheRecipe
{
	public final List<ItemStack> outputs;
	public final IngredientStack seed;
	public final IngredientStack soil;
	public final int time;

	public static ArrayList<ClocheRecipe> recipeList = new ArrayList<>();
	public static List<ClocheFertilizer> fertilizerList = new ArrayList<>();
	private static HashMap<ComparableItemStack, ResourceLocation> soilTextureMap = new HashMap<>();

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
		if(recipe.seed!=null&&recipe.soil!=null)
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
			if(ApiUtils.stackMatchesObject(seed, recipe.seed)&&ApiUtils.stackMatchesObject(soil, recipe.soil))
				return recipe;
		}
		return null;
	}


	public static class ClocheFertilizer
	{
		public final IngredientStack input;
		public final float growthModifier;

		public ClocheFertilizer(IngredientStack input, float growthModifier)
		{
			this.input = input;
			this.growthModifier = growthModifier;
		}

		public float getGrowthModifier()
		{
			return growthModifier;
		}
	}

	public static ClocheFertilizer addFertilizer(Object input, float growthModifer)
	{
		ClocheFertilizer entry = new ClocheFertilizer(ApiUtils.createIngredientStack(input), growthModifer);
		if(entry.input!=null)
		{
			fertilizerList.add(entry);
			return entry;
		}
		return null;
	}

	public static float getFertilizerGrowthModifier(ItemStack stack)
	{
		for(ClocheFertilizer e : fertilizerList)
			if(e.input.matchesItemStack(stack))
				return e.getGrowthModifier();
		return 0;
	}

	public static boolean isValidFertilizer(ItemStack stack)
	{
		return getFertilizerGrowthModifier(stack) > 0;
	}

	public static void registerSoilTexture(Tag<?> soil, ResourceLocation texture)
	{
		soilTextureMap.put(new ComparableItemStack(soil.getId()), texture);
	}

	public static void registerSoilTexture(ItemStack soil, ResourceLocation texture)
	{
		soilTextureMap.put(new ComparableItemStack(soil, false, false), texture);
	}

	public static ResourceLocation getSoilTexture(ItemStack soil)
	{
		return soilTextureMap.get(new ComparableItemStack(soil, false, false));
	}
}
