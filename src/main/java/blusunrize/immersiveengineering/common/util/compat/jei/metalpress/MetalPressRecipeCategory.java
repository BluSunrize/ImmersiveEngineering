/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.metalpress;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class MetalPressRecipeCategory extends IERecipeCategory<MetalPressRecipe>
{
	public static final RecipeType<MetalPressRecipe> TYPE = RecipeType.create(Lib.MODID, "metalpress", MetalPressRecipe.class);

	public MetalPressRecipeCategory(IGuiHelper helper)
	{
		super(TYPE, helper, "block.immersiveengineering.metal_press");
		setBackground(helper.createBlankDrawable(100, 50));
		setIcon(new ItemStack(IEBlocks.Multiblocks.METAL_PRESS));
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
	public void draw(MetalPressRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack transform, double mouseX, double mouseY)
	{
		transform.pushPose();
		transform.scale(3, 3, 1);
		this.getIcon().draw(transform, 5, 0);
		transform.popPose();
	}
}