/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.alloysmelter;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class AlloySmelterRecipeWrapper implements IRecipeWrapper
{
	private final List<List<ItemStack>> inputs;
	private final ItemStack output;

	public AlloySmelterRecipeWrapper(AlloyRecipe recipe)
	{
		this.inputs = Arrays.asList(recipe.input0.getSizedStackList(), recipe.input1.getSizedStackList());
		this.output = recipe.output;
	}

	@Override
	public void getIngredients(IIngredients ingredients)
	{
		ingredients.setInputLists(ItemStack.class, inputs);
		ingredients.setOutput(ItemStack.class, output);
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
	}
}