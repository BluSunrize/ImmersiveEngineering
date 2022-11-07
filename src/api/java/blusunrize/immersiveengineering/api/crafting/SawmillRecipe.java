/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

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

public class SawmillRecipe extends MultiblockRecipe
{
	public static RegistryObject<IERecipeSerializer<SawmillRecipe>> SERIALIZER;
	public static final CachedRecipeList<SawmillRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.SAWMILL);

	public final Ingredient input;
	public final Lazy<ItemStack> stripped;
	public final NonNullList<Lazy<ItemStack>> secondaryStripping = NonNullList.create();
	public final Lazy<ItemStack> output;
	public final NonNullList<Lazy<ItemStack>> secondaryOutputs = NonNullList.create();

	public SawmillRecipe(ResourceLocation id, Lazy<ItemStack> output, Lazy<ItemStack> stripped, Ingredient input, int energy)
	{
		super(output, IERecipeTypes.SAWMILL, id);
		this.output = output;
		this.stripped = stripped;
		this.input = input;
		setTimeAndEnergy(80, energy);

		setInputList(Lists.newArrayList(this.input));
		this.outputList = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, this.output.get()));
	}

	@Override
	protected IERecipeSerializer<SawmillRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public NonNullList<ItemStack> getActualItemOutputs()
	{
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(stripped.get());
		for(Lazy<ItemStack> output : secondaryStripping)
			if(!output.get().isEmpty())
				list.add(output.get());
		list.add(output.get());
		for(Lazy<ItemStack> output : secondaryOutputs)
			if(!output.get().isEmpty())
				list.add(output.get());
		return list;
	}

	public SawmillRecipe addToSecondaryStripping(Lazy<ItemStack> output)
	{
		Preconditions.checkNotNull(output);
		secondaryStripping.add(output);
		return this;
	}

	public SawmillRecipe addToSecondaryOutput(Lazy<ItemStack> output)
	{
		Preconditions.checkNotNull(output);
		secondaryOutputs.add(output);
		return this;
	}

	public static SawmillRecipe findRecipe(Level level, ItemStack input)
	{
		if(!input.isEmpty())
			for(SawmillRecipe recipe : RECIPES.getRecipes(level))
				if(recipe.input.test(input))
					return recipe;
		return null;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
}