/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author BluSunrize - 26.02.2017
 */
public class IEFluidTooltipCallback implements IRecipeSlotTooltipCallback
{
	@Override
	public void onTooltip(IRecipeSlotView recipeSlotView, List<Component> tooltip)
	{
		Optional<FluidStack> maybeFluid = recipeSlotView.getDisplayedIngredient(ForgeTypes.FLUID_STACK);
		if(maybeFluid.isEmpty())
			return;
		FluidStack ingredient = maybeFluid.get();
		if(ingredient.getFluid() instanceof PotionFluid potion)
		{
			List<Component> fluidInfo = new ArrayList<>();
			potion.addInformation(ingredient, fluidInfo::add);

			if(tooltip.size() > 1)
				tooltip.addAll(1, fluidInfo);
			else
				tooltip.addAll(fluidInfo);
		}
	}
}
