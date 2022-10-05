/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;

public class MultiblockCallbacks extends Callback<MultiblockPartBlockEntity<?>>
{
	public static final MultiblockCallbacks INSTANCE = new MultiblockCallbacks();

	@ComputerCallable
	public boolean getEnabled(CallbackEnvironment<MultiblockPartBlockEntity<?>> env)
	{
		return env.beforePreprocess().computerControl.isEnabled();
	}

	@ComputerCallable
	public void setEnabled(CallbackEnvironment<MultiblockPartBlockEntity<?>> env, boolean enable)
	{
		// This has to run on the BE the computer is attached to, since the computerControl object needs to see all
		// attach/detach calls, which won't work with the master BE in a potentially unloaded chunk
		env.beforePreprocess().computerControl.setEnabled(enable);
	}
}
