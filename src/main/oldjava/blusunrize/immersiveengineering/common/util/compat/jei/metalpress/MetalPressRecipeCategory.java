/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.metalpress;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Arrays;

public class MetalPressRecipeCategory extends IERecipeCategory<MetalPressRecipe>
{
	public MetalPressRecipeCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.METAL_PRESS, "block.immersiveengineering.metal_press");
		setBackground(helper.createBlankDrawable(100, 50));
		setIcon(IEMultiblockLogic.METAL_PRESS.iconStack());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, MetalPressRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 13)
				.addItemStacks(Arrays.asList(recipe.input.getMatchingStacks()))
				.setBackground(JEIHelper.slotDrawable, -1, -1);

		builder.addSlot(RecipeIngredientRole.INPUT, 57, 1)
				.addItemStack(recipe.mold.getDefaultInstance())
				.setBackground(JEIHelper.slotDrawable, -1, -1);

		builder.addSlot(RecipeIngredientRole.OUTPUT, 83, 13)
				.addItemStack(recipe.output.get())
				.setBackground(JEIHelper.slotDrawable, -1, -1);
	}

	@Override
	public void draw(MetalPressRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		graphics.pose().pushPose();
		graphics.pose().scale(3, 3, 1);
		this.getIcon().draw(graphics, 5, 0);
		graphics.pose().popPose();
	}
}