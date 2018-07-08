/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires.redstone;

import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import net.minecraft.world.World;

/**
 * Implement this interface to allow a block to interface with IE redstone wires
 */
public interface IRedstoneConnector extends IImmersiveConnectable
{
	/**
	 * Sets the RedstoneWireNetwork this connector is connected to. DO NOT update output values in here, all output values will be updated in batch.
	 *
	 * @param net the new network for this connector.
	 */
	void setNetwork(RedstoneWireNetwork net);

	/**
	 * @return the RedstoneWireNetwork this conector is connected to.
	 */
	RedstoneWireNetwork getNetwork();

	/**
	 * Called whenever the RedstoneWireNetwork is changed in some way (both adding/removing connectors and changes in RS values).
	 */
	void onChange();

	/**
	 * @return the world that this connector is in
	 */
	World getConnectorWorld();

	/**
	 * Called when the RedstoneWireNetwork updates its RS input values.
	 * As a general rule only stronger signals should override weaker signals, so you should never decrease the value of a channel in this method.
	 *
	 * @param signals the values of the RS channels up to this point. Modify this array to change output values.
	 */
	void updateInput(byte[] signals);
}
