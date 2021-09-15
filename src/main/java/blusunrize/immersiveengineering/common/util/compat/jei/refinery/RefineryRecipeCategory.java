/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.refinery;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class RefineryRecipeCategory extends IERecipeCategory<RefineryRecipe>
{
	public static final ResourceLocation UID = new ResourceLocation(Lib.MODID, "refinery");
	private final IDrawableStatic tankOverlay;

	public RefineryRecipeCategory(IGuiHelper helper)
	{
		super(RefineryRecipe.class, helper, UID, "block.immersiveengineering.refinery");
		ResourceLocation background = new ResourceLocation(Lib.MODID, "textures/gui/refinery.png");
		setBackground(helper.createDrawable(background, 6, 10, 164, 62));
		setIcon(new ItemStack(IEBlocks.Multiblocks.REFINERY));
		tankOverlay = helper.createDrawable(background, 179, 33, 16, 47);
	}

	@Override
	public void setIngredients(RefineryRecipe recipe, IIngredients ingredients)
	{
		List<List<FluidStack>> l = new ArrayList<>();
		if(recipe.input0!=null)
			l.add(recipe.input0.getMatchingFluidStacks());
		if(recipe.input1!=null)
			l.add(recipe.input1.getMatchingFluidStacks());
		ingredients.setInputLists(VanillaTypes.FLUID, l);
		ingredients.setOutput(VanillaTypes.FLUID, recipe.output);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, RefineryRecipe recipe, IIngredients ingredients)
	{
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		if(recipe.input0!=null)
		{
			guiFluidStacks.init(0, true, 7, 10, 16, 47, FluidAttributes.BUCKET_VOLUME/20, false, tankOverlay);
			guiFluidStacks.set(0, recipe.input0.getMatchingFluidStacks());
		}
		if(recipe.input1!=null)
		{
			guiFluidStacks.init(1, true, 55, 10, 16, 47, FluidAttributes.BUCKET_VOLUME/20, false, tankOverlay);
			guiFluidStacks.set(1, recipe.input1.getMatchingFluidStacks());
		}
		guiFluidStacks.init(2, false, 103, 10, 16, 47, FluidAttributes.BUCKET_VOLUME/20, false, tankOverlay);
		guiFluidStacks.set(2, recipe.output);
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}
}