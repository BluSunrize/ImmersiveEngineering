/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.RegistryObject;

import java.util.Collections;
import java.util.Map;

public class SawmillRecipe extends MultiblockRecipe
{
	public static RecipeType<SawmillRecipe> TYPE;
	public static RegistryObject<IERecipeSerializer<SawmillRecipe>> SERIALIZER;

	public final Ingredient input;
	public final ItemStack stripped;
	public final NonNullList<ItemStack> secondaryStripping = NonNullList.create();
	public final ItemStack output;
	public final NonNullList<ItemStack> secondaryOutputs = NonNullList.create();

	public SawmillRecipe(ResourceLocation id, ItemStack output, ItemStack stripped, Ingredient input, int energy)
	{
		super(output, TYPE, id);
		this.output = output;
		this.stripped = stripped;
		this.input = input;
		setTimeAndEnergy(80, energy);

		setInputList(Lists.newArrayList(this.input));
		this.outputList = NonNullList.of(ItemStack.EMPTY, this.output);
	}

	@Override
	protected IERecipeSerializer<SawmillRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public NonNullList<ItemStack> getActualItemOutputs(BlockEntity tile)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(stripped);
		for(ItemStack output : secondaryStripping)
			if(!output.isEmpty())
				list.add(output);
		list.add(output);
		for(ItemStack output : secondaryOutputs)
			if(!output.isEmpty())
				list.add(output);
		return list;
	}

	public SawmillRecipe addToSecondaryStripping(ItemStack output)
	{
		Preconditions.checkNotNull(output);
		secondaryStripping.add(output);
		return this;
	}

	public SawmillRecipe addToSecondaryOutput(ItemStack output)
	{
		Preconditions.checkNotNull(output);
		secondaryOutputs.add(output);
		return this;
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, SawmillRecipe> recipeList = Collections.emptyMap();

	public static SawmillRecipe findRecipe(ItemStack input)
	{
		if(!input.isEmpty())
			for(SawmillRecipe recipe : recipeList.values())
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