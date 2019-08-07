/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class IEConfig
{
	//TODO replace fixed-length lists with push's/pop's
	private static Predicate<Object> isSameSizeList(List<?> in)
	{
		return obj -> obj instanceof List&&((List)obj).size()==in.size();
	}

	public static class Wires
	{
		Wires(ForgeConfigSpec.Builder builder)
		{
			builder.comment("Configuration related to Immersive Engineering wires").push("wires");
			validateConnections = builder
					.comment("Drop connections with non-existing endpoints when loading the world. Use with care and backups and only when suspecting corrupted data.",
							"This option will check and load all connection endpoints and may slow down the world loading process.")
					.define("validateConnections", false);
			List<Integer> defaultTransferRates = Lists.newArrayList(2048, 8192, 32768, 0, 0, 0);
			wireTransferRate = builder
					.comment("The transfer rates in Flux/t for the wire tiers (copper, electrum, HV, Structural Rope, Cable & Redstone(no transfer) )")
					.define("wireTransferRate", defaultTransferRates, isSameSizeList(defaultTransferRates));
			List<Double> defaultLossRates = Lists.newArrayList(.05, .025, .025, 1., 1., 1.);
			wireLossRatio = builder
					.comment("The percentage of power lost every 16 blocks of distance for the wire tiers (copper, electrum, HV, Structural Rope, Cable & Redstone(no transfer) )")
					.define("wireTransferRate", defaultLossRates, isSameSizeList(defaultLossRates));
			List<Integer> defaultColours = Lists.newArrayList(0xb36c3f, 0xeda045, 0x6f6f6f, 0x967e6d, 0x6f6f6f, 0xff2f2f, 0xfaf1de, 0x9d857a);
			wireColouration = builder
					.comment("The RGB colourate of the wires.")
					.define("wireTransferRate", defaultColours, isSameSizeList(defaultColours));
			List<Integer> defaultLength = Lists.newArrayList(16, 16, 32, 32, 32, 32);
			wireLength = builder
					.comment("\"The maximum length wire can have. Copper and Electrum should be similar, Steel is meant for long range transport, Structural Rope & Cables are purely decorational\"")
					.define("wireTransferRate", defaultLength, isSameSizeList(defaultLength));
			enableWireDamage = builder.comment("If this is enabled, wires connected to power sources will cause damage to entities touching them",
					"This shouldn't cause significant lag but possibly will. If it does, please report it at https://github.com/BluSunrize/ImmersiveEngineering/issues unless there is a report of it already.")
					.define("enableWireDamage", true);
			blocksBreakWires = builder.comment("If this is enabled, placing a block in a wire will break it (drop the wire coil)")
					.define("blocksBreakWires", true);
			builder.pop();
		}

		public final BooleanValue validateConnections;
		public final ConfigValue<List<Integer>> wireTransferRate;
		public final ConfigValue<List<Double>> wireLossRatio;
		public final ConfigValue<List<Integer>> wireColouration;
		public final ConfigValue<List<Integer>> wireLength;
		public final BooleanValue enableWireDamage;
		public final BooleanValue blocksBreakWires;
	}

	public static class General
	{
		General(ForgeConfigSpec.Builder builder)
		{
			builder.push("General");
			disableFancyTESR = builder
					.comment("Disables most lighting code for certain models that are rendered dynamically (TESR). May improve FPS.",
							"Affects turrets and garden cloches")
					.define("disableFancyTESR", false);
			//TODO rename, change description?
			colourblindSupport = builder
					.comment("Support for colourblind people, gives a text-based output on capacitor sides")
					.define("colourblindSupport", false);
			nixietubeFont = builder
					.comment("Set this to false to disable the super awesome looking nixie tube front for the voltmeter and other things")
					.define("nixietubeFont", true);
			//TODO unclear whether true or false enables rescaling
			adjustManualScale = builder
					.comment("Set this to false to disable the manual's forced change of GUI scale")
					.define("adjustManualScale", false);
			//TODO is the second part still true?
			badEyesight = builder
					.comment("Set this to true if you suffer from bad eyesight. The Engineer's manual will be switched to a bold and darker text to improve readability.",
							"Note that this may lead to a break of formatting and have text go off the page in some instances. This is unavoidable.")
					.define("badEyesight", false);
			//TODO is this still necessary? Oredict is no more, do tags show automatically?
			oreTooltips = builder
					.comment("Controls if item tooltips should contain the OreDictionary names of items. These tooltips are only visible in advanced tooltip mod (F3+H)")
					.define("oreTooltips", true);
			increasedTileRenderdistance = builder
					.comment("Increase the distance at which certain TileEntities (specifically windmills) are still visible. This is a modifier, so set it to 1 for default render distance, to 2 for doubled distance and so on.")
					.defineInRange("increasedTileRenderdistance", 1.5, 0, Double.MAX_VALUE);
			preferredOres = builder
					.comment("A list of preferred Mod IDs that results of IE processes should stem from, aka which mod you want the copper to come from.",
							"This affects the ores dug by the excavator, as well as those crushing recipes that don't have associated IE items. This list is in oreder of priority.")
					.define("preferredOres", ImmutableList.of(ImmersiveEngineering.MODID));
			showUpdateNews = builder
					.comment("Set this to false to hide the update news in the manual")
					.define("showUpdateNews", true);
			villagerHouse = builder
					.comment("Set this to false to stop the IE villager house from spawning")
					.worldRestart()//TODO MC restart?
					.define("villagerHouse", true);
			enableVillagers = builder
					.comment("Set this to false to remove IE villagers from the game")
					.define("enableVillagers", true);
			//TODO is this still relevant (loot tables)?
			hempSeedWeight = builder
					.comment("The weight that hempseeds have when breaking tall grass. 5 by default, set to 0 to disable drops")
					.defineInRange("hempSeedWeight", 5, 0, Integer.MAX_VALUE);
			fancyItemHolding = builder
					.comment("Allows revolvers and other IE items to look properly held in 3rd person. This uses a coremod. Can be disabled in case of conflicts with other animation mods.")
					.define("fancyItemHolding", true);
			stencilBufferEnabled = builder
					.comment("Set to false to disable the stencil buffer. This may be necessary on older GPUs.")
					.define("stencilBufferEnabled", true);
			builder
					.comment("A list of all mods that IE has integrated compatability for", "Setting any of these to false disables the respective compat")
					.push("compat");
			for(String mod : IECompatModule.moduleClasses.keySet())
				compat.put(mod, builder
						.define(mod, true));
			builder.pop();
			enableDebug = builder
					.comment("A config setting to enable debug features. These features may vary between releases, may cause crashes, and are unsupported. Do not enable unless asked to by a developer of IE.")
					.define("enableDebug", false);
			builder.pop();
		}

		public final BooleanValue disableFancyTESR;
		public final BooleanValue colourblindSupport;
		public final BooleanValue nixietubeFont;
		public final BooleanValue adjustManualScale;
		public final BooleanValue badEyesight;
		public final BooleanValue oreTooltips;
		public final DoubleValue increasedTileRenderdistance;
		public final ConfigValue<List<String>> preferredOres;
		public final BooleanValue showUpdateNews;
		public final BooleanValue villagerHouse;
		public final BooleanValue enableVillagers;
		public final IntValue hempSeedWeight;
		public final BooleanValue fancyItemHolding;
		public final BooleanValue stencilBufferEnabled;
		public final Map<String, BooleanValue> compat = new HashMap<>();
		public final BooleanValue enableDebug;
	}

	public static class Machines
	{
		Machines(ForgeConfigSpec.Builder builder)
		{
			builder.push("machines");
			List<Integer> defaultConnectorIO = ImmutableList.of(256, 1024, 4096);
			wireConnectorInput = builder
					.comment("In- and output rates of LV,MV and HV Wire Conenctors. This is independant of the transferrate of the wires.")
					.define("wireConnectorInput", defaultConnectorIO, isSameSizeList(defaultConnectorIO));
			{
				builder.push("capacitors");
				IntValue[] temp = addCapacitorConfig(builder, "low", 100000, 256, 256);
				capacitorLvStorage = temp[0];
				capacitorLvInput = temp[1];
				capacitorLvOutput = temp[2];
				temp = addCapacitorConfig(builder, "medium", 1000000, 1024, 1024);
				capacitorMvStorage = temp[0];
				capacitorMvInput = temp[1];
				capacitorMvOutput = temp[2];
				temp = addCapacitorConfig(builder, "low", 4000000, 4096, 4096);
				capacitorHvStorage = temp[0];
				capacitorHvInput = temp[1];
				capacitorHvOutput = temp[2];
				builder.pop();
			}
			dynamo_output = builder
					.comment("The base Flux that is output by the dynamo. This will be modified by the rotation modifier of the attached water- or windmill")
					.defineInRange("dynamo_output", 3D, 0, Integer.MAX_VALUE);
			thermoelectric_output = builder
					.comment("Output modifier for the energy created by the Thermoelectric Generator")
					.defineInRange("thermoelectric_output", 1D, 0, Integer.MAX_VALUE);
			lightning_output = builder
					.comment("The Flux that will be output by the lightning rod when it is struck")
					.defineInRange("lightning_output", 4*4000000, 0, Integer.MAX_VALUE);
			dieselGen_output = builder
					.comment("The Flux per tick that the Diesel Generator will output. The burn time of the fuel determines the total output")
					.defineInRange("dieselGen_output", 4096, 0, Integer.MAX_VALUE);
			heater_consumption = builder
					.comment("The Flux per tick consumed to add one heat to a furnace. Creates up to 4 heat in the startup time and then 1 heat per tick to keep it running")
					.defineInRange("heater_consumption", 8, 1, Integer.MAX_VALUE);
			heater_speedupConsumption = builder
					.comment("The Flux per tick consumed to double the speed of the furnace. Only happens if furnace is at maximum heat.")
					.defineInRange("heater_speedupConsumption", 24, 1, Integer.MAX_VALUE);
			preheater_consumption = addPositive(builder, "preheater_consumption", 32, "The Flux per tick the Blast Furnace Preheater will consume to speed up the Blast Furnace");
			coredrill_time = addPositive(builder, "coredrill_time", 200, "The length in ticks it takes for the Core Sample Drill to figure out which mineral is found in a chunk");
			coredrill_consumption = addPositive(builder, "coredrill_consumption", 40, "The Flux per tick consumed by the Core Sample Drill");
			pump_consumption = addPositive(builder, "pump_consumption", 250, "The Flux the Fluid Pump will consume to pick up a fluid block in the world");
			pump_consumption_accelerate = addPositive(builder, "pump_consumption_accelerate", 5, "The Flux the Fluid Pump will consume pressurize and accelerate fluids, increasing the transferrate");
			pump_infiniteWater = builder
					.comment("Set this to false to disable the fluid pump being able to draw infinite water from sources")
					.define("pump_infiniteWater", true);
			pump_placeCobble = builder
					.comment("If this is set to true (default) the pump will replace fluids it picks up with cobblestone in order to reduce lag caused by flowing fluids.")
					.define("pump_placeCobble", true);
			charger_consumption = addPositive(builder, "charger_consumption", 256,
					"The Flux per tick the Charging Station can insert into an item");
			teslacoil_consumption = addPositive(builder, "teslacoil_consumption", 256,
					"The Flux per tick the Tesla Coil will consume, simply by being active");
			teslacoil_consumption_active = addPositive(builder, "teslacoil_consumption_active", 512, "The amount of Flux the Tesla Coil will consume when shocking an entity");
			teslacoil_damage = builder
					.comment("The amount of damage the Tesla Coil will do when shocking an entity")
					.defineInRange("teslacoil_damage", 6D, 0, Integer.MAX_VALUE);
			builder.pop();
		}

		private IntValue addPositive(Builder builder, String name, int defaultVal, String... desc)
		{
			return builder
					.comment(desc)
					.defineInRange(name, defaultVal, 1, Integer.MAX_VALUE);
		}

		private IntValue[] addCapacitorConfig(ForgeConfigSpec.Builder builder, String voltage, int defaultStorage, int defaultInput, int defaultOutput)
		{
			IntValue[] ret = new IntValue[3];
			builder
					.comment("Configuration for the "+voltage+" voltage capacitor")
					.push(voltage.charAt(0)+"v");
			String prefix = "capacitor"+Character.toUpperCase(voltage.charAt(0))+"V_";
			ret[0] = builder
					.comment("Maximum energy stored (Flux)")
					.defineInRange(prefix+"storage", defaultStorage, 1, Integer.MAX_VALUE);
			ret[1] = builder
					.comment("Maximum energy input (Flux/tick)")
					.defineInRange(prefix+"input", defaultInput, 1, Integer.MAX_VALUE);
			ret[2] = builder
					.comment("Maximum energy output (Flux/tick)")
					.defineInRange(prefix+"output", defaultOutput, 1, Integer.MAX_VALUE);
			builder.pop();
			return ret;
		}

		//Connectors TODO move to Wires?
		public final ConfigValue<List<Integer>> wireConnectorInput;
		//Capacitors
		public final IntValue capacitorLvStorage;
		public final IntValue capacitorLvInput;
		public final IntValue capacitorLvOutput;
		public final IntValue capacitorMvStorage;
		public final IntValue capacitorMvInput;
		public final IntValue capacitorMvOutput;
		public final IntValue capacitorHvStorage;
		public final IntValue capacitorHvInput;
		public final IntValue capacitorHvOutput;

		//Generators
		public final DoubleValue dynamo_output;
		public final DoubleValue thermoelectric_output;
		public final IntValue lightning_output;
		public final IntValue dieselGen_output;

		//Simple Machines
		public final IntValue heater_consumption;
		public final IntValue heater_speedupConsumption;
		public final IntValue preheater_consumption;
		public final IntValue coredrill_time;
		public final IntValue coredrill_consumption;
		public final IntValue pump_consumption;
		public final IntValue pump_consumption_accelerate;
		public final BooleanValue pump_infiniteWater;
		public final BooleanValue pump_placeCobble;
		public final IntValue charger_consumption;
		public final IntValue teslacoil_consumption;
		public final IntValue teslacoil_consumption_active;
		public final DoubleValue teslacoil_damage;
		public final IntValue turret_consumption;
		public final IntValue turret_chem_consumption;
		public final IntValue turret_gun_consumption;
		public final IntValue belljar_consumption;
		public final IntValue belljar_fertilizer;
		public final IntValue belljar_fluid;
		public final DoubleValue belljar_growth_mod;
		public final DoubleValue belljar_solid_fertilizer_mod;
		public final DoubleValue belljar_fluid_fertilizer_mod;

		//Lights
		public final BooleanValue lantern_spawnPrevent = true;
		public final IntValue lantern_energyDraw = 1;
		public final IntValue lantern_maximumStorage = 10;
		public final BooleanValue floodlight_spawnPrevent = true;
		public final IntValue floodlight_energyDraw = 5;
		public final IntValue floodlight_maximumStorage = 80;


		//Multiblock Recipes
		public final DoubleValue metalPress_energyModifier = 1;
		public final DoubleValue metalPress_timeModifier = 1;
		public final DoubleValue crusher_energyModifier = 1;
		public final DoubleValue crusher_timeModifier = 1;
		public final DoubleValue squeezer_energyModifier = 1;
		public final DoubleValue squeezer_timeModifier = 1;
		public final DoubleValue fermenter_energyModifier = 1;
		public final DoubleValue fermenter_timeModifier = 1;
		public final DoubleValue refinery_energyModifier = 1;
		public final DoubleValue refinery_timeModifier = 1;
		public final DoubleValue arcFurnace_energyModifier = 1;
		public final DoubleValue arcFurnace_timeModifier = 1;
		public final IntValue arcfurnace_electrodeDamage = 96000;
		public final BooleanValue arcfurnace_electrodeCrafting = false;
		public final BooleanValue arcfurnace_recycle = true;
		public final DoubleValue autoWorkbench_energyModifier = 1;
		public final DoubleValue autoWorkbench_timeModifier = 1;
		public final DoubleValue bottlingMachine_energyModifier = 1;
		public final DoubleValue bottlingMachine_timeModifier = 1;
		public final DoubleValue mixer_energyModifier = 1;
		public final DoubleValue mixer_timeModifier = 1;

		//Other Multiblock machines
		public final IntValue assembler_consumption = 80;
		public final IntValue excavator_consumption = 4096;
		public final DoubleValue excavator_speed = 1d;
		public final BooleanValue excavator_particles = true;
		public final DoubleValue excavator_chance = .2d;
		public final DoubleValue excavator_fail_chance = .05d;
		public final IntValue excavator_depletion = 38400;
		public final ConfigValue<List<Integer>> excavator_dimBlacklist = new int[]{1};

	}

	public static class Ores
	{
		public final ConfigValue<List<Integer>> ore_copper = new int[]{8, 40, 72, 8, 100};
		public final ConfigValue<List<Integer>> ore_bauxite = new int[]{4, 40, 85, 8, 100};
		public final ConfigValue<List<Integer>> ore_lead = new int[]{6, 8, 36, 4, 100};
		public final ConfigValue<List<Integer>> ore_silver = new int[]{8, 8, 40, 4, 80};
		public final ConfigValue<List<Integer>> ore_nickel = new int[]{6, 8, 24, 2, 100};
		public final ConfigValue<List<Integer>> ore_uranium = new int[]{4, 8, 24, 2, 60};
		public final ConfigValue<List<Integer>> oreDimBlacklist = new int[]{-1, 1};
		public final BooleanValue retrogen_log_flagChunk = true;
		public final BooleanValue retrogen_log_remaining = true;
		public static String retrogen_key = "DEFAULT";
		public final BooleanValue retrogen_copper = false;
		public final BooleanValue retrogen_bauxite = false;
		public final BooleanValue retrogen_lead = false;
		public final BooleanValue retrogen_silver = false;
		public final BooleanValue retrogen_nickel = false;
		public final BooleanValue retrogen_uranium = false;
	}

	public static class Tools
	{
		public final BooleanValue disableHammerCrushing = false;
		public final IntValue hammerDurabiliy = 100;
		public final IntValue cutterDurabiliy = 250;
		//		//public final BooleanValue hardmodeBulletRecipes = false;
		public final DoubleValue bulletDamage_Casull = 10f;
		public final DoubleValue bulletDamage_AP = 10f;
		public final DoubleValue bulletDamage_Buck = 2f;
		public final DoubleValue bulletDamage_Dragon = 3f;
		public final DoubleValue bulletDamage_Homing = 10f;
		public final DoubleValue bulletDamage_Wolfpack = 6f;
		public final DoubleValue bulletDamage_WolfpackPart = 4f;
		public final DoubleValue bulletDamage_Silver = 10f;
		public final DoubleValue bulletDamage_Potion = 1f;

		public final ConfigValue<List<String>> earDefenders_SoundBlacklist = new String[]{};
		public final IntValue chemthrower_consumption = 10;
		public final BooleanValue chemthrower_scroll = true;
		public final IntValue railgun_consumption = 800;
		public final DoubleValue railgun_damage = 1f;
		public final ConfigValue<List<String>> powerpack_whitelist = new String[]{};
		public final ConfigValue<List<String>> powerpack_blacklist = new String[]{"embers:ashen_cloak_chest", "ic2:batpack", "ic2:cf_pack", "ic2:energy_pack", "ic2:jetpack", "ic2:jetpack_electric", "ic2:lappack"};

		public final ConfigValue<List<String>> toolbox_tools = new String[]{};
		public final ConfigValue<List<String>> toolbox_foods = new String[]{};
		public final ConfigValue<List<String>> toolbox_wiring = new String[]{};

	}

	static final ForgeConfigSpec ieConfig;
	public static final Wires WIRES;
	public static final General GENERAL;

	static
	{
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		WIRES = new Wires(builder);
		GENERAL = new General(builder);

		ieConfig = builder.build();
	}
}
