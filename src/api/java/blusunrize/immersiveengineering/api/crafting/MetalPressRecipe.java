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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import net.minecraft.core.Holder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * @author BluSunrize - 07.01.2016
 * <p>
 * The recipe for the metal press
 */
public class MetalPressRecipe extends MultiblockRecipe
{
	public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<MetalPressRecipe>> SERIALIZER;
	public static final CachedRecipeList<MetalPressRecipe> STANDARD_RECIPES = new CachedRecipeList<>(IERecipeTypes.METAL_PRESS);
	private static final Map<ResourceLocation, MetalPressRecipe> SPECIAL_RECIPES = new HashMap<>();

	public IngredientWithSize input;
	public final Item mold;
	public final Lazy<ItemStack> output;

	public synchronized static void addSpecialRecipe(ResourceLocation rl, MetalPressRecipe recipe)
	{
		SPECIAL_RECIPES.put(rl, recipe);
	}

	public MetalPressRecipe(Lazy<ItemStack> output, IngredientWithSize input, Item mold, int energy)
	{
		super(output, IERecipeTypes.METAL_PRESS);
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

	public RecipeHolder<MetalPressRecipe> getActualRecipe(
			ResourceLocation ownId, ItemStack mold, ItemStack input, Level world
	)
	{
		return new RecipeHolder<>(ownId, this);
	}

	public static RecipeHolder<MetalPressRecipe> findRecipe(ItemStack mold, ItemStack input, Level world)
	{
		if(mold.isEmpty()||input.isEmpty())
			return null;
		List<RecipeHolder<MetalPressRecipe>> list = getRecipesByMold(world).get(mold.getItem());
		for(RecipeHolder<MetalPressRecipe> recipe : list)
			if(recipe.value().matches(mold, input, world))
				return recipe.value().getActualRecipe(recipe.id(), mold, input, world);
		return null;
	}

	public static boolean isValidMold(Level level, ItemStack itemStack)
	{
		if(itemStack.isEmpty())
			return false;
		return getRecipesByMold(level).containsKey(itemStack.getItem());
	}

	private static ArrayListMultimap<Item, RecipeHolder<MetalPressRecipe>> recipesByMold = ArrayListMultimap.create();
	private static int reloadCountForByMold = CachedRecipeList.INVALID_RELOAD_COUNT;

	private static ArrayListMultimap<Item, RecipeHolder<MetalPressRecipe>> getRecipesByMold(Level level)
	{
		if(reloadCountForByMold!=CachedRecipeList.getReloadCount())
		{
			recipesByMold = ArrayListMultimap.create();
			BiConsumer<ResourceLocation, MetalPressRecipe> addToMap = (id, recipe) -> recipesByMold.put(recipe.mold, new RecipeHolder<>(id, recipe));
			STANDARD_RECIPES.getRecipes(level).forEach(r -> addToMap.accept(r.id(), r.value()));
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