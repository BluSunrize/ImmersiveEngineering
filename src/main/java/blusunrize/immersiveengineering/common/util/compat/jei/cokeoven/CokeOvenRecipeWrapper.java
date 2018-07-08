/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.cokeoven;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class CokeOvenRecipeWrapper implements IRecipeWrapper
{
	private final List<ItemStack> inputs;
	private final ItemStack output;
	private final FluidStack creosote;

	public CokeOvenRecipeWrapper(CokeOvenRecipe recipe)
	{
		this.inputs = (List<ItemStack>)(recipe.input instanceof List?recipe.input: Arrays.asList((ItemStack)recipe.input));
		this.output = recipe.output;
		this.creosote = new FluidStack(IEContent.fluidCreosote, recipe.creosoteOutput);
	}

	@Override
	public void getIngredients(IIngredients ingredients)
	{
		ingredients.setInputs(ItemStack.class, inputs);
		ingredients.setOutput(ItemStack.class, output);
		ingredients.setOutput(FluidStack.class, creosote);
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
	}
}