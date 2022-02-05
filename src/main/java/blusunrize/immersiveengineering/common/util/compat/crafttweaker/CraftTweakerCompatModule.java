/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.common.util.compat.IECompatModules.StandardIECompatModule;
import net.minecraftforge.common.MinecraftForge;

public class CraftTweakerCompatModule extends StandardIECompatModule
{
	public CraftTweakerCompatModule()
	{
		MinecraftForge.EVENT_BUS.register(CrafttweakerEventHandlers.class);
	}
}
