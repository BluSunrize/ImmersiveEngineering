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
 * @author BluSunrize - 19.05.2017
 * <br>
 * The recipe for the alloy smelter
 */
public class AlloyRecipe extends IESerializableRecipe
{
	public static RecipeType<AlloyRecipe> TYPE;
	public static RegistryObject<IERecipeSerializer<AlloyRecipe>> SERIALIZER;

	public final IngredientWithSize input0;
	public final IngredientWithSize input1;
	public final ItemStack output;
	public final int time;

	public AlloyRecipe(ResourceLocation id, ItemStack output, IngredientWithSize input0, IngredientWithSize input1, int time)
	{
		super(output, TYPE, id);
		this.output = output;
		this.input0 = input0;
		this.input1 = input1;
		this.time = time;
	}

	@Override
	protected IERecipeSerializer<AlloyRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public ItemStack getResultItem()
	{
		return this.output;
	}

	public boolean matches(ItemStack input0, ItemStack input1) {
		if (this.input0.test(input0)&&this.input1.test(input1))
			return true;
		else if (this.input0.test(input1)&&this.input1.test(input0))
			return true;
		else
			return false;
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, AlloyRecipe> recipeList = Collections.emptyMap();

	@Deprecated
	public static AlloyRecipe findRecipe(ItemStack input0, ItemStack input1)
	{
		return findRecipe(input0, input1, null);
	}

	public static AlloyRecipe findRecipe(ItemStack input0, ItemStack input1, @Nullable AlloyRecipe hint)
	{
		if (input0.isEmpty() || input1.isEmpty())
			return null;
		if (hint != null && hint.matches(input0, input1))
			return hint;
		for(AlloyRecipe recipe : recipeList.values())
			if(recipe.matches(input0, input1))
				return recipe;
		return null;
	}
}
