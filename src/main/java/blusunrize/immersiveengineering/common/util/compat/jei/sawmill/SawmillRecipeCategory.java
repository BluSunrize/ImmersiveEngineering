/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.sawmill;

import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;

import java.util.Arrays;

public class SawmillRecipeCategory extends IERecipeCategory<SawmillRecipe>
{
	private final IDrawableStatic middle;
	private final IDrawableAnimated arrowNormal;
	private final IDrawableAnimated arrowSplit;

	public SawmillRecipeCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.SAWMILL, "block.immersiveengineering.sawmill");
		setBackground(helper.drawableBuilder(
				JEIHelper.JEI_GUI, 0, 0, 114, 26).setTextureSize(128, 128).addPadding(2, 36, 2, 12).build()
		);
		setIcon(IEMultiblockLogic.SAWMILL.iconStack());

		this.middle = helper.drawableBuilder(JEIHelper.JEI_GUI, 0, 26, 29, 16).setTextureSize(128, 128).build();
		IDrawableStatic arrowStatic = helper.drawableBuilder(JEIHelper.JEI_GUI, 29, 26, 66, 16).setTextureSize(128, 128).build();
		this.arrowSplit = helper.createAnimatedDrawable(arrowStatic, 80, IDrawableAnimated.StartDirection.LEFT, false);
		arrowStatic = helper.drawableBuilder(JEIHelper.JEI_GUI, 29, 42, 66, 17).setTextureSize(128, 128).build();
		this.arrowNormal = helper.createAnimatedDrawable(arrowStatic, 80, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, SawmillRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 3, 7)
				.addItemStacks(Arrays.asList(recipe.input.getItems()));

		if(!recipe.stripped.get().isEmpty())
			builder.addSlot(RecipeIngredientRole.OUTPUT, 47, 7)
					.addItemStack(recipe.stripped.get());

		builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 7)
				.addItemStack(recipe.output.get());

		int i = 0;
		for(Lazy<ItemStack> out : recipe.secondaryStripping)
		{
			builder.addSlot(RecipeIngredientRole.OUTPUT, 47+i%2*18, 29+i/2*18)
					.addItemStack(out.get());
			i++;
		}

		i = 0;
		for(Lazy<ItemStack> out : recipe.secondaryOutputs)
		{
			builder.addSlot(RecipeIngredientRole.OUTPUT, 91+i%2*18, 29+i/2*18)
					.addItemStack(out.get())
					.setBackground(JEIHelper.slotDrawable, -1, -1);
			i++;
		}
	}


	@Override
	public void draw(SawmillRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		if(recipe.stripped.get().isEmpty())
		{
			this.middle.draw(graphics, 36, 7);
			this.arrowNormal.draw(graphics, 22, 6);
		}
		else
			this.arrowSplit.draw(graphics, 22, 6);
	}

}