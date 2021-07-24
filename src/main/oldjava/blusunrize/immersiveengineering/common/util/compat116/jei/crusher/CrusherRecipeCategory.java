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
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		for(StackWithChance output : recipe.secondaryOutputs)
			if(!output.getStack().isEmpty())
				l.add(output.getStack());
		ingredients.setOutputs(VanillaTypes.ITEM, l);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CrusherRecipe recipe, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 0, 18);
		guiItemStacks.set(0, Arrays.asList(recipe.input.getItems()));
		guiItemStacks.setBackground(0, JEIHelper.slotDrawable);

		List<StackWithChance> validSecondaries = getValidSecondaryOutputs(recipe);
		int y = validSecondaries.isEmpty()?18: validSecondaries.size() < 2?9: 0;
		guiItemStacks.init(1, false, 77, y);
		guiItemStacks.set(1, recipe.output);
		guiItemStacks.setBackground(1, JEIHelper.slotDrawable);

		for(int i = 0; i < validSecondaries.size(); i++)
		{
			guiItemStacks.init(2+i, false, 77+i/2*44, y+18+i%2*18);
			guiItemStacks.set(2+i, validSecondaries.get(i).getStack());
			guiItemStacks.setBackground(2+i, JEIHelper.slotDrawable);
		}
	}

	@Override
	public void draw(CrusherRecipe recipe, PoseStack transform, double mouseX, double mouseY)
	{
		List<StackWithChance> validSecondaries = getValidSecondaryOutputs(recipe);
		int yBase = validSecondaries.isEmpty()?36: validSecondaries.size() < 2?27: 18;
		for(int i = 0; i < validSecondaries.size(); i++)
		{
			int x = 77+i/2*44;
			int y = yBase+i%2*18;
			ClientUtils.font().draw(
					transform,
					Utils.formatDouble(validSecondaries.get(i).getChance()*100, "0.##")+"%",
					x+21,
					y+6,
					0x777777
			);
			RenderSystem.color4f(1, 1, 1, 1);
		}
		transform.pushPose();
		transform.scale(3f, 3f, 1);
		this.getIcon().draw(transform, 8, 0);
		transform.popPose();
	}

	private List<StackWithChance> getValidSecondaryOutputs(CrusherRecipe recipe)
	{
		List<StackWithChance> validSecondaries = new ArrayList<>();
		for(StackWithChance out : recipe.secondaryOutputs)
			if(!out.getStack().isEmpty()&&out.getChance() > 0)
				validSecondaries.add(out);
		return validSecondaries;
	}
}