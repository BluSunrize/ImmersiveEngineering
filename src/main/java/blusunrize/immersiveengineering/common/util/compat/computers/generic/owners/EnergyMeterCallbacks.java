package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.EnergyMeterTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;

public class EnergyMeterCallbacks extends CallbackOwner<EnergyMeterTileEntity>
{
	public EnergyMeterCallbacks()
	{
		super(EnergyMeterTileEntity.class, "current_transformer");
	}

	@Override
	public boolean canAttachTo(EnergyMeterTileEntity candidate)
	{
		return !candidate.isDummy();
	}

	@ComputerCallable
	public int getAveragePower(CallbackEnvironment<EnergyMeterTileEntity> env)
	{
		return env.getObject().getAveragePower();
	}
}
