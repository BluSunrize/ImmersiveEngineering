/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.common.Config.IEConfig.Machines;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import com.google.common.collect.Maps;
import net.minecraftforge.common.config.Config.*;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class Config
{
	public static HashMap<String, Boolean> manual_bool = new HashMap<String, Boolean>();
	public static HashMap<String, Integer> manual_int = new HashMap<String, Integer>();
	public static HashMap<String, int[]> manual_intA = new HashMap<String, int[]>();
	public static HashMap<String, Double> manual_double = new HashMap<String, Double>();
	public static HashMap<String, double[]> manual_doubleA = new HashMap<String, double[]>();

	public static boolean seaonal_festive = false;

	@net.minecraftforge.common.config.Config(modid = ImmersiveEngineering.MODID)
	public static class IEConfig
	{
		//Wire Stuff
		@Comment({"Drop connections with non-existing endpoints when loading the world. Use with care and backups and only when suspecting corrupted data.",
				"This option will check and load all connection endpoints and may slow down the world loading process."})
		@RequiresWorldRestart
		public static boolean validateConnections = false;
		@Comment({"The transfer rates in Flux/t for the wire tiers (copper, electrum, HV, Structural Rope, Cable & Redstone(no transfer) )"})
		@Mapped(mapClass = Config.class, mapName = "manual_intA")
		public static int[] wireTransferRate = new int[]{2048, 8192, 32768, 0, 0, 0};
		@Comment({"The percentage of power lost every 16 blocks of distance for the wire tiers (copper, electrum, HV, Structural Rope, Cable & Redstone(no transfer) )"})
		public static double[] wireLossRatio = new double[]{.05, .025, .025, 1, 1, 1};

		public static int[] wireColourationDefault = new int[]{0xb36c3f, 0xeda045, 0x6f6f6f, 0x967e6d, 0x6f6f6f, 0xff2f2f, 0xfaf1de, 0x9d857a};
		@Comment({"The RGB colourate of the wires."})
		public static int[] wireColouration = wireColourationDefault;
		@Comment({"The maximum length wire can have. Copper and Electrum should be similar, Steel is meant for long range transport, Structural Rope & Cables are purely decorational"})
		public static int[] wireLength = new int[]{16, 16, 32, 32, 32, 32};
		@Comment({"If this is enabled, wires connected to power sources will cause damage to entities touching them",
				"This shouldn't cause significant lag but possibly will. If it does, please report it at https://github.com/BluSunrize/ImmersiveEngineering/issues unless there is a report of it already."})
		public static boolean enableWireDamage = true;
		@Comment({"If this is enabled, placing a block in a wire will break it (drop the wire coil)"})
		@RequiresWorldRestart
		public static boolean blocksBreakWires = true;

		@Comment({"By default all devices that accept cables have increased renderbounds to show cables even if the block itself is not in view.", "Disabling this reduces them to their minimum sizes, which might improve FPS on low-power PCs"})
		//TODO this is for TESR wires. Remove?
		public static boolean increasedRenderboxes = true;
		@Comment({"Disables most lighting code for certain models that are rendered dynamically (TESR). May improve FPS.", "Affects turrets and garden cloches"})
		public static boolean disableFancyTESR = false;
		@Comment({"Disables the fancy rendering of blueprints on the Workbench and Autoworkbench.","Set this to true if your game keeps freezing or crashing when looking at such a block."})
		public static boolean disableFancyBlueprints = true;
		@Comment({"Support for colourblind people, gives a text-based output on capacitor sides"})
		public static boolean colourblindSupport = false;
		@Comment({"Set this to false to disable the super awesome looking nixie tube front for the voltmeter and other things"})
		public static boolean nixietubeFont = true;
		@Comment({"Set this to false to disable the manual's forced change of GUI scale"})
		public static boolean adjustManualScale = false;
		@Comment({"Set this to true if you suffer from bad eyesight. The Engineer's manual will be switched to a bold and darker text to improve readability.", "Note that this may lead to a break of formatting and have text go off the page in some instances. This is unavoidable."})
		public static boolean badEyesight = false;
		@Comment({"Controls if item tooltips should contain the OreDictionary names of items. These tooltips are only visible in advanced tooltip mod (F3+H)"})
		public static boolean oreTooltips = true;
		@Comment({"Increase the distance at which certain TileEntities (specifically windmills) are still visible. This is a modifier, so set it to 1 for default render distance, to 2 for doubled distance and so on."})
		public static double increasedTileRenderdistance = 1.5;
		@Comment({"A list of preferred Mod IDs that results of IE processes should stem from, aka which mod you want the copper to come from.", "This affects the ores dug by the excavator, as well as those crushing recipes that don't have associated IE items. This list is in oreder of priority."})
		public static String[] preferredOres = new String[]{ImmersiveEngineering.MODID};
		@Comment({"Set this to false to hide the update news in the manual"})
		public static boolean showUpdateNews = true;
		@Comment({"Set this to false to stop the IE villager house from spawning"})
		@RequiresMcRestart
		public static boolean villagerHouse = true;
		@Comment({"Set this to false to remove IE villagers from the game"})
		public static boolean enableVillagers = true;
		@Comment({"The weight that hempseeds have when breaking tall grass. 5 by default, set to 0 to disable drops"})
		public static int hempSeedWeight = 5;
		@Comment({"Allows revolvers and other IE items to look properly held in 3rd person. This uses a coremod. Can be disabled in case of conflicts with other animation mods."})
		public static boolean fancyItemHolding = true;
		@Comment({"Set to false to disable the stencil buffer. This may be necessary on older GPUs."})
		@RequiresMcRestart
		public static boolean stencilBufferEnabled = true;
		@Comment({"Set to false to have Coresamples not show the coordinates of the chunk."})
		public static boolean coreSampleCoords = true;


		@Comment({"A list of all mods that IE has integrated compatability for", "Setting any of these to false disables the respective compat"})
		public static Map<String, Boolean> compat = Maps.newHashMap(Maps.toMap(IECompatModule.moduleClasses.keySet(), (s) -> Boolean.TRUE));

		@Comment({"A config setting to enable debug features. These features may vary between releases, may cause crashes, and are unsupported. Do not enable unless asked to by a developer of IE."})
		public static boolean enableDebug = false;

		@SubConfig
		public static Machines machines;
		@SubConfig
		public static Ores ores;
		@SubConfig
		public static Tools tools;


		public static class Machines
		{
			//Connectors
			@Comment({"In- and output rates of LV,MV and HV Wire Conenctors. This is independant of the transferrate of the wires."})
			@Mapped(mapClass = Config.class, mapName = "manual_intA")
			public static int[] wireConnectorInput = new int[]{256, 1024, 4096};
			//Capacitors
			@Comment({"The maximum amount of Flux that can be stored in a low-voltage capacitor"})
			@RangeInt(min = 1)
			public static int capacitorLV_storage = 100000;
			@Comment({"The maximum amount of Flux that can be input into a low-voltage capacitor (by IE net or other means)"})
			@RangeInt(min = 1)
			public static int capacitorLV_input = 256;
			@Comment({"The maximum amount of Flux that can be output from a low-voltage capacitor (by IE net or other means)"})
			@RangeInt(min = 1)
			public static int capacitorLV_output = 256;
			@Comment({"The maximum amount of Flux that can be stored in a medium-voltage capacitor"})
			@RangeInt(min = 1)
			public static int capacitorMV_storage = 1000000;
			@Comment({"The maximum amount of Flux that can be input into a medium-voltage capacitor (by IE net or other means)"})
			@RangeInt(min = 1)
			public static int capacitorMV_input = 1024;
			@Comment({"The maximum amount of Flux that can be output from a medium-voltage capacitor (by IE net or other means)"})
			@RangeInt(min = 1)
			public static int capacitorMV_output = 1024;
			@Comment({"The maximum amount of Flux that can be stored in a high-voltage capacitor"})
			@RangeInt(min = 1)
			public static int capacitorHV_storage = 4000000;
			@Comment({"The maximum amount of Flux that can be input into a high-voltage capacitor (by IE net or other means)"})
			@RangeInt(min = 1)
			public static int capacitorHV_input = 4096;
			@Comment({"The maximum amount of Flux that can be output from a high-voltage capacitor (by IE net or other means)"})
			@RangeInt(min = 1)
			public static int capacitorHV_output = 4096;

			//Generators
			@Comment({"The base Flux that is output by the dynamo. This will be modified by the rotation modifier of the attached water- or windmill"})
			@RangeDouble(min = 0)
			public static double dynamo_output = 3d;
			@Comment({"Output modifier for the energy created by the Thermoelectric Generator"})
			@RangeDouble(min = 0)
			public static double thermoelectric_output = 1d;
			@Comment({"The Flux that will be output by the lightning rod when it is struck"})
			@RangeInt(min = 0)
			public static int lightning_output = 4*4000000;
			@Comment({"The Flux per tick that the Diesel Generator will output. The burn time of the fuel determines the total output"})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 0)
			public static int dieselGen_output = 4096;
			@Comment({"Should IE register fuels for the Diesel Generator? if this is set true, IE will overwrite custom values set by crafttweaker. The default fuels are <fluid:fuel>, <fluid:diesel> and <fluid:biodiesel>"})
			public static boolean diesel_registerFuels = true;


			//Simple Machines
			@Comment({"The Flux per tick consumed to add one heat to a furnace. Creates up to 4 heat in the startup time and then 1 heat per tick to keep it running"})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 1)
			public static int heater_consumption = 8;
			@Comment({"The Flux per tick consumed to double the speed of the furnace. Only happens if furnace is at maximum heat."})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 1)
			public static int heater_speedupConsumption = 24;
			@Comment({"The Flux per tick the Blast Furnace Preheater will consume to speed up the Blast Furnace"})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 1)
			public static int preheater_consumption = 32;
			@Comment({"The length in ticks it takes for the Core Sample Drill to figure out which mineral is found in a chunk"})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 1)
			public static int coredrill_time = 200;
			@Comment({"The Flux per tick consumed by the Core Sample Drill"})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 1)
			public static int coredrill_consumption = 40;

			@Comment({"The Flux the Fluid Pump will consume to pick up a fluid block in the world"})
			@RangeInt(min = 1)
			public static int pump_consumption = 250;
			@Comment({"The Flux the Fluid Pump will consume pressurize+accelerate fluids, increasing the transferrate"})
			@RangeInt(min = 1)
			public static int pump_consumption_accelerate = 5;
			@Comment({"Set this to false to disable the fluid pump being able to draw infinite water from sources"})
			@Mapped(mapClass = Config.class, mapName = "manual_bool")
			public static boolean pump_infiniteWater = true;
			@Comment({"If this is set to true (default) the pump will replace fluids it picks up with cobblestone in order to reduce lag caused by flowing fluids."})
			@Mapped(mapClass = Config.class, mapName = "manual_bool")
			public static boolean pump_placeCobble = true;
			@Comment({"The basic transferrate of a fluid pipe, in mB/t"})
			@RangeInt(min = 1)
			public static int pipe_transferrate = 50;
			@Comment({"The transferrate of a fluid pipe when accelerated by a pump, in mB/t"})
			@RangeInt(min = 1)
			public static int pipe_transferrate_pressurized = 1000;

			@Comment({"The Flux per tick the Charging Station can insert into an item"})
			@RangeInt(min = 1)
			public static int charger_consumption = 256;
			@Comment({"The Flux per tick the Tesla Coil will consume, simply by being active"})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 1)
			public static int teslacoil_consumption = 256;
			@Comment({"The amount of Flux the Tesla Coil will consume when shocking an entity"})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 1)
			public static int teslacoil_consumption_active = 512;
			@Comment({"The amount of damage the Tesla Coil will do when shocking an entity"})
			@RangeDouble(min = 0)
			public static float teslacoil_damage = 6;
			@Comment({"The Flux per tick any turret consumes to monitor the area"})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 1)
			public static int turret_consumption = 64;
			@Comment({"The Flux per tick the chemthrower turret consumes to shoot"})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 1)
			public static int turret_chem_consumption = 32;
			@Comment({"The Flux per tick the gun turret consumes to shoot"})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 1)
			public static int turret_gun_consumption = 32;
			@Comment({"The Flux per tick the belljar consumes to grow plants"})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 1)
			public static int belljar_consumption = 8;
			@Comment({"The amount of ticks one dose of fertilizer lasts in the belljar"})
			@RangeInt(min = 1)
			public static int belljar_fertilizer = 6000;
			@Comment({"The amount of fluid the belljar uses per dose of fertilizer"})
			@RangeInt(min = 1)
			public static int belljar_fluid = 250;
			@Comment({"A modifier to apply to the belljars total growing speed"})
			@RangeDouble(min = 1e-3)
			public static float belljar_growth_mod = 1;
			@Comment({"A base-modifier for all solid fertilizers in the belljar"})
			@RangeDouble(min = 1e-3)
			public static float belljar_solid_fertilizer_mod = 1f;
			@Comment({"A base-modifier for all fluid fertilizers in the belljar"})
			@RangeDouble(min = 1e-3)
			public static float belljar_fluid_fertilizer_mod = 1f;

			//Lights
			@Comment({"Set this to false to disable the mob-spawn prevention of the Powered Lantern"})
			public static boolean lantern_spawnPrevent = true;
			@Comment({"How much Flux the powered lantern draws per tick"})
			@RangeInt(min = 1)
			public static int lantern_energyDraw = 1;
			@Comment({"How much Flux the powered lantern can hold (should be greater than the power draw)"})
			@RangeInt(min = 1)
			public static int lantern_maximumStorage = 10;
			@Comment({"Set this to false to disable the mob-spawn prevention of the Floodlight"})
			@RequiresWorldRestart
			public static boolean floodlight_spawnPrevent = true;
			@Comment({"How much Flux the floodlight draws per tick"})
			@RangeInt(min = 1)
			public static int floodlight_energyDraw = 5;
			@Comment({"How much Flux the floodlight can hold (must be at least 10x the power draw)"})
			@RangeInt(min = 1)
			public static int floodlight_maximumStorage = 80;


			//Multiblock Recipes
			@Comment({"A modifier to apply to the energy costs of every MetalPress recipe"})
			@RangeDouble(min = 1e-3, max = 1e3)
			public static float metalPress_energyModifier = 1;
			@Comment({"A modifier to apply to the time of every MetalPress recipe"})
			@RangeDouble(min = 1e-3, max = 1e3)
			public static float metalPress_timeModifier = 1;
			@Comment({"A modifier to apply to the energy costs of every Crusher recipe"})
			@RangeDouble(min = 1e-3, max = 1e3)
			public static float crusher_energyModifier = 1;
			@Comment({"A modifier to apply to the time of every Crusher recipe"})
			@RangeDouble(min = 1e-3, max = 1e3)
			public static float crusher_timeModifier = 1;
			@Comment({"A modifier to apply to the energy costs of every Squeezer recipe"})
			@RangeDouble(min = 1e-3, max = 1e3)
			public static float squeezer_energyModifier = 1;
			@Comment({"A modifier to apply to the time of every Squeezer recipe"})
			@RangeDouble(min = 1e-3, max = 1e3)
			public static float squeezer_timeModifier = 1;
			@Comment({"A modifier to apply to the energy costs of every Fermenter recipe"})
			@RangeDouble(min = 1e-3, max = 1e3)
			public static float fermenter_energyModifier = 1;
			@Comment({"A modifier to apply to the time of every Fermenter recipe"})
			@RangeDouble(min = 1e-3, max = 1e3)
			public static float fermenter_timeModifier = 1;
			@Comment({"A modifier to apply to the energy costs of every Refinery recipe"})
			@RangeDouble(min = 1e-3, max = 1e3)
			public static float refinery_energyModifier = 1;
			@Comment({"A modifier to apply to the time of every Refinery recipe. Can't be lower than 1"})
			@RangeDouble(min = 1e-3, max = 1e3)
			public static float refinery_timeModifier = 1;
			@Comment({"A modifier to apply to the energy costs of every Arc Furnace recipe"})
			@RangeDouble(min = 1e-3, max = 1e3)
			public static float arcFurnace_energyModifier = 1;
			@Comment({"A modifier to apply to the time of every Arc Furnace recipe"})
			@RangeDouble(min = 1e-3, max = 1e3)
			public static float arcFurnace_timeModifier = 1;
			@Comment({"The maximum amount of damage Graphite Electrodes can take. While the furnace is working, electrodes sustain 1 damage per tick, so this is effectively the lifetime in ticks. The default value of 96000 makes them last for 8 consecutive ingame days"})
			@RangeInt(min = 1)
			public static int arcfurnace_electrodeDamage = 96000;
			@Comment({"Set this to true to make the blueprint for graphite electrodes craftable in addition to villager/dungeon loot"})
			@Mapped(mapClass = Config.class, mapName = "manual_bool")
			@RequiresMcRestart
			public static boolean arcfurnace_electrodeCrafting = false;
			@Comment({"Set this to false to disable the Arc Furnace's recycling of armors and tools"})
			@RequiresMcRestart
			public static boolean arcfurnace_recycle = true;
			@Comment({"A modifier to apply to the energy costs of every Automatic Workbench recipe"})
			@RangeDouble(min = 1e-3)
			public static float autoWorkbench_energyModifier = 1;
			@Comment({"A modifier to apply to the time of every Automatic Workbench recipe"})
			@RangeDouble(min = 1e-3)
			public static float autoWorkbench_timeModifier = 1;
			@Comment({"A modifier to apply to the energy costs of every Bottling Machine's process"})
			@RangeDouble(min = 1e-3)
			public static float bottlingMachine_energyModifier = 1;
			@Comment({"A modifier to apply to the time of every Bottling Machine's process"})
			@RangeDouble(min = 1e-3)
			public static float bottlingMachine_timeModifier = 1;
			@Comment({"A modifier to apply to the energy costs of every Mixer's process"})
			@RangeDouble(min = 1e-3)
			public static float mixer_energyModifier = 1;
			@Comment({"A modifier to apply to the time of every Mixer's process"})
			@RangeDouble(min = 1e-3)
			public static float mixer_timeModifier = 1;

			//Other Multiblock machines
			@Comment({"The Flux the Assembler will consume to craft an item from a recipe"})
			@RangeInt(min = 1)
			public static int assembler_consumption = 80;
			//@Comment({"The Flux the Bottling Machine will consume per tick, when filling items"})
			//public static int bottlingMachine_consumption = 8;
			@Comment({"The Flux per tick the Excavator will consume to dig"})
			@Mapped(mapClass = Config.class, mapName = "manual_int")
			@RangeInt(min = 1)
			public static int excavator_consumption = 4096;
			@Comment({"The speed of the Excavator. Basically translates to how many degrees per tick it will turn."})
			@RangeDouble(min = 1e-3)
			public static double excavator_speed = 1d;
			@Comment({"Set this to false to disable the ridiculous amounts of particles the Excavator spawns"})
			public static boolean excavator_particles = true;
			@Comment({"The chance that a given chunk will contain a mineral vein."})
			@RangeDouble(min = 1e-3)
			public static double excavator_chance = .2d;
			@Comment({"The chance that the Excavator will not dig up an ore with the currently downward-facing bucket."})
			@RangeDouble(min = 0)
			public static double excavator_fail_chance = .05d;
			@Comment({"The maximum amount of yield one can get out of a chunk with the excavator. Set a number smaller than zero to make it infinite"})
			public static int excavator_depletion = 38400;
			@Comment({"List of dimensions that can't contain minerals. Default: The End."})
			public static int[] excavator_dimBlacklist = new int[]{1};

		}

		public static class Ores
		{
			@Comment({"Generation config for Copper Ore.", "Parameters: Vein size, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation"})
			@Mapped(mapClass = Config.class, mapName = "manual_intA")
			public static int[] ore_copper = new int[]{8, 40, 72, 8, 100};
			@Comment({"Generation config for Bauxite Ore.", "Parameters: Vein size, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation"})
			@Mapped(mapClass = Config.class, mapName = "manual_intA")
			public static int[] ore_bauxite = new int[]{4, 40, 85, 8, 100};
			@Comment({"Generation config for Lead Ore.", "Parameters: Vein size, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation"})
			@Mapped(mapClass = Config.class, mapName = "manual_intA")
			public static int[] ore_lead = new int[]{6, 8, 36, 4, 100};
			@Comment({"Generation config for Silver Ore.", "Parameters: Vein size, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation"})
			@Mapped(mapClass = Config.class, mapName = "manual_intA")
			public static int[] ore_silver = new int[]{8, 8, 40, 4, 80};
			@Comment({"Generation config for Nickel Ore.", "Parameters: Vein size, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation"})
			@Mapped(mapClass = Config.class, mapName = "manual_intA")
			public static int[] ore_nickel = new int[]{6, 8, 24, 2, 100};
			@Comment({"Generation config for Uranium Ore.", "Parameters: Vein size, lowest possible Y, highest possible Y, veins per chunk, chance for vein to spawn (out of 100). Set vein size to 0 to disable the generation"})
			@Mapped(mapClass = Config.class, mapName = "manual_intA")
			public static int[] ore_uranium = new int[]{4, 8, 24, 2, 60};
			@Comment({"A blacklist of dimensions in which IE ores won't spawn. By default this is Nether (-1) and End (1)"})
			public static int[] oreDimBlacklist = new int[]{-1, 1};
			@Comment({"Set this to false to disable the logging of the chunks that were flagged for retrogen."})
			public static boolean retrogen_log_flagChunk = true;
			@Comment({"Set this to false to disable the logging of the chunks that are still left to retrogen."})
			public static boolean retrogen_log_remaining = true;
			@Comment({"The retrogeneration key. Basically IE checks if this key is saved in the chunks data. If it isn't, it will perform retrogen on all ores marked for retrogen.", "Change this in combination with the retrogen booleans to regen only some of the ores."})
			public static String retrogen_key = "DEFAULT";
			@Comment({"Set this to true to allow retro-generation of Copper Ore."})
			@Mapped(mapClass = IEWorldGen.class, mapName = "retrogenMap")
			public static boolean retrogen_copper = false;
			@Comment({"Set this to true to allow retro-generation of Bauxite Ore."})
			@Mapped(mapClass = IEWorldGen.class, mapName = "retrogenMap")
			public static boolean retrogen_bauxite = false;
			@Comment({"Set this to true to allow retro-generation of Lead Ore."})
			@Mapped(mapClass = IEWorldGen.class, mapName = "retrogenMap")
			public static boolean retrogen_lead = false;
			@Comment({"Set this to true to allow retro-generation of Silver Ore."})
			@Mapped(mapClass = IEWorldGen.class, mapName = "retrogenMap")
			public static boolean retrogen_silver = false;
			@Comment({"Set this to true to allow retro-generation of Nickel Ore."})
			@Mapped(mapClass = IEWorldGen.class, mapName = "retrogenMap")
			public static boolean retrogen_nickel = false;
			@Comment({"Set this to true to allow retro-generation of Uranium Ore."})
			@Mapped(mapClass = IEWorldGen.class, mapName = "retrogenMap")
			public static boolean retrogen_uranium = false;
		}


		public static class Tools
		{
			@Comment({"Set this to true to completely disable the ore-crushing recipes with the Engineers Hammer"})
			@RequiresMcRestart
			public static boolean disableHammerCrushing = false;
			@Comment({"The maximum durability of the Engineer's Hammer. Used up when hammering ingots into plates."})
			@RangeInt(min = 1)
			public static int hammerDurabiliy = 100;
			@Comment({"The maximum durability of the Wirecutter. Used up when cutting plates into wire."})
			@RangeInt(min = 1)
			public static int cutterDurabiliy = 250;
			//@Comment({"Enable this to use the old, harder bullet recipes(require one ingot per bullet)"});
			//public static boolean hardmodeBulletRecipes = false;
			@Comment({"The amount of base damage a Casull Cartridge inflicts"})
			@RangeDouble(min = 0)
			public static float bulletDamage_Casull = 10f;
			@Comment({"The amount of base damage an ArmorPiercing Cartridge inflicts"})
			@RangeDouble(min = 0)
			public static float bulletDamage_AP = 10f;
			@Comment({"The amount of base damage a single part of Buckshot inflicts"})
			@RangeDouble(min = 0)
			public static float bulletDamage_Buck = 2f;
			@Comment({"The amount of base damage a DragonsBreath Cartridge inflicts"})
			@RangeDouble(min = 0)
			public static float bulletDamage_Dragon = 3f;
			@Comment({"The amount of base damage a Homing Cartridge inflicts"})
			@RangeDouble(min = 0)
			public static float bulletDamage_Homing = 10f;
			@Comment({"The amount of base damage a Wolfpack Cartridge inflicts"})
			@RangeDouble(min = 0)
			public static float bulletDamage_Wolfpack = 6f;
			@Comment({"The amount of damage the sub-projectiles of the Wolfpack Cartridge inflict"})
			@RangeDouble(min = 0)
			public static float bulletDamage_WolfpackPart = 4f;
			@Comment({"The amount of damage a silver bullet inflicts"})
			@RangeDouble(min = 0)
			public static float bulletDamage_Silver = 10f;
			@Comment({"The amount of base damage a Phial Cartridge inflicts"})
			@RangeDouble(min = 0)
			public static float bulletDamage_Potion = 1f;

			@Comment({"A list of sounds that should not be muffled by the Ear Defenders. Adding to this list requires knowledge of the correct sound resource names."})
			public static String[] earDefenders_SoundBlacklist = new String[]{};
			@Comment({"The mb of fluid the Chemical Thrower will consume per tick of usage"})
			@RangeInt(min = 1)
			public static int chemthrower_consumption = 10;
			@Comment({"Set this to false to disable the use of Sneak+Scroll to switch Chemthrower tanks."})
			@Mapped(mapClass = Config.class, mapName = "manual_bool")
			public static boolean chemthrower_scroll = true;
			@Comment({"The base amount of Flux consumed per shot by the Railgun"})
			@RangeInt(min = 1)
			public static int railgun_consumption = 800;
			@Comment({"A modifier for the damage of all projectiles fired by the Railgun"})
			@RangeDouble(min = 1e-3)
			public static float railgun_damage = 1f;
			@Comment({"A whitelist of armor pieces to allow attaching the capacitor backpack, formatting: [mod id]:[item name]"})
			public static String[] powerpack_whitelist = new String[]{};
			@Comment({"A blacklist of armor pieces to allow attaching the capacitor backpack, formatting: [mod id]:[item name]. Whitelist has priority over this"})
			public static String[] powerpack_blacklist = new String[]{"embers:ashen_cloak_chest", "ic2:batpack", "ic2:cf_pack", "ic2:energy_pack", "ic2:jetpack", "ic2:jetpack_electric", "ic2:lappack"};

			@Comment({"A whitelist of tools allowed in the toolbox, formatting: [mod id]:[item name]"})
			public static String[] toolbox_tools = new String[]{};
			@Comment({"A whitelist of foods allowed in the toolbox, formatting: [mod id]:[item name]"})
			public static String[] toolbox_foods = new String[]{};
			@Comment({"A whitelist of wire-related items allowed in the toolbox, formatting: [mod id]:[item name]"})
			public static String[] toolbox_wiring = new String[]{};

		}
	}

	static Configuration config;

	public static void preInit(FMLPreInitializationEvent event)
	{
		onConfigUpdate();
	}

	private static void onConfigUpdate()
	{
		if(IEConfig.validateConnections)
			IELogger.warn("Connection validation enabled");

		TileEntityConnectorLV.connectorInputValues = IEConfig.Machines.wireConnectorInput;

		Calendar calendar = Calendar.getInstance();
		seaonal_festive = calendar.get(Calendar.MONTH)+1==12;//December

		MetalPressRecipe.energyModifier = IEConfig.Machines.metalPress_energyModifier;
		MetalPressRecipe.timeModifier = IEConfig.Machines.metalPress_timeModifier;
		CrusherRecipe.energyModifier = IEConfig.Machines.crusher_energyModifier;
		CrusherRecipe.timeModifier = IEConfig.Machines.crusher_timeModifier;
		SqueezerRecipe.energyModifier = IEConfig.Machines.squeezer_energyModifier;
		SqueezerRecipe.timeModifier = IEConfig.Machines.squeezer_timeModifier;
		FermenterRecipe.energyModifier = IEConfig.Machines.fermenter_energyModifier;
		FermenterRecipe.timeModifier = IEConfig.Machines.fermenter_timeModifier;
		RefineryRecipe.energyModifier = IEConfig.Machines.refinery_energyModifier;
		RefineryRecipe.timeModifier = Math.max(1, IEConfig.Machines.refinery_timeModifier);
		ArcFurnaceRecipe.energyModifier = IEConfig.Machines.arcFurnace_energyModifier;
		ArcFurnaceRecipe.timeModifier = IEConfig.Machines.arcFurnace_timeModifier;
		BlueprintCraftingRecipe.energyModifier = IEConfig.Machines.autoWorkbench_energyModifier;
		BlueprintCraftingRecipe.timeModifier = IEConfig.Machines.autoWorkbench_timeModifier;
		MixerRecipe.energyModifier = IEConfig.Machines.mixer_energyModifier;
		MixerRecipe.timeModifier = IEConfig.Machines.mixer_timeModifier;
		BelljarHandler.solidFertilizerModifier = IEConfig.Machines.belljar_solid_fertilizer_mod;
		BelljarHandler.fluidFertilizerModifier = IEConfig.Machines.belljar_fluid_fertilizer_mod;

		Config.manual_int.put("excavator_depletion_days", Machines.excavator_depletion*45/24000);
		Config.manual_bool.put("literalRailGun", false);//preventive measure for Railcraft
		validateAndMapValues(IEConfig.class);
		WireType.wireLossRatio = IEConfig.wireLossRatio;
		WireType.wireTransferRate = IEConfig.wireTransferRate;
		WireType.wireColouration =
				(IEConfig.wireColouration.length!=IEConfig.wireColourationDefault.length)?IEConfig.wireColourationDefault: IEConfig.wireColouration;
		WireType.wireLength = IEConfig.wireLength;
	}

	public static void validateAndMapValues(Class confClass)
	{
		for(Field f : confClass.getDeclaredFields())
		{
			if(!Modifier.isStatic(f.getModifiers()))
				continue;
			Mapped mapped = f.getAnnotation(Mapped.class);
			if(mapped!=null)
				try
				{
					Class c = mapped.mapClass();
					if(c!=null)
					{
						Field mapField = c.getDeclaredField(mapped.mapName());
						if(mapField!=null)
						{
							Map map = (Map)mapField.get(null);
							if(map!=null)
								map.put(f.getName(), f.get(null));
						}
					}
				} catch(Exception e)
				{
					e.printStackTrace();
				}
			else if(f.getAnnotation(SubConfig.class)!=null)
				validateAndMapValues(f.getType());
			else if(f.getAnnotation(RangeDouble.class)!=null)
				try
				{
					RangeDouble range = f.getAnnotation(RangeDouble.class);
					Object valObj = f.get(null);
					double val;
					if(valObj instanceof Double)
						val = (double)valObj;
					else
						val = (float)valObj;
					if(val < range.min())
						f.set(null, range.min());
					else if(val > range.max())
						f.set(null, range.max());
				} catch(IllegalAccessException e)
				{
					e.printStackTrace();
				}
			else if(f.getAnnotation(RangeInt.class)!=null)
				try
				{
					RangeInt range = f.getAnnotation(RangeInt.class);
					int val = (int)f.get(null);
					if(val < range.min())
						f.set(null, range.min());
					else if(val > range.max())
						f.set(null, range.max());
				} catch(IllegalAccessException e)
				{
					e.printStackTrace();
				}
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Mapped
	{
		Class mapClass();

		String mapName();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface SubConfig
	{
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent ev)
	{
		if(ev.getModID().equals(ImmersiveEngineering.MODID))
		{
			ConfigManager.sync(ImmersiveEngineering.MODID, net.minecraftforge.common.config.Config.Type.INSTANCE);
			onConfigUpdate();
		}
	}
}
