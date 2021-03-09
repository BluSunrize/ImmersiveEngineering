package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.DieselGeneratorTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.MultiblockCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.TankCallbacks;

public class DieselGenCallbacks extends MultiblockCallbackOwner<DieselGeneratorTileEntity>
{
	public DieselGenCallbacks()
	{
		super(DieselGeneratorTileEntity.class, "diesel_generator");
		addAdditional(new TankCallbacks<>(te -> te.tanks[0], ""));
		addAdditional(MultiblockCallbacks.INSTANCE);
	}
}
