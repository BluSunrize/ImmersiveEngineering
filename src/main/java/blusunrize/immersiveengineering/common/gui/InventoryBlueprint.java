/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class InventoryBlueprint extends InventoryBasic
{
	Container container;

	private InventoryBlueprint(Container container, String name, int slots)
	{
		super("Blueprint_"+name, true, slots);
		this.container = container;
	}

	public static class Input extends InventoryBlueprint
	{
		public Input(Container container)
		{
			super(container, "Output", 6);
		}

		@Override
		public ItemStack decrStackSize(int index, int count)
		{
			ItemStack itemstack = super.decrStackSize(index, count);
			if(!itemstack.isEmpty())
				this.container.onCraftMatrixChanged(this);
			return itemstack;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack)
		{
			super.setInventorySlotContents(index, stack);
			this.container.onCraftMatrixChanged(this);
		}
	}

	public static class Output extends InventoryBlueprint
	{
		private final BlueprintCraftingRecipe[] recipes;

		public Output(Container container, BlueprintCraftingRecipe[] recipes)
		{
			super(container, "Output", recipes.length);
			this.recipes = recipes;
		}

		public void updateOutputs(IInventory inputInventory)
		{
			//Get input items
			NonNullList<ItemStack> inputs = NonNullList.withSize(inputInventory.getSizeInventory(), ItemStack.EMPTY);
			for(int i = 0; i < inputs.size(); i++)
				inputs.set(i, inputInventory.getStackInSlot(i));
			//Iterate Recipes and set output slots
			for(int i = 0; i < this.recipes.length; i++)
			{
				int craftable = recipes[i].getMaxCrafted(inputs);
				if(craftable > 0)
				{
					ItemStack out = recipes[i].output;
					craftable = Math.min(out.getCount()*craftable, 64-(64%out.getCount()));
					this.setInventorySlotContents(i, Utils.copyStackWithAmount(out, craftable));
				}
				else
					this.setInventorySlotContents(i, ItemStack.EMPTY);
			}
		}

		public void reduceIputs(IInventory inputInventory, BlueprintCraftingRecipe recipe, ItemStack taken)
		{
			//Get input items
			NonNullList<ItemStack> inputs = NonNullList.withSize(inputInventory.getSizeInventory(), ItemStack.EMPTY);
			for(int i = 0; i < inputs.size(); i++)
				inputs.set(i, inputInventory.getStackInSlot(i));
			//Consume
			recipe.consumeInputs(inputs, taken.getCount()/recipe.output.getCount());
			//Update remains
			for(int i = 0; i < inputs.size(); i++)
				inputInventory.setInventorySlotContents(i, inputs.get(i));
		}
	}
}