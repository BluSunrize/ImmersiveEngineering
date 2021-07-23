/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.fluids;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author BluSunrize - 31.07.2016
 */
public class IEItemFluidHandler extends FluidHandlerItemStack
{
	public static Component fluidItemInfoFlavor(@Nullable FluidStack fluid, int fluidCapacity)
	{
		if(fluid!=null&&fluid.getAmount() > 0)
		{
			FluidAttributes attr = fluid.getFluid().getAttributes();
			ChatFormatting rarity = attr.getRarity()==Rarity.COMMON?ChatFormatting.GRAY: attr.getRarity().color;
			return TextUtils.applyFormat(
					new TranslatableComponent(Lib.DESC_FLAVOUR+"fluidStack", fluid.getDisplayName(), fluid.getAmount(), fluidCapacity),
					rarity
			);
		}
		return TextUtils.applyFormat(
				new TranslatableComponent(Lib.DESC_FLAVOUR+"drill.empty"),
				ChatFormatting.GRAY
		);
	}

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
	public boolean isFluidValid(int tank, @Nonnull FluidStack fluid)
	{
		FluidStack tankFluid = getFluidInTank(tank);
		return (tankFluid.isEmpty()&&this.canFillFluidType(fluid))||tankFluid.isFluidEqual(fluid);
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

	@Override
	public int getTankCapacity(int tank)
	{
		return getCapacity();
	}
}
