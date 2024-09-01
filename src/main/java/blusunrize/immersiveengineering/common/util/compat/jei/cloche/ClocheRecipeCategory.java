/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.cloche;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ClocheRecipeCategory extends IERecipeCategory<ClocheRecipe>
{
	private final IDrawableStatic tankOverlay;
	private final IDrawableAnimated arrow;

	public ClocheRecipeCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.CLOCHE, "block.immersiveengineering.cloche");
		ResourceLocation background = IEApi.ieLoc("textures/gui/cloche.png");
		setBackground(helper.createDrawable(background, 0, 0, 176, 77));
		setIcon(new ItemStack(IEBlocks.MetalDevices.CLOCHE));
		tankOverlay = helper.createDrawable(background, 176, 30, 20, 51);
		arrow = helper.drawableBuilder(background, 181, 1, 13, 13).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ClocheRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 62, 34)
				.addItemStacks(Arrays.asList(recipe.seed.getItems()));

		builder.addSlot(RecipeIngredientRole.INPUT, 62, 54)
				.addItemStacks(Arrays.asList(recipe.soil.getItems()));

		NonNullList<ItemStack> outputs = recipe.outputs.get();
		for(int i = 0; i < outputs.size(); i++)
			builder.addSlot(RecipeIngredientRole.OUTPUT, 116+i%2*18, 34+i/2*18)
					.addItemStack(outputs.get(i));

		builder.addSlot(RecipeIngredientRole.INPUT, 6, 6)
				.setFluidRenderer(4000, false, 20, 51)
				.setOverlay(tankOverlay, 0, 0)
				.addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(Fluids.WATER, 4000))
				.addRichTooltipCallback(JEIHelper.fluidTooltipCallback);

		// TODO: Fix this to not be this unperformant and just bad, if there is a better way to do it
		ArrayList<ItemStack> fertilizers = new ArrayList<>(Collections.singleton(ItemStack.EMPTY));
		for(RecipeHolder<ClocheFertilizer> fertilizerList : ClocheFertilizer.RECIPES.getRecipes(Minecraft.getInstance().level))
			fertilizers.addAll(Arrays.stream(fertilizerList.value().input.getItems()).toList());

		builder.addSlot(RecipeIngredientRole.INPUT, 8, 59)
				.addItemStacks(fertilizers);
	}

	@Override
	public void draw(ClocheRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		arrow.draw(graphics, 101, 35);
	}
}