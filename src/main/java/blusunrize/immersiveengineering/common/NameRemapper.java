/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent.MissingMappings;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashMap;
import java.util.Map;

public class NameRemapper
{
	private static final Map<String, String> nameMap = new HashMap<>();

	static
	{
		nameMap.put("woodendecoration", "wooden_decoration");
		nameMap.put("storageslab", "storage_slab");
		nameMap.put("stonedecorationstairs_concrete_leaded", "stone_decoration_stairs_concrete_leaded");
		nameMap.put("fakelight", "fake_light");
		nameMap.put("woodendevice0", "wooden_device0");
		nameMap.put("woodendevice1", "wooden_device1");
		nameMap.put("stonedecoration", "stone_decoration");
		nameMap.put("metalmultiblock", "metal_multiblock");
		nameMap.put("clothdevice", "cloth_device");
		nameMap.put("stonedecorationstairs_concrete", "stone_decoration_stairs_concrete");
		nameMap.put("treatedwoodstairs2", "treated_wood_stairs2");
		nameMap.put("treatedwoodstairs1", "treated_wood_stairs1");
		nameMap.put("treatedwoodstairs0", "treated_wood_stairs0");
		nameMap.put("stonedevice", "stone_device");
		nameMap.put("sheetmetalslab", "sheetmetal_slab");
		nameMap.put("treatedwoodslab", "treated_wood_slab");
		nameMap.put("metaldevice0", "metal_device0");
		nameMap.put("metaldevice1", "metal_device1");
		nameMap.put("stonedecorationstairs_concrete_tile", "stone_decoration_stairs_concrete_tile");
		nameMap.put("metaldecoration1", "metal_decoration1");
		nameMap.put("metaldecoration2", "metal_decoration2");
		nameMap.put("metaldecoration0", "metal_decoration0");
		nameMap.put("treatedwood", "treated_wood");
		nameMap.put("stonedecorationslab", "stone_decoration_slab");
		nameMap.put("stonedecorationstairs_hempcrete", "stone_decoration_stairs_hempcrete");
		nameMap.put("faradaysuit_feet", "faraday_suit_feet");
		nameMap.put("faradaysuit_head", "faraday_suit_head");
		nameMap.put("fakeicon", "fake_icon");
		nameMap.put("fluorescenttube", "fluorescent_tube");
		nameMap.put("shaderbag", "shader_bag");
		nameMap.put("faradaysuit_legs", "faraday_suit_legs");
		nameMap.put("graphiteelectrode", "graphite_electrode");
		nameMap.put("faradaysuit_chest", "faraday_suit_chest");
		nameMap.put(ImmersiveEngineering.MODID+"flammable", "flammable");
		nameMap.put(ImmersiveEngineering.MODID+"slippery", "slippery");
		nameMap.put(ImmersiveEngineering.MODID+"conductive", "conductive");
		nameMap.put(ImmersiveEngineering.MODID+"sticky", "sticky");
		nameMap.put(ImmersiveEngineering.MODID+"stunned", "stunned");
		nameMap.put(ImmersiveEngineering.MODID+"concreteFeet", "concreteFeet");
		nameMap.put(ImmersiveEngineering.MODID+"flashed", "flashed");
	}

	public static void remap(MissingMappings<?> ev)
	{
		for(MissingMappings.Mapping miss : ev.getMappings())
		{
			String newName = nameMap.get(miss.key.getPath());
			if(newName!=null)
			{
				ResourceLocation newLoc = new ResourceLocation(ImmersiveEngineering.MODID, newName);

				IForgeRegistryEntry newTarget = miss.registry.getValue(newLoc);
				if(newTarget!=null)
				{
					IELogger.info("Successfully remapped RegistryEntry "+miss.key);
					miss.remap(newTarget);
				}
				else
					miss.warn();

			}
			else
			{
				IELogger.error("Couldn't remap "+miss.key);
			}
		}
	}
}