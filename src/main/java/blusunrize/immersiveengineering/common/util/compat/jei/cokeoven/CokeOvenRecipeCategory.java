/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.cokeoven;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import java.util.Arrays;

public class CokeOvenRecipeCategory extends IERecipeCategory<CokeOvenRecipe>
{
	private final IDrawableStatic tankOverlay;
	private final IDrawableAnimated flame;

	public CokeOvenRecipeCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.COKE_OVEN, "block.immersiveengineering.coke_oven");
		ResourceLocation background = new ResourceLocation(Lib.MODID, "textures/gui/coke_oven.png");
		setBackground(helper.createDrawable(background, 26, 16, 123, 55));
		setIcon(IEMultiblockLogic.COKE_OVEN.iconStack());
		tankOverlay = helper.createDrawable(background, 178, 33, 16, 47);
		flame = helper.drawableBuilder(background, 177, 0, 14, 14).buildAnimated(500, IDrawableAnimated.StartDirection.TOP, true);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CokeOvenRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 4, 19)
				.addItemStacks(Arrays.asList(recipe.input.getMatchingStacks()));

		IRecipeSlotBuilder outputSlotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 19);
		if(!recipe.output.get().isEmpty())
			outputSlotBuilder.addItemStack(recipe.output.get());

		if(recipe.creosoteOutput > 0){
			int tankSize = Math.max(FluidType.BUCKET_VOLUME,  recipe.creosoteOutput);
			builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 4)
					.setFluidRenderer(tankSize, false, 16, 47)
					.setOverlay(tankOverlay, 0, 0)
					.addIngredient(ForgeTypes.FLUID_STACK, new FluidStack(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput))
					.addTooltipCallback(JEIHelper.fluidTooltipCallback);
		}
	}

	@Override
	public void draw(CokeOvenRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		flame.draw(graphics, 31, 20);
	}
}