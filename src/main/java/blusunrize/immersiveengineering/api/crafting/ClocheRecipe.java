package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction.ClocheRenderReference;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClocheRecipe extends IESerializableRecipe
{
	public static IRecipeType<ClocheRecipe> TYPE = IRecipeType.register(Lib.MODID+":cloche");
	public static RegistryObject<IERecipeSerializer<ClocheRecipe>> SERIALIZER;

	public final List<ItemStack> outputs;
	public final Ingredient seed;
	public final Ingredient soil;
	public final int time;
	public final ClocheRenderReference renderReference;
	public final ClocheRenderFunction renderFunction;

	// Initialized by reload listener
	public static Map<ResourceLocation, ClocheRecipe> recipeList;
	public static List<ClocheFertilizer> fertilizerList = new ArrayList<>();
	private static List<Pair<Ingredient, ResourceLocation>> soilTextureList = new ArrayList<>();

	public ClocheRecipe(ResourceLocation id, List<ItemStack> outputs, Ingredient seed, Ingredient soil, int time, ClocheRenderReference renderReference)
	{
		super(outputs.get(0), TYPE, id);
		this.outputs = outputs;
		this.seed = seed;
		this.soil = soil;
		this.time = time;
		this.renderReference = renderReference;
		this.renderFunction = ClocheRenderFunction.RENDER_FUNCTION_FACTORIES.get(renderReference.getType()).apply(renderReference.getBlock());
	}

	public ClocheRecipe(ResourceLocation id, ItemStack output, Ingredient seed, Ingredient soil, int time, ClocheRenderReference renderReference)
	{
		this(id, ImmutableList.of(output), seed, soil, time, renderReference);
	}

	@Override
	protected IERecipeSerializer<ClocheRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return this.outputs.get(0);
	}

	public static ClocheRecipe findRecipe(ItemStack seed, ItemStack soil)
	{
		for(ClocheRecipe recipe : recipeList.values())
		{
			if(ApiUtils.stackMatchesObject(seed, recipe.seed)&&ApiUtils.stackMatchesObject(soil, recipe.soil))
				return recipe;
		}
		return null;
	}

	/* ========== FERTILIZER ========== */

	public static class ClocheFertilizer
	{
		public final Ingredient input;
		public final float growthModifier;

		public ClocheFertilizer(Ingredient input, float growthModifier)
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
	 * Registers a fertilizer along with its growth modifier
	 *
	 * @param input
	 * @param growthModifer
	 * @return the finished ClocheFertilizer object, or null if registration failed
	 */
	public static ClocheFertilizer addFertilizer(Ingredient input, float growthModifer)
	{
		ClocheFertilizer entry = new ClocheFertilizer(input, growthModifer);
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
			if(e.input.test(stack))
				return e.getGrowthModifier();
		return 0;
	}

	public static boolean isValidFertilizer(ItemStack stack)
	{
		return getFertilizerGrowthModifier(stack) > 0;
	}

	/* ========== SOIL TEXTURE ========== */

	/**
	 * Registers a given input to cause a replacement of the rendered soil texture
	 *
	 * @param soil
	 * @param texture
	 */
	public static void registerSoilTexture(Ingredient soil, ResourceLocation texture)
	{
		soilTextureList.add(Pair.of(soil, texture));
	}

	public static ResourceLocation getSoilTexture(ItemStack soil)
	{
		for(Pair<Ingredient, ResourceLocation> entry : soilTextureList)
			if(entry.getKey().test(soil))
				return entry.getValue();
		return null;
	}
}
