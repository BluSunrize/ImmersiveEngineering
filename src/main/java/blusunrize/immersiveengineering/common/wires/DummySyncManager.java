/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.IWireSyncManager;

public class DummySyncManager implements IWireSyncManager
{
	public static final IWireSyncManager INSTANCE = new DummySyncManager();

	private DummySyncManager()
	{
	}

	@Override
	public void onConnectionAdded(Connection c)
	{
	}

	@Override
	public void onConnectionRemoved(Connection c)
	{
	}
}
