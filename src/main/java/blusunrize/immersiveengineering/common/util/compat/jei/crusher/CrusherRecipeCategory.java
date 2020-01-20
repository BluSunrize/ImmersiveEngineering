/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.crusher;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class CrusherRecipeCategory extends IERecipeCategory<CrusherRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "crusher");

	public CrusherRecipeCategory(IGuiHelper helper)
	{
		super(CrusherRecipe.class, helper, UID, "block.immersiveengineering.crusher");
		setBackground(helper.createBlankDrawable(140, 54));
		setIcon(new ItemStack(IEBlocks.Multiblocks.crusher));
	}

	@Override
	public void setIngredients(CrusherRecipe recipe, IIngredients ingredients)
	{
		ingredients.setInputLists(VanillaTypes.ITEM, JEIIngredientStackListBuilder.make(recipe.input).build());
		NonNullList<ItemStack> l = ListUtils.fromItems(recipe.output);
		if(recipe.secondaryOutput!=null)
			for(ItemStack stack : recipe.secondaryOutput)
				if(!stack.isEmpty())
					l.add(stack);
		ingredients.setOutputs(VanillaTypes.ITEM, l);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CrusherRecipe recipe, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 18);
		guiItemStacks.set(0, recipe.input.getSizedStackList());
		guiItemStacks.setBackground(0, JEIHelper.slotDrawable);

		int y = recipe.secondaryOutput==null||recipe.secondaryOutput.length==0?18: recipe.secondaryOutput.length < 2?9: 0;
		guiItemStacks.init(1, false, 77, y);
		guiItemStacks.set(1, recipe.output);
		guiItemStacks.setBackground(1, JEIHelper.slotDrawable);

		if(recipe.secondaryOutput!=null)
			for(int i = 0; i < recipe.secondaryOutput.length; i++)
			{
				guiItemStacks.init(2+i, false, 77+i/2*44, y+18+i%2*18);
				guiItemStacks.set(2+i, recipe.secondaryOutput[i]);
				guiItemStacks.setBackground(2+i, JEIHelper.slotDrawable);
			}
	}

	@Override
	public void draw(CrusherRecipe recipe, double mouseX, double mouseY)
	{
		int yBase = recipe.secondaryOutput==null||recipe.secondaryOutput.length==0?36: recipe.secondaryOutput.length < 2?27: 18;
		if(recipe.secondaryOutput!=null)
			for(int i = 0; i < recipe.secondaryOutput.length; i++)
			{
				int x = 77+i/2*44;
				int y = yBase+i%2*18;
				if(i < recipe.secondaryChance.length)
				{
					ClientUtils.font().drawString(Utils.formatDouble(recipe.secondaryChance[i]*100, "0.##")+"%", x+21, y+6, 0x777777);
					GlStateManager.color4f(1, 1, 1, 1);
				}
			}
		GlStateManager.pushMatrix();
		GlStateManager.scalef(3f, 3f, 1);
		this.getIcon().draw(8, 0);
		GlStateManager.popMatrix();
	}
}