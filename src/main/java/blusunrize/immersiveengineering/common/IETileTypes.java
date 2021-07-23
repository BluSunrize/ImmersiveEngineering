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
import blusunrize.immersiveengineering.common.blocks.stone.AlloySmelterTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceAdvancedTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceTileEntity.CrudeBlastFurnaceTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.CokeOvenTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class IETileTypes
{
	public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(
			ForgeRegistries.TILE_ENTITIES, ImmersiveEngineering.MODID);

	public static final RegistryObject<BlockEntityType<BalloonTileEntity>> BALLOON = REGISTER.register(
			"balloon", makeType(BalloonTileEntity::new, Cloth.balloon)
	);
	public static final RegistryObject<BlockEntityType<StripCurtainTileEntity>> STRIP_CURTAIN = REGISTER.register(
			"stripcurtain", makeType(StripCurtainTileEntity::new, Cloth.curtain)
	);
	public static final RegistryObject<BlockEntityType<ShaderBannerTileEntity>> SHADER_BANNER = REGISTER.register(
			"shaderbanner",
			makeTypeMultipleBlocks(ShaderBannerTileEntity::new, ImmutableSet.of(Cloth.shaderBanner, Cloth.shaderBannerWall))
	);
	public static final RegistryObject<BlockEntityType<CokeOvenTileEntity>> COKE_OVEN = REGISTER.register(
			"cokeoven", makeType(CokeOvenTileEntity::new, Multiblocks.cokeOven)
	);
	public static final RegistryObject<BlockEntityType<CrudeBlastFurnaceTileEntity>> BLAST_FURNACE = REGISTER.register(
			"blastfurnace", makeType(CrudeBlastFurnaceTileEntity::new, Multiblocks.blastFurnace)
	);
	public static final RegistryObject<BlockEntityType<BlastFurnaceAdvancedTileEntity>> BLAST_FURNACE_ADVANCED = REGISTER.register(
			"blastfurnaceadvanced", makeType(BlastFurnaceAdvancedTileEntity::new, Multiblocks.blastFurnaceAdv)
	);
	public static final RegistryObject<BlockEntityType<CoresampleTileEntity>> CORE_SAMPLE = REGISTER.register(
			"coresample", makeType(CoresampleTileEntity::new, StoneDecoration.coresample)
	);
	public static final RegistryObject<BlockEntityType<AlloySmelterTileEntity>> ALLOY_SMELTER = REGISTER.register(
			"alloysmelter", makeType(AlloySmelterTileEntity::new, Multiblocks.alloySmelter)
	);
	public static final RegistryObject<BlockEntityType<CraftingTableTileEntity>> CRAFTING_TABLE = REGISTER.register(
			"craftingtable", makeType(CraftingTableTileEntity::new, WoodenDevices.craftingTable)
	);
	public static final RegistryObject<BlockEntityType<WoodenCrateTileEntity>> WOODEN_CRATE = REGISTER.register(
			"woodencrate", makeType(WoodenCrateTileEntity::new, WoodenDevices.crate)
	);
	public static final RegistryObject<BlockEntityType<WoodenBarrelTileEntity>> WOODEN_BARREL = REGISTER.register(
			"woodenbarrel", makeType(WoodenBarrelTileEntity::new, WoodenDevices.woodenBarrel)
	);
	public static final RegistryObject<BlockEntityType<ModWorkbenchTileEntity>> MOD_WORKBENCH = REGISTER.register(
			"modworkbench", makeType(ModWorkbenchTileEntity::new, WoodenDevices.workbench)
	);
	public static final RegistryObject<BlockEntityType<CircuitTableTileEntity>> CIRCUIT_TABLE = REGISTER.register(
			"circuittable", makeType(CircuitTableTileEntity::new, WoodenDevices.circuitTable)
	);
	public static final RegistryObject<BlockEntityType<SorterTileEntity>> SORTER = REGISTER.register(
			"sorter", makeType(SorterTileEntity::new, WoodenDevices.sorter)
	);
	public static final RegistryObject<BlockEntityType<ItemBatcherTileEntity>> ITEM_BATCHER = REGISTER.register(
			"itembatcher", makeType(ItemBatcherTileEntity::new, WoodenDevices.itemBatcher)
	);
	public static final RegistryObject<BlockEntityType<TurntableTileEntity>> TURNTABLE = REGISTER.register(
			"turntable", makeType(TurntableTileEntity::new, WoodenDevices.turntable)
	);
	public static final RegistryObject<BlockEntityType<FluidSorterTileEntity>> FLUID_SORTER = REGISTER.register(
			"fluidsorter", makeType(FluidSorterTileEntity::new, WoodenDevices.fluidSorter)
	);
	public static final RegistryObject<BlockEntityType<LogicUnitTileEntity>> LOGIC_UNIT = REGISTER.register(
			"logicunit", makeType(LogicUnitTileEntity::new, WoodenDevices.logicUnit)
	);
	public static final RegistryObject<BlockEntityType<WatermillTileEntity>> WATERMILL = REGISTER.register(
			"watermill", makeType(WatermillTileEntity::new, WoodenDevices.watermill)
	);
	public static final RegistryObject<BlockEntityType<WindmillTileEntity>> WINDMILL = REGISTER.register(
			"windmill", makeType(WindmillTileEntity::new, WoodenDevices.windmill)
	);
	public static final RegistryObject<BlockEntityType<RazorWireTileEntity>> RAZOR_WIRE = REGISTER.register(
			"razorwire", makeType(RazorWireTileEntity::new, MetalDevices.razorWire)
	);
	public static final RegistryObject<BlockEntityType<ToolboxTileEntity>> TOOLBOX = REGISTER.register(
			"toolbox", makeType(ToolboxTileEntity::new, MetalDevices.toolbox)
	);
	public static final RegistryObject<BlockEntityType<StructuralArmTileEntity>> STRUCTURAL_ARM = REGISTER.register(
			"structuralarm",
			makeTypeMultipleBlocks(StructuralArmTileEntity::new, ImmutableSet.of(MetalDecoration.slopeAlu, MetalDecoration.slopeSteel))
	);
	public static final RegistryObject<BlockEntityType<ConnectorStructuralTileEntity>> CONNECTOR_STRUCTURAL = REGISTER.register(
			"connectorstructural", makeType(ConnectorStructuralTileEntity::new, Connectors.connectorStructural)
	);
	public static final RegistryObject<BlockEntityType<TransformerTileEntity>> TRANSFORMER = REGISTER.register(
			"transformer", makeType(TransformerTileEntity::new, Connectors.transformer)
	);
	public static final RegistryObject<BlockEntityType<PostTransformerTileEntity>> POST_TRANSFORMER = REGISTER.register(
			"posttransformer", makeType(PostTransformerTileEntity::new, Connectors.postTransformer)
	);
	public static final RegistryObject<BlockEntityType<TransformerHVTileEntity>> TRANSFORMER_HV = REGISTER.register(
			"transformerhv", makeType(TransformerHVTileEntity::new, Connectors.transformerHV)
	);
	public static final RegistryObject<BlockEntityType<BreakerSwitchTileEntity>> BREAKER_SWITCH = REGISTER.register(
			"breakerswitch", makeType(BreakerSwitchTileEntity::new, Connectors.breakerswitch)
	);
	public static final RegistryObject<BlockEntityType<RedstoneBreakerTileEntity>> REDSTONE_BREAKER = REGISTER.register(
			"redstonebreaker", makeType(RedstoneBreakerTileEntity::new, Connectors.redstoneBreaker)
	);
	public static final RegistryObject<BlockEntityType<EnergyMeterTileEntity>> ENERGY_METER = REGISTER.register(
			"energymeter", makeType(EnergyMeterTileEntity::new, Connectors.currentTransformer)
	);
	public static final RegistryObject<BlockEntityType<ConnectorRedstoneTileEntity>> CONNECTOR_REDSTONE = REGISTER.register(
			"connectorredstone", makeType(ConnectorRedstoneTileEntity::new, Connectors.connectorRedstone)
	);
	public static final RegistryObject<BlockEntityType<ConnectorProbeTileEntity>> CONNECTOR_PROBE = REGISTER.register(
			"connectorprobe", makeType(ConnectorProbeTileEntity::new, Connectors.connectorProbe)
	);
	public static final RegistryObject<BlockEntityType<ConnectorBundledTileEntity>> CONNECTOR_BUNDLED = REGISTER.register(
			"connectorbundled", makeType(ConnectorBundledTileEntity::new, Connectors.connectorBundled)
	);
	public static final RegistryObject<BlockEntityType<FeedthroughTileEntity>> FEEDTHROUGH = REGISTER.register(
			"feedthrough", makeType(FeedthroughTileEntity::new, Connectors.feedthrough)
	);
	public static final RegistryObject<BlockEntityType<CapacitorTileEntity>> CAPACITOR_LV = REGISTER.register(
			"capacitorlv", makeType(() -> new CapacitorTileEntity(IEServerConfig.MACHINES.lvCapConfig), MetalDevices.capacitorLV)
	);
	public static final RegistryObject<BlockEntityType<CapacitorTileEntity>> CAPACITOR_MV = REGISTER.register(
			"capacitormv", makeType(() -> new CapacitorTileEntity(IEServerConfig.MACHINES.mvCapConfig), MetalDevices.capacitorMV)
	);
	public static final RegistryObject<BlockEntityType<CapacitorTileEntity>> CAPACITOR_HV = REGISTER.register(
			"capacitorhv", makeType(() -> new CapacitorTileEntity(IEServerConfig.MACHINES.hvCapConfig), MetalDevices.capacitorHV)
	);
	public static final RegistryObject<BlockEntityType<CapacitorCreativeTileEntity>> CAPACITOR_CREATIVE = REGISTER.register(
			"capacitorcreative", makeType(CapacitorCreativeTileEntity::new, MetalDevices.capacitorCreative)
	);
	public static final RegistryObject<BlockEntityType<MetalBarrelTileEntity>> METAL_BARREL = REGISTER.register(
			"metalbarrel", makeType(MetalBarrelTileEntity::new, MetalDevices.barrel)
	);
	public static final RegistryObject<BlockEntityType<FluidPumpTileEntity>> FLUID_PUMP = REGISTER.register(
			"fluidpump", makeType(FluidPumpTileEntity::new, MetalDevices.fluidPump)
	);
	public static final RegistryObject<BlockEntityType<FluidPlacerTileEntity>> FLUID_PLACER = REGISTER.register(
			"fluidplacer", makeType(FluidPlacerTileEntity::new, MetalDevices.fluidPlacer)
	);
	public static final RegistryObject<BlockEntityType<BlastFurnacePreheaterTileEntity>> BLASTFURNACE_PREHEATER = REGISTER.register(
			"blastfurnacepreheater", makeType(BlastFurnacePreheaterTileEntity::new, MetalDevices.blastFurnacePreheater)
	);
	public static final RegistryObject<BlockEntityType<FurnaceHeaterTileEntity>> FURNACE_HEATER = REGISTER.register(
			"furnaceheater", makeType(FurnaceHeaterTileEntity::new, MetalDevices.furnaceHeater)
	);
	public static final RegistryObject<BlockEntityType<DynamoTileEntity>> DYNAMO = REGISTER.register(
			"dynamo", makeType(DynamoTileEntity::new, MetalDevices.dynamo)
	);
	public static final RegistryObject<BlockEntityType<ThermoelectricGenTileEntity>> THERMOELECTRIC_GEN = REGISTER.register(
			"thermoelectricgen", makeType(ThermoelectricGenTileEntity::new, MetalDevices.thermoelectricGen)
	);
	public static final RegistryObject<BlockEntityType<ElectricLanternTileEntity>> ELECTRIC_LANTERN = REGISTER.register(
			"electriclantern", makeType(ElectricLanternTileEntity::new, MetalDevices.electricLantern)
	);
	public static final RegistryObject<BlockEntityType<ChargingStationTileEntity>> CHARGING_STATION = REGISTER.register(
			"chargingstation", makeType(ChargingStationTileEntity::new, MetalDevices.chargingStation)
	);
	public static final RegistryObject<BlockEntityType<FluidPipeTileEntity>> FLUID_PIPE = REGISTER.register(
			"fluidpipe", makeType(FluidPipeTileEntity::new, MetalDevices.fluidPipe)
	);
	public static final RegistryObject<BlockEntityType<SampleDrillTileEntity>> SAMPLE_DRILL = REGISTER.register(
			"sampledrill", makeType(SampleDrillTileEntity::new, MetalDevices.sampleDrill)
	);
	public static final RegistryObject<BlockEntityType<TeslaCoilTileEntity>> TESLACOIL = REGISTER.register(
			"teslacoil", makeType(TeslaCoilTileEntity::new, MetalDevices.teslaCoil)
	);
	public static final RegistryObject<BlockEntityType<FloodlightTileEntity>> FLOODLIGHT = REGISTER.register(
			"floodlight", makeType(FloodlightTileEntity::new, MetalDevices.floodlight)
	);
	public static final RegistryObject<BlockEntityType<TurretChemTileEntity>> TURRET_CHEM = REGISTER.register(
			"turretchem", makeType(TurretChemTileEntity::new, MetalDevices.turretChem)
	);
	public static final RegistryObject<BlockEntityType<TurretGunTileEntity>> TURRET_GUN = REGISTER.register(
			"turretgun", makeType(TurretGunTileEntity::new, MetalDevices.turretGun)
	);
	public static final RegistryObject<BlockEntityType<ClocheTileEntity>> CLOCHE = REGISTER.register(
			"cloche", makeType(ClocheTileEntity::new, MetalDevices.cloche)
	);
	public static final RegistryObject<BlockEntityType<ChuteTileEntity>> CHUTE = REGISTER.register(
			"chute", makeTypeMultipleBlocks(ChuteTileEntity::new, MetalDevices.chutes.values())
	);
	public static final RegistryObject<BlockEntityType<MetalPressTileEntity>> METAL_PRESS = REGISTER.register(
			"metalpress", makeType(MetalPressTileEntity::new, Multiblocks.metalPress)
	);
	public static final RegistryObject<BlockEntityType<CrusherTileEntity>> CRUSHER = REGISTER.register(
			"crusher", makeType(CrusherTileEntity::new, Multiblocks.crusher)
	);
	public static final RegistryObject<BlockEntityType<SawmillTileEntity>> SAWMILL = REGISTER.register(
			"sawmill", makeType(SawmillTileEntity::new, Multiblocks.sawmill)
	);
	public static final RegistryObject<BlockEntityType<SheetmetalTankTileEntity>> SHEETMETAL_TANK = REGISTER.register(
			"sheetmetaltank", makeType(SheetmetalTankTileEntity::new, Multiblocks.tank)
	);
	public static final RegistryObject<BlockEntityType<SiloTileEntity>> SILO = REGISTER.register(
			"silo", makeType(SiloTileEntity::new, Multiblocks.silo)
	);
	public static final RegistryObject<BlockEntityType<AssemblerTileEntity>> ASSEMBLER = REGISTER.register(
			"assembler", makeType(AssemblerTileEntity::new, Multiblocks.assembler)
	);
	public static final RegistryObject<BlockEntityType<AutoWorkbenchTileEntity>> AUTO_WORKBENCH = REGISTER.register(
			"autoworkbench", makeType(AutoWorkbenchTileEntity::new, Multiblocks.autoWorkbench)
	);
	public static final RegistryObject<BlockEntityType<BottlingMachineTileEntity>> BOTTLING_MACHINE = REGISTER.register(
			"bottlingmachine", makeType(BottlingMachineTileEntity::new, Multiblocks.bottlingMachine)
	);
	public static final RegistryObject<BlockEntityType<SqueezerTileEntity>> SQUEEZER = REGISTER.register(
			"squeezer", makeType(SqueezerTileEntity::new, Multiblocks.squeezer)
	);
	public static final RegistryObject<BlockEntityType<FermenterTileEntity>> FERMENTER = REGISTER.register(
			"fermenter", makeType(FermenterTileEntity::new, Multiblocks.fermenter)
	);
	public static final RegistryObject<BlockEntityType<RefineryTileEntity>> REFINERY = REGISTER.register(
			"refinery", makeType(RefineryTileEntity::new, Multiblocks.refinery)
	);
	public static final RegistryObject<BlockEntityType<DieselGeneratorTileEntity>> DIESEL_GENERATOR = REGISTER.register(
			"dieselgenerator", makeType(DieselGeneratorTileEntity::new, Multiblocks.dieselGenerator)
	);
	public static final RegistryObject<BlockEntityType<BucketWheelTileEntity>> BUCKET_WHEEL = REGISTER.register(
			"bucketwheel", makeType(BucketWheelTileEntity::new, Multiblocks.bucketWheel)
	);
	public static final RegistryObject<BlockEntityType<ExcavatorTileEntity>> EXCAVATOR = REGISTER.register(
			"excavator", makeType(ExcavatorTileEntity::new, Multiblocks.excavator)
	);
	public static final RegistryObject<BlockEntityType<ArcFurnaceTileEntity>> ARC_FURNACE = REGISTER.register(
			"arcfurnace", makeType(ArcFurnaceTileEntity::new, Multiblocks.arcFurnace)
	);
	public static final RegistryObject<BlockEntityType<LightningrodTileEntity>> LIGHTNING_ROD = REGISTER.register(
			"lightningrod", makeType(LightningrodTileEntity::new, Multiblocks.lightningrod)
	);
	public static final RegistryObject<BlockEntityType<MixerTileEntity>> MIXER = REGISTER.register(
			"mixer", makeType(MixerTileEntity::new, Multiblocks.mixer)
	);
	public static final RegistryObject<BlockEntityType<FakeLightTileEntity>> FAKE_LIGHT = REGISTER.register(
			"fakelight", makeType(FakeLightTileEntity::new, Misc.fakeLight)
	);

	static
	{
		EnergyConnectorTileEntity.registerConnectorTEs(REGISTER);
	}

	private static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeType(Supplier<T> create, Supplier<? extends Block> valid)
	{
		return makeTypeMultipleBlocks(create, ImmutableSet.of(valid));
	}

	private static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeTypeMultipleBlocks(
			Supplier<T> create, Collection<? extends Supplier<? extends Block>> valid
	)
	{
		return () -> new BlockEntityType<>(
				create, ImmutableSet.copyOf(valid.stream().map(Supplier::get).collect(Collectors.toList())), null
		);
	}
}
