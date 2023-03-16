/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the coke oven
 */
public class CokeOvenRecipe extends IESerializableRecipe
{
	public static RegistryObject<IERecipeSerializer<CokeOvenRecipe>> SERIALIZER;
	public static final CachedRecipeList<CokeOvenRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.COKE_OVEN);

	public final IngredientWithSize input;
	public final Lazy<ItemStack> output;
	public final int time;
	public final int creosoteOutput;

	public CokeOvenRecipe(ResourceLocation id, Lazy<ItemStack> output, IngredientWithSize input, int time, int creosoteOutput)
	{
		super(output, IERecipeTypes.COKE_OVEN, id);
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
	public ItemStack getResultItem(RegistryAccess access)
	{
		return this.output.get();
	}

	public static CokeOvenRecipe findRecipe(Level level, ItemStack input)
	{
		return findRecipe(level, input, null);
	}

	public static CokeOvenRecipe findRecipe(Level level, ItemStack input, @Nullable CokeOvenRecipe hint)
	{
		if (input.isEmpty())
			return null;
		if (hint != null && hint.matches(input))
			return hint;
		for(CokeOvenRecipe recipe : RECIPES.getRecipes(level))
			if(recipe.matches(input))
				return recipe;
		return null;
	}

}
