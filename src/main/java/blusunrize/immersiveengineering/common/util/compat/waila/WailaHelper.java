package blusunrize.immersiveengineering.common.util.compat.waila;

import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import cpw.mods.fml.common.event.FMLInterModComms;

public class WailaHelper extends IECompatModule
{
	public WailaHelper()
	{
		super("Waila");
	}

	@Override
	public void init()
	{
		 FMLInterModComms.sendMessage("Waila", "register", "blusunrize.immersiveengineering.common.util.compat.waila.IEWailaDataProvider.callbackRegister");
	}

	@Override
	public void postInit()
	{
	}
}