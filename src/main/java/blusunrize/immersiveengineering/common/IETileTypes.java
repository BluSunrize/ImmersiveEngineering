/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.FakeLightBlock.FakeLightTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.cloth.BalloonTileEntity;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerTileEntity;
import blusunrize.immersiveengineering.common.blocks.cloth.StripCurtainTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.stone.*;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class IETileTypes
{
	public static final DeferredRegister<TileEntityType<?>> REGISTER = DeferredRegister.create(
			ForgeRegistries.TILE_ENTITIES, ImmersiveEngineering.MODID);

	public static final RegistryObject<TileEntityType<BalloonTileEntity>> BALLOON = REGISTER.register(
			"balloon", makeType(BalloonTileEntity::new, Cloth.balloon)
	);
	public static final RegistryObject<TileEntityType<StripCurtainTileEntity>> STRIP_CURTAIN = REGISTER.register(
			"stripcurtain", makeType(StripCurtainTileEntity::new, Cloth.curtain)
	);
	public static final RegistryObject<TileEntityType<ShaderBannerTileEntity>> SHADER_BANNER = REGISTER.register(
			"shaderbanner",
			makeTypeMultipleBlocks(ShaderBannerTileEntity::new, ImmutableSet.of(Cloth.shaderBanner, Cloth.shaderBannerWall))
	);
	public static final RegistryObject<TileEntityType<CokeOvenTileEntity>> COKE_OVEN = REGISTER.register(
			"cokeoven", makeType(CokeOvenTileEntity::new, Multiblocks.cokeOven)
	);
	public static final RegistryObject<TileEntityType<BlastFurnaceTileEntity>> BLAST_FURNACE = REGISTER.register(
			"blastfurnace", makeType(BlastFurnaceTileEntity::new, Multiblocks.blastFurnace)
	);
	public static final RegistryObject<TileEntityType<BlastFurnaceAdvancedTileEntity>> BLAST_FURNACE_ADVANCED = REGISTER.register(
			"blastfurnaceadvanced", makeType(BlastFurnaceAdvancedTileEntity::new, Multiblocks.blastFurnaceAdv)
	);
	public static final RegistryObject<TileEntityType<CoresampleTileEntity>> CORE_SAMPLE = REGISTER.register(
			"coresample", makeType(CoresampleTileEntity::new, StoneDecoration.coresample)
	);
	public static final RegistryObject<TileEntityType<AlloySmelterTileEntity>> ALLOY_SMELTER = REGISTER.register(
			"alloysmelter", makeType(AlloySmelterTileEntity::new, Multiblocks.alloySmelter)
	);
	public static final RegistryObject<TileEntityType<CraftingTableTileEntity>> CRAFTING_TABLE = REGISTER.register(
			"craftingtable", makeType(CraftingTableTileEntity::new, WoodenDevices.craftingTable)
	);
	public static final RegistryObject<TileEntityType<WoodenCrateTileEntity>> WOODEN_CRATE = REGISTER.register(
			"woodencrate", makeType(WoodenCrateTileEntity::new, WoodenDevices.crate)
	);
	public static final RegistryObject<TileEntityType<WoodenBarrelTileEntity>> WOODEN_BARREL = REGISTER.register(
			"woodenbarrel", makeType(WoodenBarrelTileEntity::new, WoodenDevices.woodenBarrel)
	);
	public static final RegistryObject<TileEntityType<ModWorkbenchTileEntity>> MOD_WORKBENCH = REGISTER.register(
			"modworkbench", makeType(ModWorkbenchTileEntity::new, WoodenDevices.workbench)
	);
	public static final RegistryObject<TileEntityType<CircuitTableTileEntity>> CIRCUIT_TABLE = REGISTER.register(
			"circuittable", makeType(CircuitTableTileEntity::new, WoodenDevices.circuitTable)
	);
	public static final RegistryObject<TileEntityType<SorterTileEntity>> SORTER = REGISTER.register(
			"sorter", makeType(SorterTileEntity::new, WoodenDevices.sorter)
	);
	public static final RegistryObject<TileEntityType<ItemBatcherTileEntity>> ITEM_BATCHER = REGISTER.register(
			"itembatcher", makeType(ItemBatcherTileEntity::new, WoodenDevices.itemBatcher)
	);
	public static final RegistryObject<TileEntityType<TurntableTileEntity>> TURNTABLE = REGISTER.register(
			"turntable", makeType(TurntableTileEntity::new, WoodenDevices.turntable)
	);
	public static final RegistryObject<TileEntityType<FluidSorterTileEntity>> FLUID_SORTER = REGISTER.register(
			"fluidsorter", makeType(FluidSorterTileEntity::new, WoodenDevices.fluidSorter)
	);
	public static final RegistryObject<TileEntityType<LogicUnitTileEntity>> LOGIC_UNIT = REGISTER.register(
			"logicunit", makeType(LogicUnitTileEntity::new, WoodenDevices.logicUnit)
	);
	public static final RegistryObject<TileEntityType<WatermillTileEntity>> WATERMILL = REGISTER.register(
			"watermill", makeType(WatermillTileEntity::new, WoodenDevices.watermill)
	);
	public static final RegistryObject<TileEntityType<WindmillTileEntity>> WINDMILL = REGISTER.register(
			"windmill", makeType(WindmillTileEntity::new, WoodenDevices.windmill)
	);
	public static final RegistryObject<TileEntityType<RazorWireTileEntity>> RAZOR_WIRE = REGISTER.register(
			"razorwire", makeType(RazorWireTileEntity::new, MetalDevices.razorWire)
	);
	public static final RegistryObject<TileEntityType<ToolboxTileEntity>> TOOLBOX = REGISTER.register(
			"toolbox", makeType(ToolboxTileEntity::new, MetalDevices.toolbox)
	);
	public static final RegistryObject<TileEntityType<StructuralArmTileEntity>> STRUCTURAL_ARM = REGISTER.register(
			"structuralarm",
			makeTypeMultipleBlocks(StructuralArmTileEntity::new, ImmutableSet.of(MetalDecoration.slopeAlu, MetalDecoration.slopeSteel))
	);
	public static final RegistryObject<TileEntityType<ConnectorStructuralTileEntity>> CONNECTOR_STRUCTURAL = REGISTER.register(
			"connectorstructural", makeType(ConnectorStructuralTileEntity::new, Connectors.connectorStructural)
	);
	public static final RegistryObject<TileEntityType<TransformerTileEntity>> TRANSFORMER = REGISTER.register(
			"transformer", makeType(TransformerTileEntity::new, Connectors.transformer)
	);
	public static final RegistryObject<TileEntityType<PostTransformerTileEntity>> POST_TRANSFORMER = REGISTER.register(
			"posttransformer", makeType(PostTransformerTileEntity::new, Connectors.postTransformer)
	);
	public static final RegistryObject<TileEntityType<TransformerHVTileEntity>> TRANSFORMER_HV = REGISTER.register(
			"transformerhv", makeType(TransformerHVTileEntity::new, Connectors.transformerHV)
	);
	public static final RegistryObject<TileEntityType<BreakerSwitchTileEntity>> BREAKER_SWITCH = REGISTER.register(
			"breakerswitch", makeType(BreakerSwitchTileEntity::new, Connectors.breakerswitch)
	);
	public static final RegistryObject<TileEntityType<RedstoneBreakerTileEntity>> REDSTONE_BREAKER = REGISTER.register(
			"redstonebreaker", makeType(RedstoneBreakerTileEntity::new, Connectors.redstoneBreaker)
	);
	public static final RegistryObject<TileEntityType<EnergyMeterTileEntity>> ENERGY_METER = REGISTER.register(
			"energymeter", makeType(EnergyMeterTileEntity::new, Connectors.currentTransformer)
	);
	public static final RegistryObject<TileEntityType<ConnectorRedstoneTileEntity>> CONNECTOR_REDSTONE = REGISTER.register(
			"connectorredstone", makeType(ConnectorRedstoneTileEntity::new, Connectors.connectorRedstone)
	);
	public static final RegistryObject<TileEntityType<ConnectorProbeTileEntity>> CONNECTOR_PROBE = REGISTER.register(
			"connectorprobe", makeType(ConnectorProbeTileEntity::new, Connectors.connectorProbe)
	);
	public static final RegistryObject<TileEntityType<ConnectorBundledTileEntity>> CONNECTOR_BUNDLED = REGISTER.register(
			"connectorbundled", makeType(ConnectorBundledTileEntity::new, Connectors.connectorBundled)
	);
	public static final RegistryObject<TileEntityType<FeedthroughTileEntity>> FEEDTHROUGH = REGISTER.register(
			"feedthrough", makeType(FeedthroughTileEntity::new, Connectors.feedthrough)
	);
	public static final RegistryObject<TileEntityType<CapacitorTileEntity>> CAPACITOR_LV = REGISTER.register(
			"capacitorlv", makeType(() -> new CapacitorTileEntity(IEServerConfig.MACHINES.lvCapConfig), MetalDevices.capacitorLV)
	);
	public static final RegistryObject<TileEntityType<CapacitorTileEntity>> CAPACITOR_MV = REGISTER.register(
			"capacitormv", makeType(() -> new CapacitorTileEntity(IEServerConfig.MACHINES.mvCapConfig), MetalDevices.capacitorMV)
	);
	public static final RegistryObject<TileEntityType<CapacitorTileEntity>> CAPACITOR_HV = REGISTER.register(
			"capacitorhv", makeType(() -> new CapacitorTileEntity(IEServerConfig.MACHINES.hvCapConfig), MetalDevices.capacitorHV)
	);
	public static final RegistryObject<TileEntityType<CapacitorCreativeTileEntity>> CAPACITOR_CREATIVE = REGISTER.register(
			"capacitorcreative", makeType(CapacitorCreativeTileEntity::new, MetalDevices.capacitorCreative)
	);
	public static final RegistryObject<TileEntityType<MetalBarrelTileEntity>> METAL_BARREL = REGISTER.register(
			"metalbarrel", makeType(MetalBarrelTileEntity::new, MetalDevices.barrel)
	);
	public static final RegistryObject<TileEntityType<FluidPumpTileEntity>> FLUID_PUMP = REGISTER.register(
			"fluidpump", makeType(FluidPumpTileEntity::new, MetalDevices.fluidPump)
	);
	public static final RegistryObject<TileEntityType<FluidPlacerTileEntity>> FLUID_PLACER = REGISTER.register(
			"fluidplacer", makeType(FluidPlacerTileEntity::new, MetalDevices.fluidPlacer)
	);
	public static final RegistryObject<TileEntityType<BlastFurnacePreheaterTileEntity>> BLASTFURNACE_PREHEATER = REGISTER.register(
			"blastfurnacepreheater", makeType(BlastFurnacePreheaterTileEntity::new, MetalDevices.blastFurnacePreheater)
	);
	public static final RegistryObject<TileEntityType<FurnaceHeaterTileEntity>> FURNACE_HEATER = REGISTER.register(
			"furnaceheater", makeType(FurnaceHeaterTileEntity::new, MetalDevices.furnaceHeater)
	);
	public static final RegistryObject<TileEntityType<DynamoTileEntity>> DYNAMO = REGISTER.register(
			"dynamo", makeType(DynamoTileEntity::new, MetalDevices.dynamo)
	);
	public static final RegistryObject<TileEntityType<ThermoelectricGenTileEntity>> THERMOELECTRIC_GEN = REGISTER.register(
			"thermoelectricgen", makeType(ThermoelectricGenTileEntity::new, MetalDevices.thermoelectricGen)
	);
	public static final RegistryObject<TileEntityType<ElectricLanternTileEntity>> ELECTRIC_LANTERN = REGISTER.register(
			"electriclantern", makeType(ElectricLanternTileEntity::new, MetalDevices.electricLantern)
	);
	public static final RegistryObject<TileEntityType<ChargingStationTileEntity>> CHARGING_STATION = REGISTER.register(
			"chargingstation", makeType(ChargingStationTileEntity::new, MetalDevices.chargingStation)
	);
	public static final RegistryObject<TileEntityType<FluidPipeTileEntity>> FLUID_PIPE = REGISTER.register(
			"fluidpipe", makeType(FluidPipeTileEntity::new, MetalDevices.fluidPipe)
	);
	public static final RegistryObject<TileEntityType<SampleDrillTileEntity>> SAMPLE_DRILL = REGISTER.register(
			"sampledrill", makeType(SampleDrillTileEntity::new, MetalDevices.sampleDrill)
	);
	public static final RegistryObject<TileEntityType<TeslaCoilTileEntity>> TESLACOIL = REGISTER.register(
			"teslacoil", makeType(TeslaCoilTileEntity::new, MetalDevices.teslaCoil)
	);
	public static final RegistryObject<TileEntityType<FloodlightTileEntity>> FLOODLIGHT = REGISTER.register(
			"floodlight", makeType(FloodlightTileEntity::new, MetalDevices.floodlight)
	);
	public static final RegistryObject<TileEntityType<TurretChemTileEntity>> TURRET_CHEM = REGISTER.register(
			"turretchem", makeType(TurretChemTileEntity::new, MetalDevices.turretChem)
	);
	public static final RegistryObject<TileEntityType<TurretGunTileEntity>> TURRET_GUN = REGISTER.register(
			"turretgun", makeType(TurretGunTileEntity::new, MetalDevices.turretGun)
	);
	public static final RegistryObject<TileEntityType<ClocheTileEntity>> CLOCHE = REGISTER.register(
			"cloche", makeType(ClocheTileEntity::new, MetalDevices.cloche)
	);
	public static final RegistryObject<TileEntityType<ChuteTileEntity>> CHUTE = REGISTER.register(
			"chute", makeTypeMultipleBlocks(ChuteTileEntity::new, MetalDevices.chutes.values())
	);
	public static final RegistryObject<TileEntityType<MetalPressTileEntity>> METAL_PRESS = REGISTER.register(
			"metalpress", makeType(MetalPressTileEntity::new, Multiblocks.metalPress)
	);
	public static final RegistryObject<TileEntityType<CrusherTileEntity>> CRUSHER = REGISTER.register(
			"crusher", makeType(CrusherTileEntity::new, Multiblocks.crusher)
	);
	public static final RegistryObject<TileEntityType<SawmillTileEntity>> SAWMILL = REGISTER.register(
			"sawmill", makeType(SawmillTileEntity::new, Multiblocks.sawmill)
	);
	public static final RegistryObject<TileEntityType<SheetmetalTankTileEntity>> SHEETMETAL_TANK = REGISTER.register(
			"sheetmetaltank", makeType(SheetmetalTankTileEntity::new, Multiblocks.tank)
	);
	public static final RegistryObject<TileEntityType<SiloTileEntity>> SILO = REGISTER.register(
			"silo", makeType(SiloTileEntity::new, Multiblocks.silo)
	);
	public static final RegistryObject<TileEntityType<AssemblerTileEntity>> ASSEMBLER = REGISTER.register(
			"assembler", makeType(AssemblerTileEntity::new, Multiblocks.assembler)
	);
	public static final RegistryObject<TileEntityType<AutoWorkbenchTileEntity>> AUTO_WORKBENCH = REGISTER.register(
			"autoworkbench", makeType(AutoWorkbenchTileEntity::new, Multiblocks.autoWorkbench)
	);
	public static final RegistryObject<TileEntityType<BottlingMachineTileEntity>> BOTTLING_MACHINE = REGISTER.register(
			"bottlingmachine", makeType(BottlingMachineTileEntity::new, Multiblocks.bottlingMachine)
	);
	public static final RegistryObject<TileEntityType<SqueezerTileEntity>> SQUEEZER = REGISTER.register(
			"squeezer", makeType(SqueezerTileEntity::new, Multiblocks.squeezer)
	);
	public static final RegistryObject<TileEntityType<FermenterTileEntity>> FERMENTER = REGISTER.register(
			"fermenter", makeType(FermenterTileEntity::new, Multiblocks.fermenter)
	);
	public static final RegistryObject<TileEntityType<RefineryTileEntity>> REFINERY = REGISTER.register(
			"refinery", makeType(RefineryTileEntity::new, Multiblocks.refinery)
	);
	public static final RegistryObject<TileEntityType<DieselGeneratorTileEntity>> DIESEL_GENERATOR = REGISTER.register(
			"dieselgenerator", makeType(DieselGeneratorTileEntity::new, Multiblocks.dieselGenerator)
	);
	public static final RegistryObject<TileEntityType<BucketWheelTileEntity>> BUCKET_WHEEL = REGISTER.register(
			"bucketwheel", makeType(BucketWheelTileEntity::new, Multiblocks.bucketWheel)
	);
	public static final RegistryObject<TileEntityType<ExcavatorTileEntity>> EXCAVATOR = REGISTER.register(
			"excavator", makeType(ExcavatorTileEntity::new, Multiblocks.excavator)
	);
	public static final RegistryObject<TileEntityType<ArcFurnaceTileEntity>> ARC_FURNACE = REGISTER.register(
			"arcfurnace", makeType(ArcFurnaceTileEntity::new, Multiblocks.arcFurnace)
	);
	public static final RegistryObject<TileEntityType<LightningrodTileEntity>> LIGHTNING_ROD = REGISTER.register(
			"lightningrod", makeType(LightningrodTileEntity::new, Multiblocks.lightningrod)
	);
	public static final RegistryObject<TileEntityType<MixerTileEntity>> MIXER = REGISTER.register(
			"mixer", makeType(MixerTileEntity::new, Multiblocks.mixer)
	);
	public static final RegistryObject<TileEntityType<FakeLightTileEntity>> FAKE_LIGHT = REGISTER.register(
			"fakelight", makeType(FakeLightTileEntity::new, Misc.fakeLight)
	);

	static
	{
		EnergyConnectorTileEntity.registerConnectorTEs(REGISTER);
	}

	private static <T extends TileEntity> Supplier<TileEntityType<T>> makeType(Supplier<T> create, Supplier<? extends Block> valid)
	{
		return makeTypeMultipleBlocks(create, ImmutableSet.of(valid));
	}

	private static <T extends TileEntity> Supplier<TileEntityType<T>> makeTypeMultipleBlocks(
			Supplier<T> create, Collection<? extends Supplier<? extends Block>> valid
	)
	{
		return () -> new TileEntityType<>(
				create, ImmutableSet.copyOf(valid.stream().map(Supplier::get).collect(Collectors.toList())), null
		);
	}
}
