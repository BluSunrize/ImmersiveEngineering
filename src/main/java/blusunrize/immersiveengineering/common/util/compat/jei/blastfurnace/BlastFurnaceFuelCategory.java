/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe.BlastFurnaceFuel;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDevices;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class BlastFurnaceFuelCategory extends IERecipeCategory<BlastFurnaceFuel, BlastFurnaceFuelWrapper>
{
	public static ResourceLocation background = new ResourceLocation("minecraft:textures/gui/container/furnace.png");
	IDrawable flame;

	public BlastFurnaceFuelCategory(IGuiHelper helper)
	{
		super("blastfurnace.fuel", "gui.immersiveengineering.blastFurnace.fuel", helper.createDrawable(background, 55, 38, 18, 32, 0, 0, 0, 80), BlastFurnaceFuel.class, new ItemStack(IEContent.blockStoneDevice, 1, BlockTypes_StoneDevices.BLAST_FURNACE.getMeta()), new ItemStack(IEContent.blockStoneDevice, 1, BlockTypes_StoneDevices.BLAST_FURNACE_ADVANCED.getMeta()));

		flame = helper.createDrawable(BlastFurnaceRecipeCategory.background, 176, 0, 14, 14);
	}

	@Nullable
	@Override
	public IDrawable getIcon()
	{
		return flame;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BlastFurnaceFuelWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 14);
		guiItemStacks.set(0, ingredients.getInputs(ItemStack.class).get(0));
	}

	@Override
	public boolean isRecipeValid(BlastFurnaceFuel recipe)
	{
		return true;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(BlastFurnaceFuel recipe)
	{
		if(recipe!=null&&recipe.input!=null)
			return new BlastFurnaceFuelWrapper(JEIHelper.jeiHelpers.getGuiHelper(), recipe.input.getStackList(), recipe.burnTime);
		return null;
	}
}