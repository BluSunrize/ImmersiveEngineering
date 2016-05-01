package blusunrize.immersiveengineering.common.util.compat.waila;

import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class WailaHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
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