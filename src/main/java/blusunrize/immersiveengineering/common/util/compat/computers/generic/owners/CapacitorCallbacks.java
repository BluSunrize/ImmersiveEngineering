package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.CapacitorTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.EnergyCallbacks;

public class CapacitorCallbacks extends CallbackOwner<CapacitorTileEntity>
{
	public CapacitorCallbacks(String voltage)
	{
		super(CapacitorTileEntity.class, "capacitor_"+voltage);
		addAdditional(EnergyCallbacks.INSTANCE);
	}
}
