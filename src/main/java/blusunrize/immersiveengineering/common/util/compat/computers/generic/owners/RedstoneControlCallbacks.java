/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;

public class RedstoneControlCallbacks extends Callback<RSState>
{
	@ComputerCallable
	public boolean getEnabled(CallbackEnvironment<RSState> env)
	{
		return env.object().getComputerControlState().isEnabled();
	}

	@ComputerCallable
	public void setEnabled(CallbackEnvironment<RSState> env, boolean enable)
	{
		env.object().getComputerControlState().setEnabled(enable);
	}
}
