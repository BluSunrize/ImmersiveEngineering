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
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;

public class CokeOvenRecipeCategory extends IERecipeCategory<CokeOvenRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "cokeoven");
	private final IDrawableStatic tankOverlay;

	public CokeOvenRecipeCategory(IGuiHelper helper)
	{
		super(CokeOvenRecipe.class, helper, UID, "block.immersiveengineering.coke_oven");
		ResourceLocation background = new ResourceLocation(Lib.MODID, "textures/gui/coke_oven.png");
		setBackground(helper.createDrawable(background, 8, 16, 142, 54));
		setIcon(new ItemStack(IEBlocks.Multiblocks.cokeOven));
		tankOverlay = helper.createDrawable(background, 178, 33, 16, 47);
	}

	@Override
	public void setIngredients(CokeOvenRecipe recipe, IIngredients ingredients)
	{
		ingredients.setInputLists(VanillaTypes.ITEM, JEIIngredientStackListBuilder.make(recipe.input).build());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.output);
		ingredients.setOutput(VanillaTypes.FLUID, new FluidStack(IEContent.fluidCreosote, recipe.creosoteOutput));
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CokeOvenRecipe recipe, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 21, 18);
		guiItemStacks.set(0, Arrays.asList(recipe.input.getMatchingStacks()));

		guiItemStacks.init(1, false, 76, 18);
		if(!recipe.output.isEmpty())
			guiItemStacks.set(1, recipe.output);

		if(recipe.creosoteOutput > 0)
		{
			IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
			guiFluidStacks.init(0, false, 121, 4, 16, 47, 5*FluidAttributes.BUCKET_VOLUME, false, tankOverlay);
			guiFluidStacks.set(0, new FluidStack(IEContent.fluidCreosote, recipe.creosoteOutput));
			guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
		}
	}
}