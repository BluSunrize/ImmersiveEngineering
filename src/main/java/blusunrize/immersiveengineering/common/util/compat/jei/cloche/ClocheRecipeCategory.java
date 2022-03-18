/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.cloche;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class ClocheRecipeCategory extends IERecipeCategory<ClocheRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "cloche");

	public ClocheRecipeCategory(IGuiHelper helper)
	{
		super(ClocheRecipe.class, helper, UID, "block.immersiveengineering.cloche");
		setBackground(helper.createBlankDrawable(100, 50));
		setIcon(new ItemStack(IEBlocks.MetalDevices.CLOCHE));
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ClocheRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 5, 7)
				.addItemStacks(Arrays.asList(recipe.seed.getItems()))
				.setBackground(JEIHelper.slotDrawable, -1, -1);

		builder.addSlot(RecipeIngredientRole.INPUT, 5, 31)
				.addItemStacks(Arrays.asList(recipe.soil.getItems()))
				.setBackground(JEIHelper.slotDrawable, -1, -1);

		for(int i = 0; i < recipe.outputs.size(); i++)
			builder.addSlot(RecipeIngredientRole.OUTPUT, 65+i%2*18, 13+i/2*18)
					.addItemStack(recipe.outputs.get(i).get())
					.setBackground(JEIHelper.slotDrawable, -1, -1);
	}

	@Override
	public void draw(ClocheRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack transform, double mouseX, double mouseY)
	{
		transform.pushPose();
		transform.scale(3, 3, 1);
		this.getIcon().draw(transform, 7, 0);
		transform.popPose();
	}
}