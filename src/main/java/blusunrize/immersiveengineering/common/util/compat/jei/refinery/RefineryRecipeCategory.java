/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.refinery;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidType;

import java.util.Arrays;

public class RefineryRecipeCategory extends IERecipeCategory<RefineryRecipe>
{
	private final IDrawableStatic tankOverlay;

	public RefineryRecipeCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.REFINERY, "block.immersiveengineering.refinery");
		ResourceLocation background = new ResourceLocation(Lib.MODID, "textures/gui/refinery.png");
		setBackground(helper.createDrawable(background, 6, 10, 125, 62));
		setIcon(IEMultiblockLogic.REFINERY.iconStack());
		tankOverlay = helper.createDrawable(background, 179, 33, 16, 47);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RefineryRecipe recipe, IFocusGroup focuses)
	{
		if(recipe.input0!=null)
			builder.addSlot(RecipeIngredientRole.INPUT, 7, 10)
					.setFluidRenderer(FluidType.BUCKET_VOLUME/20, false, 16, 47)
					.addIngredients(ForgeTypes.FLUID_STACK, recipe.input0.getMatchingFluidStacks())
					.setOverlay(tankOverlay, 0, 0);
		if(recipe.input1!=null)
			builder.addSlot(RecipeIngredientRole.INPUT, 34, 10)
					.setFluidRenderer(FluidType.BUCKET_VOLUME/20, false, 16, 47)
					.setOverlay(tankOverlay, 0, 0)
					.addIngredients(ForgeTypes.FLUID_STACK, recipe.input1.getMatchingFluidStacks());
		if(!recipe.catalyst.isEmpty())
		{
			builder.addSlot(RecipeIngredientRole.INPUT, 67, 16)
					.addItemStacks(Arrays.asList(recipe.catalyst.getItems()));
		}
		builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 10)
				.setFluidRenderer(FluidType.BUCKET_VOLUME/20, false, 16, 47)
				.setOverlay(tankOverlay, 0, 0)
				.addIngredient(ForgeTypes.FLUID_STACK, recipe.output)
				.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}
}