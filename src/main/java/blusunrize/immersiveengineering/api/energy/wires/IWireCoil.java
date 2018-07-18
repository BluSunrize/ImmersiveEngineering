/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
 * @author BluSunrize - 26.06.2015
 * <p>
 * An Interface to be implemented by Items that can be used to connect two connectors
 */
public interface IWireCoil
{
	WireType getWireType(ItemStack stack);

	default boolean canConnectCable(ItemStack stack, TileEntity targetEntity)
	{
		return true;
	}

	default int getMaxLength(ItemStack stack)
	{
		WireType wire = getWireType(stack);
		return wire.getMaxLength();
	}

	default void consumeWire(ItemStack stack, int length)
	{
		stack.shrink(1);
	}
}
