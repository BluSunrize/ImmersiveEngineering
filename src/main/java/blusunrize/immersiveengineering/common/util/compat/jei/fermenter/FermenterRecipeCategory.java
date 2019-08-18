/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.fermenter;

import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
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

public class FermenterRecipeCategory extends IERecipeCategory<FermenterRecipe, FermenterRecipeWrapper>
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/fermenter.png");
	private final IDrawable tankOverlay;

	public FermenterRecipeCategory(IGuiHelper helper)
	{
		super("fermenter", "tile.immersiveengineering.metal_multiblock.fermenter.name", helper.createDrawable(background, 6, 12, 164, 59), FermenterRecipe.class, new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.FERMENTER.getMeta()));
		tankOverlay = helper.createDrawable(background, 177, 31, 16, 47, -2, 2, -2, 2);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, FermenterRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 1, 6);
		guiItemStacks.init(1, false, 84, 40);
		guiItemStacks.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
		if(ingredients.getOutputs(VanillaTypes.ITEM).size() > 0)
			guiItemStacks.set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
		if(ingredients.getOutputs(VanillaTypes.FLUID).size() > 0)
		{
			IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
			List<FluidStack> lfs = ingredients.getOutputs(VanillaTypes.FLUID).get(0);
			guiFluidStacks.init(0, false, 106, 9, 16, 47, lfs.get(0).amount*4, false, tankOverlay);
			guiFluidStacks.set(0, lfs);
			guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
		}
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(FermenterRecipe recipe)
	{
		return new FermenterRecipeWrapper(recipe);
	}
}