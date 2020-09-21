/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.sawmill;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

public class SawmillRecipeCategory extends IERecipeCategory<SawmillRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "sawmill");

	public SawmillRecipeCategory(IGuiHelper helper)
	{
		super(SawmillRecipe.class, helper, UID, "block.immersiveengineering.sawmill");
		setBackground(helper.createBlankDrawable(140, 54));
		setIcon(new ItemStack(IEBlocks.Multiblocks.sawmill));
	}

	@Override
	public void setIngredients(SawmillRecipe recipe, IIngredients ingredients)
	{
		ingredients.setInputLists(VanillaTypes.ITEM, JEIIngredientStackListBuilder.make(recipe.input).build());
		NonNullList<ItemStack> l = ListUtils.fromItems(recipe.output);
		if(!recipe.stripped.isEmpty())
			l.add(recipe.stripped);
		l.addAll(recipe.secondaryStripping);
		l.addAll(recipe.secondaryOutputs);
		ingredients.setOutputs(VanillaTypes.ITEM, l);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SawmillRecipe recipe, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 12);
		guiItemStacks.set(0, Arrays.asList(recipe.input.getMatchingStacks()));
		guiItemStacks.setBackground(0, JEIHelper.slotDrawable);

		int slot = 1;
		if(!recipe.stripped.isEmpty())
		{
			guiItemStacks.init(slot, false, 52, 12);
			guiItemStacks.set(slot, recipe.stripped);
			guiItemStacks.setBackground(slot++, JEIHelper.slotDrawable);
		}

		guiItemStacks.init(slot, false, 96, 12);
		guiItemStacks.set(slot, recipe.output);
		guiItemStacks.setBackground(slot++, JEIHelper.slotDrawable);

		int i = 0;
		for(ItemStack out : recipe.secondaryStripping)
		{
			guiItemStacks.init(slot, false, 52+i%2*18, 30+i/2*18);
			guiItemStacks.set(slot, out);
			guiItemStacks.setBackground(slot++, JEIHelper.slotDrawable);
		}

		i = 0;
		for(ItemStack out : recipe.secondaryOutputs)
		{
			guiItemStacks.init(slot, false, 96+i%2*18, 30+i/2*18);
			guiItemStacks.set(slot, out);
			guiItemStacks.setBackground(slot++, JEIHelper.slotDrawable);
		}
	}

	@Override
	public void draw(SawmillRecipe recipe, double mouseX, double mouseY)
	{
		RenderSystem.pushMatrix();
		RenderSystem.scalef(3f, 3f, 1);
		this.getIcon().draw(8, 4);
		RenderSystem.popMatrix();
	}

}