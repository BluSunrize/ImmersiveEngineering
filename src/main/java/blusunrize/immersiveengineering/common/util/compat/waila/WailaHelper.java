/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

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
	public void registerRecipes()
	{
	}

	@Override
	public void init()
	{
		FMLInterModComms.sendMessage("waila", "register", "blusunrize.immersiveengineering.common.util.compat.waila.IEWailaDataProvider.callbackRegister");
	}

	@Override
	public void postInit()
	{
	}
}