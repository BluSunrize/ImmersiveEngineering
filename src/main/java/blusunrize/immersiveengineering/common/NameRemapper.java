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
	private static final String mappings = "woodendecoration:wooden_decoration\n"+
			"storageslab:storage_slab\n"+
			"stonedecorationstairs_concrete_leaded:stone_decoration_stairs_concrete_leaded\n"+
			"fakelight:fake_light\n"+
			"woodendevice0:wooden_device0\n"+
			"woodendevice1:wooden_device1\n"+
			"stonedecoration:stone_decoration\n"+
			"metalmultiblock:metal_multiblock\n"+
			"clothdevice:cloth_device\n"+
			"stonedecorationstairs_concrete:stone_decoration_stairs_concrete\n"+
			"treatedwoodstairs2:treated_wood_stairs2\n"+
			"treatedwoodstairs1:treated_wood_stairs1\n"+
			"treatedwoodstairs0:treated_wood_stairs0\n"+
			"stonedevice:stone_device\n"+
			"sheetmetalslab:sheetmetal_slab\n"+
			"treatedwoodslab:treated_wood_slab\n"+
			"metaldevice0:metal_device0\n"+
			"metaldevice1:metal_device1\n"+
			"stonedecorationstairs_concrete_tile:stone_decoration_stairs_concrete_tile\n"+
			"metaldecoration1:metal_decoration1\n"+
			"metaldecoration2:metal_decoration2\n"+
			"metaldecoration0:metal_decoration0\n"+
			"treatedwood:treated_wood\n"+
			"stonedecorationslab:stone_decoration_slab\n"+
			"stonedecorationstairs_hempcrete:stone_decoration_stairs_hempcrete\n"+
			"faradaysuit_feet:faraday_suit_feet\n"+
			"faradaysuit_head:faraday_suit_head\n"+
			"fakeicon:fake_icon\n"+
			"fluorescenttube:fluorescent_tube\n"+
			"shaderbag:shader_bag\n"+
			"faradaysuit_legs:faraday_suit_legs\n"+
			"graphiteelectrode:graphite_electrode\n"+
			"faradaysuit_chest:faraday_suit_chest";
	private static final Map<String, String> nameMap = new HashMap<>();

	public static void init()
	{
		String[] lines = mappings.split("\n");
		for(String l : lines)
		{
			String[] mapping = l.split(":");
			if(mapping.length==2)
			{
				nameMap.put(mapping[0], mapping[1]);
			}
		}
	}

	public static void remap(MissingMappings<?> ev)
	{
		for(MissingMappings.Mapping miss : ev.getMappings())
		{
			String newName = nameMap.get(miss.key.getResourcePath());
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

			} else
			{
				IELogger.error("Couldn't remap "+miss.key);
			}
		}
	}
}