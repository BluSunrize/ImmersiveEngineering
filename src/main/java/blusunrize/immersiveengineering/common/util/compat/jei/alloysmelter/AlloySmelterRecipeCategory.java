/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.alloysmelter;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class AlloySmelterRecipeCategory extends IERecipeCategory<AlloyRecipe>
{
	private final IDrawableAnimated flame;
	private final IDrawableAnimated arrow;

	public AlloySmelterRecipeCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.ALLOY, "block.immersiveengineering.alloy_smelter");
		ResourceLocation background = new ResourceLocation(Lib.MODID, "textures/gui/alloy_smelter.png");
		setBackground(helper.createDrawable(background, 36, 15, 106, 56));
		setIcon(IEMultiblockLogic.ALLOY_SMELTER.iconStack());
		flame = helper.drawableBuilder(background, 177, 0, 14, 14).buildAnimated(200, IDrawableAnimated.StartDirection.TOP, true);
		arrow = helper.drawableBuilder(background, 176, 14, 24, 17).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, AlloyRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 2, 2)
				.addItemStacks(recipe.input0.getMatchingStackList());
		builder.addSlot(RecipeIngredientRole.INPUT, 30, 2)
				.addItemStacks(recipe.input1.getMatchingStackList());
		builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 20)
				.addItemStack(recipe.output.get());
	}

	@Override
	public void draw(AlloyRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		flame.draw(graphics, 18, 21);
		arrow.draw(graphics, 47, 20);
	}
}