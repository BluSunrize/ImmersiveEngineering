package blusunrize.immersiveengineering.api.energy.wires;

import net.minecraft.item.ItemStack;

/**
 * @author BluSunrize - 26.06.2015
 *
 * An Interface to be implemented by Items that can be used to connect two connectors
 */
public interface IWireCoil
{
	WireType getWireType(ItemStack stack);
}
