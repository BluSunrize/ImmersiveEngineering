package blusunrize.immersiveengineering.common;

import java.util.HashMap;

import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Config
{
	public static HashMap<String, Boolean> config_boolean = new HashMap();
	public static HashMap<String, Integer> config_int = new HashMap();
	public static HashMap<String, double[]> config_doubleArray = new HashMap();
	public static HashMap<String, int[]> config_intArray = new HashMap();

	//	public static int[] cableTransferRate;
	//	public static double[] cableLossRatio;
	//	public static int[] cableColouration;
	//	public static boolean increasedRenderboxes;
	//	
	//	public static int capacitor_storage;
	//	public static int capacitor_input;
	//	public static int capacitor_output;
	//	public static int dynamo_output;
	//	public static int capacitorHV_storage;
	//	public static int capacitorHV_input;
	//	public static int capacitorHV_output;

	public static void init(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();


		setIntArray("cableTransferRate", config.get("General", "Cable transfer rates", new int[]{256,1024,4096}, "The transfer rates in RF/t for the cable tiers (copper, electrum, HV)").getIntList());
		setDoubleArray("cableLossRatio", config.get("General", "Cable loss", new double[]{.05,.025,.1 }, "The percentage of power lost every 16 blocks of distancefor the cable tiers (copper, electrum, HV)").getDoubleList());
		setIntArray("cableColouration", config.get("General", "Cable colouration", new int[]{0xd4804a,0xedad62,0x6f6f6f}, "").getIntList());
		
		setBoolean("increasedRenderboxes", config.get("General", "Increased Renderboxes", true, "By default all devices that accept cables have increased renderbounds to show cables even if hte block itself is not in view. Disablign this reduces them to their minimum sizes, which might improve FPS on low-power PCs").getBoolean());
		
		setBoolean("ic2compat", config.get("General", "IC2 Compatability", true, "Set this to false to prevent wires from accepting and outputting EU").getBoolean());
		setBoolean("gregtechcompat", config.get("General", "GregTech Compatability", true, "Set this to false to prevent wires from outputting GregTech EU").getBoolean());
		
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
		
		setInt("squeezer_consumption", config.get("Machines", "Squeezer: Consumed", 10, "The RF per tick per item that the Squeezer will consume to create Plant Oil").getInt());
		setInt("fermenter_consumption", config.get("Machines", "Fermenter: Consumed", 10, "The RF per tick per item that the Fermenter will consume to create Ethanol").getInt());
		setInt("refinery_consumption", config.get("Machines", "Refinery: Consumed", 80, "The RF per tick the Fermenter will consume to mix two fluids").getInt());
		
		
		setIntArray("ore_copper", config.get("OreGen", "Copper", new int[]{8, 40,72, 8,100}, "Generation config for Copper Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_bauxite", config.get("OreGen", "Bauxite", new int[]{8, 40,85, 8,100}, "Generation config for Bauxite Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_lead", config.get("OreGen", "Lead", new int[]{6,  8,36, 4,100}, "Generation config for Lead Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_silver", config.get("OreGen", "Silver", new int[]{8,  8,40, 4,80}, "Generation config for Silver Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());
		setIntArray("ore_nickel", config.get("OreGen", "Nickel", new int[]{6,  8,24, 2,100}, "Generation config for Nickel Ore. Parameters: Blocks per vein, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation").getIntList());

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