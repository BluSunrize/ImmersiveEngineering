/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class IEStats
{
	public static final ResourceLocation WIRE_DEATHS = registerCustomStat("wire_deaths", StatFormatter.DEFAULT);
	public static final ResourceLocation SKYHOOK_DISTANCE = registerCustomStat("skyhook_distance", StatFormatter.DISTANCE);

	// Force classloading/static init
	public static void init()
	{
	}

	private static ResourceLocation registerCustomStat(String name, StatFormatter formatter)
	{
		ResourceLocation regName = ImmersiveEngineering.rl(name);
		Registry.register(Registry.CUSTOM_STAT, regName, regName);
		Stats.CUSTOM.get(regName, formatter);
		return regName;
	}
}
