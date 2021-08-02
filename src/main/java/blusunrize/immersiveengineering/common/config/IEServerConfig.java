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
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorTileEntity;
import blusunrize.immersiveengineering.common.config.CachedConfig.*;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import blusunrize.immersiveengineering.common.wires.IEWireTypes.IEWireType;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class IEServerConfig
{
	public static class Wires
	{
		Wires(CachedConfig.Builder builder)
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
					new EnergyWireConfig(builder, "copper", 16, 2048, 0.05)
			);
			energyWireConfigs.put(
					IEWireType.ELECTRUM,
					new EnergyWireConfig(builder, "electrum", 16, 8192, 0.025)
			);
			energyWireConfigs.put(
					IEWireType.STEEL,
					new EnergyWireConfig(builder, "hv", 32, 32768, 0.025)
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

			protected WireConfig(CachedConfig.Builder builder, String name, int defLength, boolean doPop)
			{
				builder.push(name);
				maxLength = builder.comment("The maximum length of "+name+" wires")
						.defineInRange("maxLength", defLength, 0, Integer.MAX_VALUE);
				if(doPop)
					builder.pop();
			}

			public WireConfig(CachedConfig.Builder builder, String name, int defLength)
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
				this.lossRatio = builder.comment("The percentage of power lost every 16 blocks of distance in "+name+" wire")
						.defineInRange("loss", defLoss, 0, 1);
				this.connectorRate = builder
						.comment("In- and output rates of "+name+" wire connectors. This is independant of the transferrate of the wires.")
						.defineInRange("wireConnectorInput", defRate/8, 0, Integer.MAX_VALUE);
				builder.pop();
			}
		}
	}

	public static class Machines
	{
		Machines(CachedConfig.Builder builder)
		{
			builder.push("machines");
			{
				builder.push("capacitors");
				lvCapConfig = new CapacitorConfig(builder, () -> IETileTypes.CAPACITOR_LV.get(), "low", 100000, 256, 256);
				mvCapConfig = new CapacitorConfig(builder, () -> IETileTypes.CAPACITOR_MV.get(), "medium", 1000000, 1024, 1024);
				hvCapConfig = new CapacitorConfig(builder, () -> IETileTypes.CAPACITOR_HV.get(), "high", 4000000, 4096, 4096);
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
				pump_infiniteWater = builder
						.comment("Set this to false to disable the fluid pump being able to draw infinite water from sources")
						.define("infiniteWater", true);
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
				cloche_solid_fertilizer_mod = builder
						.comment("A base-modifier for all solid fertilizers in the cloche")
						.defineInRange("solid_fertilizer_mod", 1, 1e-3, 1e3);
				cloche_fluid_fertilizer_mod = builder
						.comment("A base-modifier for all fluid fertilizers in the cloche")
						.defineInRange("fluid_fertilizer_mod", 1, 1e-3, 1e3);
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
				excavator_dimBlacklist = builder
						.comment("List of dimensions that can't contain minerals. Default: The End.")
						.defineList("dimBlacklist", ImmutableList.of(DimensionType.END_LOCATION.location().toString()),
								obj -> true);
				builder.pop();
			}
			builder.pop();
		}

		private <T extends MultiblockRecipe> MachineRecipeConfig<T> addMachineEnergyTimeModifiers(Builder builder, String machine)
		{
			return addMachineEnergyTimeModifiers(builder, machine, true);
		}

		private <T extends MultiblockRecipe> MachineRecipeConfig<T> addMachineEnergyTimeModifiers(Builder builder, String machine, boolean popCategory)
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
			return new MachineRecipeConfig<>(energy, time);
		}

		public static class CapacitorConfig
		{
			public static final CapacitorConfig CREATIVE = new CapacitorConfig(
					Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, () -> IETileTypes.CAPACITOR_CREATIVE.get()
			);
			public final IntSupplier storage;
			public final IntSupplier input;
			public final IntSupplier output;
			public final Supplier<BlockEntityType<? extends CapacitorTileEntity>> tileType;

			private CapacitorConfig(Builder builder, Supplier<BlockEntityType<? extends CapacitorTileEntity>> tileType, String voltage, int defaultStorage, int defaultInput, int defaultOutput)
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

			private CapacitorConfig(int storage, int input, int output, Supplier<BlockEntityType<? extends CapacitorTileEntity>> type)
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
		public final DoubleValue refineryConfig;
		public final MachineRecipeConfig<ArcFurnaceRecipe> arcFurnaceConfig;
		public final IntValue arcfurnace_electrodeDamage;
		public final MachineRecipeConfig<BlueprintCraftingRecipe> autoWorkbenchConfig;
		public final MachineRecipeConfig<BottlingMachineRecipe> bottlingMachineConfig;
		public final MachineRecipeConfig<MixerRecipe> mixerConfig;
		public final MachineRecipeConfig<SawmillRecipe> sawmillConfig;
		public final IntValue sawmill_bladeDamage;

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

			public T apply(T in)
			{
				in.modifyTimeAndEnergy(timeModifier, energyModifier);
				return in;
			}
		}
	}

	public static class Ores
	{
		Ores(Builder builder)
		{
			builder.push("ores");
			//Server
			ore_copper = new OreConfig(builder, "copper", 8, 40, 72, 8);
			ore_bauxite = new OreConfig(builder, "bauxite", 4, 40, 85, 8);
			ore_lead = new OreConfig(builder, "lead", 6, 8, 36, 4);
			ore_silver = new OreConfig(builder, "silver", 8, 8, 40, 4);
			ore_nickel = new OreConfig(builder, "nickel", 6, 8, 24, 2);
			ore_uranium = new OreConfig(builder, "uranium", 4, 8, 24, 2);
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
			//Server
			// TODO read too early. Can that be worked around?
			hammerDurabiliy = addPositive(builder, "hammer_durability", 100, "The maximum durability of the Engineer's Hammer. Used up when hammering ingots into plates.");
			cutterDurabiliy = addPositive(builder, "cutter_durability", 250, "The maximum durability of the Wirecutter. Used up when cutting plates into wire.");
			{
				builder.push("bullet_damage");
				bulletDamage_Casull = addNonNegative(builder, "casull", 10, "The amount of base damage a Casull Cartridge inflicts");
				bulletDamage_AP = addNonNegative(builder, "ap", 10, "The amount of base damage a armor piercing Cartridge inflicts");
				bulletDamage_Buck = addNonNegative(builder, "buck", 2, "The amount of base damage a single part of buckshot inflicts");
				bulletDamage_Dragon = addNonNegative(builder, "dragon", 3, "The amount of base damage a dragon breath cartridge inflicts");
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
			{
				builder.push("powerpack");
				powerpack_whitelist = builder
						.comment("A whitelist of armor pieces to allow attaching the capacitor backpack, formatting: [mod id]:[item name]")
						.defineList("whitelist", ImmutableList.of(), obj -> true);
				//TODO update list for 1.16.3
				powerpack_blacklist = builder
						.comment("A blacklist of armor pieces to allow attaching the capacitor backpack, formatting: [mod id]:[item name]. Whitelist has priority over this")
						.defineList("blacklist", ImmutableList.of(
								"embers:ashen_cloak_chest", "ic2:batpack", "ic2:cf_pack", "ic2:energy_pack", "ic2:jetpack", "ic2:jetpack_electric", "ic2:lappack"
						), obj -> true);
				builder.pop();
			}
			{
				builder.push("toolbox");
				toolbox_tools = builder
						.comment("A whitelist of tools allowed in the toolbox, formatting: [mod id]:[item name]")
						.defineList("tools", ImmutableList.of(), obj -> true);
				toolbox_foods = builder
						.comment("A whitelist of foods allowed in the toolbox, formatting: [mod id]:[item name]")
						.defineList("foods", ImmutableList.of(), obj -> true);
				toolbox_wiring = builder
						.comment("A whitelist of wire-related allowed in the toolbox, formatting: [mod id]:[item name]")
						.defineList("wiring", ImmutableList.of(), obj -> true);
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

	public static final CachedConfig CONFIG_SPEC;
	public static final Wires WIRES;
	public static final Machines MACHINES;
	public static final Ores ORES;
	public static final Tools TOOLS;

	static
	{
		CachedConfig.Builder builder = new CachedConfig.Builder();
		WIRES = new Wires(builder);
		MACHINES = new Machines(builder);
		ORES = new Ores(builder);
		TOOLS = new Tools(builder);

		CONFIG_SPEC = builder.build();
	}

	private static Config rawConfig;

	public static Config getRawConfig()
	{
		if(rawConfig==null)
			try
			{
				Field childConfig = ForgeConfigSpec.class.getDeclaredField("childConfig");
				childConfig.setAccessible(true);
				rawConfig = (Config)childConfig.get(IEServerConfig.CONFIG_SPEC);
				Preconditions.checkNotNull(rawConfig);
			} catch(Exception x)
			{
				throw new RuntimeException(x);
			}
		return rawConfig;
	}

	@SubscribeEvent
	public static void onConfigReload(ModConfigEvent ev)
	{
		if(CONFIG_SPEC.reloadIfMatched(ev, Type.SERVER))
		{
			rawConfig = ev.getConfig().getConfigData();
			refresh();
		}
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
