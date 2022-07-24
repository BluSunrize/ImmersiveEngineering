/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.PositionedItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class ManualElementBlueprint extends ManualElementIECrafting
{
	public ManualElementBlueprint(ManualInstance manual, ItemStack... stacks)
	{
		super(manual, stacks);
		recalculateCraftingRecipes();
	}

	@Override
	public void recalculateCraftingRecipes()
	{
		this.recipes.clear();

		for(String category : BlueprintCraftingRecipe.recipeCategories)
			for(BlueprintCraftingRecipe recipe : BlueprintCraftingRecipe.findRecipes(Minecraft.getInstance().level, category))
				for(int iStack = 0; iStack < stacks.length; iStack++)
				{
					ItemStack output = stacks[iStack];
					if(!recipe.output.get().isEmpty()&&ManualUtils.stackMatchesObject(recipe.output.get(), output)&&recipe.inputs!=null&&recipe.inputs.length > 0)
					{
						int h = (int)Math.ceil(recipe.inputs.length/2f);
						PositionedItemStack[] pIngredients = new PositionedItemStack[recipe.inputs.length+2];
						for(int i = 0; i < recipe.inputs.length; i++)
							pIngredients[i] = new PositionedItemStack(recipe.inputs[i].getMatchingStacks(), 32+i%2*18, i/2*18);
						int middle = (int)(h/2f*18);
						pIngredients[pIngredients.length-2] = new PositionedItemStack(recipe.output.get(), 86, middle-9);
						pIngredients[pIngredients.length-1] = new PositionedItemStack(BlueprintCraftingRecipe.getTypedBlueprint(category), 8, middle-9);

						this.recipes.add(pIngredients);
						this.arrowPositions.add(new ArrowPosition(69, middle-5));
						if(h*18 > yOff)
							yOff = h*18;
					}
				}
		this.providedItems.clear();
		for(ItemStack stack : stacks)
			this.addProvidedItem(stack);
	}
}