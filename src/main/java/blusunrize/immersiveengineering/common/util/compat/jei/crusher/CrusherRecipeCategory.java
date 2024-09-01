/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.crusher;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrusherRecipeCategory extends IERecipeCategory<CrusherRecipe>
{
	public CrusherRecipeCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.CRUSHER, "block.immersiveengineering.crusher");
		setBackground(helper.createBlankDrawable(140, 54));
		setIcon(IEMultiblockLogic.CRUSHER.iconStack());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CrusherRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 2, 20)
				.addItemStacks(Arrays.asList(recipe.input.getItems()))
				.setBackground(JEIHelper.slotDrawable, -1, -1);

		List<StackWithChance> validSecondaries = getValidSecondaryOutputs(recipe);
		int y = 1+(validSecondaries.isEmpty()?18: validSecondaries.size() < 2?9: 0);
		builder.addSlot(RecipeIngredientRole.OUTPUT, 78, y)
				.addItemStack(recipe.output.get())
				.setBackground(JEIHelper.slotDrawable, -1, -1);

		for(int i = 0; i < validSecondaries.size(); i++)
			builder.addSlot(RecipeIngredientRole.OUTPUT, 78+i/2*44, y+18+i%2*18)
					.addItemStack(validSecondaries.get(i).stack().get())
					.setBackground(JEIHelper.slotDrawable, -1, -1);
	}

	@Override
	public void draw(CrusherRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		List<StackWithChance> validSecondaries = getValidSecondaryOutputs(recipe);
		int yBase = validSecondaries.isEmpty()?36: validSecondaries.size() < 2?27: 18;
		for(int i = 0; i < validSecondaries.size(); i++)
		{
			int x = 77+i/2*44;
			int y = yBase+i%2*18;
			graphics.drawString(
					ClientUtils.font(),
					Utils.formatDouble(validSecondaries.get(i).chance()*100, "0.##")+"%",
					x+21,
					y+6,
					0x777777,
					false
			);
		}
		graphics.pose().pushPose();
		graphics.pose().scale(3f, 3f, 1);
		this.getIcon().draw(graphics, 8, 0);
		graphics.pose().popPose();
	}

	private List<StackWithChance> getValidSecondaryOutputs(CrusherRecipe recipe)
	{
		List<StackWithChance> validSecondaries = new ArrayList<>();
		for(StackWithChance out : recipe.secondaryOutputs)
			if(!out.stack().get().isEmpty()&&out.chance() > 0)
				validSecondaries.add(out);
		return validSecondaries;
	}
}