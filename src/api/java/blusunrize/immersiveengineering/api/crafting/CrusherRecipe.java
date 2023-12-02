/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import net.minecraft.core.Holder;

import java.util.List;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * @author BluSunrize - 01.05.2015
 * <p>
 * The recipe for the crusher
 */
public class CrusherRecipe extends MultiblockRecipe
{
	public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<CrusherRecipe>> SERIALIZER;
	public static final CachedRecipeList<CrusherRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.CRUSHER);

	public final Ingredient input;
	public final Lazy<ItemStack> output;
	public final List<StackWithChance> secondaryOutputs;

	public CrusherRecipe(Lazy<ItemStack> output, Ingredient input, int energy, List<StackWithChance> secondaryOutputs)
	{
		super(output, IERecipeTypes.CRUSHER);
		this.output = output;
		this.input = input;
		this.secondaryOutputs = secondaryOutputs;
		setTimeAndEnergy(50, energy);

		setInputList(Lists.newArrayList(this.input));
		this.outputList = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, this.output.get()));
	}

	@Override
	protected IERecipeSerializer<CrusherRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public NonNullList<ItemStack> getActualItemOutputs()
	{
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(output.get());
		for(StackWithChance output : secondaryOutputs)
		{
			ItemStack realStack = output.stack().get();
			if(!realStack.isEmpty()&&ApiUtils.RANDOM.nextFloat() < output.chance())
				list.add(realStack);
		}
		return list;
	}

	public static RecipeHolder<CrusherRecipe> findRecipe(Level level, ItemStack input)
	{
		for(RecipeHolder<CrusherRecipe> recipe : RECIPES.getRecipes(level))
			if(recipe.value().input.test(input))
				return recipe;
		return null;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 4;
	}

}