package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ClocheRecipe
{
	public final List<ItemStack> outputs;
	public final IngredientStack seed;
	public final IngredientStack soil;
	public final int time;
	public final ClocheRenderFunction renderFunction;

	/**
	 * Builds a render function for any 1-block crops with an age property
	 */
	public static Function<Block, ClocheRenderFunction> RENDER_FUNCTION_CROP;
	/**
	 * Builds a render function for stacking plants like sugarcane or cactus
	 */
	public static Function<Block, ClocheRenderFunction> RENDER_FUNCTION_STACK;
	/**
	 * Builds a render function for stem-grown plants like melon or pumpkin
	 */
	public static Function<Block, ClocheRenderFunction> RENDER_FUNCTION_STEM;
	/**
	 * Builds a render function for any block, making it grow in size
	 */
	public static Function<Block, ClocheRenderFunction> RENDER_FUNCTION_GENERIC;

	public static ArrayList<ClocheRecipe> recipeList = new ArrayList<>();
	public static List<ClocheFertilizer> fertilizerList = new ArrayList<>();
	private static List<Pair<IngredientStack, ResourceLocation>> soilTextureList = new ArrayList<>();

	public ClocheRecipe(List<ItemStack> outputs, Object seed, Object soil, int time, ClocheRenderFunction renderFunction)
	{
		this.outputs = outputs;
		this.seed = ApiUtils.createIngredientStack(seed);
		this.soil = ApiUtils.createIngredientStack(soil);
		this.time = time;
		this.renderFunction = renderFunction;
	}

	public ClocheRecipe(ItemStack output, Object seed, Object soil, int time, ClocheRenderFunction renderFunction)
	{
		this(ImmutableList.of(output), seed, soil, time, renderFunction);
	}

	/**
	 * Add a recipe for a List of ItemStack outputs
	 *
	 * @param outputs
	 * @param seed    Item, Block, ItemStack or Tag
	 * @param soil    Item, Block, ItemStack or Tag
	 * @param time    time in ticks for the recipe to grow
	 */
	public static void addRecipe(List<ItemStack> outputs, Object seed, Object soil, int time, ClocheRenderFunction renderFunction)
	{
		ClocheRecipe recipe = new ClocheRecipe(outputs, seed, soil, time, renderFunction);
		if(recipe.seed!=null&&recipe.soil!=null)
			recipeList.add(recipe);
	}

	/**
	 * Add a recipe for a single ItemStack output
	 *
	 * @param output
	 * @param seed   Item, Block, ItemStack or Tag
	 * @param soil   Item, Block, ItemStack or Tag
	 * @param time   time in ticks for the recipe to grow
	 */
	public static void addRecipe(ItemStack output, Object seed, Object soil, int time, ClocheRenderFunction renderFunction)
	{
		addRecipe(ImmutableList.of(output), seed, soil, time, renderFunction);
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

	/* ========== FERTILIZER ========== */

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

	/**
	 * Registers a fertilizer (Item, Block, ItemStack, Tag) along with its growth modifier
	 *
	 * @param input
	 * @param growthModifer
	 * @return the finished ClocheFertilizer object, or null if registration failed
	 */
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

	/* ========== SOIL TEXTURE ========== */

	/**
	 * Registers a given input (Item, Block, ItemStack, Tag) to cause a replacement of the rendered soil texture
	 *
	 * @param soil
	 * @param texture
	 */
	public static void registerSoilTexture(Object soil, ResourceLocation texture)
	{
		soilTextureList.add(Pair.of(ApiUtils.createIngredientStack(soil), texture));
	}

	public static ResourceLocation getSoilTexture(ItemStack soil)
	{
		for(Pair<IngredientStack, ResourceLocation> entry : soilTextureList)
			if(entry.getKey().matches(soil))
				return entry.getValue();
		return null;
	}

	/* ========== CUSTOM RENDERING ========== */

	public interface ClocheRenderFunction
	{
		float getScale(ItemStack seed, float growth);

		Collection<Pair<BlockState, TransformationMatrix>> getBlocks(ItemStack stack, float growth);
	}

}
