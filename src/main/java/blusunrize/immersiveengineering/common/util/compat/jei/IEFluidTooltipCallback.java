/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author BluSunrize - 26.02.2017
 */
public class IEFluidTooltipCallback implements IRecipeSlotRichTooltipCallback
{
	@Override
	public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip)
	{
		Optional<FluidStack> maybeFluid = recipeSlotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK);
		if(maybeFluid.isEmpty())
			return;
		FluidStack ingredient = maybeFluid.get();
		if(ingredient.getFluid() instanceof PotionFluid potion)
		{
			List<Component> fluidInfo = new ArrayList<>();
			potion.addInformation(ingredient, fluidInfo::add);

			tooltip.addAll(fluidInfo);
		}
	}
}
