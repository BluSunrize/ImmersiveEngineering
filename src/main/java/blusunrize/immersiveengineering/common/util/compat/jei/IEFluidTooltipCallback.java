/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.common.util.IEFluid;
import mezz.jei.api.gui.ITooltipCallback;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BluSunrize - 26.02.2017
 */
public class IEFluidTooltipCallback implements ITooltipCallback<FluidStack>
{
	@Override
	public void onTooltip(int slotIndex, boolean input, FluidStack ingredient, List<String> tooltip)
	{
		if(ingredient!=null&&ingredient.getFluid() instanceof IEFluid)
		{
			ArrayList<String> fluidInfo = new ArrayList<String>();
			((IEFluid)ingredient.getFluid()).addTooltipInfo(ingredient, null, fluidInfo);
			if(tooltip.size() > 1)
				tooltip.addAll(1, fluidInfo);
			else
				tooltip.addAll(fluidInfo);
		}
	}
}
