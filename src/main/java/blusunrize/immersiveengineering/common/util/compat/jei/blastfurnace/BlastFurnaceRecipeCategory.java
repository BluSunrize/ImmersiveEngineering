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
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.util.ListUtils;
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

public class BlastFurnaceRecipeCategory extends IERecipeCategory<BlastFurnaceRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "blastfurnace");
	private final IDrawableAnimated flame;
	private final IDrawableAnimated arrow;

	public BlastFurnaceRecipeCategory(IGuiHelper helper)
	{
		super(BlastFurnaceRecipe.class, helper, UID, "gui.immersiveengineering.blastFurnace");
		ResourceLocation background = new ResourceLocation(Lib.MODID, "textures/gui/blast_furnace.png");
		setBackground(helper.createDrawable(background, 42, 9, 100, 64));
		setIcon(helper.createDrawableIngredient(new ItemStack(IEBlocks.Multiblocks.blastFurnace)));
		flame = helper.drawableBuilder(background, 177, 0, 14, 14).buildAnimated(300, IDrawableAnimated.StartDirection.TOP, true);
		arrow = helper.drawableBuilder(background, 176, 14, 24, 17).buildAnimated(300, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public void setIngredients(BlastFurnaceRecipe recipe, IIngredients ingredients)
	{
		ingredients.setInputLists(VanillaTypes.ITEM, JEIIngredientStackListBuilder.make(recipe.input).build());
		ingredients.setOutputs(VanillaTypes.ITEM, ListUtils.fromItems(recipe.output, recipe.slag));
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BlastFurnaceRecipe recipe, IIngredients iIngredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 9, 7);
		guiItemStacks.init(1, false, 69, 7);
		guiItemStacks.init(2, false, 69, 43);
		guiItemStacks.set(0, Arrays.asList(recipe.input.getMatchingStacks()));
		guiItemStacks.set(1, recipe.output);
		guiItemStacks.set(2, recipe.slag);
	}

	@Override
	public void draw(BlastFurnaceRecipe recipe, PoseStack poseStack, double mouseX, double mouseY)
	{
		flame.draw(poseStack, 12, 27);
		arrow.draw(poseStack, 33, 26);
	}
}