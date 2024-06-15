/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

public class IEItemInterfaces
{
	public interface IColouredItem
	{
		int getColourForIEItem(ItemStack stack, int pass);
	}

	public interface IAdvancedFluidItem
	{
		int getCapacity(ItemStack stack, int baseCapacity);

		default boolean allowFluid(ItemStack container, FluidStack fluid)
		{
			return true;
		}

		default FluidStack getFluid(ItemStack container)
		{
			Optional<FluidStack> optional = FluidUtils.getFluidContained(container);
			if(optional.isPresent())
				return optional.orElseThrow(RuntimeException::new).copy();
			else
				return null;
		}
	}

	public interface IBulletContainer
	{
		NonNullList<ItemStack> getBullets(ItemStack container);

		int getBulletCount(ItemStack container);
	}

	public interface IScrollwheel
	{
		void onScrollwheel(ItemStack stack, Player playerEntity, boolean forward);
	}
}
