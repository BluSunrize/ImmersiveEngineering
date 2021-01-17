/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.redstone;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;

/**
 * Implement this interface to allow a block to interface with IE redstone wires
 */
public interface IRedstoneConnector extends IImmersiveConnectable
{
	/**
	 * Called whenever the RedstoneWireNetwork is changed in some way (both adding/removing connectors and changes in RS values).
	 */
	void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler);

	/**
	 * Called when the RedstoneWireNetwork updates its RS input values.
	 * As a general rule only stronger signals should override weaker signals, so you should never decrease the value of
	 * a channel in this method.
	 *
	 * @param signals the values of the RS channels up to this point. Modify this array to change output values.
	 */
	void updateInput(byte[] signals, ConnectionPoint cp);
}
