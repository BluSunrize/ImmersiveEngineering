/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.refinery;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class RefineryRecipeCategory extends IERecipeCategory<RefineryRecipe, RefineryRecipeWrapper>
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/refinery.png");
	private final IDrawable tankOverlay;

	public RefineryRecipeCategory(IGuiHelper helper)
	{
		super("refinery", "tile.immersiveengineering.metal_multiblock.refinery.name", helper.createDrawable(background, 6, 10, 164, 62), RefineryRecipe.class, new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.REFINERY.getMeta()));
		tankOverlay = helper.createDrawable(background, 177, 31, 16, 47, -2, 2, -2, 2);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, RefineryRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		List<List<FluidStack>> inputs = ingredients.getInputs(FluidStack.class);
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		if(inputs.size() > 0)
		{
			guiFluidStacks.init(0, true, 7, 10, 16, 47, 6000, false, tankOverlay);
			guiFluidStacks.set(0, inputs.get(0));

			if(inputs.size() > 1)
			{
				guiFluidStacks.init(1, true, 55, 10, 16, 47, 6000, false, tankOverlay);
				guiFluidStacks.set(1, inputs.get(1));
			}
		}
		guiFluidStacks.init(2, false, 103, 10, 16, 47, 6000, false, tankOverlay);
		guiFluidStacks.set(2, ingredients.getOutputs(FluidStack.class).get(0));

		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(RefineryRecipe recipe)
	{
		return new RefineryRecipeWrapper(recipe);
	}
}