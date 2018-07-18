/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

/**
 * @author BluSunrize - 31.07.2016
 */
public class IEItemFluidHandler extends FluidHandlerItemStack
{
	public IEItemFluidHandler(ItemStack container, int capacity)
	{
		super(container, capacity);
	}

	public int getCapacity()
	{
		if(container.getItem() instanceof IAdvancedFluidItem)
			return ((IAdvancedFluidItem)container.getItem()).getCapacity(container, capacity);
		return capacity;
	}

	@Override
	public boolean canFillFluidType(FluidStack fluid)
	{
		if(container.getItem() instanceof IAdvancedFluidItem)
			return ((IAdvancedFluidItem)container.getItem()).allowFluid(container, fluid);
		return true;
	}

	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		return new FluidTankProperties[]{new FluidTankProperties(getFluid(), getCapacity())};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		if(container.getCount()!=1||resource==null||resource.amount <= 0||!canFillFluidType(resource))
			return 0;

		FluidStack contained = getFluid();
		if(contained==null)
		{
			int fillAmount = Math.min(getCapacity(), resource.amount);
			if(doFill)
			{
				FluidStack filled = resource.copy();
				filled.amount = fillAmount;
				setFluid(filled);
			}
			return fillAmount;
		}
		else
		{
			if(contained.isFluidEqual(resource))
			{
				int fillAmount = Math.min(getCapacity()-contained.amount, resource.amount);
				if(doFill&&fillAmount > 0)
				{
					contained.amount += fillAmount;
					setFluid(contained);
				}
				return fillAmount;
			}
			return 0;
		}
	}
}
