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
import blusunrize.immersiveengineering.api.wires.WireLogger;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("WeakerAccess")
@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class IEConfig
{
	//TODO replace fixed-length lists with push's/pop's
	private static <T> Predicate<Object> isSameSizeList(List<T> in)
	{
		return isSameSizeList(in, obj -> true);
	}

	private static <T> Predicate<Object> isSameSizeList(List<T> in, Predicate<T> elementChecker)
	{
		Preconditions.checkArgument(!in.isEmpty());
		return obj -> {
			if(!(obj instanceof List)||((List<?>)obj).size()!=in.size())
				return false;
			Class<?> clazz = in.get(0).getClass();
			for(Object o : (List<?>)obj)
				if(!clazz.isInstance(o)||!elementChecker.test((T)o))
					return false;
			return true;
		};
	}

	public static class Wires
	{
		Wires(ForgeConfigSpec.Builder builder)
		{
			builder.comment("Configuration related to Immersive Engineering wires").push("wires");
			sanitizeConnections = builder
					.comment("Attempts to make the internal data structures used for wires consistent with the connectors in the world."+
									"Use with care and backups and only when suspecting corrupted data.",
							"This option will check and load all connection endpoints and may slow down the world loading process.")
					.define("sanitizeConnections", false);
			builder.push("debug");
			enableWireLogger = builder
					.comment("Enable detailed logging for the wire network. This can be useful for developers to track"+
							" down issues related to wires.")
					.define("enableWireLogger", false);
			validateNet = builder
					.comment("Run sanity checks on the wire network after every interaction. This will cause a decent "+
							"amount of lag and a lot of log spam if the wire network isn't fully intact. Only enable "+
							"when asked to by an IE developer.")
					.define("validateNets", false);
			builder.pop();
			List<Integer> defaultTransferRates = Lists.newArrayList(2048, 8192, 32768, 0, 0, 0);
			wireTransferRate = builder
					.comment("The transfer rates in Flux/t for the wire tiers (copper, electrum, HV, Structural Rope, Cable & Redstone (no transfer) )")
					.define("wireTransferRate", defaultTransferRates, isSameSizeList(defaultTransferRates, i -> i >= 0));
			List<Double> defaultLossRates = Lists.newArrayList(.05, .025, .025, 1., 1., 1.);
			wireLossRatio = builder
					.comment("The percentage of power lost every 16 blocks of distance for the wire tiers (copper, electrum, HV, Structural Rope, Cable & Redstone(no transfer) )")
					.define("wireLossRatio", defaultLossRates, isSameSizeList(defaultLossRates, d -> d >= 0));
			List<Integer> defaultColours = Lists.newArrayList(0xb36c3f, 0xeda045, 0x6f6f6f, 0x967e6d, 0x6f6f6f, 0xff2f2f, 0xfaf1de, 0x9d857a);
			wireColouration = builder
					.comment("The RGB colourate of the wires.")
					.define("wireColouration", defaultColours, isSameSizeList(defaultColours));
			List<Integer> defaultLength = Lists.newArrayList(16, 16, 32, 32, 32, 32);
			wireLength = builder
					.comment("\"The maximum length wire can have. Copper and Electrum should be similar, Steel is meant for long range transport, Structural Rope & Cables are purely decorational\"")
					.define("wireLength", defaultLength, isSameSizeList(defaultLength, i -> i > 0));
			enableWireDamage = builder.comment("If this is enabled, wires connected to power sources will cause damage to entities touching them",
					"This shouldn't cause significant lag but possibly will. If it does, please report it at https://github.com/BluSunrize/ImmersiveEngineering/issues unless there is a report of it already.")
					.define("enableWireDamage", true);
			blocksBreakWires = builder.comment("If this is enabled, placing a block in a wire will break it (drop the wire coil)")
					.define("blocksBreakWires", true);
			builder.pop();
		}

		public final BooleanValue sanitizeConnections;
		public final BooleanValue enableWireLogger;
		public final BooleanValue validateNet;
		public final ConfigValue<List<? extends Integer>> wireTransferRate;
		public final ConfigValue<List<? extends Double>> wireLossRatio;
		public final ConfigValue<List<? extends Integer>> wireColouration;
		public final ConfigValue<List<? extends Integer>> wireLength;
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
			showTextOverlay = builder
					.comment("Show the text overlay for various blocks, such as the configuration of capacitors or pumps")
					.define("showTextOverlay", true);
			nixietubeFont = builder
					.comment("Set this to false to disable the super awesome looking nixie tube front for the voltmeter and other things")
					.define("nixietubeFont", true);
			//TODO unclear whether true or false enables rescaling
			adjustManualScale = builder
					.comment("Set this to false to disable the manual's forced change of GUI scale")
					.define("adjustManualScale", false);
			badEyesight = builder
					.comment("Set this to true if you suffer from bad eyesight. The Engineer's manual will be switched to a bold and darker text to improve readability.")
					.define("badEyesight", false);
			tagTooltips = builder
					.comment("Controls if item tooltips should contain the tags names of items. These tooltips are only visible in advanced tooltip mode (F3+H)")
					.define("tagTooltips", true);
			increasedTileRenderdistance = builder
					.comment("Increase the distance at which certain TileEntities (specifically windmills) are still visible. This is a modifier, so set it to 1 for default render distance, to 2 for doubled distance and so on.")
					.defineInRange("increasedTileRenderdistance", 1.5, 0, Double.MAX_VALUE);
			preferredOres = builder
					.comment("A list of preferred Mod IDs that results of IE processes should stem from, aka which mod you want the copper to come from.",
							"This affects the ores dug by the excavator, as well as those crushing recipes that don't have associated IE items. This list is in oreder of priority.")
					.defineList("preferredOres", ImmutableList.of(ImmersiveEngineering.MODID), obj -> true);
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
			enableVBOs = builder
					//TODO comment
					.define("enableVBO", true);
			builder.pop();
		}

		public final BooleanValue disableFancyTESR;
		public final BooleanValue showTextOverlay;
		public final BooleanValue nixietubeFont;
		public final BooleanValue adjustManualScale;
		public final BooleanValue badEyesight;
		public final BooleanValue tagTooltips;
		public final DoubleValue increasedTileRenderdistance;
		public final ConfigValue<List<? extends String>> preferredOres;
		public final BooleanValue showUpdateNews;
		public final BooleanValue villagerHouse;
		public final BooleanValue enableVillagers;
		public final IntValue hempSeedWeight;
		public final BooleanValue fancyItemHolding;
		public final BooleanValue stencilBufferEnabled;
		public final Map<String, BooleanValue> compat = new HashMap<>();
		public final BooleanValue enableDebug;
		public final BooleanValue enableVBOs;
	}

	public static class Machines
	{
		Machines(ForgeConfigSpec.Builder builder)
		{
			builder.push("machines");
			List<Integer> defaultConnectorIO = ImmutableList.of(256, 1024, 4096);
			wireConnectorInput = builder
					.comment("In- and output rates of LV,MV and HV Wire Conenctors. This is independant of the transferrate of the wires.")
					.define("wireConnectorInput", defaultConnectorIO, isSameSizeList(defaultConnectorIO, i -> i >= 0));
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
				temp = addCapacitorConfig(builder, "high", 4000000, 4096, 4096);
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
			turret_consumption = addPositive(builder, "turret_consumption", 64, "The Flux per tick any turret consumes to monitor the area");
			turret_chem_consumption = addPositive(builder, "turret_chem_consumption", 32, "The Flux per tick the chemthrower turret consumes to shoot");
			turret_gun_consumption = addPositive(builder, "turret_gun_consumption", 32, "The Flux per tick the gun turret consumes to shoot");
			cloche_consumption = addPositive(builder, "garden_cloche_consumption", 8, "The Flux per tick the cloche consumes to grow plants");
			cloche_fertilizer = addPositive(builder, "garden_cloche_fertilizer", 6000, "The amount of ticks one dose of fertilizer lasts in the cloche");
			cloche_fluid = addPositive(builder, "garden_cloche_fluid", 250, "The amount of fluid the cloche uses per dose of fertilizer");
			cloche_growth_mod = builder
					.comment("A modifier to apply to the cloches total growing speed")
					.defineInRange("garden_cloche_growth_modifier", 1, 1e-3, 1e3);
			cloche_solid_fertilizer_mod = builder
					.comment("A base-modifier for all solid fertilizers in the cloche")
					.defineInRange("garden_cloche_solid_fertilizer_mod", 1, 1e-3, 1e3);
			cloche_fluid_fertilizer_mod = builder
					.comment("A base-modifier for all fluid fertilizers in the cloche")
					.defineInRange("garden_cloche_fluid_fertilizer_mod", 1, 1e-3, 1e3);
			lantern_spawnPrevent = builder
					.comment("Set this to false to disable the mob-spawn prevention of the Powered Lantern")
					.worldRestart()
					.define("lantern_SpawnPrevent", true);
			lantern_energyDraw = addPositive(builder, "lantern_energyDraw", 1, "How much Flux the powered lantern draws per tick");
			lantern_maximumStorage = addPositive(builder, "lantern_max_storage", 10, "How much Flux the powered lantern can hold (should be greater than the power draw)");
			floodlight_spawnPrevent = builder
					.comment("Set this to false to disable the mob-spawn prevention of the Floodlight")
					.worldRestart()
					.define("floodlight_spawnPrevent", true);
			floodlight_energyDraw = addPositive(builder, "floodlight_energyDraw", 5, "How much Flux the floodlight draws per tick");
			floodlight_maximumStorage = addPositive(builder, "floodlight_max_storage", 80, "How much Flux the floodlight can hold (must be at least 10x the power draw)");
			metalPressConfig = addMachineEnergyTimeModifiers(builder, "metal press");
			crusherConfig = addMachineEnergyTimeModifiers(builder, "crusher");
			squeezerConfig = addMachineEnergyTimeModifiers(builder, "squeezer");
			fermenterConfig = addMachineEnergyTimeModifiers(builder, "fermenter");
			refineryConfig = addMachineEnergyTimeModifiers(builder, "refinery");
			arcFurnaceConfig = addMachineEnergyTimeModifiers(builder, "arc furnace");
			autoWorkbenchConfig = addMachineEnergyTimeModifiers(builder, "auto workbench");
			bottlingMachineConfig = addMachineEnergyTimeModifiers(builder, "bottling machine");
			mixerConfig = addMachineEnergyTimeModifiers(builder, "mixer");
			arcfurnace_electrodeDamage = addPositive(builder, "arcfurnace_electrodeDamage", 96000, "The maximum amount of damage Graphite Electrodes can take. While the furnace is working, electrodes sustain 1 damage per tick, so this is effectively the lifetime in ticks. The default value of 96000 makes them last for 8 consecutive ingame days");
			arcfurnace_electrodeCrafting = builder
					.comment("Set this to true to make the blueprint for graphite electrodes craftable in addition to villager/dungeon loot")
					.define("arcfurnace_electrodeCrafting", false);
			arcfurnace_recycle = builder
					.comment("Set this to false to disable the Arc Furnace's recycling of armors and tools")
					.define("arcfurnace_recycle", true);

			assembler_consumption = addPositive(builder, "assembler_consumption", 80, "The Flux the Assembler will consume to craft an item from a recipe");
			excavator_consumption = addPositive(builder, "excavator_consumption", 4096, "The Flux per tick the Excavator will consume to dig");
			excavator_speed = builder
					.comment("The speed of the Excavator. Basically translates to how many degrees per tick it will turn.")
					.defineInRange("excavator_speed", 1, 1e-3, 1e3);
			excavator_particles = builder
					.comment("Set this to false to disable the ridiculous amounts of particles the Excavator spawns")
					.define("excavator_particles", true);
			excavator_theshold = builder
					.comment("The threshold the perlin noise has to cross for a mineral vein to be generated. Higher means less likely.")
					.defineInRange("excavator_chance", .9, 0, 1);
			excavator_yield = builder
					.comment("The maximum amount of yield one can get out of a chunk with the excavator. Set a number smaller than zero to make it infinite")
					.defineInRange("excavator_yield", 38400, -1, Integer.MAX_VALUE);
			excavator_initial_depletion = builder
					.comment("The maximum depletion a vein can start with, as a decimal value. When a vein generates, a random percentage up to this value is depleted from it")
					.defineInRange("excavator_initial_depletion", .2, 0, 1);
			excavator_dimBlacklist = builder
					.comment("List of dimensions that can't contain minerals. Default: The End.")
					.defineList("excavator_dimBlacklist", ImmutableList.of(DimensionType.THE_END.getRegistryName().toString()),
							obj -> true);
			builder.pop();
		}

		private <T extends MultiblockRecipe> MachineRecipeConfig<T> addMachineEnergyTimeModifiers(Builder builder, String machine)
		{
			String pathName = machine.toLowerCase(Locale.ENGLISH).replace(' ', '_');
			DoubleValue energy = builder
					.comment("A modifier to apply to the energy costs of every "+machine+" recipe")
					.defineInRange(pathName+"_energyModifier", 1, 1e-3, 1e3);
			DoubleValue time = builder
					.comment("A modifier to apply to the time of every "+machine+" recipe")
					.defineInRange(pathName+"_timeModifier", 1, 1e-3, 1e3);
			return new MachineRecipeConfig<>(energy, time);
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
		public final ConfigValue<List<? extends Integer>> wireConnectorInput;
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
		public final IntValue cloche_consumption;
		public final IntValue cloche_fertilizer;
		public final IntValue cloche_fluid;
		public final DoubleValue cloche_growth_mod;
		public final DoubleValue cloche_solid_fertilizer_mod;
		public final DoubleValue cloche_fluid_fertilizer_mod;

		//Lights
		public final BooleanValue lantern_spawnPrevent;
		public final IntValue lantern_energyDraw;
		public final IntValue lantern_maximumStorage;
		public final BooleanValue floodlight_spawnPrevent;
		public final IntValue floodlight_energyDraw;
		public final IntValue floodlight_maximumStorage;


		//Multiblock Recipes
		public final MachineRecipeConfig<MetalPressRecipe> metalPressConfig;
		public final MachineRecipeConfig<CrusherRecipe> crusherConfig;
		public final MachineRecipeConfig<SqueezerRecipe> squeezerConfig;
		public final MachineRecipeConfig<FermenterRecipe> fermenterConfig;
		public final MachineRecipeConfig<RefineryRecipe> refineryConfig;
		public final MachineRecipeConfig<ArcFurnaceRecipe> arcFurnaceConfig;
		public final IntValue arcfurnace_electrodeDamage;
		public final BooleanValue arcfurnace_electrodeCrafting;
		public final BooleanValue arcfurnace_recycle;
		public final MachineRecipeConfig<BlueprintCraftingRecipe> autoWorkbenchConfig;
		public final MachineRecipeConfig<BottlingMachineRecipe> bottlingMachineConfig;
		public final MachineRecipeConfig<MixerRecipe> mixerConfig;

		//Other Multiblock machines
		public final IntValue assembler_consumption;
		public final IntValue excavator_consumption;
		public final DoubleValue excavator_speed;
		public final BooleanValue excavator_particles;
		public final DoubleValue excavator_theshold;
		public final IntValue excavator_yield;
		public final DoubleValue excavator_initial_depletion;
		public final ConfigValue<List<? extends String>> excavator_dimBlacklist;

		public static class MachineRecipeConfig<T extends MultiblockRecipe>
		{
			public final DoubleValue energyModifier;
			public final DoubleValue timeModifier;

			public MachineRecipeConfig(DoubleValue energyModifier, DoubleValue timeModifier)
			{
				this.energyModifier = energyModifier;
				this.timeModifier = timeModifier;
			}

			public T apply(T recipe)
			{
				recipe.modifyTimeAndEnergy(timeModifier.get(), energyModifier.get());
				return recipe;
			}
		}
	}

	public static class Ores
	{
		Ores(Builder builder)
		{
			builder.push("ores");
			//TODO these may need to be adjusted
			ore_copper = new OreConfig(builder, "copper", 8, 40, 72, 8);
			ore_bauxite = new OreConfig(builder, "bauxite", 4, 40, 85, 8);
			ore_lead = new OreConfig(builder, "lead", 6, 8, 36, 4);
			ore_silver = new OreConfig(builder, "silver", 8, 8, 40, 4);
			ore_nickel = new OreConfig(builder, "nickel", 6, 8, 24, 2);
			ore_uranium = new OreConfig(builder, "uranium", 4, 8, 24, 2);
			oreDimBlacklist = builder
					.comment("A blacklist of dimensions in which IE ores won't spawn. By default this is Nether and End")
					.defineList("dimension_blocklist", ImmutableList.of(
							DimensionType.THE_NETHER.getRegistryName().toString(), DimensionType.THE_END.getRegistryName().toString()
					), obj -> true);
			retrogen_key = builder
					.comment("The retrogeneration key. Basically IE checks if this key is saved in the chunks data. If it isn't, it will perform retrogen on all ores marked for retrogen.", "Change this in combination with the retrogen booleans to regen only some of the ores.")
					.define("retrogen_key", "DEFAULT");
			retrogen_log_flagChunk = builder
					.comment("Set this to false to disable the logging of the chunks that were flagged for retrogen.")
					.define("retrogen_log_flagChunk", true);
			retrogen_log_remaining = builder
					.comment("Set this to false to disable the logging of the chunks that are still left to retrogen.")
					.define("retrogen_log_remaining", true);
			builder.pop();
		}


		public final OreConfig ore_copper;
		public final OreConfig ore_bauxite;
		public final OreConfig ore_lead;
		public final OreConfig ore_silver;
		public final OreConfig ore_nickel;
		public final OreConfig ore_uranium;
		public final ConfigValue<List<? extends String>> oreDimBlacklist;
		public final BooleanValue retrogen_log_flagChunk;
		public final BooleanValue retrogen_log_remaining;
		public final ConfigValue<String> retrogen_key;

		public static class OreConfig
		{
			public final IntValue veinSize;
			public final IntValue minY;
			public final IntValue maxY;
			public final IntValue veinsPerChunk;
			public final BooleanValue retrogenEnabled;

			private OreConfig(Builder builder, String name, int defSize, int defMinY, int defMaxY, int defNumPerChunk)
			{
				builder
						.comment("Ore generation config - "+name)
						.push(name);
				veinSize = builder
						.comment("The maximum size of a vein. Set to 0 to disable generation")
						.defineInRange("vein_size", defSize, 0, Integer.MAX_VALUE);
				minY = builder
						.comment("The minimum Y coordinate this ore can spawn at")
						.defineInRange("min_y", defMinY, Integer.MIN_VALUE, Integer.MAX_VALUE);
				maxY = builder
						.comment("The maximum Y coordinate this ore can spawn at")
						.defineInRange("max_y", defMaxY, Integer.MIN_VALUE, Integer.MAX_VALUE);
				veinsPerChunk = builder
						.comment("The average number of veins per chunk")
						.defineInRange("avg_veins_per_chunk", defNumPerChunk, 0, Integer.MAX_VALUE);
				retrogenEnabled = builder
						.comment("Set this to true to allow retro-generation of "+name+" Ore.")
						.define("retrogen_enable", false);
				builder.pop();
			}
		}
	}

	public static class Tools
	{
		Tools(Builder builder)
		{
			builder.push("tools");
			disableHammerCrushing = builder
					.comment("Set this to true to completely disable the ore-crushing recipes with the Engineers Hammer")
					.define("disable_hammer_crushing", false);
			hammerDurabiliy = addPositive(builder, "hammer_durability", 100, "The maximum durability of the Engineer's Hammer. Used up when hammering ingots into plates.");
			cutterDurabiliy = addPositive(builder, "cutter_durability", 250, "The maximum durability of the Wirecutter. Used up when cutting plates into wire.");
			bulletDamage_Casull = addNonNegative(builder, "bulletDamage_casull", 10, "The amount of base damage a Casull Cartridge inflicts");
			bulletDamage_AP = addNonNegative(builder, "bulletDamage_ap", 10, "The amount of base damage a armor piercing Cartridge inflicts");
			bulletDamage_Buck = addNonNegative(builder, "bulletDamage_buck", 2, "The amount of base damage a single part of buckshot inflicts");
			bulletDamage_Dragon = addNonNegative(builder, "bulletDamage_dragon", 3, "The amount of base damage a dragon breath cartridge inflicts");
			bulletDamage_Homing = addNonNegative(builder, "bulletDamage_homing", 10, "The amount of base damage a homing cartridge inflicts");
			bulletDamage_Wolfpack = addNonNegative(builder, "bulletDamage_wolfpack", 4, "The amount of base damage a wolfpack cartridge inflicts");
			bulletDamage_WolfpackPart = addNonNegative(builder, "bulletDamage_wolfpack_part", 8, "The amount of base damage the sub-projectiles of a  wolfpack cartridge inflicts");
			bulletDamage_Silver = addNonNegative(builder, "bulletDamage_silver", 10, "The amount of damage a silver bullet inflicts");
			bulletDamage_Potion = addNonNegative(builder, "bulletDamage_phial", 1, "The amount of base damage a phial cartridge inflicts");
			earDefenders_SoundBlacklist = builder
					.comment("A list of sounds that should not be muffled by the Ear Defenders. Adding to this list requires knowledge of the correct sound resource names.")
					.defineList("earDefenders_SoundBlacklist", ImmutableList.of(), obj -> true);
			chemthrower_consumption = addPositive(builder, "chemthrower_consumption", 10, "The mb of fluid the Chemical Thrower will consume per tick of usage");
			chemthrower_scroll = builder
					.comment("Set this to false to disable the use of Sneak+Scroll to switch Chemthrower tanks.")
					.define("chemthrower_scroll", true);
			railgun_consumption = addPositive(builder, "railgun_consumption", 800, "The base amount of Flux consumed per shot by the Railgun");
			railgun_damage = addNonNegative(builder, "railgun_damage_modifier", 1, "A modifier for the damage of all projectiles fired by the Railgun");
			powerpack_whitelist = builder
					.comment("A whitelist of armor pieces to allow attaching the capacitor backpack, formatting: [mod id]:[item name]")
					.defineList("powerpack_whitelist", ImmutableList.of(), obj -> true);
			powerpack_blacklist = builder
					.comment("A blacklist of armor pieces to allow attaching the capacitor backpack, formatting: [mod id]:[item name]. Whitelist has priority over this")
					.defineList("powerpack_blacklist", ImmutableList.of(
							"embers:ashen_cloak_chest", "ic2:batpack", "ic2:cf_pack", "ic2:energy_pack", "ic2:jetpack", "ic2:jetpack_electric", "ic2:lappack"
					), obj -> true);
			toolbox_tools = builder
					.comment("A whitelist of tools allowed in the toolbox, formatting: [mod id]:[item name]")
					.defineList("toolbox_tools", ImmutableList.of(), obj -> true);
			toolbox_foods = builder
					.comment("A whitelist of foods allowed in the toolbox, formatting: [mod id]:[item name]")
					.defineList("toolbox_foods", ImmutableList.of(), obj -> true);
			toolbox_wiring = builder
					.comment("A whitelist of wire-related allowed in the toolbox, formatting: [mod id]:[item name]")
					.defineList("toolbox_wiring", ImmutableList.of(), obj -> true);
			builder.pop();
		}

		private DoubleValue addNonNegative(Builder builder, String name, double defaultVal, String... desc)
		{
			return builder
					.comment(desc)
					.defineInRange(name, defaultVal, 0, Double.MAX_VALUE);
		}

		public final BooleanValue disableHammerCrushing;
		public final IntValue hammerDurabiliy;
		public final IntValue cutterDurabiliy;
		public final DoubleValue bulletDamage_Casull;
		public final DoubleValue bulletDamage_AP;
		public final DoubleValue bulletDamage_Buck;
		public final DoubleValue bulletDamage_Dragon;
		public final DoubleValue bulletDamage_Homing;
		public final DoubleValue bulletDamage_Wolfpack;
		public final DoubleValue bulletDamage_WolfpackPart;
		public final DoubleValue bulletDamage_Silver;
		public final DoubleValue bulletDamage_Potion;

		public final ConfigValue<List<? extends String>> earDefenders_SoundBlacklist;
		public final IntValue chemthrower_consumption;
		public final BooleanValue chemthrower_scroll;
		public final IntValue railgun_consumption;
		public final DoubleValue railgun_damage;
		public final ConfigValue<List<? extends String>> powerpack_whitelist;
		public final ConfigValue<List<? extends String>> powerpack_blacklist;

		public final ConfigValue<List<? extends String>> toolbox_tools;
		public final ConfigValue<List<? extends String>> toolbox_foods;
		public final ConfigValue<List<? extends String>> toolbox_wiring;

	}

	private static IntValue addPositive(Builder builder, String name, int defaultVal, String... desc)
	{
		return builder
				.comment(desc)
				.defineInRange(name, defaultVal, 1, Integer.MAX_VALUE);
	}

	public static final ForgeConfigSpec ALL;
	public static final Wires WIRES;
	public static final General GENERAL;
	public static final Machines MACHINES;
	public static final Ores ORES;
	public static final Tools TOOLS;
	public static final CachedConfigValues CACHED = new CachedConfigValues();

	static
	{
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		WIRES = new Wires(builder);
		GENERAL = new General(builder);
		MACHINES = new Machines(builder);
		ORES = new Ores(builder);
		TOOLS = new Tools(builder);

		ALL = builder.build();
	}

	private static Config rawConfig;

	public static Config getRawConfig()
	{
		if(rawConfig==null)
			try
			{
				Field childConfig = ForgeConfigSpec.class.getDeclaredField("childConfig");
				childConfig.setAccessible(true);
				rawConfig = (Config)childConfig.get(IEConfig.ALL);
				Preconditions.checkNotNull(rawConfig);
			} catch(Exception x)
			{
				throw new RuntimeException(x);
			}
		return rawConfig;
	}

	private static double[] toDoubleArray(ConfigValue<List<? extends Double>> in)
	{
		Double[] temp = in.get().toArray(new Double[0]);
		double[] ret = new double[temp.length];
		for(int i = 0; i < temp.length; ++i)
			ret[i] = temp[i];
		return ret;
	}

	private static int[] toIntArray(ConfigValue<List<? extends Integer>> in)
	{
		Integer[] temp = in.get().toArray(new Integer[0]);
		int[] ret = new int[temp.length];
		for(int i = 0; i < temp.length; ++i)
			ret[i] = temp[i];
		return ret;
	}

	@SubscribeEvent
	public static void onConfigReload(ModConfig.Reloading ev)
	{
		CACHED.wireLossRatio = toDoubleArray(WIRES.wireLossRatio);
		CACHED.wireTransferRate = toIntArray(WIRES.wireTransferRate);
		CACHED.connectorInputRates = toIntArray(MACHINES.wireConnectorInput);
		CACHED.blocksBreakWires = WIRES.blocksBreakWires.get();
		CACHED.wireDamage = WIRES.enableWireDamage.get();
		Level wireLoggerLevel;
		if(WIRES.enableWireLogger.get())
			wireLoggerLevel = Level.ALL;
		else
			wireLoggerLevel = Level.WARN;
		Configurator.setLevel(WireLogger.logger.getName(), wireLoggerLevel);
		rawConfig = null;
		if(CACHED.badEyesight!=GENERAL.badEyesight.get())
		{
			CACHED.badEyesight = GENERAL.badEyesight.get();
			ImmersiveEngineering.proxy.resetManual();
		}
	}

	@SubscribeEvent
	public static void onConfigLoad(ModConfig.Loading ev)
	{
		onConfigReload(null);
	}

	public static class CachedConfigValues
	{
		public double[] wireLossRatio;
		public int[] wireTransferRate;
		public boolean blocksBreakWires;
		public boolean wireDamage;
		public int[] connectorInputRates;
		public boolean badEyesight;
	}
}
