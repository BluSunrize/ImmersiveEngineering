package blusunrize.immersiveengineering.common.util.compat.jei.cokeoven;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class CokeOvenRecipeWrapper extends BlankRecipeWrapper
{
	private final List<ItemStack> inputs;
	private final ItemStack output;
	private final FluidStack creosote;
	public CokeOvenRecipeWrapper(CokeOvenRecipe recipe)
	{
		this.inputs = (List<ItemStack>)(recipe.input instanceof List?recipe.input: Arrays.asList((ItemStack)recipe.input));
		this.output = recipe.output;
		this.creosote = new FluidStack(IEContent.fluidCreosote,recipe.creosoteOutput);
	}
	@Override
	public List<ItemStack> getInputs()
	{
		return inputs;
	}
	@Override
	public List<ItemStack> getOutputs()
	{
		return Collections.singletonList(output);
	}
	@Override
	public List<FluidStack> getFluidOutputs()
	{
		return Arrays.asList(creosote);
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight)
	{
	}

	public static List<CokeOvenRecipeWrapper> getRecipes(IJeiHelpers jeiHelpers)
	{
		List<CokeOvenRecipeWrapper> recipes = new ArrayList<>();
		for(CokeOvenRecipe r : CokeOvenRecipe.recipeList)
			recipes.add(new CokeOvenRecipeWrapper(r));
		return recipes;
	}
}