/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.arcfurnace;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingRecipe;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Arrays;

public class ArcFurnaceRecipeCategory extends IERecipeCategory<ArcFurnaceRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "arcfurnace");
	public static final ResourceLocation UID_RECYCLING = new ResourceLocation(Lib.MODID, "arcfurnace_recycling");

	public ArcFurnaceRecipeCategory(IGuiHelper helper, Class<? extends ArcFurnaceRecipe> recipeClass, ResourceLocation uid)
	{
		super(recipeClass, helper, uid, "block.immersiveengineering.arc_furnace");
		setBackground(helper.createBlankDrawable(140, 54));
		setIcon(helper.createDrawableIngredient(new ItemStack(IEBlocks.Multiblocks.arcFurnace)));
	}

	public static ArcFurnaceRecipeCategory getDefault(IGuiHelper helper)
	{
		return new ArcFurnaceRecipeCategory(helper, ArcFurnaceRecipe.class, UID);
	}

	public static ArcFurnaceRecipeCategory getRecycling(IGuiHelper helper)
	{
		ArcFurnaceRecipeCategory cat = new ArcFurnaceRecipeCategory(helper, ArcRecyclingRecipe.class, UID_RECYCLING);
		cat.localizedName += " - Recycling";
		cat.setIcon(helper.drawableBuilder(new ResourceLocation(Lib.MODID, "textures/gui/recycle.png"), 0, 0, 16, 16).setTextureSize(16, 16).build());
		return cat;
	}

	@Override
	public void setIngredients(ArcFurnaceRecipe recipe, IIngredients ingredients)
	{
		ingredients.setInputLists(VanillaTypes.ITEM, JEIIngredientStackListBuilder.make(recipe.input).add(recipe.additives).build());
		NonNullList<ItemStack> l = ListUtils.fromItems(recipe.output);
		if(recipe.slag==null)
			System.out.println("ERROR ON RECIPE");
		else if(!recipe.slag.isEmpty())
			l.add(recipe.slag);
		ingredients.setOutputs(VanillaTypes.ITEM, l);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, ArcFurnaceRecipe recipe, IIngredients iIngredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		int i = 0;
		guiItemStacks.init(i, true, 20, 0);
		guiItemStacks.set(i++, Arrays.asList(recipe.input.getMatchingStacks()));
		ItemStack simulatedInput = recipe.input.getRandomizedExampleStack(0);

		NonNullList<ItemStack> simulatedAdditives = NonNullList.withSize(recipe.additives.length, ItemStack.EMPTY);
		for(int j = 0; j < recipe.additives.length; j++)
		{
			guiItemStacks.init(i, true, 12+j%2*18, 18+j/2*18);
			guiItemStacks.set(i++, Arrays.asList(recipe.additives[j].getMatchingStacks()));
			simulatedAdditives.set(j, recipe.additives[j].getRandomizedExampleStack(0));
		}

		NonNullList<ItemStack> simulatedOutput = recipe.getOutputs(simulatedInput, simulatedAdditives);
		int outputSize = simulatedOutput.size();
		for(int j = 0; j < outputSize; j++)
		{
			ItemStack out = simulatedOutput.get(j);
			if(out.isEmpty())
				System.out.println("BLU, YOU FUCKED UP");
			else
			{
				int x = 122-(Math.min(outputSize-1, 2)*18)+j%3*18;
				int y = (outputSize > 3?0: 18)+(j/3*18);
				guiItemStacks.init(i, false, x, y);
				guiItemStacks.set(i++, out);
			}
		}
		if(!recipe.slag.isEmpty())
		{
			guiItemStacks.init(i, false, 122, 36);
			guiItemStacks.set(i++, recipe.slag);
		}
	}

	@Override
	public void draw(ArcFurnaceRecipe recipe, PoseStack transform, double mouseX, double mouseY)
	{
		JEIHelper.slotDrawable.draw(transform, 20, 0);
		for(int j = 0; j < 4; j++)
			JEIHelper.slotDrawable.draw(transform, 12+j%2*18, 18+j/2*18);
		for(int j = 0; j < 6; j++)
			JEIHelper.slotDrawable.draw(transform, 86+j%3*18, 0+j/3*18);
		JEIHelper.slotDrawable.draw(transform, 122, 36);

		float time = recipe.getTotalProcessTime();
		float energy = recipe.getTotalProcessEnergy()/time;
		Utils.formatDouble(energy, "#.##");
		String s = I18n.get("desc.immersiveengineering.info.ift", Utils.formatDouble(energy, "#.##"));
		ClientUtils.font().draw(transform, s, 54, 38, 0x777777);
		s = I18n.get("desc.immersiveengineering.info.seconds", Utils.formatDouble(time/20, "#.##"));
		ClientUtils.font().draw(transform, s, 54, 48, 0x777777);
	}
}