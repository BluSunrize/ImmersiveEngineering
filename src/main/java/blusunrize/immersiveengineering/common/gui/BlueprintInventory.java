/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlueprintInventory extends Inventory
{
	private final BlueprintCraftingRecipe[] recipes;

	public BlueprintInventory(Container container, BlueprintCraftingRecipe[] recipes)
	{
		super(recipes.length);
		this.recipes = recipes;
	}

	public void updateOutputs(IInventory inputInventory)
	{
		//Get input items
		NonNullList<ItemStack> inputs = NonNullList.withSize(inputInventory.getSizeInventory()-1, ItemStack.EMPTY);
		for(int i = 0; i < inputs.size(); i++)
			inputs.set(i, inputInventory.getStackInSlot(i+1));
		//Iterate Recipes and set output slots
		for(int i = 0; i < this.recipes.length; i++)
		{
			if(recipes[i].matchesRecipe(inputs))
			{
				ItemStack out = recipes[i].output;
				this.setInventorySlotContents(i, ItemHandlerHelper.copyStackWithSize(out, out.getCount()));
			}
			else
				this.setInventorySlotContents(i, ItemStack.EMPTY);
		}
	}

	public void reduceIputs(IInventory inputInventory, BlueprintCraftingRecipe recipe, ItemStack taken)
	{
		//Get input items
		NonNullList<ItemStack> inputs = NonNullList.withSize(inputInventory.getSizeInventory()-1, ItemStack.EMPTY);
		for(int i = 0; i < inputs.size(); i++)
			inputs.set(i, inputInventory.getStackInSlot(i+1));
		//Consume
		recipe.consumeInputs(inputs, (int)Math.ceil((double)taken.getCount()/recipe.output.getCount()));
		//Update remains
		for(int i = 0; i < inputs.size(); i++)
			inputInventory.setInventorySlotContents(i+1, inputs.get(i));

		updateOutputs(inputInventory);
	}
}