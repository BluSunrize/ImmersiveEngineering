/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.config;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe.RecipeMultiplier;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.utils.codec.IECodecs;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.wires.IEWireTypes.IEWireType;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.*;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class IEServerConfig
{
	public static class Wires
	{
		Wires(ModConfigSpec.Builder builder)
		{
			builder.comment("Configuration related to Immersive Engineering wires").push("wires");
			// Server
			sanitizeConnections = builder
					.comment("Attempts to make the internal data structures used for wires consistent with the connectors in the world."+
									"Use with care and backups and only when suspecting corrupted data.",
							"This option will check and load all connection endpoints and may slow down the world loading process.")
					.define("sanitizeConnections", false);
			// Split: Color in client, all others in server
			energyWireConfigs.put(
					IEWireType.COPPER,
					new EnergyWireConfig(builder, "copper", 16, 2048, 0.0125)
			);
			energyWireConfigs.put(
					IEWireType.ELECTRUM,
					new EnergyWireConfig(builder, "electrum", 16, 8192, 0.003)
			);
			energyWireConfigs.put(
					IEWireType.STEEL,
					new EnergyWireConfig(builder, "hv", 32, 32768, 0.0008)
			);
			wireConfigs.put(
					IEWireType.STRUCTURE_ROPE,
					new WireConfig(builder, "rope", 32)
			);
			wireConfigs.put(
					IEWireType.STRUCTURE_STEEL,
					new WireConfig(builder, "cable", 32)
			);
			wireConfigs.put(
					IEWireType.REDSTONE,
					new WireConfig(builder, "redstone", 32)
			);
			wireConfigs.put(
					IEWireType.COPPER_INSULATED,
					new WireConfig(builder, "insulated_copper", 16)
			);
			wireConfigs.put(
					IEWireType.ELECTRUM_INSULATED,
					new WireConfig(builder, "insulated_electrum", 16)
			);
			wireConfigs.putAll(energyWireConfigs);
			//Server
			enableWireDamage = builder.comment("If this is enabled, wires connected to power sources will cause damage to entities touching them",
							"This shouldn't cause significant lag but possibly will. If it does, please report it at https://github.com/BluSunrize/ImmersiveEngineering/issues unless there is a report of it already.")
					.define("enableWireDamage", true);
			blocksBreakWires = builder.comment("If this is enabled, placing a block in a wire will break it (drop the wire coil)")
					.define("blocksBreakWires", true);
			builder.pop();
		}

		public final BooleanValue sanitizeConnections;
		public final BooleanValue enableWireDamage;
		public final BooleanValue blocksBreakWires;
		public final Map<IEWireType, WireConfig> wireConfigs = new EnumMap<>(IEWireType.class);
		public final Map<IEWireType, EnergyWireConfig> energyWireConfigs = new EnumMap<>(IEWireType.class);

		public static class WireConfig
		{
			public final IntValue maxLength;

			protected WireConfig(ModConfigSpec.Builder builder, String name, int defLength, boolean doPop)
			{
				builder.push(name);
				maxLength = builder.comment("The maximum length of "+name+" wires")
						.defineInRange("maxLength", defLength, 0, Integer.MAX_VALUE);
				if(doPop)
					builder.pop();
			}

			public WireConfig(ModConfigSpec.Builder builder, String name, int defLength)
			{
				this(builder, name, defLength, true);
			}
		}

		public static class EnergyWireConfig extends WireConfig
		{
			public final IntValue transferRate;
			public final IntValue connectorRate;
			public final DoubleValue lossRatio;

			public EnergyWireConfig(Builder builder, String name, int defLength, int defRate, double defLoss)
			{
				super(builder, name, defLength, false);
				this.transferRate = builder.comment("The transfer rate of "+name+" wire in IF/t")
						.defineInRange("transferRate", defRate, 0, Integer.MAX_VALUE);
				this.lossRatio = builder.comment("The percentage of received power lost every 16 blocks of distance in "+name+" wire. This means exponential loss!")
						.defineInRange("distanceLoss", defLoss, 0, 1);
				this.connectorRate = builder
						.comment("In- and output rates of "+name+" wire connectors. This is independent of the transfer rate of the wires.")
						.defineInRange("wireConnectorInput", defRate/8, 0, Integer.MAX_VALUE);
				builder.pop();
			}
		}
	}

	public static class Machines
	{
		Machines(ModConfigSpec.Builder builder)
		{
			builder.push("machines");
			{
				builder.push("capacitors");
				lvCapConfig = new CapacitorConfig(builder, () -> IEBlockEntities.CAPACITOR_LV.get(), "low", 100000, 256, 256);
				mvCapConfig = new CapacitorConfig(builder, () -> IEBlockEntities.CAPACITOR_MV.get(), "medium", 1000000, 1024, 1024);
				hvCapConfig = new CapacitorConfig(builder, () -> IEBlockEntities.CAPACITOR_HV.get(), "high", 4000000, 4096, 4096);
				builder.pop();
			}
			// TODO: Make this multiplier one on next major (1.21?) update
			dynamo_output = builder
					.comment("Output modifier for the energy created by the kinetic dynamo. This will be modified by the rotation modifier of the attached water- or windmill")
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
			{
				builder.push("external_heater");
				heater_consumption = builder
						.comment("The Flux per tick consumed to add one heat to a furnace. Creates up to 4 heat in the startup time and then 1 heat per tick to keep it running")
						.defineInRange("consumption", 8, 1, Integer.MAX_VALUE);
				heater_speedupConsumption = builder
						.comment("The Flux per tick consumed to double the speed of the furnace. Only happens if furnace is at maximum heat.")
						.defineInRange("speedupConsumption", 24, 1, Integer.MAX_VALUE);
				builder.pop();
			}
			preheater_consumption = addPositive(builder, "preheater_consumption", 32, "The Flux per tick the Blast Furnace Preheater will consume to speed up the Blast Furnace");
			{
				builder.push("coredrill");
				coredrill_time = addPositive(builder, "time", 200, "The length in ticks it takes for the Core Sample Drill to figure out which mineral is found in a chunk");
				coredrill_consumption = addPositive(builder, "consumption", 40, "The Flux per tick consumed by the Core Sample Drill");
				builder.pop();
			}
			{
				builder.push("pump");
				pump_consumption = addPositive(builder, "consumption", 250, "The Flux the Fluid Pump will consume to pick up a fluid block in the world");
				pump_consumption_accelerate = addPositive(builder, "consumption_accelerate", 5, "The Flux the Fluid Pump will consume pressurize and accelerate fluids, increasing the transferrate");
				pump_placeCobble = builder
						.comment("If this is set to true (default) the pump will replace fluids it picks up with cobblestone in order to reduce lag caused by flowing fluids.")
						.define("placeCobble", true);
				builder.pop();
			}
			charger_consumption = addPositive(builder, "charger_consumption", 256,
					"The Flux per tick the Charging Station can insert into an item");
			{
				builder.push("teslacoil");
				teslacoil_consumption = addPositive(builder, "consumption", 256,
						"The Flux per tick the Tesla Coil will consume, simply by being active");
				teslacoil_consumption_active = addPositive(builder, "consumption_active", 512, "The amount of Flux the Tesla Coil will consume when shocking an entity");
				teslacoil_damage = builder
						.comment("The amount of damage the Tesla Coil will do when shocking an entity")
						.defineInRange("damage", 6D, 0, Integer.MAX_VALUE);
				builder.pop();
			}
			{
				builder.push("turret");
				turret_consumption = addPositive(builder, "consumption", 64, "The Flux per tick any turret consumes to monitor the area");
				turret_chem_consumption = addPositive(builder, "chem_consumption", 32, "The Flux per tick the chemthrower turret consumes to shoot");
				turret_gun_consumption = addPositive(builder, "gun_consumption", 32, "The Flux per tick the gun turret consumes to shoot");
				builder.pop();
			}
			{
				builder.push("garden_cloche");
				cloche_consumption = addPositive(builder, "consumption", 8, "The Flux per tick the cloche consumes to grow plants");
				cloche_fertilizer = addPositive(builder, "fertilizer", 6000, "The amount of ticks one dose of fertilizer lasts in the cloche");
				cloche_fluid = addPositive(builder, "fluid", 250, "The amount of fluid the cloche uses per dose of fertilizer");
				cloche_growth_mod = builder
						.comment("A modifier to apply to the cloches total growing speed")
						.defineInRange("growth_modifier", 1, 1e-3, 1e3);
				builder.pop();
			}
			{
				builder.push("lantern");
				lantern_spawnPrevent = builder
						.comment("Set this to false to disable the mob-spawn prevention of the Powered Lantern")
						.worldRestart()
						.define("spawnPrevent", true);
				lantern_energyDraw = addPositive(builder, "energyDraw", 1, "How much Flux the powered lantern draws per tick");
				lantern_maximumStorage = addPositive(builder, "maxStorage", 10, "How much Flux the powered lantern can hold (should be greater than the power draw)");
				builder.pop();
			}
			{
				builder.push("floodlight");
				floodlight_spawnPrevent = builder
						.comment("Set this to false to disable the mob-spawn prevention of the Floodlight")
						.worldRestart()
						.define("spawnPrevent", true);
				floodlight_energyDraw = addPositive(builder, "energyDraw", 5, "How much Flux the floodlight draws per tick");
				floodlight_maximumStorage = addPositive(builder, "max_storage", 80, "How much Flux the floodlight can hold (must be at least 10x the power draw)");
				builder.pop();
			}
			metalPressConfig = addMachineEnergyTimeModifiers(builder, "metal press");
			crusherConfig = addMachineEnergyTimeModifiers(builder, "crusher");
			squeezerConfig = addMachineEnergyTimeModifiers(builder, "squeezer");
			fermenterConfig = addMachineEnergyTimeModifiers(builder, "fermenter");
			{
				builder.push("refinery");
				refineryConfig = builder
						.comment("A modifier to apply to the energy costs of every refinery recipe")
						.defineInRange("energyModifier", 1, 1e-3, 1e3);
				builder.pop();
			}
			autoWorkbenchConfig = addMachineEnergyTimeModifiers(builder, "auto workbench");
			bottlingMachineConfig = addMachineEnergyTimeModifiers(builder, "bottling machine");
			mixerConfig = addMachineEnergyTimeModifiers(builder, "mixer");
			sawmillConfig = addMachineEnergyTimeModifiers(builder, "sawmill");
			sawmill_bladeDamage = addPositive(builder, "sawmill_bladeDamage", 5, "The amount of damage a sawblade in the sawmill takes for a single recipe.");
			{
				arcFurnaceConfig = addMachineEnergyTimeModifiers(builder, "arc furnace", false);
				arcfurnace_electrodeDamage = addPositive(builder, "electrodeDamage", 96000, "The maximum amount of damage Graphite Electrodes can take. While the furnace is working, electrodes sustain 1 damage per tick, so this is effectively the lifetime in ticks. The default value of 96000 makes them last for 8 consecutive ingame days");
				builder.comment(
						"The recycling functionality of the arc furnace is no longer controlled by a config option.",
						"Like all IE recipes, this is now controlled via a datapack, using the recipe file:",
						"immersiveengineering/recipes/arc_recycling_list.json",
						"To disable recycling, add that file to your datapack and fill it with the following content:",
						"{",
						"  \"type\": \"immersiveengineering:generated_list\",",
						"  \"conditions\": [ { \"type\": \"forge:false\" } ]",
						"}"
				).define("recycle", "");
				builder.pop();
			}

			assembler_consumption = addPositive(builder, "assembler_consumption", 80, "The Flux the Assembler will consume to craft an item from a recipe");
			{
				builder.push("excavator");
				excavator_consumption = addPositive(builder, "consumption", 4096, "The Flux per tick the Excavator will consume to dig");
				excavator_speed = builder
						.comment("The speed of the Excavator. Basically translates to how many degrees per tick it will turn.")
						.defineInRange("speed", 1, 1e-3, 1e3);
				excavator_particles = builder
						.comment("Set this to false to disable the ridiculous amounts of particles the Excavator spawns")
						.define("particles", true);
				excavator_theshold = builder
						.comment("The threshold the perlin noise has to cross for a mineral vein to be generated. Higher means less likely.")
						.defineInRange("chance", .9, 0, 1);
				excavator_yield = builder
						.comment("The maximum amount of yield one can get out of a chunk with the excavator. Set a number smaller than zero to make it infinite")
						.defineInRange("yield", 38400, -1, Integer.MAX_VALUE);
				excavator_initial_depletion = builder
						.comment("The maximum depletion a vein can start with, as a decimal value. When a vein generates, a random percentage up to this value is depleted from it")
						.defineInRange("initial_depletion", .2, 0, 1);
				builder.pop();
			}
			radio_tower_consumption = addPositive(builder, "radio_tower_consumption", 128, "The Flux the Radio Tower will consume per tick to remain active");
			resonanz_observer_consumption = addPositive(builder, "resonanz_observer_consumption", 128, "The Flux the Resonanz Observer will consume per tick to remain active");
			resonanz_observer_paper_duration = addPositive(builder, "resonanz_observer_paper_duration", 60, "The duration in seconds that a single piece of paper will fuel the Resonanz Observer for");
			resonanz_observer_radius = addPositive(builder, "resonanz_observer_radius", 32, "The radius in blocks the Resonanz Observer will cover. The actual loaded chunks may extend beyond this radius.");
			builder.pop();
		}

		public void populateAPI()
		{
			MetalPressRecipe.MULTIPLIERS.setValue(metalPressConfig);
			CrusherRecipe.MULTIPLIERS.setValue(crusherConfig);
			SqueezerRecipe.MULTIPLIERS.setValue(squeezerConfig);
			FermenterRecipe.MULTIPLIERS.setValue(fermenterConfig);
			RefineryRecipe.MULTIPLIERS.setValue(new RecipeMultiplier(() -> 1, refineryConfig::get));
			ArcFurnaceRecipe.MULTIPLIERS.setValue(arcFurnaceConfig);
			BlueprintCraftingRecipe.MULTIPLIERS.setValue(autoWorkbenchConfig);
			BottlingMachineRecipe.MULTIPLIERS.setValue(bottlingMachineConfig);
			MixerRecipe.MULTIPLIERS.setValue(mixerConfig);
			SawmillRecipe.MULTIPLIERS.setValue(sawmillConfig);
		}

		private RecipeMultiplier addMachineEnergyTimeModifiers(Builder builder, String machine)
		{
			return addMachineEnergyTimeModifiers(builder, machine, true);
		}

		private RecipeMultiplier addMachineEnergyTimeModifiers(Builder builder, String machine, boolean popCategory)
		{
			builder.push(machine.replace(' ', '_'));
			DoubleValue energy = builder
					.comment("A modifier to apply to the energy costs of every "+machine+" recipe")
					.defineInRange("energyModifier", 1, 1e-3, 1e3);
			DoubleValue time = builder
					.comment("A modifier to apply to the time of every "+machine+" recipe")
					.defineInRange("timeModifier", 1, 1e-3, 1e3);
			if(popCategory)
				builder.pop();
			return new RecipeMultiplier(energy::get, time::get);
		}

		public static class CapacitorConfig
		{
			public static final CapacitorConfig CREATIVE = new CapacitorConfig(
					Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, () -> IEBlockEntities.CAPACITOR_CREATIVE.get()
			);
			public final IntSupplier storage;
			public final IntSupplier input;
			public final IntSupplier output;
			public final Supplier<BlockEntityType<? extends CapacitorBlockEntity>> tileType;

			private CapacitorConfig(Builder builder, Supplier<BlockEntityType<? extends CapacitorBlockEntity>> tileType, String voltage, int defaultStorage, int defaultInput, int defaultOutput)
			{
				this.tileType = tileType;
				builder
						.comment("Configuration for the "+voltage+" voltage capacitor")
						.push(voltage.charAt(0)+"v");
				storage = builder
						.comment("Maximum energy stored (Flux)")
						.defineInRange("storage", defaultStorage, 1, Integer.MAX_VALUE)::get;
				input = builder
						.comment("Maximum energy input (Flux/tick)")
						.defineInRange("input", defaultInput, 1, Integer.MAX_VALUE)::get;
				output = builder
						.comment("Maximum energy output (Flux/tick)")
						.defineInRange("output", defaultOutput, 1, Integer.MAX_VALUE)::get;
				builder.pop();
			}

			private CapacitorConfig(int storage, int input, int output, Supplier<BlockEntityType<? extends CapacitorBlockEntity>> type)
			{
				this.storage = () -> storage;
				this.input = () -> input;
				this.output = () -> output;
				this.tileType = type;
			}
		}

		//Capacitors
		public final CapacitorConfig lvCapConfig;
		public final CapacitorConfig mvCapConfig;
		public final CapacitorConfig hvCapConfig;

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

		//Lights
		public final BooleanValue lantern_spawnPrevent;
		public final IntValue lantern_energyDraw;
		public final IntValue lantern_maximumStorage;
		public final BooleanValue floodlight_spawnPrevent;
		public final IntValue floodlight_energyDraw;
		public final IntValue floodlight_maximumStorage;


		//Multiblock Recipes
		public final RecipeMultiplier metalPressConfig;
		public final RecipeMultiplier crusherConfig;
		public final RecipeMultiplier squeezerConfig;
		public final RecipeMultiplier fermenterConfig;
		public final DoubleValue refineryConfig;
		public final RecipeMultiplier arcFurnaceConfig;
		public final IntValue arcfurnace_electrodeDamage;
		public final RecipeMultiplier autoWorkbenchConfig;
		public final RecipeMultiplier bottlingMachineConfig;
		public final RecipeMultiplier mixerConfig;
		public final RecipeMultiplier sawmillConfig;
		public final IntValue sawmill_bladeDamage;

		//Other Multiblock machines
		public final IntValue assembler_consumption;
		public final IntValue excavator_consumption;
		public final DoubleValue excavator_speed;
		public final BooleanValue excavator_particles;
		public final DoubleValue excavator_theshold;
		public final IntValue excavator_yield;
		public final DoubleValue excavator_initial_depletion;
		public final IntValue radio_tower_consumption;
		public final IntValue resonanz_observer_consumption;
		public final IntValue resonanz_observer_paper_duration;
		public final IntValue resonanz_observer_radius;
	}

	public static class Ores
	{
		Ores(Builder builder)
		{
			builder.push("ores");
			//Server
			for(VeinType type : VeinType.values())
				ores.put(type, new OreConfig(builder, type));
			retrogenExcavatorVeins = builder
					.comment("Set to true to generate excavator veins in chunks not generated with IE")
					.define("retrogen_excavator_veins", false);
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


		public final Map<VeinType, OreConfig> ores = new EnumMap<>(VeinType.class);
		public final BooleanValue retrogenExcavatorVeins;
		public final BooleanValue retrogen_log_flagChunk;
		public final BooleanValue retrogen_log_remaining;
		public final ConfigValue<String> retrogen_key;

		public static class OreConfig
		{
			public final EnumValue<OreDistribution> distribution;
			public final DoubleValue airExposure;
			public final IntValue veinSize;
			public final IntValue minY;
			public final IntValue maxY;
			public final IntValue veinsPerChunk;
			public final BooleanValue retrogenEnabled;

			private OreConfig(Builder builder, VeinType type)
			{
				String name = type.getVeinName();
				builder
						.comment("Ore generation config - "+name)
						.push(name);
				distribution = builder
						.comment("The distribution shape. UNIFORM is evenly distributed across the height range, TRAPEZOID favors the middle of the range.")
						.defineEnum("distribution", type.defaultDistribution);
				airExposure = builder
						.comment("Chance for ores to not generate, if they are exposed to air. 0 means ignoring air exposure, 1 requires being burried.")
						.defineInRange("air_exposure", type.defaultAirExposure, 0, 1);
				veinSize = builder
						.comment("The maximum size of a vein. Set to 0 to disable generation")
						.defineInRange("vein_size", type.defaultVeinSize, 0, Integer.MAX_VALUE);
				minY = builder
						.comment("The minimum Y coordinate this ore can spawn at")
						.defineInRange("min_y", type.defaultMinY, Integer.MIN_VALUE, Integer.MAX_VALUE);
				maxY = builder
						.comment("The maximum Y coordinate this ore can spawn at")
						.defineInRange("max_y", type.defaultMaxY, Integer.MIN_VALUE, Integer.MAX_VALUE);
				veinsPerChunk = builder
						.comment("The number of veins attempted to be generated per chunk")
						.defineInRange("attempts_per_chunk", type.defaultVeinsPerChunk, 0, Integer.MAX_VALUE);
				retrogenEnabled = builder
						.comment("Set this to true to allow retro-generation of "+name+" Ore.")
						.define("retrogen_enable", false);
				builder.pop();
			}
		}

		public enum OreDistribution
		{
			UNIFORM,
			TRAPEZOID;
		}

		public enum VeinType
		{

			BAUXITE(EnumMetals.ALUMINUM, OreDistribution.TRAPEZOID, 0, 6, 32, 112, 16),
			LEAD(EnumMetals.LEAD, OreDistribution.TRAPEZOID, 0, 8, -32, 80, 12),
			SILVER(EnumMetals.SILVER, OreDistribution.TRAPEZOID, 0.25, 9, -48, 32, 10),
			NICKEL(EnumMetals.NICKEL, OreDistribution.UNIFORM, 0, 5, -64, 24, 7),
			DEEP_NICKEL(EnumMetals.NICKEL, OreDistribution.TRAPEZOID, 0, 6, -120, -8, 11),
			URANIUM(EnumMetals.URANIUM, OreDistribution.TRAPEZOID, 0.5, 4, -64, -16, 9),
			;
			public static final VeinType[] VALUES = values();
			public static final MapCodec<VeinType> CODEC = IECodecs.enumCodec(VALUES).fieldOf("veinType");

			public final EnumMetals metal;
			private final OreDistribution defaultDistribution;
			private final double defaultAirExposure;
			private final int defaultVeinSize;
			private final int defaultMinY;
			private final int defaultMaxY;
			private final int defaultVeinsPerChunk;

			VeinType(EnumMetals metal, OreDistribution defaultDistribution, double defaultAirExposure, int defaultVeinSize, int defaultMinY, int defaultMaxY, int defaultVeinsPerChunk)
			{
				this.metal = metal;
				this.defaultDistribution = defaultDistribution;
				this.defaultAirExposure = defaultAirExposure;
				this.defaultVeinSize = defaultVeinSize;
				this.defaultMinY = defaultMinY;
				this.defaultMaxY = defaultMaxY;
				this.defaultVeinsPerChunk = defaultVeinsPerChunk;
			}

			public String getVeinName()
			{
				return name().toLowerCase(Locale.ROOT);
			}
		}
	}

	public static class Tools
	{
		Tools(Builder builder)
		{
			builder.push("tools");
			hammerDurabiliy = addPositive(builder, "hammer_durability", 100, "The maximum durability of the Engineer's Hammer. Used up when hammering ingots into plates.");
			cutterDurabiliy = addPositive(builder, "cutter_durability", 250, "The maximum durability of the Wirecutter. Used up when cutting plates into wire.");
			{
				builder.push("bullet_damage");
				bulletDamage_Casull = addNonNegative(builder, "casull", 10, "The amount of base damage a Casull Cartridge inflicts");
				bulletDamage_AP = addNonNegative(builder, "ap", 10, "The amount of base damage a armor piercing Cartridge inflicts");
				bulletDamage_Buck = addNonNegative(builder, "buck", 2, "The amount of base damage a single part of buckshot inflicts");
				bulletDamage_Dragon = addNonNegative(builder, "dragon", 1, "The amount of base damage a dragon breath cartridge inflicts");
				bulletDamage_Homing = addNonNegative(builder, "homing", 10, "The amount of base damage a homing cartridge inflicts");
				bulletDamage_Wolfpack = addNonNegative(builder, "wolfpack", 4, "The amount of base damage a wolfpack cartridge inflicts");
				bulletDamage_WolfpackPart = addNonNegative(builder, "wolfpack_part", 8, "The amount of base damage the sub-projectiles of a  wolfpack cartridge inflicts");
				bulletDamage_Silver = addNonNegative(builder, "silver", 10, "The amount of damage a silver bullet inflicts");
				bulletDamage_Potion = addNonNegative(builder, "phial", 1, "The amount of base damage a phial cartridge inflicts");
				builder.pop();
			}
			// Server
			{
				builder.push("chemthrower");
				chemthrower_consumption = addPositive(builder, "consumption", 10, "The mb of fluid the Chemical Thrower will consume per tick of usage");
				chemthrower_scroll = builder
						.comment("Set this to false to disable the use of Sneak+Scroll to switch Chemthrower tanks.")
						.define("scroll", true);
				builder.pop();
			}
			{
				builder.push("railgun");
				railgun_consumption = addPositive(builder, "consumption", 800, "The base amount of Flux consumed per shot by the Railgun");
				railgun_damage = addNonNegative(builder, "damage_modifier", 1, "A modifier for the damage of all projectiles fired by the Railgun");
				builder.pop();
			}
			builder.pop();
		}

		private DoubleValue addNonNegative(Builder builder, String name, double defaultVal, String... desc)
		{
			return builder
					.comment(desc)
					.defineInRange(name, defaultVal, 0, Double.MAX_VALUE);
		}

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

		public final IntValue chemthrower_consumption;
		//TODO rename to include buzzsaw
		public final BooleanValue chemthrower_scroll;
		public final IntValue railgun_consumption;
		public final DoubleValue railgun_damage;

	}

	private static IntValue addPositive(Builder builder, String name, int defaultVal, String... desc)
	{
		return builder
				.comment(desc)
				.defineInRange(name, defaultVal, 1, Integer.MAX_VALUE);
	}

	public static final ModConfigSpec CONFIG_SPEC;
	public static final Wires WIRES;
	public static final Machines MACHINES;
	public static final Ores ORES;
	public static final Tools TOOLS;

	static
	{
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		WIRES = new Wires(builder);
		MACHINES = new Machines(builder);
		ORES = new Ores(builder);
		TOOLS = new Tools(builder);

		CONFIG_SPEC = builder.build();
	}

	@SubscribeEvent
	public static void onConfigReload(ModConfigEvent.Reloading ev)
	{
		if(CONFIG_SPEC==ev.getConfig().getSpec())
			refresh();
	}

	@SubscribeEvent
	public static void onConfigLoad(ModConfigEvent.Loading ev)
	{
		if(CONFIG_SPEC==ev.getConfig().getSpec())
			refresh();
	}

	public static int getOrDefault(IntValue value)
	{
		return CONFIG_SPEC.isLoaded()?value.get(): value.getDefault();
	}

	public static void refresh()
	{
		ExternalHeaterHandler.defaultFurnaceEnergyCost = IEServerConfig.MACHINES.heater_consumption.get();
		ExternalHeaterHandler.defaultFurnaceSpeedupCost = IEServerConfig.MACHINES.heater_speedupConsumption.get();
		ExcavatorHandler.mineralVeinYield = IEServerConfig.MACHINES.excavator_yield.get();
		ExcavatorHandler.initialVeinDepletion = IEServerConfig.MACHINES.excavator_initial_depletion.get();
		ExcavatorHandler.mineralNoiseThreshold = IEServerConfig.MACHINES.excavator_theshold.get();
		IEWorldGen.onConfigUpdated();
	}
}
