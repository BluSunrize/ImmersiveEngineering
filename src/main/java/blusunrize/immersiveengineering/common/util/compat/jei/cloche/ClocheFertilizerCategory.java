/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.cloche;

import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.jei.DoubleIcon;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

public class ClocheFertilizerCategory extends IERecipeCategory<ClocheFertilizer>
{
	public ClocheFertilizerCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.CLOCHE_FERTILIZER, "desc.immersiveengineering.jei.category.fertilizer");
		setBackground(helper.createBlankDrawable(150, 50));
		setIcon(new DoubleIcon(
				helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, IEBlocks.MetalDevices.CLOCHE.asItem().getDefaultInstance()),
				helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Misc.FERTILIZER.get().getDefaultInstance()),
				0.6f
		));
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ClocheFertilizer recipe, IFocusGroup focuses)
	{
			builder.addSlot(RecipeIngredientRole.INPUT, 33, 13)
					.addItemStack(recipe.input.getItems()[0])
					.setBackground(JEIHelper.slotDrawable, -1, -1);
	}

	@Override
	public void draw(ClocheFertilizer recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		graphics.pose().pushPose();
		graphics.pose().scale(3, 3, 1);
		this.getIcon().draw(graphics, -2, 0);
		graphics.pose().popPose();
		String growthModifier = I18n.get("desc.immersiveengineering.jei.cloche_modifier", Utils.formatDouble(recipe.growthModifier, "#.##"));
		graphics.drawString(ClientUtils.font(), growthModifier, 53, 17, 0x777777, false);
	}
}