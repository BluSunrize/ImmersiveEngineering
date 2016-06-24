package blusunrize.immersiveengineering.common;

import java.util.Calendar;
import java.util.HashMap;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config
{
	public static HashMap<String, Boolean> config_boolean = new HashMap();
	public static HashMap<String, Integer> config_int = new HashMap();
	public static HashMap<String, Double> config_double = new HashMap();
	public static HashMap<String, String> config_string = new HashMap();
	public static HashMap<String, double[]> config_doubleArray = new HashMap();
	public static HashMap<String, int[]> config_intArray = new HashMap();
	public static HashMap<String, String[]> config_stringArray = new HashMap();

	static Configuration config;
	public static void preInit(FMLPreInitializationEvent event)
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

		Property wireProperty = config.get("general", "Wire transfer rates", new int[]{2048,8192,32768,0,0}, "The transfer rates in Flux/t for the wire tiers (copper, electrum, HV, Structural Rope & Cable(no transfer) )");
		if(wireProperty.getIntList().length<5)
			wireProperty.set(new int[]{2048,8192,32768,0,0});
		setIntArray("wireTransferRate", wireProperty.getIntList());

		wireProperty = config.get("general", "Wire loss", new double[]{.05,.025,.025,1,1}, "The percentage of power lost every 16 blocks of distance for the wire tiers (copper, electrum, HV, Structural Rope & Cable(no transfer) )");
		if(wireProperty.getDoubleList().length<5)
			wireProperty.set(new double[]{.05,.025,.025,1,1});
		setDoubleArray("wireLossRatio", wireProperty.getDoubleList());

		wireProperty = config.get("general", "Wire colouration", new int[]{0xd4804a,0xedad62,0x6f6f6f, 0x967e6d,0x6f6f6f}, "The RGB colourate of the wires.");
		if(wireProperty.getIntList().length<5)
			wireProperty.set(new int[]{0xb36c3f,0xeda045,0x6f6f6f, 0x967e6d,0x6f6f6f});
		setIntArray("wireColouration", wireProperty.getIntList());

		wireProperty = config.get("general", "Wire length", new int[]{16,16,32,32,32}, "The maximum length wire can have. Copper and Electrum should be similar, Steel is meant for long range transport, Structural Rope & Cables are purely decorational");
		if(wireProperty.getIntList().length<5)
			wireProperty.set(new int[]{16,16,32,32,32});
		setIntArray("wireLength", wireProperty.getIntList());

		setInt("revolverSheetID", config.get("general", "TextureSheet: Revolvers", 94, "The ID of the texture sheet used for revolvers. This should probably never conflict since not many mods do custom sheets.").getInt());
		IEApi.revolverTextureSheetID = getInt("revolverSheetID");
		setBoolean("increasedRenderboxes", config.get("general", "Increased Renderboxes", true, "By default all devices that accept cables have increased renderbounds to show cables even if the block itself is not in view. Disabling this reduces them to their minimum sizes, which might improve FPS on low-power PCs").getBoolean());
		setBoolean("colourblindSupport", config.get("general", "ColourblindSupport", false, "Support for colourblind people, gives a text-based output on capacitor sides").getBoolean());
		setBoolean("nixietubeFont", config.get("general", "NixietubeFont", true, "Set this to false to disable the super awesome looking nixie tube front for the voltmeter and other things").getBoolean());
		setBoolean("adjustManualScale", config.get("general", "AutoscaleManual", false, "Set this to false to disable tge manual's forced change of GUI scale").getBoolean());
		setBoolean("badEyesight", config.get("general", "BadEyesight", false, "Set this to true if you suffer from bad eyesight. The Engineer's manual will be switched to a bold and darker text to improve readability. Note that this may lead to a break of formatting and have text go off the page in some instances. This is unavoidable.").getBoolean());
		setDouble("increasedTileRenderdistance", config.get("general", "Increased Tile Renderdistance", 1.5, "Increase the distance at which certain TileEntities (specifically windmills) are still visible. This is a modifier, so set it to 1 for default render distance, to 2 for doubled distance and so on.").getDouble());
		setBoolean("disableHammerCrushing", config.get("general", "Disable Hammer Crushing", false, "Set this to true to completely disable the ore-crushing recipes with the Engineers Hammer").getBoolean());
		setInt("hammerDurabiliy", config.get("general", "Hammer Durability", 100, "The maximum durability of the Engineer's Hammer. Used up when hammering ingots into plates.").getInt());
		setInt("cutterDurabiliy", config.get("general", "Wirecutter Durability", 250, "The maximum durability of the Wirecutter. Used up when cutting plates into wire.").getInt());
		setStringArray("preferredOres", config.get("general", "Preferred Ores", new String[]{"ImmersiveEngineering","ThermalFoundation"}, "A list of preferred Mod IDs that results of IE processes should stem from, aka which mod you want the copper to come from. This affects the ores dug by the excavator, as well as those crushing recipes that don't have associated IE items. This list is in oreder of priority.").getStringList());
		setBoolean("showUpdateNews", config.get("general", "Show Update News", true, "Set this to false to hide the update news in the manual").getBoolean());

		Calendar calendar = Calendar.getInstance();
		setBoolean("christmas", calendar.get(2)+1==12);

		setBoolean("ic2compat", config.get("general", "IC2 Compatability", true, "Set this to false to prevent wires from accepting and outputting EU").getBoolean());
		setBoolean("gregtechcompat", config.get("general", "GregTech Compatability", true, "Set this to false to prevent wires from outputting GregTech EU").getBoolean());
		setInt("euConversion", config.get("general", "EU Conversion", 4, "The amount of Flux that equal 1 EU. 4 by default, so 4Flux == 1EU and .25EU == 1Flux").getInt());

		setInt("villager_engineer", config.get("general", "Villager ID: Engineer", 512, "The villager ID for the Engineer Villager. Change if it conflicts").getInt());

		Property propConnectorInput = config.get("machines", "Wire Connector Input", new int[]{256,1024,4096}, "In- and output rates of LV,MV and HV Wire Conenctors. This is independant of the transferrate of the wires.");
		if(propConnectorInput.getIntList().length<3)
			propConnectorInput.set(new int[]{256,1024,4096});
		TileEntityConnectorLV.connectorInputValues = propConnectorInput.getIntList();
		setIntArray("wireConnectorInput", propConnectorInput.getIntList());

		//Capacitors
		setInt("capacitorLV_storage", config.get("machines", "Capacitor LV: Flux Storage", 100000, "The maximum amount of Flux that can be stored in a low-voltage capacitor").getInt());
		setInt("capacitorLV_input", config.get("machines", "Capacitor LV: Input", 256, "The maximum amount of Flux that can be input into a low-voltage capacitor (by IE net or other means)").getInt());
		setInt("capacitorLV_output", config.get("machines", "Capacitor LV: Output", 256, "The maximum amount of Flux that can be output from a low-voltage capacitor (by IE net or other means)").getInt());

		setInt("capacitorMV_storage", config.get("machines", "Capacitor MV: Flux Storage", 1000000, "The maximum amount of Flux that can be stored in a medium-voltage capacitor").getInt());
		setInt("capacitorMV_input", config.get("machines", "Capacitor MV: Input", 1024, "The maximum amount of Flux that can be input into a medium-voltage capacitor (by IE net or other means)").getInt());
		setInt("capacitorMV_output", config.get("machines", "Capacitor MV: Output", 1024, "The maximum amount of Flux that can be output from a medium-voltage capacitor (by IE net or other means)").getInt());

		setInt("capacitorHV_storage", config.get("machines", "Capacitor HV: Flux Storage", 4000000, "The maximum amount of Flux that can be stored in a high-voltage capacitor").getInt());
		setInt("capacitorHV_input", config.get("machines", "Capacitor HV: Input", 4096, "The maximum amount of Flux that can be input into a high-voltage capacitor (by IE net or other means)").getInt());
		setInt("capacitorHV_output", config.get("machines", "Capacitor HV: Output", 4096, "The maximum amount of Flux that can be output from a high-voltage capacitor (by IE net or other means)").getInt());

		//Generators
		setDouble("dynamo_output", config.get("machines", "Dynamo: Output", 3d, "The base Flux that is output by the dynamo. This will be modified by the rotation modifier of the attached water- or windmill").getDouble());
		setDouble("thermoelectric_output", config.get("machines", "Thermoelectric: Output", 1d, "Output modifier for the energy created by the Thermoelectric Generator").getDouble());
		//setInt("lightning_output", config.get("machines", "Lightning Rod: Output", 4*4000000, "The Flux that will be output by the lightning rod when it is struck").getInt());
		setInt("dieselGen_output", config.get("machines", "Diesel Generator: Output", 4096, "The Flux per tick that the Diesel Generator will output. The burn time of the fuel determines the total output").getInt());

		//Simple Machines
		setInt("heater_consumption", config.get("machines", "Heater: Flux per Heat", 8, "The Flux per tick consumed to add one heat to a furnace. Creates up to 4 heat in the startup time and then 1 heat per tick to keep it running").getInt());
		setInt("heater_speedupConsumption", config.get("machines", "Heater: Speedup", 24, "The Flux per tick consumed to double the speed of the furnace. Only happens if furnace is at maximum heat.").getInt());
		setInt("preheater_consumption", config.get("machines", "BlastFurnacePreheater: Charge", 32, "The Flux per tick the Blast Furnace Preheater will consume to speed up the Blast Furnace").getInt());
		setInt("coredrill_time", config.get("machines", "Core Sample Drill: Evaluation Time", 200, "The length in ticks it takes for the Core Sample Drill to figure out which mineral is found in a chunk").getInt());
		setInt("coredrill_consumption", config.get("machines", "Core Sample Drill: Consumption", 40, "The Flux per tick consumed by the Core Sample Drill").getInt());
		setInt("pump_consumption", config.get("machines", "Fluid Pump: Consumed", 250, "The Flux the Fluid Pump will consume to pick up a fluid block in the world").getInt());
		setInt("pump_consumption_accelerate", config.get("machines", "Fluid Pump: Acceleration", 5, "The Flux the Fluid Pump will consume pressurize+accellerate fluids, increasing the transferrate").getInt());
		setBoolean("pump_infiniteWater", config.get("machines", "Fluid Pump: Infinite Water", true, "Set this to false to disable the fluid pump being able to draw infinite water from sources").getBoolean());
		setBoolean("pump_placeCobble", config.get("machines", "Fluid Pump: Cobble", true, "If this is set to true (default) the pump will replace fluids it picks up with cobblestone in order to reduce lag caused by flowing fluids.").getBoolean());
		setInt("charger_consumption", config.get("machines", "ChargingStation: Charge", 256, "The Flux per tick the Charging Station can insert into an item").getInt());
		setInt("teslacoil_consumption", config.get("machines", "TeslaCoil: Passive", 256, "The Flux per tick the Tesla Coil will consume, simply by being active").getInt());
		setInt("teslacoil_consumption_active", config.get("machines", "TeslaCoil: Active", 512, "The amount of Flux the Tesla Coil will consume when shocking an entity").getInt());
		setDouble("teslacoil_damage", config.get("machines", "TeslaCoil: Damage", 6, "The amount of damage the Tesla Coil will do when shocking an entity").getInt());
		
		//Lights
		setBoolean("lantern_spawnPrevent", config.get("machines", "Powered Lantern: Spawn Prevention", true, "Set this to false to disable the mob-spawn prevention of the Powered Lantern").getBoolean());
		setBoolean("floodlight_spawnPrevent", config.get("machines", "Floodlight: Spawn Prevention", true, "Set this to false to disable the mob-spawn prevention of the Floodlight").getBoolean());


		//Multiblock Recipes
		MetalPressRecipe.energyModifier = (float)config.get("machines", "Metal Press: EnergyModifier", 1d, "A modifier to apply to the energy costs of every MetalPress recipe").getDouble();
		MetalPressRecipe.timeModifier = (float)config.get("machines", "Metal Press: TimeModifier", 1d, "A modifier to apply to the time of every MetalPress recipe").getDouble();
		CrusherRecipe.energyModifier = (float)config.get("machines", "Crusher: EnergyModifier", 1d, "A modifier to apply to the energy costs of every Crusher recipe").getDouble();
		CrusherRecipe.timeModifier = (float)config.get("machines", "Crusher: TimeModifier", 1d, "A modifier to apply to the time of every Crusher recipe").getDouble();
		SqueezerRecipe.energyModifier = (float)config.get("machines", "Squeezer: EnergyModifier", 1d, "A modifier to apply to the energy costs of every Squeezer recipe").getDouble();
		SqueezerRecipe.timeModifier = (float)config.get("machines", "Squeezer: TimeModifier", 1d, "A modifier to apply to the time of every Squeezer recipe").getDouble();
		FermenterRecipe.energyModifier = (float)config.get("machines", "Fermenter: EnergyModifier", 1d, "A modifier to apply to the energy costs of every Fermenter recipe").getDouble();
		FermenterRecipe.timeModifier = (float)config.get("machines", "Fermenter: TimeModifier", 1d, "A modifier to apply to the time of every Fermenter recipe").getDouble();
		RefineryRecipe.energyModifier = (float)config.get("machines", "Refinery: EnergyModifier", 1d, "A modifier to apply to the energy costs of every Refinery recipe").getDouble();
		RefineryRecipe.timeModifier = (float)config.get("machines", "Refinery: TimeModifier", 1d, "A modifier to apply to the time of every Refinery recipe").getDouble();
		ArcFurnaceRecipe.energyModifier = (float)config.get("machines", "Arc Furnace: EnergyModifier", 1d, "A modifier to apply to the energy costs of every Arc Furnace recipe").getDouble();
		ArcFurnaceRecipe.timeModifier = (float)config.get("machines", "Arc Furnace: TimeModifier", 1d, "A modifier to apply to the time of every Arc Furnace recipe").getDouble();
		setInt("arcfurnace_electrodeDamage", config.get("machines", "Arc Furnace: Graphite Electrodes", 96000, "The maximum amount of damage Graphite Electrodes can take. While the furnace is working, electrodes sustain 1 damage per tick, so this is effectively the lifetime in ticks. The default value of 96000 makes them last for 8 consecutive ingame days").getInt());
		setBoolean("arcfurnace_electrodeCrafting", config.get("machines", "Arc Furnace: Craftable Blueprint", false, "Set this to true to make the blueprint for graphite electrodes craftable in addition to villager/dungeon loot").getBoolean());
		setBoolean("arcfurnace_recycle", config.get("machines", "Arc Furnace: Recycling", true, "Set this to false to disable the Arc Furnace's recycling of armors and tools").getBoolean());

		//Other Multiblock machines
		setInt("assembler_consumption", config.get("machines", "Assembler: Consumed", 80, "The Flux the Assembler will consume to craft an item from a recipe").getInt());
		//setInt("bottlingMachine_consumption", config.get("machines", "Bottling: Consumed", 8, "The Flux the Bottling Machine will consume per tick, when filling items").getInt());
		setInt("excavator_consumption", config.get("machines", "Excavator: Consumed", 4096, "The Flux per tick the Excavator will consume to dig").getInt());
		setDouble("excavator_speed", config.get("machines", "Excavator: Speed", 1d, "The speed of the Excavator. Basically translates to how many degrees per tick it will turn.").getDouble());
		setBoolean("excavator_particles", config.get("machines", "Excavator: Particles", true, "Set this to false to disable the ridiculous amounts of particles the Excavator spawns").getBoolean());
		setDouble("excavator_chance", config.get("machines", "Excavator: Mineral Chance", .2d, "The chance that a given chunk will contain a mineral vein.").getDouble());
		setInt("excavator_depletion", config.get("machines", "Excavator: Mineral Depletion", 38400, "The maximum amount of yield one can get out of a chunk with the excavator. Set a number smaller than zero to make it infinite").getInt());
		//setInt("excavator_depletion_days", getInt("excavator_depletion")*45/24000);
		setIntArray("excavator_dimBlacklist", config.get("machines", "Excavator: Mineral Dimension Blacklist", new int[]{1}, "List of dimensions that can't contain minerals. Default: The End.").getIntList());

		setIntArray("ore_copper", config.get("oregen", "Copper", new int[]{8, 40,72, 8,100}, "Generation config for Copper Ore. Parameters: Vein size, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_bauxite", config.get("oregen", "Bauxite", new int[]{4, 40,85, 8,100}, "Generation config for Bauxite Ore. Parameters: Vein size, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_lead", config.get("oregen", "Lead", new int[]{6,  8,36, 4,100}, "Generation config for Lead Ore. Parameters: Vein size, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_silver", config.get("oregen", "Silver", new int[]{8,  8,40, 4,80}, "Generation config for Silver Ore. Parameters: Vein size, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_nickel", config.get("oregen", "Nickel", new int[]{6,  8,24, 2,100}, "Generation config for Nickel Ore. Parameters: Vein size, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_uranium", config.get("oregen", "Uranium", new int[]{4,  8,24, 2,60}, "Generation config for Uranium Ore. Parameters: Vein size, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("oreDimBlacklist", config.get("oregen", "DimensionBlacklist", new int[]{-1,1}, "A blacklist of dimensions in which IE ores won't spawn. By default this is Nether (-1) and End (1)").getIntList());
		setBoolean("retrogen_log_flagChunk", config.get("oregen", "retrogen_log_chunkFlagged", true, "Set this to false to disable the logging of the chunks that were flagged for retrogen.").getBoolean());
		setBoolean("retrogen_log_remaining", config.get("oregen", "retrogen_log_chunksRemaining", true, "Set this to false to disable the logging of the chunks that are still left to retrogen.").getBoolean());
		setString("retrogen_key", config.get("oregen", "retrogen_key", "DEFAULT", "The retrogeneration key. Basically IE checks if this key is saved in the chunks data. If it isn't, it will perform retrogen on all ores marked for retrogen. Change this in combination with the retrogen booleans to regen only some of the ores.").getString());
		setBoolean("retrogen_copper", config.get("oregen", "retrogen_Copper", false, "Set this to true to allow retro-generation of Copper Ore.").getBoolean());
		setBoolean("retrogen_bauxite", config.get("oregen", "retrogen_Bauxite", false, "Set this to true to allow retro-generation of Bauxite Ore.").getBoolean());
		setBoolean("retrogen_lead", config.get("oregen", "retrogen_Lead", false, "Set this to true to allow retro-generation of Lead Ore.").getBoolean());
		setBoolean("retrogen_silver", config.get("oregen", "retrogen_Silver", false, "Set this to true to allow retro-generation of Silver Ore.").getBoolean());
		setBoolean("retrogen_nickel", config.get("oregen", "retrogen_Nickel", false, "Set this to true to allow retro-generation of Nickel Ore.").getBoolean());
		setBoolean("retrogen_uranium", config.get("oregen", "retrogen_Uranium", false, "Set this to true to allow retro-generation of Uranium Ore.").getBoolean());

		//Tools
		//setBoolean("hardmodeBulletRecipes", config.get("tools", "Bullets: Hardmode Recipes", false, "Enable this to use the old, harder bullet recipes(require one ingot per bullet)").getBoolean());
		setDouble("BulletDamage-Casull", config.get("tools", "BulletDamage-Casull", 10d, "The amount of base damage a Casull Cartridge inflicts").getDouble());
		setDouble("BulletDamage-AP", config.get("tools", "BulletDamage-AP", 10d, "The amount of base damage an ArmorPiercing Cartridge inflicts").getDouble());
		setDouble("BulletDamage-Buck", config.get("tools", "BulletDamage-Buck", 2d, "The amount of base damage a single part of Buckshot inflicts").getDouble());
		setDouble("BulletDamage-Dragon", config.get("tools", "BulletDamage-Dragon", 3d, "The amount of base damage a DragonsBreath Cartridge inflicts").getDouble());
		setDouble("BulletDamage-Homing", config.get("tools", "BulletDamage-Homing", 10d, "The amount of base damage a Homing Cartridge inflicts").getDouble());
		setDouble("BulletDamage-Wolfpack", config.get("tools", "BulletDamage-Wolfpack", 6d, "The amount of base damage a Wolfpack Cartridge inflicts").getDouble());
		setDouble("BulletDamage-WolfpackPart", config.get("tools", "BulletDamage-WolfpackPart", 4d, "The amount of damage the sub-projectiles of the Wolfpack Cartridge inflict").getDouble());
		setDouble("BulletDamage-Silver", config.get("tools", "BulletDamage-Silver", 10d, "The amount of damage a silver bullet inflicts").getDouble());
		setDouble("BulletDamage-Potion", config.get("tools", "BulletDamage-Potion", 1d, "The amount of base damage a Phial Cartridge inflicts").getDouble());

		setStringArray("EarDefenders_SoundBlacklist", config.get("tools", "EarDefenders: Blacklist", new String[]{}, "A list of sounds that should not be muffled by the Ear Defenders. Adding to this list requires knowledge of the correct sound resource names.").getStringList());

		setInt("chemthrower_consumption", config.get("tools", "ChemThrower: Consumed", 10, "The mb of fluid the Chemical Thrower will consume per tick of usage").getInt());

		setInt("railgun_consumption", config.get("tools", "Railgun: Consumed", 800, "The base amount of Flux consumed per shot by the Railgun").getInt());

		//Compat
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

	public static void setString(String key, String s)
	{
		config_string.put(key, s);
	}
	public static String getString(String key)
	{
		return config_string.get(key);
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
}
