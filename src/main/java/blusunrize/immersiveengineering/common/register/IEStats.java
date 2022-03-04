/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD)
public class IEStats
{
	public static ResourceLocation WIRE_DEATHS;
	public static ResourceLocation SKYHOOK_DISTANCE;

	@SubscribeEvent
	// Just need *some* registry event, since all registries are apparently unfrozen during those
	public static void registerStats(RegistryEvent.Register<Block> ev)
	{
		WIRE_DEATHS = registerCustomStat("wire_deaths", StatFormatter.DEFAULT);
		SKYHOOK_DISTANCE = registerCustomStat("skyhook_distance", StatFormatter.DISTANCE);
	}

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
