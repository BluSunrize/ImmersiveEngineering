/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.metalpress;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class MetalPressRecipeCategory extends IERecipeCategory<MetalPressRecipe, MetalPressRecipeWrapper>
{
	static ItemStack metalPressStack;

	public MetalPressRecipeCategory(IGuiHelper helper)
	{
		super("metalPress", "tile.immersiveengineering.metal_multiblock.metal_press.name", helper.createBlankDrawable(140, 50), MetalPressRecipe.class, new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.METAL_PRESS.getMeta()));
		metalPressStack = new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.METAL_PRESS.getMeta());
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, MetalPressRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 20, 3);
		guiItemStacks.init(1, true, 71, 0);
		guiItemStacks.init(2, false, 102, 3);
		guiItemStacks.set(0, recipeWrapper.recipeInputs[0]);
		guiItemStacks.setBackground(0, JEIHelper.slotDrawable);
		guiItemStacks.set(1, recipeWrapper.recipeInputs[1]);
		guiItemStacks.set(2, ingredients.getOutputs(ItemStack.class).get(0));
		guiItemStacks.setBackground(2, JEIHelper.slotDrawable);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(MetalPressRecipe recipe)
	{
		return new MetalPressRecipeWrapper(recipe);
	}
}