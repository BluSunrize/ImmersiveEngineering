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
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingRecipe;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ArcFurnaceRecipeCategory extends IERecipeCategory<ArcFurnaceRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "arcfurnace");
	public static final ResourceLocation UID_RECYCLING = new ResourceLocation(Lib.MODID, "arcfurnace_recycling");
	private final IDrawableStatic arrow;

	public ArcFurnaceRecipeCategory(IGuiHelper helper, Class<? extends ArcFurnaceRecipe> recipeClass, ResourceLocation uid)
	{
		super(recipeClass, helper, uid, "block.immersiveengineering.arc_furnace");
		setBackground(helper.createBlankDrawable(148, 54));
		setIcon(helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(IEBlocks.Multiblocks.ARC_FURNACE)));
		arrow = helper.drawableBuilder(JEIHelper.JEI_GUI, 19, 4, 24, 18).setTextureSize(128, 128).build();
	}

	public static ArcFurnaceRecipeCategory getDefault(IGuiHelper helper)
	{
		return new ArcFurnaceRecipeCategory(helper, ArcFurnaceRecipe.class, UID);
	}

	public static ArcFurnaceRecipeCategory getRecycling(IGuiHelper helper)
	{
		ArcFurnaceRecipeCategory cat = new ArcFurnaceRecipeCategory(helper, ArcRecyclingRecipe.class, UID_RECYCLING);
		cat.title.append(" - Recycling");
		cat.setIcon(helper.drawableBuilder(new ResourceLocation(Lib.MODID, "textures/gui/recycle.png"), 0, 0, 16, 16).setTextureSize(16, 16).build());
		return cat;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ArcFurnaceRecipe recipe, IFocusGroup focuses)
	{
		super.setRecipe(builder, recipe, focuses);
		int x = (148-getWidth(recipe))/2+1;

		builder.addSlot(RecipeIngredientRole.INPUT, x+8, 1)
				.addItemStacks(recipe.input.getMatchingStackList());

		for(int j = 0; j < recipe.additives.length; j++)
			builder.addSlot(RecipeIngredientRole.INPUT, x+j%2*18, 19+j/2*18)
					.addItemStacks(recipe.additives[j].getMatchingStackList());

		NonNullList<ItemStack> simulatedOutput = recipe.getBaseOutputs();
		int outputSize = simulatedOutput.size();
		for(int j = 0; j < outputSize; j++)
			builder.addSlot(RecipeIngredientRole.OUTPUT, x+68+j%2*18, j/2*18+1)
					.addItemStack(simulatedOutput.get(j));

		int xSecondary = x+(outputSize > 1?106: 88);
		for(int j = 0; j < recipe.secondaryOutputs.size(); j++)
			builder.addSlot(RecipeIngredientRole.OUTPUT, xSecondary, j*18+1)
					.addItemStack(recipe.secondaryOutputs.get(j).stack().get());

		if(!recipe.slag.get().isEmpty())
			builder.addSlot(RecipeIngredientRole.OUTPUT, x+68, 37)
					.addItemStack(recipe.slag.get());
	}

	private int getWidth(ArcFurnaceRecipe recipe)
	{
		int w = 86;
		if(recipe.getBaseOutputs().size() > 1)
			w += 18;
		if(recipe.secondaryOutputs.size() > 0)
			w += 40;
		return w;
	}

	@Override
	public void draw(ArcFurnaceRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack transform, double mouseX, double mouseY)
	{
		int x = (148-getWidth(recipe))/2;
		arrow.draw(transform, x+40, 10);

		JEIHelper.slotDrawable.draw(transform, x+8, 0);
		for(int j = 0; j < 4; j++)
			JEIHelper.slotDrawable.draw(transform, x+j%2*18, 18+j/2*18);
		for(int j = 0; j < recipe.getBaseOutputs().size(); j++)
			JEIHelper.slotDrawable.draw(transform, x+68+j%2*18, j/2*18);

		int xSecondary = x+(recipe.getBaseOutputs().size() > 1?106: 88);
		for(int j = 0; j < recipe.secondaryOutputs.size(); j++)
		{
			JEIHelper.slotDrawable.draw(transform, xSecondary, j*18);
			ClientUtils.font().draw(
					transform,
					Utils.formatDouble(recipe.secondaryOutputs.get(j).chance()*100, "0.##")+"%",
					xSecondary+20,
					j*18+6,
					0x777777
			);
		}
		JEIHelper.slotDrawable.draw(transform, x+68, 36);
	}

	@Override
	public List<Component> getTooltipStrings(ArcFurnaceRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY)
	{
		int x = (148-getWidth(recipe))/2;
		if(mouseX >= x+40&&mouseX <= x+64&&mouseY >= 8&&mouseY <= 26)
		{
			float time = recipe.getTotalProcessTime();
			float energy = recipe.getTotalProcessEnergy()/time;
			Utils.formatDouble(energy, "#.##");
			return Arrays.asList(
					Component.translatable("desc.immersiveengineering.info.ift", Utils.formatDouble(energy, "#.##")),
					Component.translatable("desc.immersiveengineering.info.seconds", Utils.formatDouble(time/20, "#.##"))
			);
		}
		return super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
	}
}