package blusunrize.immersiveengineering.common;

import java.util.HashMap;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import blusunrize.immersiveengineering.api.WireType;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Config
{
	public static HashMap<String, Boolean> config_boolean = new HashMap();
	public static HashMap<String, Integer> config_int = new HashMap();
	public static HashMap<String, Double> config_double = new HashMap();
	public static HashMap<String, double[]> config_doubleArray = new HashMap();
	public static HashMap<String, int[]> config_intArray = new HashMap();

	public static void init(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		Property cableProperty = config.get("General", "Cable transfer rates", new int[]{256,1024,4096,0,0,0}, "The transfer rates in RF/t for the cable tiers (copper, electrum, HV, Strutural Rope & Cable(no transfer) )");
		if(cableProperty.getIntList().length<WireType.values().length)
			cableProperty.set(new int[]{256,1024,4096,0,0,0});
		setIntArray("cableTransferRate", cableProperty.getIntList());

		cableProperty = config.get("General", "Cable loss", new double[]{.05,.025,.1,1,1,1}, "The percentage of power lost every 16 blocks of distance for the cable tiers (copper, electrum, HV, Strutural Rope & Cable(no transfer) )");
		if(cableProperty.getDoubleList().length<WireType.values().length)
			cableProperty.set(new double[]{.05,.025,.1,1,1,1});
		setDoubleArray("cableLossRatio", cableProperty.getDoubleList());

		cableProperty = config.get("General", "Cable colouration", new int[]{0xd4804a,0xedad62,0x6f6f6f, 0x967e6d,0x6f6f6f,0x141D3C}, "");
		if(cableProperty.getIntList().length<WireType.values().length)
			cableProperty.set(new int[]{0xb36c3f,0xeda045,0x6f6f6f, 0x967e6d,0x6f6f6f,0x141D3C});
		setIntArray("cableColouration", cableProperty.getIntList());

		cableProperty = config.get("General", "Cable length", new int[]{16,16,32,32,32,32}, "The maximum length cables can have. Copper and Electrum should be similar, Steel is meant for long range transport, Structural Rope & Cables are purely decorational");
		if(cableProperty.getIntList().length<WireType.values().length)
			cableProperty.set(new int[]{16,16,32,32,32,32});
		setIntArray("cableLength", cableProperty.getIntList());

		setBoolean("increasedRenderboxes", config.get("General", "Increased Renderboxes", true, "By default all devices that accept cables have increased renderbounds to show cables even if the block itself is not in view. Disabling this reduces them to their minimum sizes, which might improve FPS on low-power PCs").getBoolean());
		setBoolean("colourblindSupport", config.get("General", "Support for colourblind people, gives a text-based output on capacitor sides", false).getBoolean());
		setBoolean("increasedTileRenderdistance", config.get("General", "Increased Tile Renderdistance", false, "Increase the distance at which certain TileEntities (specifically windmills) are still visible. Disable this to increase performance on weaker PCs").getBoolean());

		setBoolean("ic2compat", config.get("General", "IC2 Compatability", true, "Set this to false to prevent wires from accepting and outputting EU").getBoolean());
		setBoolean("gregtechcompat", config.get("General", "GregTech Compatability", true, "Set this to false to prevent wires from outputting GregTech EU").getBoolean());
		setInt("euConversion", config.get("General", "EU Conversion", 4, "The amount of RF that equal 1 EU. 4 by default, so 4RF == 1EU and .25EU == 1RF").getInt());
		
		setInt("capacitorLV_storage", config.get("Machines", "Capacitor LV: RF Storage", 100000, "The maximum amount of RF that can be stored in a low-voltage capacitor").getInt());
		setInt("capacitorLV_input", config.get("Machines", "Capacitor LV: Input", 256, "The maximum amount of RF that can be input into a low-voltage capacitor (by IE net or other means)").getInt());
		setInt("capacitorLV_output", config.get("Machines", "Capacitor LV: Output", 256, "The maximum amount of RF that can be output from a low-voltage capacitor (by IE net or other means)").getInt());

		setInt("capacitorMV_storage", config.get("Machines", "Capacitor MV: RF Storage", 1000000, "The maximum amount of RF that can be stored in a medium-voltage capacitor").getInt());
		setInt("capacitorMV_input", config.get("Machines", "Capacitor MV: Input", 1024, "The maximum amount of RF that can be input into a medium-voltage capacitor (by IE net or other means)").getInt());
		setInt("capacitorMV_output", config.get("Machines", "Capacitor MV: Output", 1024, "The maximum amount of RF that can be output from a medium-voltage capacitor (by IE net or other means)").getInt());

		setInt("capacitorHV_storage", config.get("Machines", "Capacitor HV: RF Storage", 4000000, "The maximum amount of RF that can be stored in a high-voltage capacitor").getInt());
		setInt("capacitorHV_input", config.get("Machines", "Capacitor HV: Input", 4096, "The maximum amount of RF that can be input into a high-voltage capacitor (by IE net or other means)").getInt());
		setInt("capacitorHV_output", config.get("Machines", "Capacitor HV: Output", 4096, "The maximum amount of RF that can be output from a high-voltage capacitor (by IE net or other means)").getInt());

		setInt("dynamo_output", config.get("Machines", "Dynamo: Output", 3, "The base RF that is output by the dynamo. This will be modified by the rotation modifier of the attached water- or windmill").getInt());
		setInt("lightning_output", config.get("Machines", "Lightning Rod: Output", 4*4000000, "The RF that will be output by the lightning rod when it is struck").getInt());
		setInt("dieselGen_output", config.get("Machines", "Diesel Generator: Output", 4096, "The RF per tick that the Diesel Generator will output. The burn time of the fuel determines the total output").getInt());

		setInt("heater_consumption", config.get("Machines", "Heater: RF per Heat", 8, "The RF per tick consumed to add one heat to a furnace. Creates up to 4 heat in the startup time and then 1 heat per tick to keep it running").getInt());
		setInt("heater_speedupConsumption", config.get("Machines", "Heater: Speedup", 24, "The RF per tick consumed to double the speed of the furnace. Only happens if furnace is at maximum heat.").getInt());
		setInt("crusher_consumption", config.get("Machines", "Crusher: Consumed", 80, "The RF per tick consumed by the Crusher. Will also directly influence the speed.").getInt());
		setInt("squeezer_consumption", config.get("Machines", "Squeezer: Consumed", 10, "The RF per tick per item that the Squeezer will consume to create Plant Oil").getInt());
		setInt("fermenter_consumption", config.get("Machines", "Fermenter: Consumed", 10, "The RF per tick per item that the Fermenter will consume to create Ethanol").getInt());
		setInt("refinery_consumption", config.get("Machines", "Refinery: Consumed", 80, "The RF per tick the Refinery will consume to mix two fluids").getInt());

		setInt("excavator_consumption", config.get("Machines", "Excavator: Consumed", 4096, "The RF per tick the Excavator will consume to dig").getInt());
		setDouble("excavator_speed", config.get("Machines", "Excavator: Speed", 1f, "The speed of the Excavator. Basically translates to how many degrees per tick it will turn.").getDouble());
		setDouble("excavator_chance", config.get("Machines", "Excavator: Chance", .05f, "The chance that the Excavator will not dig up an ore with the currently downward-facing bucket.").getDouble());
		setBoolean("excavator_particles", config.get("Machines", "Excavator: Particles", true, "Set this to false to disable the ridiculous amounts of particles the Excavator spawns").getBoolean());
		setInt("excavator_depletion", config.get("Machines", "Excavator: Mineral Depletion", 76800, "The maximum amount of yield one can get out of a chunk with the excavator. Set a number smaller than zero to make it infinite").getInt());
		setInt("excavator_depletion_days", getInt("excavator_depletion")*45/24000);

		setInt("coredrill_time", config.get("Machines", "Core Sample Drill: Evaluation Time", 600, "The length in ticks it takes for the Core Sample Drill to figure out which mineral is found in a chunk").getInt());
		setInt("coredrill_consumption", config.get("Machines", "Core Sample Drill: Consumption", 40, "The RF per tick consumed by the Core Sample Drill").getInt());

		setIntArray("ore_copper", config.get("OreGen", "Copper", new int[]{8, 40,72, 8,100}, "Generation config for Copper Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_bauxite", config.get("OreGen", "Bauxite", new int[]{4, 40,85, 8,100}, "Generation config for Bauxite Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_lead", config.get("OreGen", "Lead", new int[]{6,  8,36, 4,100}, "Generation config for Lead Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_silver", config.get("OreGen", "Silver", new int[]{8,  8,40, 4,80}, "Generation config for Silver Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_nickel", config.get("OreGen", "Nickel", new int[]{6,  8,24, 2,100}, "Generation config for Nickel Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());

		setDouble("BulletDamage-Casull", config.get("Tools", "BulletDamage-Casull", 7, "The amount of base damage a Casull Cartridge inflicts").getDouble());
		setDouble("BulletDamage-AP", config.get("Tools", "BulletDamage-AP", 7, "The amount of base damage an ArmorPiercing Cartridge inflicts").getDouble());
		setDouble("BulletDamage-Buck", config.get("Tools", "BulletDamage-Buck", 1, "The amount of base damage a single part of Buckshot inflicts").getDouble());
		setDouble("BulletDamage-Dragon", config.get("Tools", "BulletDamage-Dragon", 4, "The amount of base damage a DragonsBreath Cartridge inflicts").getDouble());

		
		//		Property propReGen = config.get("TESTING", "ReGen", false);
		//		propReGen.set(false);

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
}