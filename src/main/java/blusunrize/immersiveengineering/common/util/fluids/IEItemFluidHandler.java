/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fluids;

import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
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
	public int fill(FluidStack resource, FluidAction doFill)
	{
		if(container.getCount()!=1||resource.isEmpty()||!canFillFluidType(resource))
			return 0;

		FluidStack contained = getFluid();
		if(contained.isEmpty())
		{
			int fillAmount = Math.min(getCapacity(), resource.getAmount());
			if(doFill.execute())
			{
				FluidStack filled = resource.copy();
				filled.setAmount(fillAmount);
				setFluid(filled);
			}
			return fillAmount;
		}
		else
		{
			if(contained.isFluidEqual(resource))
			{
				int fillAmount = Math.min(getCapacity()-contained.getAmount(), resource.getAmount());
				if(doFill.execute()&&fillAmount > 0)
				{
					contained.grow(fillAmount);
					setFluid(contained);
				}
				return fillAmount;
			}
			return 0;
		}
	}
}
