package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControlState;

public class MultiblockCallbacks extends Callback<MultiblockPartTileEntity<?>>
{
	public static final MultiblockCallbacks INSTANCE = new MultiblockCallbacks();

	@ComputerCallable
	public boolean isRunning(CallbackEnvironment<PoweredMultiblockTileEntity<?, ?>> env)
	{
		return env.getObject().shouldRenderAsActive();
	}

	@ComputerCallable
	public void setEnabled(CallbackEnvironment<PoweredMultiblockTileEntity<?, ?>> env, boolean enable)
	{
		env.getObject().computerControl = new ComputerControlState(env.getIsAttached(), enable);
	}
}
