/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class SawmillRecipe extends MultiblockRecipe
{
	public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<SawmillRecipe>> SERIALIZER;
	public static final CachedRecipeList<SawmillRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.SAWMILL);
	public static final SetRestrictedField<RecipeMultiplier> MULTIPLIERS = SetRestrictedField.common();

	public final Ingredient input;
	public final TagOutput stripped;
	public final TagOutputList secondaryStripping;
	public final TagOutput output;
	public final TagOutputList secondaryOutputs;

	public SawmillRecipe(
			TagOutput output, TagOutput stripped, Ingredient input, int energy,
			TagOutputList secondaryStripping, TagOutputList secondaryOutputs
	)
	{
		super(output, IERecipeTypes.SAWMILL, 80, energy, MULTIPLIERS);
		this.output = output;
		this.stripped = stripped;
		this.input = input;
		this.secondaryOutputs = secondaryOutputs;
		this.secondaryStripping = secondaryStripping;

		setInputList(Lists.newArrayList(this.input));
		this.outputList = new TagOutputList(List.of(output));
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
		for(ItemStack output : secondaryStripping.get())
			if(!output.isEmpty())
				list.add(output);
		list.add(output.get());
		for(ItemStack output : secondaryOutputs.get())
			if(!output.isEmpty())
				list.add(output);
		return list;
	}

	public static SawmillRecipe findRecipe(Level level, ItemStack input)
	{
		if(!input.isEmpty())
			for(RecipeHolder<SawmillRecipe> recipe : RECIPES.getRecipes(level))
				if(recipe.value().input.test(input))
					return recipe.value();
		return null;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
}