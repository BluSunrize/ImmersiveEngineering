/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class BlueprintInventory extends SimpleContainer
{
	private final List<RecipeHolder<BlueprintCraftingRecipe>> recipes;

	public BlueprintInventory(AbstractContainerMenu container, List<RecipeHolder<BlueprintCraftingRecipe>> recipes)
	{
		super(recipes.size());
		this.recipes = recipes;
	}

	public void updateOutputs(Container inputInventory)
	{
		//Get input items
		NonNullList<ItemStack> inputs = NonNullList.withSize(inputInventory.getContainerSize()-1, ItemStack.EMPTY);
		for(int i = 0; i < inputs.size(); i++)
			inputs.set(i, inputInventory.getItem(i+1));
		//Iterate Recipes and set output slots
		for(int i = 0; i < this.recipes.size(); i++)
		{
			if(recipes.get(i).value().matchesRecipe(inputs))
				this.setItem(i, recipes.get(i).value().output.get().copy());
			else
				this.setItem(i, ItemStack.EMPTY);
		}
	}

	public void reduceIputs(Container inputInventory, BlueprintCraftingRecipe recipe, ItemStack taken)
	{
		//Get input items
		NonNullList<ItemStack> inputs = NonNullList.withSize(inputInventory.getContainerSize()-1, ItemStack.EMPTY);
		for(int i = 0; i < inputs.size(); i++)
			inputs.set(i, inputInventory.getItem(i+1));
		//Consume
		recipe.consumeInputs(inputs, 1);
		//Update remains
		for(int i = 0; i < inputs.size(); i++)
			inputInventory.setItem(i+1, inputs.get(i));

		updateOutputs(inputInventory);
	}
}