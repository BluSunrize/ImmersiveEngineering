/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.cloche;

import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class ClocheFertilizerCategory extends IERecipeCategory<ClocheFertilizer>
{
	public ClocheFertilizerCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.CLOCHE_FERTILIZER, "ie.jei.category.fertilizer");
		setBackground(helper.createBlankDrawable(100, 50));
		setIcon(new ItemStack(IEBlocks.MetalDevices.CLOCHE));
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ClocheFertilizer recipe, IFocusGroup focuses)
	{
			builder.addSlot(RecipeIngredientRole.INPUT, 35, 13)
					.addItemStack(recipe.input.getItems()[0])
					.setBackground(JEIHelper.slotDrawable, -1, -1);
	}

	@Override
	public void draw(ClocheFertilizer recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		graphics.pose().pushPose();
		graphics.pose().scale(3, 3, 1);
		this.getIcon().draw(graphics, -1, 0);
		graphics.pose().popPose();
	}
}