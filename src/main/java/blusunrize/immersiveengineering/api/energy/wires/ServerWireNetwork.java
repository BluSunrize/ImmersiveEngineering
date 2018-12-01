/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import blusunrize.immersiveengineering.api.energy.wires.AbstractWireNetwork.Connection;

public class ServerWireNetwork extends AbstractWireNetwork<Connection>
{
	public ServerWireNetwork()
	{
		super(connFromNBT);
	}
}
