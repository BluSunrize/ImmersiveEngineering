/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.squeezer;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

public class SqueezerRecipeCategory extends IERecipeCategory<SqueezerRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "squeezer");
	private final IDrawableStatic tankOverlay;

	public SqueezerRecipeCategory(IGuiHelper helper)
	{
		super(SqueezerRecipe.class, helper, UID, "block.immersiveengineering.squeezer");
		ResourceLocation background = new ResourceLocation(Lib.MODID, "textures/gui/squeezer.png");
		setBackground(helper.createDrawable(background, 6, 12, 164, 59));
		setIcon(new ItemStack(IEBlocks.Multiblocks.squeezer));
		tankOverlay = helper.createDrawable(background, 179, 33, 16, 47);
	}

	@Override
	public void setIngredients(SqueezerRecipe recipe, IIngredients ingredients)
	{
		ingredients.setInputLists(VanillaTypes.ITEM, JEIIngredientStackListBuilder.make(recipe.input).build());
		if(!recipe.itemOutput.isEmpty())
			ingredients.setOutput(VanillaTypes.ITEM, recipe.itemOutput);
		if(recipe.fluidOutput!=null)
			ingredients.setOutput(VanillaTypes.FLUID, recipe.fluidOutput);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SqueezerRecipe recipe, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 1, 22);
		guiItemStacks.init(1, false, 84, 40);
		guiItemStacks.set(0, Arrays.asList(recipe.input.getMatchingStacks()));
		if(!recipe.itemOutput.isEmpty())
			guiItemStacks.set(1, recipe.itemOutput);
		if(recipe.fluidOutput!=null)
		{
			IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
			guiFluidStacks.init(0, false, 106, 9, 16, 47, 500, false, tankOverlay);
			guiFluidStacks.set(0, recipe.fluidOutput);
			guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
		}
	}
}