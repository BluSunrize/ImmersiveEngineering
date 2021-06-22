/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BluSunrize - 26.02.2017
 */
public class IEFluidTooltipCallback implements ITooltipCallback<FluidStack>
{
	@Override
	public void onTooltip(int slotIndex, boolean input, FluidStack ingredient, List<ITextComponent> tooltip)
	{
		if(ingredient.getFluid() instanceof PotionFluid)
		{
			List<ITextComponent> fluidInfo = new ArrayList<>();
			((PotionFluid)ingredient.getFluid()).addInformation(ingredient, fluidInfo);

			if(tooltip.size() > 1)
				tooltip.addAll(1, fluidInfo);
			else
				tooltip.addAll(fluidInfo);
		}
	}
}
