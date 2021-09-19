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
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Arrays;

public class AlloySmelterRecipeCategory extends IERecipeCategory<AlloyRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "alloysmelter");

	public AlloySmelterRecipeCategory(IGuiHelper helper)
	{
		super(AlloyRecipe.class, helper, UID, "block.immersiveengineering.alloy_smelter");
		setBackground(helper.createDrawable(new ResourceLocation(Lib.MODID, "textures/gui/alloy_smelter.png"), 32, 16, 116, 54));
		setIcon(new ItemStack(IEBlocks.Multiblocks.alloySmelter));
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
		guiItemStacks.init(0, true, 5, 0);
		guiItemStacks.set(0, Arrays.asList(recipe.input0.getMatchingStacks()));

		guiItemStacks.init(1, false, 33, 0);
		guiItemStacks.set(1, Arrays.asList(recipe.input1.getMatchingStacks()));

		guiItemStacks.init(2, false, 87, 18);
		guiItemStacks.set(2, recipe.output);
	}
}