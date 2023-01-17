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
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BluSunrize - 01.05.2015
 * <p>
 * The recipe for the crusher
 */
public class CrusherRecipe extends MultiblockRecipe
{
	public static RegistryObject<IERecipeSerializer<CrusherRecipe>> SERIALIZER;
	public static final CachedRecipeList<CrusherRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.CRUSHER);

	public final Ingredient input;
	public final Lazy<ItemStack> output;
	public final List<StackWithChance> secondaryOutputs = new ArrayList<>();

	public CrusherRecipe(ResourceLocation id, Lazy<ItemStack> output, Ingredient input, int energy)
	{
		super(output, IERecipeTypes.CRUSHER, id);
		this.output = output;
		this.input = input;
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

	/**
	 * Adds secondary outputs to the recipe. Should the recipe have secondary outputs, these will be added /in addition/
	 */
	public CrusherRecipe addToSecondaryOutput(StackWithChance output)
	{
		Preconditions.checkNotNull(output);
		secondaryOutputs.add(output);
		return this;
	}

	public static CrusherRecipe findRecipe(Level level, ItemStack input)
	{
		for(CrusherRecipe recipe : RECIPES.getRecipes(level))
			if(recipe.input.test(input))
				return recipe;
		return null;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 4;
	}

}