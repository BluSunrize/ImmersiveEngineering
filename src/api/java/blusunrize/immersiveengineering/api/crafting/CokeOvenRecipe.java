/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the coke oven
 */
public class CokeOvenRecipe extends IESerializableRecipe
{
	public static RecipeType<CokeOvenRecipe> TYPE;
	public static RegistryObject<IERecipeSerializer<CokeOvenRecipe>> SERIALIZER;

	public final IngredientWithSize input;
	public final ItemStack output;
	public final int time;
	public final int creosoteOutput;

	public CokeOvenRecipe(ResourceLocation id, ItemStack output, IngredientWithSize input, int time, int creosoteOutput)
	{
		super(output, TYPE, id);
		this.output = output;
		this.input = input;
		this.time = time;
		this.creosoteOutput = creosoteOutput;
	}

	public boolean matches(ItemStack stack) {
		return input.test(stack);
	}

	@Override
	protected IERecipeSerializer getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getResultItem()
	{
		return this.output;
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, CokeOvenRecipe> recipeList = Collections.emptyMap();

	public static CokeOvenRecipe findRecipe(ItemStack input)
	{
		return findRecipe(input, null);
	}

	public static CokeOvenRecipe findRecipe(ItemStack input, @Nullable CokeOvenRecipe hint)
	{
		if (input.isEmpty())
			return null;
		if (hint != null && hint.matches(input))
			return hint;
		for(CokeOvenRecipe recipe : recipeList.values())
			if(recipe.matches(input))
				return recipe;
		return null;
	}

}
