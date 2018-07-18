/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.workbench;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDevice0;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import com.google.common.collect.Lists;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class WorkbenchRecipeCategory extends IERecipeCategory<BlueprintCraftingRecipe, WorkbenchRecipeWrapper>
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/workbench.png");

	public WorkbenchRecipeCategory(IGuiHelper helper)
	{
		super("workbench", "tile.immersiveengineering.wooden_device0.workbench.name", helper.createDrawable(background, 0, 0, 176, 74), BlueprintCraftingRecipe.class, new ItemStack(IEContent.blockWoodenDevice0, 1, BlockTypes_WoodenDevice0.WORKBENCH.getMeta()), new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.AUTO_WORKBENCH.getMeta()));
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, WorkbenchRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 24, 16);
		guiItemStacks.set(0, Lists.newArrayList(BlueprintCraftingRecipe.getTypedBlueprint(recipeWrapper.blueprintCategory)));
		guiItemStacks.setBackground(0, JEIHelper.slotDrawable);
		for(int i = 0; i < recipeWrapper.recipeInputs.length; i++)
		{
			guiItemStacks.init(1+i, true, 80+i%2*18, 20+i/2*18);
			guiItemStacks.set(1+i, recipeWrapper.recipeInputs[i]);
			guiItemStacks.setBackground(1+i, JEIHelper.slotDrawable);
		}
		guiItemStacks.init(1+recipeWrapper.recipeInputs.length, false, 140, 24);
		guiItemStacks.set(1+recipeWrapper.recipeInputs.length, recipeWrapper.recipeOutputs[0]);
		guiItemStacks.setBackground(1+recipeWrapper.recipeInputs.length, JEIHelper.slotDrawable);
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(BlueprintCraftingRecipe recipe)
	{
		return new WorkbenchRecipeWrapper(recipe);
	}
}