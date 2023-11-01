/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author BluSunrize - 07.01.2016
 * <p>
 * The recipe for the metal press
 */
public class MetalPressRecipe extends MultiblockRecipe
{
	public static RegistryObject<IERecipeSerializer<MetalPressRecipe>> SERIALIZER;
	public static final CachedRecipeList<MetalPressRecipe> STANDARD_RECIPES = new CachedRecipeList<>(IERecipeTypes.METAL_PRESS);
	private static final List<MetalPressRecipe> SPECIAL_RECIPES = new ArrayList<>();

	public IngredientWithSize input;
	public final Item mold;
	public final Lazy<ItemStack> output;

	public synchronized static void addSpecialRecipe(MetalPressRecipe recipe)
	{
		SPECIAL_RECIPES.add(recipe);
	}

	public MetalPressRecipe(ResourceLocation id, Lazy<ItemStack> output, IngredientWithSize input, Item mold, int energy)
	{
		super(output, IERecipeTypes.METAL_PRESS, id);
		this.output = output;
		this.input = input;
		this.mold = mold;
		setTimeAndEnergy(60, energy);

		setInputListWithSizes(Lists.newArrayList(this.input));
		this.outputList = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, this.output.get()));
	}

	@Override
	protected IERecipeSerializer<MetalPressRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	public MetalPressRecipe setInputSize(int size)
	{
		this.input = new IngredientWithSize(this.input.getBaseIngredient(), size);
		return this;
	}

	public boolean matches(ItemStack mold, ItemStack input, Level world)
	{
		return this.input.test(input);
	}

	public MetalPressRecipe getActualRecipe(ItemStack mold, ItemStack input, Level world)
	{
		return this;
	}

	public static MetalPressRecipe findRecipe(ItemStack mold, ItemStack input, Level world)
	{
		if(mold.isEmpty()||input.isEmpty())
			return null;
		List<MetalPressRecipe> list = getRecipesByMold(world).get(mold.getItem());
		for(MetalPressRecipe recipe : list)
			if(recipe.matches(mold, input, world))
				return recipe.getActualRecipe(mold, input, world);
		return null;
	}

	public static boolean isValidMold(Level level, ItemStack itemStack)
	{
		if(itemStack.isEmpty())
			return false;
		return getRecipesByMold(level).containsKey(itemStack.getItem());
	}

	private static ArrayListMultimap<Item, MetalPressRecipe> recipesByMold = ArrayListMultimap.create();
	private static int reloadCountForByMold = CachedRecipeList.INVALID_RELOAD_COUNT;

	private static ArrayListMultimap<Item, MetalPressRecipe> getRecipesByMold(Level level)
	{
		if(reloadCountForByMold!=CachedRecipeList.getReloadCount())
		{
			recipesByMold = ArrayListMultimap.create();
			Consumer<MetalPressRecipe> addToMap = recipe -> recipesByMold.put(recipe.mold, recipe);
			STANDARD_RECIPES.getRecipes(level).forEach(addToMap);
			SPECIAL_RECIPES.forEach(addToMap);
			reloadCountForByMold = CachedRecipeList.getReloadCount();
		}
		return recipesByMold;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
}