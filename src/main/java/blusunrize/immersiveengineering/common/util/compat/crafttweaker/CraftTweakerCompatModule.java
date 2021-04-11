/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import net.minecraftforge.common.MinecraftForge;

public class CraftTweakerCompatModule extends IECompatModule
{

	@Override
	public void preInit()
	{
		MinecraftForge.EVENT_BUS.register(CrafttweakerEventHandlers.class);
	}

	@Override
	public void init()
	{

	}

	@Override
	public void postInit()
	{

	}
}
