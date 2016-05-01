package blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class BlastFurnaceRecipeWrapper extends BlankRecipeWrapper
{
	private final List<ItemStack> inputs;
	private final ItemStack output;
	private final ItemStack slag;
	public BlastFurnaceRecipeWrapper(BlastFurnaceRecipe recipe)
	{
		this.inputs = (List<ItemStack>)(recipe.input instanceof List?recipe.input: Arrays.asList((ItemStack)recipe.input));
		this.output = recipe.output;
		this.slag = recipe.slag;
	}
	@Override
	public List<ItemStack> getInputs()
	{
		return inputs;
	}
	@Override
	public List<ItemStack> getOutputs()
	{
		return Arrays.asList(output, slag);
	}

	public ItemStack getSmeltingOutput()
	{
		return output;
	}
	public ItemStack getSlagOutput()
	{
		return slag;
	}
	
	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight)
	{
	}

	public static List<BlastFurnaceRecipeWrapper> getRecipes(IJeiHelpers jeiHelpers)
	{
		List<BlastFurnaceRecipeWrapper> recipes = new ArrayList<>();
		for(BlastFurnaceRecipe r : BlastFurnaceRecipe.recipeList)
			recipes.add(new BlastFurnaceRecipeWrapper(r));
		return recipes;
	}
}