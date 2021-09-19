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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the blast furnace
 */
public class BlastFurnaceRecipe extends IESerializableRecipe
{
	public static RecipeType<BlastFurnaceRecipe> TYPE;
	public static RegistryObject<IERecipeSerializer<BlastFurnaceRecipe>> SERIALIZER;

	public final IngredientWithSize input;
	public final ItemStack output;
	@Nonnull
	public final ItemStack slag;
	public final int time;

	public BlastFurnaceRecipe(ResourceLocation id, ItemStack output, IngredientWithSize input, int time, @Nonnull ItemStack slag)
	{
		super(output, TYPE, id);
		this.output = output;
		this.input = input;
		this.time = time;
		this.slag = slag;
	}

	@Override
	protected IERecipeSerializer<BlastFurnaceRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getResultItem()
	{
		return output;
	}

	public boolean matches(ItemStack input) {
		return this.input.test(input);
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, BlastFurnaceRecipe> recipeList = Collections.emptyMap();

	@Deprecated
	public static BlastFurnaceRecipe findRecipe(ItemStack input)
	{
		return findRecipe(input, null);
	}

	public static BlastFurnaceRecipe findRecipe(ItemStack input, @Nullable BlastFurnaceRecipe hint)
	{
		if (input.isEmpty())
			return null;
		if (hint != null && hint.matches(input))
			return hint;
		for(BlastFurnaceRecipe recipe : recipeList.values())
			if(recipe.matches(input))
				return recipe;
		return null;
	}
}