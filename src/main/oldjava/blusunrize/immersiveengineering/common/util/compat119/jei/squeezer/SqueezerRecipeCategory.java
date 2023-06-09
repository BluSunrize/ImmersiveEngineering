/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.squeezer;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidType;

import java.util.Arrays;

public class SqueezerRecipeCategory extends IERecipeCategory<SqueezerRecipe>
{
	private final IDrawableStatic tankOverlay;

	public SqueezerRecipeCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.SQUEEZER, "block.immersiveengineering.squeezer");
		ResourceLocation background = new ResourceLocation(Lib.MODID, "textures/gui/squeezer.png");
		setBackground(helper.createDrawable(background, 6, 12, 126, 59));
		setIcon(IEMultiblockLogic.SQUEEZER.iconStack());
		tankOverlay = helper.createDrawable(background, 179, 33, 16, 47);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, SqueezerRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 2, 23)
				.addItemStacks(Arrays.asList(recipe.input.getMatchingStacks()));
		IRecipeSlotBuilder outputBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 41);
		if(!recipe.itemOutput.get().isEmpty())
			outputBuilder.addItemStack(recipe.itemOutput.get());
		if(recipe.fluidOutput!=null&&!recipe.fluidOutput.isEmpty())
		{
			int tankSize = Math.max(FluidType.BUCKET_VOLUME/4, recipe.fluidOutput.getAmount());
			builder.addSlot(RecipeIngredientRole.OUTPUT, 106, 9)
					.setFluidRenderer(tankSize, false, 16, 47)
					.setOverlay(tankOverlay, 0, 0)
					.addIngredient(ForgeTypes.FLUID_STACK, recipe.fluidOutput)
					.addTooltipCallback(JEIHelper.fluidTooltipCallback);
		}
	}
}