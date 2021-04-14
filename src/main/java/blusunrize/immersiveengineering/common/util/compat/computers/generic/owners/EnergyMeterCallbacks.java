/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

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
