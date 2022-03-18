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
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class AlloySmelterRecipeCategory extends IERecipeCategory<AlloyRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "alloysmelter");
	private final IDrawableAnimated flame;
	private final IDrawableAnimated arrow;

	public AlloySmelterRecipeCategory(IGuiHelper helper)
	{
		super(AlloyRecipe.class, helper, UID, "block.immersiveengineering.alloy_smelter");
		ResourceLocation background = new ResourceLocation(Lib.MODID, "textures/gui/alloy_smelter.png");
		setBackground(helper.createDrawable(background, 36, 15, 106, 56));
		setIcon(new ItemStack(IEBlocks.Multiblocks.ALLOY_SMELTER));
		flame = helper.drawableBuilder(background, 177, 0, 14, 14).buildAnimated(200, IDrawableAnimated.StartDirection.TOP, true);
		arrow = helper.drawableBuilder(background, 176, 14, 24, 17).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, AlloyRecipe recipe, IFocusGroup focuses)
	{
		super.setRecipe(builder, recipe, focuses);
		builder.addSlot(RecipeIngredientRole.INPUT, 2, 2)
				.addItemStacks(recipe.input0.getMatchingStackList());
		builder.addSlot(RecipeIngredientRole.INPUT, 30, 2)
				.addItemStacks(recipe.input1.getMatchingStackList());
		builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 20)
				.addItemStack(recipe.output.get());
	}

	@Override
	public void draw(AlloyRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY)
	{
		flame.draw(stack, 18, 21);
		arrow.draw(stack, 47, 20);
	}
}