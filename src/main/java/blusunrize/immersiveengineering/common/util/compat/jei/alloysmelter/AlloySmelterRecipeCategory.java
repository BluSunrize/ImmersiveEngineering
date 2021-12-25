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
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Arrays;

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
		setIcon(new ItemStack(IEBlocks.Multiblocks.alloySmelter));
		flame = helper.drawableBuilder(background, 177, 0, 14, 14).buildAnimated(200, IDrawableAnimated.StartDirection.TOP, true);
		arrow = helper.drawableBuilder(background, 176, 14, 24, 17).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public void setIngredients(AlloyRecipe recipe, IIngredients ingredients)
	{
		ingredients.setInputLists(VanillaTypes.ITEM, JEIIngredientStackListBuilder.make(recipe.input0, recipe.input1).build());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.output);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AlloyRecipe recipe, IIngredients iIngredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 1, 1);
		guiItemStacks.set(0, Arrays.asList(recipe.input0.getMatchingStacks()));

		guiItemStacks.init(1, false, 29, 1);
		guiItemStacks.set(1, Arrays.asList(recipe.input1.getMatchingStacks()));

		guiItemStacks.init(2, false, 83, 19);
		guiItemStacks.set(2, recipe.output);
	}

	@Override
	public void draw(AlloyRecipe recipe, PoseStack poseStack, double mouseX, double mouseY)
	{
		flame.draw(poseStack, 18, 21);
		arrow.draw(poseStack, 47, 20);
	}
}