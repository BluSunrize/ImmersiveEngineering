/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.squeezer;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class SqueezerRecipeCategory extends IERecipeCategory<SqueezerRecipe, SqueezerRecipeWrapper>
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/squeezer.png");
	private final IDrawable tankOverlay;

	public SqueezerRecipeCategory(IGuiHelper helper)
	{
		super("squeezer", "tile.immersiveengineering.metal_multiblock.squeezer.name", helper.createDrawable(background, 6, 12, 164, 59), SqueezerRecipe.class, new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.SQUEEZER.getMeta()));
		tankOverlay = helper.createDrawable(background, 177, 31, 16, 47, -2, 2, -2, 2);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SqueezerRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiItemStacks.init(0, true, 1, 22);
		guiItemStacks.init(1, false, 84, 40);
		guiItemStacks.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
		if(ingredients.getOutputs(VanillaTypes.FLUID).size() > 0)
			guiItemStacks.set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
		if(ingredients.getOutputs(VanillaTypes.FLUID).size() > 0)
		{
			List<FluidStack> lfs = ingredients.getOutputs(VanillaTypes.FLUID).get(0);
			guiFluidStacks.init(0, false, 106, 9, 16, 47, lfs.get(0).amount*4, false, tankOverlay);
			guiFluidStacks.set(0, lfs);
			guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
		}
		else
			guiFluidStacks.init(0, false, 106, 9, 16, 47, 4000, false, tankOverlay);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(SqueezerRecipe recipe)
	{
		return new SqueezerRecipeWrapper(recipe);
	}
}