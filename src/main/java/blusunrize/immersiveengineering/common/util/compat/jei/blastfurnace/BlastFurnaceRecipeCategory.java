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
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

public class BlastFurnaceRecipeCategory extends IERecipeCategory<BlastFurnaceRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "blastfurnace");

	public BlastFurnaceRecipeCategory(IGuiHelper helper)
	{
		super(BlastFurnaceRecipe.class, helper, UID, "gui.immersiveengineering.blastFurnace");
		setBackground(helper.createDrawable(new ResourceLocation(Lib.MODID, "textures/gui/blast_furnace.png"), 8, 8, 142, 65));
		setIcon(helper.createDrawableIngredient(new ItemStack(IEBlocks.Multiblocks.blastFurnace)));
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
		guiItemStacks.init(0, true, 43, 8);
		guiItemStacks.init(1, false, 103, 8);
		guiItemStacks.init(2, false, 103, 44);
		guiItemStacks.set(0, Arrays.asList(recipe.input.getMatchingStacks()));
		guiItemStacks.set(1, recipe.output);
		guiItemStacks.set(2, recipe.slag);
	}
}