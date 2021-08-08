/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.FakeLightBlock.FakeLightBlockEntity;
import blusunrize.immersiveengineering.common.blocks.MultiblockBEType;
import blusunrize.immersiveengineering.common.blocks.cloth.BalloonBlockEntity;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerBlockEntity;
import blusunrize.immersiveengineering.common.blocks.cloth.StripCurtainBlockEntity;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.stone.AlloySmelterBlockEntity;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceAdvancedBlockEntity;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceBlockEntity.CrudeBlastFurnaceBlockEntity;
import blusunrize.immersiveengineering.common.blocks.stone.CokeOvenBlockEntity;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleBlockEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class IEBlockEntities
{
	public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(
			ForgeRegistries.BLOCK_ENTITIES, ImmersiveEngineering.MODID);

	public static final RegistryObject<BlockEntityType<BalloonBlockEntity>> BALLOON = REGISTER.register(
			"balloon", makeType(BalloonBlockEntity::new, Cloth.balloon)
	);
	public static final RegistryObject<BlockEntityType<StripCurtainBlockEntity>> STRIP_CURTAIN = REGISTER.register(
			"stripcurtain", makeType(StripCurtainBlockEntity::new, Cloth.curtain)
	);
	public static final RegistryObject<BlockEntityType<ShaderBannerBlockEntity>> SHADER_BANNER = REGISTER.register(
			"shaderbanner",
			makeTypeMultipleBlocks(ShaderBannerBlockEntity::new, ImmutableSet.of(Cloth.shaderBanner, Cloth.shaderBannerWall))
	);
	public static final MultiblockBEType<CokeOvenBlockEntity> COKE_OVEN = makeMultiblock(
			"cokeoven", CokeOvenBlockEntity::new, Multiblocks.cokeOven
	);
	public static final MultiblockBEType<CrudeBlastFurnaceBlockEntity> BLAST_FURNACE = makeMultiblock(
			"blastfurnace", CrudeBlastFurnaceBlockEntity::new, Multiblocks.blastFurnace
	);
	public static final MultiblockBEType<BlastFurnaceAdvancedBlockEntity> BLAST_FURNACE_ADVANCED = makeMultiblock(
			"blastfurnaceadvanced", BlastFurnaceAdvancedBlockEntity::new, Multiblocks.blastFurnaceAdv
	);
	public static final RegistryObject<BlockEntityType<CoresampleBlockEntity>> CORE_SAMPLE = REGISTER.register(
			"coresample", makeType(CoresampleBlockEntity::new, StoneDecoration.coresample)
	);
	public static final MultiblockBEType<AlloySmelterBlockEntity> ALLOY_SMELTER = makeMultiblock(
			"alloysmelter", AlloySmelterBlockEntity::new, Multiblocks.alloySmelter
	);
	public static final RegistryObject<BlockEntityType<CraftingTableBlockEntity>> CRAFTING_TABLE = REGISTER.register(
			"craftingtable", makeType(CraftingTableBlockEntity::new, WoodenDevices.craftingTable)
	);
	public static final RegistryObject<BlockEntityType<WoodenCrateBlockEntity>> WOODEN_CRATE = REGISTER.register(
			"woodencrate", makeType(WoodenCrateBlockEntity::new, WoodenDevices.crate)
	);
	public static final RegistryObject<BlockEntityType<WoodenBarrelBlockEntity>> WOODEN_BARREL = REGISTER.register(
			"woodenbarrel", makeType(WoodenBarrelBlockEntity::new, WoodenDevices.woodenBarrel)
	);
	public static final RegistryObject<BlockEntityType<ModWorkbenchBlockEntity>> MOD_WORKBENCH = REGISTER.register(
			"modworkbench", makeType(ModWorkbenchBlockEntity::new, WoodenDevices.workbench)
	);
	public static final RegistryObject<BlockEntityType<CircuitTableBlockEntity>> CIRCUIT_TABLE = REGISTER.register(
			"circuittable", makeType(CircuitTableBlockEntity::new, WoodenDevices.circuitTable)
	);
	public static final RegistryObject<BlockEntityType<SorterBlockEntity>> SORTER = REGISTER.register(
			"sorter", makeType(SorterBlockEntity::new, WoodenDevices.sorter)
	);
	public static final RegistryObject<BlockEntityType<ItemBatcherBlockEntity>> ITEM_BATCHER = REGISTER.register(
			"itembatcher", makeType(ItemBatcherBlockEntity::new, WoodenDevices.itemBatcher)
	);
	public static final RegistryObject<BlockEntityType<TurntableBlockEntity>> TURNTABLE = REGISTER.register(
			"turntable", makeType(TurntableBlockEntity::new, WoodenDevices.turntable)
	);
	public static final RegistryObject<BlockEntityType<FluidSorterBlockEntity>> FLUID_SORTER = REGISTER.register(
			"fluidsorter", makeType(FluidSorterBlockEntity::new, WoodenDevices.fluidSorter)
	);
	public static final RegistryObject<BlockEntityType<LogicUnitBlockEntity>> LOGIC_UNIT = REGISTER.register(
			"logicunit", makeType(LogicUnitBlockEntity::new, WoodenDevices.logicUnit)
	);
	public static final RegistryObject<BlockEntityType<WatermillBlockEntity>> WATERMILL = REGISTER.register(
			"watermill", makeType(WatermillBlockEntity::new, WoodenDevices.watermill)
	);
	public static final RegistryObject<BlockEntityType<WindmillBlockEntity>> WINDMILL = REGISTER.register(
			"windmill", makeType(WindmillBlockEntity::new, WoodenDevices.windmill)
	);
	public static final RegistryObject<BlockEntityType<RazorWireBlockEntity>> RAZOR_WIRE = REGISTER.register(
			"razorwire", makeType(RazorWireBlockEntity::new, MetalDevices.razorWire)
	);
	public static final RegistryObject<BlockEntityType<ToolboxBlockEntity>> TOOLBOX = REGISTER.register(
			"toolbox", makeType(ToolboxBlockEntity::new, MetalDevices.toolbox)
	);
	public static final RegistryObject<BlockEntityType<StructuralArmBlockEntity>> STRUCTURAL_ARM = REGISTER.register(
			"structuralarm",
			makeTypeMultipleBlocks(StructuralArmBlockEntity::new, ImmutableSet.of(MetalDecoration.slopeAlu, MetalDecoration.slopeSteel))
	);
	public static final RegistryObject<BlockEntityType<ConnectorStructuralBlockEntity>> CONNECTOR_STRUCTURAL = REGISTER.register(
			"connectorstructural", makeType(ConnectorStructuralBlockEntity::new, Connectors.connectorStructural)
	);
	public static final RegistryObject<BlockEntityType<TransformerBlockEntity>> TRANSFORMER = REGISTER.register(
			"transformer", makeType(TransformerBlockEntity::new, Connectors.transformer)
	);
	public static final RegistryObject<BlockEntityType<PostTransformerBlockEntity>> POST_TRANSFORMER = REGISTER.register(
			"posttransformer", makeType(PostTransformerBlockEntity::new, Connectors.postTransformer)
	);
	public static final RegistryObject<BlockEntityType<TransformerHVBlockEntity>> TRANSFORMER_HV = REGISTER.register(
			"transformerhv", makeType(TransformerHVBlockEntity::new, Connectors.transformerHV)
	);
	public static final RegistryObject<BlockEntityType<BreakerSwitchBlockEntity>> BREAKER_SWITCH = REGISTER.register(
			"breakerswitch", makeType(BreakerSwitchBlockEntity::new, Connectors.breakerswitch)
	);
	public static final RegistryObject<BlockEntityType<RedstoneBreakerBlockEntity>> REDSTONE_BREAKER = REGISTER.register(
			"redstonebreaker", makeType(RedstoneBreakerBlockEntity::new, Connectors.redstoneBreaker)
	);
	public static final RegistryObject<BlockEntityType<EnergyMeterBlockEntity>> ENERGY_METER = REGISTER.register(
			"energymeter", makeType(EnergyMeterBlockEntity::new, Connectors.currentTransformer)
	);
	public static final RegistryObject<BlockEntityType<ConnectorRedstoneBlockEntity>> CONNECTOR_REDSTONE = REGISTER.register(
			"connectorredstone", makeType(ConnectorRedstoneBlockEntity::new, Connectors.connectorRedstone)
	);
	public static final RegistryObject<BlockEntityType<ConnectorProbeBlockEntity>> CONNECTOR_PROBE = REGISTER.register(
			"connectorprobe", makeType(ConnectorProbeBlockEntity::new, Connectors.connectorProbe)
	);
	public static final RegistryObject<BlockEntityType<ConnectorBundledBlockEntity>> CONNECTOR_BUNDLED = REGISTER.register(
			"connectorbundled", makeType(ConnectorBundledBlockEntity::new, Connectors.connectorBundled)
	);
	public static final RegistryObject<BlockEntityType<FeedthroughBlockEntity>> FEEDTHROUGH = REGISTER.register(
			"feedthrough", makeType(FeedthroughBlockEntity::new, Connectors.feedthrough)
	);
	public static final RegistryObject<BlockEntityType<CapacitorBlockEntity>> CAPACITOR_LV = REGISTER.register(
			"capacitorlv", makeType((pos, state) -> new CapacitorBlockEntity(IEServerConfig.MACHINES.lvCapConfig, pos, state), MetalDevices.capacitorLV)
	);
	public static final RegistryObject<BlockEntityType<CapacitorBlockEntity>> CAPACITOR_MV = REGISTER.register(
			"capacitormv", makeType((pos, state) -> new CapacitorBlockEntity(IEServerConfig.MACHINES.mvCapConfig, pos, state), MetalDevices.capacitorMV)
	);
	public static final RegistryObject<BlockEntityType<CapacitorBlockEntity>> CAPACITOR_HV = REGISTER.register(
			"capacitorhv", makeType((pos, state) -> new CapacitorBlockEntity(IEServerConfig.MACHINES.hvCapConfig, pos, state), MetalDevices.capacitorHV)
	);
	public static final RegistryObject<BlockEntityType<CapacitorCreativeBlockEntity>> CAPACITOR_CREATIVE = REGISTER.register(
			"capacitorcreative", makeType(CapacitorCreativeBlockEntity::new, MetalDevices.capacitorCreative)
	);
	public static final RegistryObject<BlockEntityType<MetalBarrelBlockEntity>> METAL_BARREL = REGISTER.register(
			"metalbarrel", makeType(MetalBarrelBlockEntity::new, MetalDevices.barrel)
	);
	public static final RegistryObject<BlockEntityType<FluidPumpBlockEntity>> FLUID_PUMP = REGISTER.register(
			"fluidpump", makeType(FluidPumpBlockEntity::new, MetalDevices.fluidPump)
	);
	public static final RegistryObject<BlockEntityType<FluidPlacerBlockEntity>> FLUID_PLACER = REGISTER.register(
			"fluidplacer", makeType(FluidPlacerBlockEntity::new, MetalDevices.fluidPlacer)
	);
	public static final RegistryObject<BlockEntityType<BlastFurnacePreheaterBlockEntity>> BLASTFURNACE_PREHEATER = REGISTER.register(
			"blastfurnacepreheater", makeType(BlastFurnacePreheaterBlockEntity::new, MetalDevices.blastFurnacePreheater)
	);
	public static final RegistryObject<BlockEntityType<FurnaceHeaterBlockEntity>> FURNACE_HEATER = REGISTER.register(
			"furnaceheater", makeType(FurnaceHeaterBlockEntity::new, MetalDevices.furnaceHeater)
	);
	public static final RegistryObject<BlockEntityType<DynamoBlockEntity>> DYNAMO = REGISTER.register(
			"dynamo", makeType(DynamoBlockEntity::new, MetalDevices.dynamo)
	);
	public static final RegistryObject<BlockEntityType<ThermoelectricGenBlockEntity>> THERMOELECTRIC_GEN = REGISTER.register(
			"thermoelectricgen", makeType(ThermoelectricGenBlockEntity::new, MetalDevices.thermoelectricGen)
	);
	public static final RegistryObject<BlockEntityType<ElectricLanternBlockEntity>> ELECTRIC_LANTERN = REGISTER.register(
			"electriclantern", makeType(ElectricLanternBlockEntity::new, MetalDevices.electricLantern)
	);
	public static final RegistryObject<BlockEntityType<ChargingStationBlockEntity>> CHARGING_STATION = REGISTER.register(
			"chargingstation", makeType(ChargingStationBlockEntity::new, MetalDevices.chargingStation)
	);
	public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE = REGISTER.register(
			"fluidpipe", makeType(FluidPipeBlockEntity::new, MetalDevices.fluidPipe)
	);
	public static final RegistryObject<BlockEntityType<SampleDrillBlockEntity>> SAMPLE_DRILL = REGISTER.register(
			"sampledrill", makeType(SampleDrillBlockEntity::new, MetalDevices.sampleDrill)
	);
	public static final RegistryObject<BlockEntityType<TeslaCoilBlockEntity>> TESLACOIL = REGISTER.register(
			"teslacoil", makeType(TeslaCoilBlockEntity::new, MetalDevices.teslaCoil)
	);
	public static final RegistryObject<BlockEntityType<FloodlightBlockEntity>> FLOODLIGHT = REGISTER.register(
			"floodlight", makeType(FloodlightBlockEntity::new, MetalDevices.floodlight)
	);
	public static final RegistryObject<BlockEntityType<TurretChemBlockEntity>> TURRET_CHEM = REGISTER.register(
			"turretchem", makeType(TurretChemBlockEntity::new, MetalDevices.turretChem)
	);
	public static final RegistryObject<BlockEntityType<TurretGunBlockEntity>> TURRET_GUN = REGISTER.register(
			"turretgun", makeType(TurretGunBlockEntity::new, MetalDevices.turretGun)
	);
	public static final RegistryObject<BlockEntityType<ClocheBlockEntity>> CLOCHE = REGISTER.register(
			"cloche", makeType(ClocheBlockEntity::new, MetalDevices.cloche)
	);
	public static final RegistryObject<BlockEntityType<ChuteBlockEntity>> CHUTE = REGISTER.register(
			"chute", makeTypeMultipleBlocks(ChuteBlockEntity::new, MetalDevices.chutes.values())
	);
	public static final MultiblockBEType<MetalPressBlockEntity> METAL_PRESS = makeMultiblock(
			"metalpress", MetalPressBlockEntity::new, Multiblocks.metalPress
	);
	public static final MultiblockBEType<CrusherBlockEntity> CRUSHER = makeMultiblock(
			"crusher", CrusherBlockEntity::new, Multiblocks.crusher
	);
	public static final MultiblockBEType<SawmillBlockEntity> SAWMILL = makeMultiblock(
			"sawmill", SawmillBlockEntity::new, Multiblocks.sawmill
	);
	public static final MultiblockBEType<SheetmetalTankBlockEntity> SHEETMETAL_TANK = makeMultiblock(
			"sheetmetaltank", SheetmetalTankBlockEntity::new, Multiblocks.tank
	);
	public static final MultiblockBEType<SiloBlockEntity> SILO = makeMultiblock(
			"silo", SiloBlockEntity::new, Multiblocks.silo
	);
	public static final MultiblockBEType<AssemblerBlockEntity> ASSEMBLER = makeMultiblock(
			"assembler", AssemblerBlockEntity::new, Multiblocks.assembler
	);
	public static final MultiblockBEType<AutoWorkbenchBlockEntity> AUTO_WORKBENCH = makeMultiblock(
			"autoworkbench", AutoWorkbenchBlockEntity::new, Multiblocks.autoWorkbench
	);
	public static final MultiblockBEType<BottlingMachineBlockEntity> BOTTLING_MACHINE = makeMultiblock(
			"bottlingmachine", BottlingMachineBlockEntity::new, Multiblocks.bottlingMachine
	);
	public static final MultiblockBEType<SqueezerBlockEntity> SQUEEZER = makeMultiblock(
			"squeezer", SqueezerBlockEntity::new, Multiblocks.squeezer
	);
	public static final MultiblockBEType<FermenterBlockEntity> FERMENTER = makeMultiblock(
			"fermenter", FermenterBlockEntity::new, Multiblocks.fermenter
	);
	public static final MultiblockBEType<RefineryBlockEntity> REFINERY = makeMultiblock(
			"refinery", RefineryBlockEntity::new, Multiblocks.refinery
	);
	public static final MultiblockBEType<DieselGeneratorBlockEntity> DIESEL_GENERATOR = makeMultiblock(
			"dieselgenerator", DieselGeneratorBlockEntity::new, Multiblocks.dieselGenerator
	);
	public static final MultiblockBEType<BucketWheelBlockEntity> BUCKET_WHEEL = makeMultiblock(
			"bucketwheel", BucketWheelBlockEntity::new, Multiblocks.bucketWheel
	);
	public static final MultiblockBEType<ExcavatorBlockEntity> EXCAVATOR = makeMultiblock(
			"excavator", ExcavatorBlockEntity::new, Multiblocks.excavator
	);
	public static final MultiblockBEType<ArcFurnaceBlockEntity> ARC_FURNACE = makeMultiblock(
			"arcfurnace", ArcFurnaceBlockEntity::new, Multiblocks.arcFurnace
	);
	public static final MultiblockBEType<LightningrodBlockEntity> LIGHTNING_ROD = makeMultiblock(
			"lightningrod", LightningrodBlockEntity::new, Multiblocks.lightningrod
	);
	public static final MultiblockBEType<MixerBlockEntity> MIXER = makeMultiblock(
			"mixer", MixerBlockEntity::new, Multiblocks.mixer
	);
	public static final RegistryObject<BlockEntityType<FakeLightBlockEntity>> FAKE_LIGHT = REGISTER.register(
			"fakelight", makeType(FakeLightBlockEntity::new, Misc.fakeLight)
	);

	static
	{
		EnergyConnectorBlockEntity.registerConnectorTEs(REGISTER);
	}

	public static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeType(BlockEntityType.BlockEntitySupplier<T> create, Supplier<? extends Block> valid)
	{
		return makeTypeMultipleBlocks(create, ImmutableSet.of(valid));
	}

	public static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeTypeMultipleBlocks(
			BlockEntityType.BlockEntitySupplier<T> create, Collection<? extends Supplier<? extends Block>> valid
	)
	{
		return () -> new BlockEntityType<>(
				create, ImmutableSet.copyOf(valid.stream().map(Supplier::get).collect(Collectors.toList())), null
		);
	}

	private static <T extends MultiblockPartBlockEntity<T>>
	MultiblockBEType<T> makeMultiblock(String name, MultiblockBEType.BEWithTypeConstructor<T> make, Supplier<? extends Block> block) {
		return new MultiblockBEType<>(
				name, REGISTER, make, block, state -> state.hasProperty(IEProperties.MULTIBLOCKSLAVE) && !state.getValue(IEProperties.MULTIBLOCKSLAVE)
		);
	}
}
