package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.util.compat.GeneralComputerHelper;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import dan200.computercraft.api.ComputerCraftAPI;

public class ComputercraftHelper extends IECompatModule
{

	@Override
	public void preInit() {}

	@Override
	public void init()
	{
		ComputerCraftAPI.registerPeripheralProvider(new IEPeripheralProvider());
	}

	@Override
	public void postInit()
	{
		GeneralComputerHelper.addComputerManualContent();
	}

}
