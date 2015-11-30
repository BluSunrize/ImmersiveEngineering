package blusunrize.immersiveengineering.common;

import java.util.HashMap;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Config
{
	public static HashMap<String, Boolean> config_boolean = new HashMap();
	public static HashMap<String, Integer> config_int = new HashMap();
	public static HashMap<String, Double> config_double = new HashMap();
	public static HashMap<String, double[]> config_doubleArray = new HashMap();
	public static HashMap<String, int[]> config_intArray = new HashMap();
	public static HashMap<String, String[]> config_stringArray = new HashMap();

	static Configuration config;
	public static void init(FMLPreInitializationEvent event)
	{
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		double currentVersion = ImmersiveEngineering.VERSION_D;
		Property propLastVersion = config.get("general","LastVersion",.54, "The last version of IE that was run in this instance. DO NOT CHANGE THIS, IT WILL BREAK THINGS.");
		//		Property propReGen = config.get("general", "RegenTransferValues", true);
		double lastVersion = propLastVersion.getDouble();
		if(lastVersion<currentVersion)
		{
			propLastVersion.set(currentVersion);
			IELogger.info("The Previous Version of IE was outdated!");
			if(lastVersion<.6)
			{
				IELogger.info("The config on transfer rates of IE wires will be reset to the default.");
				config.getCategory("general").remove("Cable transfer rates");
				config.getCategory("general").remove("Cable loss");
			}
			if(lastVersion<.61)
			{
				IELogger.info("The config on bullet damage will be reset to the default.");
				config.getCategory("tools").remove("BulletDamage-Casull");
				config.getCategory("tools").remove("BulletDamage-AP");
				config.getCategory("tools").remove("BulletDamage-Buck");
				config.getCategory("tools").remove("BulletDamage-Dragon");
				config.getCategory("tools").remove("BulletDamage-Homing");
				config.getCategory("tools").remove("BulletDamage-Wolfpack");
				config.getCategory("tools").remove("BulletDamage-WolfpackPart");
				config.getCategory("tools").remove("BulletDamage-Silver");
				config.getCategory("tools").remove("BulletDamage-Potion");
			}
			config.getCategory("general").remove("Show Update News");
		}

		Property connectionValidation = config.get("general", "Validate Connections", false, "Drop connections with non-existing endpoints when loading the world. Use with care and backups and only when suspecting corrupted data. This option will check and load all connection endpoints and may slow down the world loading process.");
		if(connectionValidation.getBoolean())
		{
			IELogger.warn("Connection validation enabled");
		}
		setBoolean("validateConnections", connectionValidation.getBoolean());

		Property cableProperty = config.get("general", "Cable transfer rates", new int[]{2048,8192,32768,0,0}, "The transfer rates in RF/t for the cable tiers (copper, electrum, HV, Structural Rope & Cable(no transfer) )");
		if(cableProperty.getIntList().length<5)
			cableProperty.set(new int[]{2048,8192,32768,0,0});
		setIntArray("cableTransferRate", cableProperty.getIntList());

		cableProperty = config.get("general", "Cable loss", new double[]{.05,.025,.025,1,1}, "The percentage of power lost every 16 blocks of distance for the cable tiers (copper, electrum, HV, Structural Rope & Cable(no transfer) )");
		if(cableProperty.getDoubleList().length<5)
			cableProperty.set(new double[]{.05,.025,.1,1,1});
		setDoubleArray("cableLossRatio", cableProperty.getDoubleList());

		cableProperty = config.get("general", "Cable colouration", new int[]{0xd4804a,0xedad62,0x6f6f6f, 0x967e6d,0x6f6f6f}, "");
		if(cableProperty.getIntList().length<5)
			cableProperty.set(new int[]{0xb36c3f,0xeda045,0x6f6f6f, 0x967e6d,0x6f6f6f});
		setIntArray("cableColouration", cableProperty.getIntList());

		cableProperty = config.get("general", "Cable length", new int[]{16,16,32,32,32}, "The maximum length cables can have. Copper and Electrum should be similar, Steel is meant for long range transport, Structural Rope & Cables are purely decorational");
		if(cableProperty.getIntList().length<5)
			cableProperty.set(new int[]{16,16,32,32,32});
		setIntArray("cableLength", cableProperty.getIntList());

		setInt("revolverSheetID", config.get("general", "TextureSheet: Revolvers", 94, "The ID of the texture sheet used for revolvers. This should probably never conflict since not many mods do custom sheets.").getInt());
		IEApi.revolverTextureSheetID = getInt("revolverSheetID");
		setBoolean("increasedRenderboxes", config.get("general", "Increased Renderboxes", true, "By default all devices that accept cables have increased renderbounds to show cables even if the block itself is not in view. Disabling this reduces them to their minimum sizes, which might improve FPS on low-power PCs").getBoolean());
		setBoolean("colourblindSupport", config.get("general", "ColourblindSupport", false, "Support for colourblind people, gives a text-based output on capacitor sides").getBoolean());
		setBoolean("adjustManualScale", config.get("general", "AutoscaleManual", true, "Set this to false to disable tge manual's forced change of GUI scale").getBoolean());
		setDouble("increasedTileRenderdistance", config.get("general", "Increased Tile Renderdistance", 1.5, "Increase the distance at which certain TileEntities (specifically windmills) are still visible. This is a modifier, so set it to 1 for default render distance, to 2 for doubled distance and so on.").getDouble());
		setBoolean("disableHammerCrushing", config.get("general", "Disable Hammer Crushing", false, "Set this to true to completely disable the ore-crushing recipes with the Engineers Hammer").getBoolean());
		setStringArray("preferredOres", config.get("general", "Preferred Ores", new String[]{"ImmersiveEngineering","ThermalFoundation"}, "A list of preferred Mod IDs that results of IE processes should stem from, aka which mod you want the copper to come from. This affects the ores dug by the excavator, as well as those crushing recipes that don't have associated IE items. This list is in oreder of priority.").getStringList());
		setBoolean("showUpdateNews", config.get("general", "Show Update News", true, "Set this to false to hide the update news in the manual").getBoolean());


		setBoolean("ic2compat", config.get("general", "IC2 Compatability", true, "Set this to false to prevent wires from accepting and outputting EU").getBoolean());
		setBoolean("gregtechcompat", config.get("general", "GregTech Compatability", true, "Set this to false to prevent wires from outputting GregTech EU").getBoolean());
		setInt("euConversion", config.get("general", "EU Conversion", 4, "The amount of RF that equal 1 EU. 4 by default, so 4RF == 1EU and .25EU == 1RF").getInt());

		setInt("villager_engineer", config.get("general", "Villager ID: Engineer", 512, "The villager ID for the Engineer Villager. Change if it conflicts").getInt());

		Property propConnectorInput = config.get("machines", "Wire Connector Input", new int[]{256,1024,4096}, "In- and output rates of LV,MV and HV Wire Conenctors. This is independant of the transferrate of the wires.");
		if(propConnectorInput.getIntList().length<3)
			propConnectorInput.set(new int[]{256,1024,4096});
		TileEntityConnectorLV.connectorInputValues = propConnectorInput.getIntList();
		setIntArray("wireConnectorInput", propConnectorInput.getIntList());

		setInt("capacitorLV_storage", config.get("machines", "Capacitor LV: RF Storage", 100000, "The maximum amount of RF that can be stored in a low-voltage capacitor").getInt());
		setInt("capacitorLV_input", config.get("machines", "Capacitor LV: Input", 256, "The maximum amount of RF that can be input into a low-voltage capacitor (by IE net or other means)").getInt());
		setInt("capacitorLV_output", config.get("machines", "Capacitor LV: Output", 256, "The maximum amount of RF that can be output from a low-voltage capacitor (by IE net or other means)").getInt());

		setInt("capacitorMV_storage", config.get("machines", "Capacitor MV: RF Storage", 1000000, "The maximum amount of RF that can be stored in a medium-voltage capacitor").getInt());
		setInt("capacitorMV_input", config.get("machines", "Capacitor MV: Input", 1024, "The maximum amount of RF that can be input into a medium-voltage capacitor (by IE net or other means)").getInt());
		setInt("capacitorMV_output", config.get("machines", "Capacitor MV: Output", 1024, "The maximum amount of RF that can be output from a medium-voltage capacitor (by IE net or other means)").getInt());

		setInt("capacitorHV_storage", config.get("machines", "Capacitor HV: RF Storage", 4000000, "The maximum amount of RF that can be stored in a high-voltage capacitor").getInt());
		setInt("capacitorHV_input", config.get("machines", "Capacitor HV: Input", 4096, "The maximum amount of RF that can be input into a high-voltage capacitor (by IE net or other means)").getInt());
		setInt("capacitorHV_output", config.get("machines", "Capacitor HV: Output", 4096, "The maximum amount of RF that can be output from a high-voltage capacitor (by IE net or other means)").getInt());

		setDouble("dynamo_output", config.get("machines", "Dynamo: Output", 3d, "The base RF that is output by the dynamo. This will be modified by the rotation modifier of the attached water- or windmill").getDouble());
		setDouble("thermoelectric_output", config.get("machines", "Thermoelectric: Output", 1d, "Output modifier for the energy created by the Thermoelectric Generator").getDouble());
		setInt("lightning_output", config.get("machines", "Lightning Rod: Output", 4*4000000, "The RF that will be output by the lightning rod when it is struck").getInt());
		setInt("dieselGen_output", config.get("machines", "Diesel Generator: Output", 4096, "The RF per tick that the Diesel Generator will output. The burn time of the fuel determines the total output").getInt());

		setInt("heater_consumption", config.get("machines", "Heater: RF per Heat", 8, "The RF per tick consumed to add one heat to a furnace. Creates up to 4 heat in the startup time and then 1 heat per tick to keep it running").getInt());
		setInt("heater_speedupConsumption", config.get("machines", "Heater: Speedup", 24, "The RF per tick consumed to double the speed of the furnace. Only happens if furnace is at maximum heat.").getInt());
		setInt("crusher_consumption", config.get("machines", "Crusher: Consumed", 120, "The RF per tick consumed by the Crusher. Will also directly influence the speed.").getInt());
		setInt("squeezer_consumption", config.get("machines", "Squeezer: Consumed", 10, "The RF per tick per item that the Squeezer will consume to create Plant Oil").getInt());
		setInt("fermenter_consumption", config.get("machines", "Fermenter: Consumed", 10, "The RF per tick per item that the Fermenter will consume to create Ethanol").getInt());
		setInt("refinery_consumption", config.get("machines", "Refinery: Consumed", 80, "The RF per tick the Refinery will consume to mix two fluids").getInt());

		setInt("excavator_consumption", config.get("machines", "Excavator: Consumed", 4096, "The RF per tick the Excavator will consume to dig").getInt());
		setDouble("excavator_speed", config.get("machines", "Excavator: Speed", 1d, "The speed of the Excavator. Basically translates to how many degrees per tick it will turn.").getDouble());
		setDouble("excavator_chance", config.get("machines", "Excavator: Chance", .05d, "The chance that the Excavator will not dig up an ore with the currently downward-facing bucket.").getDouble());
		setBoolean("excavator_particles", config.get("machines", "Excavator: Particles", true, "Set this to false to disable the ridiculous amounts of particles the Excavator spawns").getBoolean());
		setInt("excavator_depletion", config.get("machines", "Excavator: Mineral Depletion", 76800, "The maximum amount of yield one can get out of a chunk with the excavator. Set a number smaller than zero to make it infinite").getInt());
		setInt("excavator_depletion_days", getInt("excavator_depletion")*45/24000);

		setInt("coredrill_time", config.get("machines", "Core Sample Drill: Evaluation Time", 600, "The length in ticks it takes for the Core Sample Drill to figure out which mineral is found in a chunk").getInt());
		setInt("coredrill_consumption", config.get("machines", "Core Sample Drill: Consumption", 40, "The RF per tick consumed by the Core Sample Drill").getInt());

		setInt("arcfurnace_electrodeDamage", config.get("machines", "Arc Furnace: Graphite Electrodes", 96000, "The maximum amount of damage Graphite Electrodes can take. While the furnace is working, electrodes sustain 1 damage per tick, so this is effectively the lifetime in ticks. The default value of 96000 makes them last for 8 consecutive ingame days").getInt());
		setBoolean("arcfurnace_electrodeCrafting", config.get("machines", "Arc Furnace: Craftable Blueprint", false, "Set this to true to make the blueprint for graphite electrodes craftable in addition to villager/dungeon loot").getBoolean());
		setBoolean("arcfurnace_recycle", config.get("machines", "Arc Furnace: Recycling", true, "Set this to false to disable the Arc Furnace's recycling of armors and tools").getBoolean());
		
		setBoolean("lantern_spawnPrevent", config.get("machines", "Powered Lantern: Spawn Prevention", true, "Set this to false to disable the mob-spawn prevention of the Powered Lantern").getBoolean());
		setBoolean("floodlight_spawnPrevent", config.get("machines", "Floodlight: Spawn Prevention", true, "Set this to false to disable the mob-spawn prevention of the Floodlight").getBoolean());
		
		setInt("pump_consumption", config.get("machines", "Fluid Pump: Consumed", 250, "The RF the Fluid Pump will consume to pick up a fluid block in the world").getInt());
		setInt("pump_consumption_accelerate", config.get("machines", "Fluid Pump: Acceleration", 5, "The RF the Fluid Pump will consume pressurize+accellerate fluids, increasing the transferrate").getInt());
		setBoolean("pump_infiniteWater", config.get("machines", "Fluid Pump: Infinite Water", true, "Set this to false to disable the fluid pump being able to draw infinite water from sources").getBoolean());
		setInt("assembler_consumption", config.get("machines", "Assembler: Consumed", 80, "The RF the Assembler will consume to craft an item from a recipe").getInt());
		setInt("bottlingMachine_consumption", config.get("machines", "Bottling: Consumed", 8, "The RF the Bottling Machine will consume per tick, when filling items").getInt());
		setInt("charger_consumption", config.get("machines", "ChargingStation: Charge", 4000, "The RF per tick the Charging Station can insert into an item").getInt());
		

		setIntArray("ore_copper", config.get("oregen", "Copper", new int[]{8, 40,72, 8,100}, "Generation config for Copper Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_bauxite", config.get("oregen", "Bauxite", new int[]{4, 40,85, 8,100}, "Generation config for Bauxite Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_lead", config.get("oregen", "Lead", new int[]{6,  8,36, 4,100}, "Generation config for Lead Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_silver", config.get("oregen", "Silver", new int[]{8,  8,40, 4,80}, "Generation config for Silver Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_nickel", config.get("oregen", "Nickel", new int[]{6,  8,24, 2,100}, "Generation config for Nickel Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("oreDimBlacklist", config.get("oregen", "DimensionBlacklist", new int[]{-1,1}, "A blacklist of dimensions in which IE ores won't spawn. By default this is Nether (-1) and End (1)").getIntList());

		setBoolean("hardmodeBulletRecipes", config.get("tools", "Bullets: Hardmode Recipes", false, "Enable this to use the old, harder bullet recipes(require one ingot per bullet)").getBoolean());
		setDouble("BulletDamage-Casull", config.get("tools", "BulletDamage-Casull", 10d, "The amount of base damage a Casull Cartridge inflicts").getDouble());
		setDouble("BulletDamage-AP", config.get("tools", "BulletDamage-AP", 10d, "The amount of base damage an ArmorPiercing Cartridge inflicts").getDouble());
		setDouble("BulletDamage-Buck", config.get("tools", "BulletDamage-Buck", 2d, "The amount of base damage a single part of Buckshot inflicts").getDouble());
		setDouble("BulletDamage-Dragon", config.get("tools", "BulletDamage-Dragon", 3d, "The amount of base damage a DragonsBreath Cartridge inflicts").getDouble());
		setDouble("BulletDamage-Homing", config.get("tools", "BulletDamage-Homing", 10d, "The amount of base damage a Homing Cartridge inflicts").getDouble());
		setDouble("BulletDamage-Wolfpack", config.get("tools", "BulletDamage-Wolfpack", 6d, "The amount of base damage a Wolfpack Cartridge inflicts").getDouble());
		setDouble("BulletDamage-WolfpackPart", config.get("tools", "BulletDamage-WolfpackPart", 4d, "The amount of damage the sub-projectiles of the Wolfpack Cartridge inflict").getDouble());
		setDouble("BulletDamage-Silver", config.get("tools", "BulletDamage-Silver", 10d, "The amount of damage a silver bullet inflicts").getDouble());
		setDouble("BulletDamage-Potion", config.get("tools", "BulletDamage-Potion", 1d, "The amount of base damage a Phial Cartridge inflicts").getDouble());
		
		setInt("chemthrower_consumption", config.get("tools", "ChemThrower: Consumed", 10, "The mb of fluid the Chemical Thrower will consume per tick of usage").getInt());

		for(String key : IECompatModule.moduleClasses.keySet())
			setBoolean("compat_"+key, config.get("compatability", "Enable Compatmodule: "+key, true, "Set this to false to disable IE's built in compatability with "+key).getBoolean());

		config.save();
	}

	public static void setBoolean(String key, boolean b)
	{
		config_boolean.put(key, b);
	}
	public static boolean getBoolean(String key)
	{
		Boolean b = config_boolean.get(key);
		return b!=null?b.booleanValue():false;
	}

	public static void setInt(String key, int i)
	{
		config_int.put(key, i);
	}
	public static int getInt(String key)
	{
		Integer i = config_int.get(key);
		return i!=null?i.intValue():0;
	}

	public static void setDouble(String key, double d)
	{
		config_double.put(key, d);
	}
	public static double getDouble(String key)
	{
		Double d = config_double.get(key);
		return d!=null?d.floatValue():0;
	}

	public static void setDoubleArray(String key, double[] dA)
	{
		config_doubleArray.put(key, dA);
	}
	public static double[] getDoubleArray(String key)
	{
		return config_doubleArray.get(key);
	}

	public static void setIntArray(String key, int[] iA)
	{
		config_intArray.put(key, iA);
	}
	public static int[] getIntArray(String key)
	{
		return config_intArray.get(key);
	}

	public static void setStringArray(String key, String[] dA)
	{
		config_stringArray.put(key, dA);
	}
	public static String[] getStringArray(String key)
	{
		return config_stringArray.get(key);
	}

	public static int getPotionID(int base, String key)
	{
//		config.load();
		int i = config.get("potions", key, IEPotions.getNextPotionId(base), "The potion ID for the "+key+" potion effect").getInt();
		config.save();
		return i;
	}
}