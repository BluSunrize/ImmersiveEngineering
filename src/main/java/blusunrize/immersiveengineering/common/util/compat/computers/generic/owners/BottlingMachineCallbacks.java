package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.BottlingMachineTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.TankCallbacks;

public class BottlingMachineCallbacks extends MultiblockCallbackOwner<BottlingMachineTileEntity>
{
	public BottlingMachineCallbacks()
	{
		super(BottlingMachineTileEntity.class, "bottling_machine");
		addAdditional(PoweredMBCallbacks.INSTANCE);
		addAdditional(new TankCallbacks<>(te -> te.tanks[0], ""));
	}
}
