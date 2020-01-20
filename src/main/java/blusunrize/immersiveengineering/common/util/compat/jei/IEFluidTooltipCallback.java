/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.common.util.fluids.IEFluid;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
			ArrayList<ITextComponent> fluidInfo = new ArrayList<ITextComponent>();
			((IEFluid)ingredient.getFluid()).addTooltipInfo(ingredient, null, fluidInfo);

			List<String> tooltipAppend = fluidInfo.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList());
			if(tooltip.size() > 1)
				tooltip.addAll(1, tooltipAppend);
			else
				tooltip.addAll(tooltipAppend);
		}
	}
}
