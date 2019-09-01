/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.bottlingmachine;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
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

public class BottlingMachineRecipeCategory extends IERecipeCategory<BottlingMachineRecipe, BottlingMachineRecipeWrapper>
{
	public static ResourceLocation background = new ResourceLocation("immersiveengineering:textures/gui/fermenter.png");
	private final IDrawable tankOverlay;
	static ItemStack bottlignMachineStack;

	public BottlingMachineRecipeCategory(IGuiHelper helper)
	{
		super("bottlingMachine", "tile.immersiveengineering.metal_multiblock.bottling_machine.name", helper.createBlankDrawable(140, 50), BottlingMachineRecipe.class, new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.BOTTLING_MACHINE.getMeta()));
		tankOverlay = helper.createDrawable(background, 177, 31, 20, 51, -2, 2, -2, 2);
		bottlignMachineStack = new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.BOTTLING_MACHINE.getMeta());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BottlingMachineRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 12);
		guiItemStacks.init(1, false, 100, 12);
		guiItemStacks.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
		guiItemStacks.set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
		guiItemStacks.setBackground(0, JEIHelper.slotDrawable);
		guiItemStacks.setBackground(1, JEIHelper.slotDrawable);

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		List<FluidStack> lfs = ingredients.getInputs(VanillaTypes.FLUID).get(0);
		guiFluidStacks.init(0, true, 75, 0, 16, 47, lfs.get(0).amount*4, false, tankOverlay);
		guiFluidStacks.set(0, lfs);
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(BottlingMachineRecipe recipe)
	{
		return new BottlingMachineRecipeWrapper(recipe);
	}
}