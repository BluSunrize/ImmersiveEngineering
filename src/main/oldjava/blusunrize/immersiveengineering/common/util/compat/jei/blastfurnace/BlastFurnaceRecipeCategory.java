/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

public class BlastFurnaceRecipeCategory extends IERecipeCategory<BlastFurnaceRecipe>
{
	private final IDrawableAnimated flame;
	private final IDrawableAnimated arrow;

	public BlastFurnaceRecipeCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.BLAST_FURNACE, "gui.immersiveengineering.blastFurnace");
		ResourceLocation background = new ResourceLocation(Lib.MODID, "textures/gui/blast_furnace.png");
		setBackground(helper.createDrawable(background, 42, 9, 100, 64));
		setIcon(helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, IEMultiblockLogic.BLAST_FURNACE.iconStack()));
		flame = helper.drawableBuilder(background, 177, 0, 14, 14).buildAnimated(300, IDrawableAnimated.StartDirection.TOP, true);
		arrow = helper.drawableBuilder(background, 176, 14, 24, 17).buildAnimated(300, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, BlastFurnaceRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 10, 8)
				.addItemStacks(Arrays.asList(recipe.input.getMatchingStacks()));
		builder.addSlot(RecipeIngredientRole.OUTPUT, 70, 8)
				.addItemStack(recipe.output.get());
		builder.addSlot(RecipeIngredientRole.OUTPUT, 70, 44)
				.addItemStack(recipe.slag.get());
	}

	@Override
	public void draw(BlastFurnaceRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		flame.draw(graphics, 12, 27);
		arrow.draw(graphics, 33, 26);
		String burnTime = I18n.get("desc.immersiveengineering.info.seconds", Utils.formatDouble(recipe.time/20f, "#.##"));
		graphics.drawString(ClientUtils.font(), burnTime, 68-ClientUtils.font().width(burnTime), 48, 0x777777, false);
	}
}