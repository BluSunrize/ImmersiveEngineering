package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.ExcavatorTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;

public class ExcavatorCallbacks extends MultiblockCallbackOwner<ExcavatorTileEntity>
{
	public ExcavatorCallbacks()
	{
		super(ExcavatorTileEntity.class, "exavator");
		addAdditional(PoweredMBCallbacks.INSTANCE);
	}
}
