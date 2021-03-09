package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;

public class PoweredMBCallbacks extends Callback<PoweredMultiblockTileEntity<?, ?>>
{
	public static final PoweredMBCallbacks INSTANCE = new PoweredMBCallbacks();

	public PoweredMBCallbacks()
	{
		addAdditional(EnergyCallbacks.INSTANCE);
		addAdditional(MultiblockCallbacks.INSTANCE);
	}
	//TODO general access to recipes?
}
