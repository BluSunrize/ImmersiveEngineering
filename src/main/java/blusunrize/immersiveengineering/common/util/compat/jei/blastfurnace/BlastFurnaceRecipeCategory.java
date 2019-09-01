/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDevices;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class BlastFurnaceRecipeCategory extends IERecipeCategory<BlastFurnaceRecipe, BlastFurnaceRecipeWrapper>
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/blast_furnace.png");

	public BlastFurnaceRecipeCategory(IGuiHelper helper)
	{
		super("blastfurnace", "gui.immersiveengineering.blastFurnace", helper.createDrawable(background, 8, 8, 142, 65), BlastFurnaceRecipe.class, new ItemStack(IEContent.blockStoneDevice, 1, BlockTypes_StoneDevices.BLAST_FURNACE.getMeta()), new ItemStack(IEContent.blockStoneDevice, 1, BlockTypes_StoneDevices.BLAST_FURNACE_ADVANCED.getMeta()));
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BlastFurnaceRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 43, 8);
		guiItemStacks.init(1, false, 103, 8);
		guiItemStacks.init(2, false, 103, 44);
		guiItemStacks.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
		guiItemStacks.set(1, recipeWrapper.getSmeltingOutput());
		guiItemStacks.set(2, recipeWrapper.getSlagOutput());
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(BlastFurnaceRecipe recipe)
	{
		return new BlastFurnaceRecipeWrapper(recipe);
	}
}