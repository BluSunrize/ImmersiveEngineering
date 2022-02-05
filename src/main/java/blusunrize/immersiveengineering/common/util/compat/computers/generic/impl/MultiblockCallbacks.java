/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;

public class MultiblockCallbacks extends Callback<MultiblockPartBlockEntity<?>>
{
	public static final MultiblockCallbacks INSTANCE = new MultiblockCallbacks();

	@ComputerCallable
	public boolean isRunning(CallbackEnvironment<PoweredMultiblockBlockEntity<?, ?>> env)
	{
		return env.object().shouldRenderAsActive();
	}

	@ComputerCallable
	public void setEnabled(CallbackEnvironment<PoweredMultiblockBlockEntity<?, ?>> env, boolean enable)
	{
		env.object().computerControl.setEnabled(enable);
	}
}
