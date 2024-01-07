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
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.MultiblockBEType;
import blusunrize.immersiveengineering.common.blocks.cloth.BalloonBlockEntity;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerBlockEntity;
import blusunrize.immersiveengineering.common.blocks.cloth.StripCurtainBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleBlockEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class IEBlockEntities
{
	public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(
			ForgeRegistries.BLOCK_ENTITY_TYPES, ImmersiveEngineering.MODID
	);

	public static final RegistryObject<BlockEntityType<BalloonBlockEntity>> BALLOON = REGISTER.register(
			"balloon", makeType(BalloonBlockEntity::new, Cloth.BALLOON)
	);
	public static final RegistryObject<BlockEntityType<StripCurtainBlockEntity>> STRIP_CURTAIN = REGISTER.register(
			"stripcurtain", makeType(StripCurtainBlockEntity::new, Cloth.STRIP_CURTAIN)
	);
	public static final RegistryObject<BlockEntityType<ShaderBannerBlockEntity>> SHADER_BANNER = REGISTER.register(
			"shaderbanner",
			makeTypeMultipleBlocks(ShaderBannerBlockEntity::new, ImmutableSet.of(Cloth.SHADER_BANNER, Cloth.SHADER_BANNER_WALL))
	);
	public static final RegistryObject<BlockEntityType<CoresampleBlockEntity>> CORE_SAMPLE = REGISTER.register(
			"coresample", makeType(CoresampleBlockEntity::new, StoneDecoration.CORESAMPLE)
	);
	public static final RegistryObject<BlockEntityType<CraftingTableBlockEntity>> CRAFTING_TABLE = REGISTER.register(
			"craftingtable", makeType(CraftingTableBlockEntity::new, WoodenDevices.CRAFTING_TABLE)
	);
	public static final RegistryObject<BlockEntityType<WoodenCrateBlockEntity>> WOODEN_CRATE = REGISTER.register(
			"woodencrate", makeType(WoodenCrateBlockEntity::new, WoodenDevices.CRATE)
	);
	public static final RegistryObject<BlockEntityType<WoodenBarrelBlockEntity>> WOODEN_BARREL = REGISTER.register(
			"woodenbarrel", makeType(WoodenBarrelBlockEntity::new, WoodenDevices.WOODEN_BARREL)
	);
	public static final RegistryObject<BlockEntityType<ModWorkbenchBlockEntity>> MOD_WORKBENCH = REGISTER.register(
			"modworkbench", makeType(ModWorkbenchBlockEntity::new, WoodenDevices.WORKBENCH)
	);
	public static final RegistryObject<BlockEntityType<CircuitTableBlockEntity>> CIRCUIT_TABLE = REGISTER.register(
			"circuittable", makeType(CircuitTableBlockEntity::new, WoodenDevices.CIRCUIT_TABLE)
	);
	public static final RegistryObject<BlockEntityType<SorterBlockEntity>> SORTER = REGISTER.register(
			"sorter", makeType(SorterBlockEntity::new, WoodenDevices.SORTER)
	);
	public static final RegistryObject<BlockEntityType<ItemBatcherBlockEntity>> ITEM_BATCHER = REGISTER.register(
			"itembatcher", makeType(ItemBatcherBlockEntity::new, WoodenDevices.ITEM_BATCHER)
	);
	public static final RegistryObject<BlockEntityType<TurntableBlockEntity>> TURNTABLE = REGISTER.register(
			"turntable", makeType(TurntableBlockEntity::new, WoodenDevices.TURNTABLE)
	);
	public static final RegistryObject<BlockEntityType<FluidSorterBlockEntity>> FLUID_SORTER = REGISTER.register(
			"fluidsorter", makeType(FluidSorterBlockEntity::new, WoodenDevices.FLUID_SORTER)
	);
	public static final RegistryObject<BlockEntityType<LogicUnitBlockEntity>> LOGIC_UNIT = REGISTER.register(
			"logicunit", makeType(LogicUnitBlockEntity::new, WoodenDevices.LOGIC_UNIT)
	);
	public static final MultiblockBEType<WatermillBlockEntity> WATERMILL = makeMultiblock(
			"watermill", WatermillBlockEntity::new, WoodenDevices.WATERMILL
	);
	public static final RegistryObject<BlockEntityType<WindmillBlockEntity>> WINDMILL = REGISTER.register(
			"windmill", makeType(WindmillBlockEntity::new, WoodenDevices.WINDMILL)
	);
	public static final RegistryObject<BlockEntityType<RazorWireBlockEntity>> RAZOR_WIRE = REGISTER.register(
			"razorwire", makeType(RazorWireBlockEntity::new, MetalDevices.RAZOR_WIRE)
	);
	public static final RegistryObject<BlockEntityType<ToolboxBlockEntity>> TOOLBOX = REGISTER.register(
			"toolbox", makeType(ToolboxBlockEntity::new, MetalDevices.TOOLBOX)
	);
	public static final RegistryObject<BlockEntityType<StructuralArmBlockEntity>> STRUCTURAL_ARM = REGISTER.register(
			"structuralarm",
			makeTypeMultipleBlocks(StructuralArmBlockEntity::new, ImmutableSet.of(MetalDecoration.ALU_SLOPE, MetalDecoration.STEEL_SLOPE))
	);
	public static final RegistryObject<BlockEntityType<ConnectorStructuralBlockEntity>> CONNECTOR_STRUCTURAL = REGISTER.register(
			"connectorstructural", makeType(ConnectorStructuralBlockEntity::new, Connectors.CONNECTOR_STRUCTURAL)
	);
	public static final RegistryObject<BlockEntityType<TransformerBlockEntity>> TRANSFORMER = REGISTER.register(
			"transformer", makeType(TransformerBlockEntity::new, Connectors.TRANSFORMER)
	);
	public static final RegistryObject<BlockEntityType<PostTransformerBlockEntity>> POST_TRANSFORMER = REGISTER.register(
			"posttransformer", makeType(PostTransformerBlockEntity::new, Connectors.POST_TRANSFORMER)
	);
	public static final RegistryObject<BlockEntityType<TransformerHVBlockEntity>> TRANSFORMER_HV = REGISTER.register(
			"transformerhv", makeType(TransformerHVBlockEntity::new, Connectors.TRANSFORMER_HV)
	);
	public static final RegistryObject<BlockEntityType<BreakerSwitchBlockEntity>> BREAKER_SWITCH = REGISTER.register(
			"breakerswitch", makeType(BreakerSwitchBlockEntity::new, Connectors.BREAKER_SWITCH)
	);
	public static final RegistryObject<BlockEntityType<RedstoneBreakerBlockEntity>> REDSTONE_BREAKER = REGISTER.register(
			"redstonebreaker", makeType(RedstoneBreakerBlockEntity::new, Connectors.REDSTONE_BREAKER)
	);
	public static final MultiblockBEType<EnergyMeterBlockEntity> ENERGY_METER = makeMultiblock(
			"energymeter", EnergyMeterBlockEntity::new, Connectors.CURRENT_TRANSFORMER
	);
	public static final RegistryObject<BlockEntityType<ConnectorRedstoneBlockEntity>> CONNECTOR_REDSTONE = REGISTER.register(
			"connectorredstone", makeType(ConnectorRedstoneBlockEntity::new, Connectors.CONNECTOR_REDSTONE)
	);
	public static final RegistryObject<BlockEntityType<ConnectorProbeBlockEntity>> CONNECTOR_PROBE = REGISTER.register(
			"connectorprobe", makeType(ConnectorProbeBlockEntity::new, Connectors.CONNECTOR_PROBE)
	);
	public static final RegistryObject<BlockEntityType<ConnectorBundledBlockEntity>> CONNECTOR_BUNDLED = REGISTER.register(
			"connectorbundled", makeType(ConnectorBundledBlockEntity::new, Connectors.CONNECTOR_BUNDLED)
	);
	public static final RegistryObject<BlockEntityType<FeedthroughBlockEntity>> FEEDTHROUGH = REGISTER.register(
			"feedthrough", makeType(FeedthroughBlockEntity::new, Connectors.FEEDTHROUGH)
	);
	public static final RegistryObject<BlockEntityType<CapacitorBlockEntity>> CAPACITOR_LV = REGISTER.register(
			"capacitorlv", makeType((pos, state) -> new CapacitorBlockEntity(IEServerConfig.MACHINES.lvCapConfig, pos, state), MetalDevices.CAPACITOR_LV)
	);
	public static final RegistryObject<BlockEntityType<CapacitorBlockEntity>> CAPACITOR_MV = REGISTER.register(
			"capacitormv", makeType((pos, state) -> new CapacitorBlockEntity(IEServerConfig.MACHINES.mvCapConfig, pos, state), MetalDevices.CAPACITOR_MV)
	);
	public static final RegistryObject<BlockEntityType<CapacitorBlockEntity>> CAPACITOR_HV = REGISTER.register(
			"capacitorhv", makeType((pos, state) -> new CapacitorBlockEntity(IEServerConfig.MACHINES.hvCapConfig, pos, state), MetalDevices.CAPACITOR_HV)
	);
	public static final RegistryObject<BlockEntityType<CapacitorCreativeBlockEntity>> CAPACITOR_CREATIVE = REGISTER.register(
			"capacitorcreative", makeType(CapacitorCreativeBlockEntity::new, MetalDevices.CAPACITOR_CREATIVE)
	);
	public static final RegistryObject<BlockEntityType<MetalBarrelBlockEntity>> METAL_BARREL = REGISTER.register(
			"metalbarrel", makeType(MetalBarrelBlockEntity::new, MetalDevices.BARREL)
	);
	public static final MultiblockBEType<FluidPumpBlockEntity> FLUID_PUMP = makeMultiblock(
			"fluidpump", FluidPumpBlockEntity::new, MetalDevices.FLUID_PUMP
	);
	public static final RegistryObject<BlockEntityType<FluidPlacerBlockEntity>> FLUID_PLACER = REGISTER.register(
			"fluidplacer", makeType(FluidPlacerBlockEntity::new, MetalDevices.FLUID_PLACER)
	);
	public static final MultiblockBEType<BlastFurnacePreheaterBlockEntity> BLASTFURNACE_PREHEATER = makeMultiblock(
			"blastfurnacepreheater", BlastFurnacePreheaterBlockEntity::new, MetalDevices.BLAST_FURNACE_PREHEATER
	);
	public static final RegistryObject<BlockEntityType<FurnaceHeaterBlockEntity>> FURNACE_HEATER = REGISTER.register(
			"furnaceheater", makeType(FurnaceHeaterBlockEntity::new, MetalDevices.FURNACE_HEATER)
	);
	public static final RegistryObject<BlockEntityType<DynamoBlockEntity>> DYNAMO = REGISTER.register(
			"dynamo", makeType(DynamoBlockEntity::new, MetalDevices.DYNAMO)
	);
	public static final RegistryObject<BlockEntityType<ThermoelectricGenBlockEntity>> THERMOELECTRIC_GEN = REGISTER.register(
			"thermoelectricgen", makeType(ThermoelectricGenBlockEntity::new, MetalDevices.THERMOELECTRIC_GEN)
	);
	public static final RegistryObject<BlockEntityType<ElectricLanternBlockEntity>> ELECTRIC_LANTERN = REGISTER.register(
			"electriclantern", makeType(ElectricLanternBlockEntity::new, MetalDevices.ELECTRIC_LANTERN)
	);
	public static final RegistryObject<BlockEntityType<ChargingStationBlockEntity>> CHARGING_STATION = REGISTER.register(
			"chargingstation", makeType(ChargingStationBlockEntity::new, MetalDevices.CHARGING_STATION)
	);
	public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE = REGISTER.register(
			"fluidpipe", makeType(FluidPipeBlockEntity::new, MetalDevices.FLUID_PIPE)
	);
	public static final MultiblockBEType<SampleDrillBlockEntity> SAMPLE_DRILL = makeMultiblock(
			"sampledrill", SampleDrillBlockEntity::new, MetalDevices.SAMPLE_DRILL
	);
	public static final MultiblockBEType<TeslaCoilBlockEntity> TESLACOIL = makeMultiblock(
			"teslacoil", TeslaCoilBlockEntity::new, MetalDevices.TESLA_COIL
	);
	public static final RegistryObject<BlockEntityType<FloodlightBlockEntity>> FLOODLIGHT = REGISTER.register(
			"floodlight", makeType(FloodlightBlockEntity::new, MetalDevices.FLOODLIGHT)
	);
	public static final MultiblockBEType<TurretChemBlockEntity> TURRET_CHEM = makeMultiblock(
			"turretchem", TurretChemBlockEntity::new, MetalDevices.TURRET_CHEM
	);
	public static final MultiblockBEType<TurretGunBlockEntity> TURRET_GUN = makeMultiblock(
			"turretgun", TurretGunBlockEntity::new, MetalDevices.TURRET_GUN
	);
	public static final MultiblockBEType<ClocheBlockEntity> CLOCHE = makeMultiblock(
			"cloche", ClocheBlockEntity::new, MetalDevices.CLOCHE
	);
	public static final RegistryObject<BlockEntityType<ChuteBlockEntity>> CHUTE = REGISTER.register(
			"chute", makeTypeMultipleBlocks(ChuteBlockEntity::new, MetalDevices.CHUTES.values())
	);
	public static final RegistryObject<BlockEntityType<ElectromagnetBlockEntity>> ELECTROMAGNET = REGISTER.register(
			"electromagnet", makeType(ElectromagnetBlockEntity::new, MetalDevices.ELECTROMAGNET)
	);
	public static final RegistryObject<BlockEntityType<FakeLightBlockEntity>> FAKE_LIGHT = REGISTER.register(
			"fakelight", makeType(FakeLightBlockEntity::new, Misc.FAKE_LIGHT)
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

	private static <T extends BlockEntity & IGeneralMultiblock>
	MultiblockBEType<T> makeMultiblock(String name, MultiblockBEType.BEWithTypeConstructor<T> make, Supplier<? extends Block> block)
	{
		return new MultiblockBEType<>(
				name, REGISTER, make, block, state -> state.hasProperty(IEProperties.MULTIBLOCKSLAVE)&&!state.getValue(IEProperties.MULTIBLOCKSLAVE)
		);
	}
}
