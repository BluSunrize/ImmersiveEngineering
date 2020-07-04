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
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;

public class ClocheRecipeCategory extends IERecipeCategory<ClocheRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "cloche");

	public ClocheRecipeCategory(IGuiHelper helper)
	{
		super(ClocheRecipe.class, helper, UID, "block.immersiveengineering.cloche");
		setBackground(helper.createBlankDrawable(100, 50));
		setIcon(new ItemStack(IEBlocks.MetalDevices.cloche));
	}

	@Override
	public void setIngredients(ClocheRecipe recipe, IIngredients ingredients)
	{
		List<List<ItemStack>> l = JEIIngredientStackListBuilder.make(recipe.seed).add(recipe.soil).build();
		ingredients.setInputLists(VanillaTypes.ITEM, l);
		ingredients.setOutputs(VanillaTypes.ITEM, recipe.outputs);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, ClocheRecipe recipe, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 4, 6);
		guiItemStacks.set(0, Arrays.asList(recipe.seed.getMatchingStacks()));
		guiItemStacks.setBackground(0, JEIHelper.slotDrawable);

		guiItemStacks.init(1, true, 4, 30);
		guiItemStacks.set(1, Arrays.asList(recipe.soil.getMatchingStacks()));
		guiItemStacks.setBackground(1, JEIHelper.slotDrawable);

		for(int i = 0; i < recipe.outputs.size(); i++)
		{
			guiItemStacks.init(2+i, false, 64+i%2*18, 12+i/2*18);
			guiItemStacks.set(2+i, recipe.outputs.get(i));
			guiItemStacks.setBackground(2+i, JEIHelper.slotDrawable);
		}
	}

	@Override
	public void draw(ClocheRecipe recipe, MatrixStack transform, double mouseX, double mouseY)
	{
		transform.push();
		transform.scale(3, 3, 1);
		this.getIcon().draw(transform, 7, 0);
		transform.pop();
	}
}