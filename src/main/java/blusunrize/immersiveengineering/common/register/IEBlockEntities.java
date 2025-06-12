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
import blusunrize.immersiveengineering.common.blocks.IEHangingSignBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IESignBlockEntity;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class IEBlockEntities
{
	public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, ImmersiveEngineering.MODID
	);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BalloonBlockEntity>> BALLOON = REGISTER.register(
			"balloon", makeType(BalloonBlockEntity::new, Cloth.BALLOON)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StripCurtainBlockEntity>> STRIP_CURTAIN = REGISTER.register(
			"stripcurtain", makeType(StripCurtainBlockEntity::new, Cloth.STRIP_CURTAIN)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShaderBannerBlockEntity>> SHADER_BANNER = REGISTER.register(
			"shaderbanner",
			makeTypeMultipleBlocks(ShaderBannerBlockEntity::new, ImmutableSet.of(Cloth.SHADER_BANNER, Cloth.SHADER_BANNER_WALL))
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoresampleBlockEntity>> CORE_SAMPLE = REGISTER.register(
			"coresample", makeType(CoresampleBlockEntity::new, StoneDecoration.CORESAMPLE)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IESignBlockEntity>> SIGN = REGISTER.register(
			"sign", makeTypeMultipleBlocks(IESignBlockEntity::new,
					Stream.of(WoodenDecoration.SIGN, MetalDecoration.STEEL_SIGN, MetalDecoration.ALU_SIGN).mapMulti(SignHolder::mapMultiSign).toList())
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IEHangingSignBlockEntity>> HANGING_SIGN = REGISTER.register(
			"hanging_sign", makeTypeMultipleBlocks(IEHangingSignBlockEntity::new,
					Stream.of(WoodenDecoration.SIGN, MetalDecoration.STEEL_SIGN, MetalDecoration.ALU_SIGN).mapMulti(SignHolder::mapMultiHanging).toList())
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraftingTableBlockEntity>> CRAFTING_TABLE = REGISTER.register(
			"craftingtable", makeType(CraftingTableBlockEntity::new, WoodenDevices.CRAFTING_TABLE)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodenCrateBlockEntity>> WOODEN_CRATE = REGISTER.register(
			"woodencrate", makeTypeMultipleBlocks(WoodenCrateBlockEntity::new, List.of(WoodenDevices.CRATE, WoodenDevices.REINFORCED_CRATE))
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodenBarrelBlockEntity>> WOODEN_BARREL = REGISTER.register(
			"woodenbarrel", makeType(WoodenBarrelBlockEntity::new, WoodenDevices.WOODEN_BARREL)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModWorkbenchBlockEntity>> MOD_WORKBENCH = REGISTER.register(
			"modworkbench", makeType(ModWorkbenchBlockEntity::new, WoodenDevices.WORKBENCH)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlueprintShelfBlockEntity>> BLUEPRINT_SHELF = REGISTER.register(
			"blueprint_shelf", makeType(BlueprintShelfBlockEntity::new, WoodenDevices.BLUEPRINT_SHELF)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CircuitTableBlockEntity>> CIRCUIT_TABLE = REGISTER.register(
			"circuittable", makeType(CircuitTableBlockEntity::new, WoodenDevices.CIRCUIT_TABLE)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SorterBlockEntity>> SORTER = REGISTER.register(
			"sorter", makeType(SorterBlockEntity::new, WoodenDevices.SORTER)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemBatcherBlockEntity>> ITEM_BATCHER = REGISTER.register(
			"itembatcher", makeType(ItemBatcherBlockEntity::new, WoodenDevices.ITEM_BATCHER)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TurntableBlockEntity>> TURNTABLE = REGISTER.register(
			"turntable", makeType(TurntableBlockEntity::new, WoodenDevices.TURNTABLE)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidSorterBlockEntity>> FLUID_SORTER = REGISTER.register(
			"fluidsorter", makeType(FluidSorterBlockEntity::new, WoodenDevices.FLUID_SORTER)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LogicUnitBlockEntity>> LOGIC_UNIT = REGISTER.register(
			"logicunit", makeType(LogicUnitBlockEntity::new, WoodenDevices.LOGIC_UNIT)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineInterfaceBlockEntity>> MACHINE_INTERFACE = REGISTER.register(
			"machineinterface", makeType(MachineInterfaceBlockEntity::new, WoodenDevices.MACHINE_INTERFACE)
	);
	public static final MultiblockBEType<WatermillBlockEntity> WATERMILL = makeMultiblock(
			"watermill", WatermillBlockEntity::new, WoodenDevices.WATERMILL
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WindmillBlockEntity>> WINDMILL = REGISTER.register(
			"windmill", makeType(WindmillBlockEntity::new, WoodenDevices.WINDMILL)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RazorWireBlockEntity>> RAZOR_WIRE = REGISTER.register(
			"razorwire", makeType(RazorWireBlockEntity::new, MetalDevices.RAZOR_WIRE)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ToolboxBlockEntity>> TOOLBOX = REGISTER.register(
			"toolbox", makeType(ToolboxBlockEntity::new, MetalDevices.TOOLBOX)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StructuralArmBlockEntity>> STRUCTURAL_ARM = REGISTER.register(
			"structuralarm",
			makeTypeMultipleBlocks(StructuralArmBlockEntity::new, ImmutableSet.of(MetalDecoration.ALU_SLOPE, MetalDecoration.STEEL_SLOPE))
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConnectorStructuralBlockEntity>> CONNECTOR_STRUCTURAL = REGISTER.register(
			"connectorstructural", makeType(ConnectorStructuralBlockEntity::new, Connectors.CONNECTOR_STRUCTURAL)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TransformerBlockEntity>> TRANSFORMER = REGISTER.register(
			"transformer", makeType(TransformerBlockEntity::new, Connectors.TRANSFORMER)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PostTransformerBlockEntity>> POST_TRANSFORMER = REGISTER.register(
			"posttransformer", makeType(PostTransformerBlockEntity::new, Connectors.POST_TRANSFORMER)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TransformerHVBlockEntity>> TRANSFORMER_HV = REGISTER.register(
			"transformerhv", makeType(TransformerHVBlockEntity::new, Connectors.TRANSFORMER_HV)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BreakerSwitchBlockEntity>> BREAKER_SWITCH = REGISTER.register(
			"breakerswitch", makeType(BreakerSwitchBlockEntity::new, Connectors.BREAKER_SWITCH)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneBreakerBlockEntity>> REDSTONE_BREAKER = REGISTER.register(
			"redstonebreaker", makeType(RedstoneBreakerBlockEntity::new, Connectors.REDSTONE_BREAKER)
	);
	public static final MultiblockBEType<EnergyMeterBlockEntity> ENERGY_METER = makeMultiblock(
			"energymeter", EnergyMeterBlockEntity::new, Connectors.CURRENT_TRANSFORMER
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConnectorRedstoneBlockEntity>> CONNECTOR_REDSTONE = REGISTER.register(
			"connectorredstone", makeType(ConnectorRedstoneBlockEntity::new, Connectors.CONNECTOR_REDSTONE)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConnectorProbeBlockEntity>> CONNECTOR_PROBE = REGISTER.register(
			"connectorprobe", makeType(ConnectorProbeBlockEntity::new, Connectors.CONNECTOR_PROBE)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConnectorBundledBlockEntity>> CONNECTOR_BUNDLED = REGISTER.register(
			"connectorbundled", makeType(ConnectorBundledBlockEntity::new, Connectors.CONNECTOR_BUNDLED)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneStateCellBlockEntity>> REDSTONE_STATE_CELL = REGISTER.register(
			"redstonestatecell", makeType(RedstoneStateCellBlockEntity::new, Connectors.REDSTONE_STATE_CELL)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneTimerBlockEntity>> REDSTONE_TIMER = REGISTER.register(
			"redstonetimer", makeType(RedstoneTimerBlockEntity::new, Connectors.REDSTONE_TIMER)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneSwitchboardBlockEntity>> REDSTONE_SWITCHBOARD = REGISTER.register(
			"redstoneswitchboard", makeType(RedstoneSwitchboardBlockEntity::new, Connectors.REDSTONE_SWITCHBOARD)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SirenBlockEntity>> SIREN = REGISTER.register(
			"siren", makeType(SirenBlockEntity::new, Connectors.SIREN)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FeedthroughBlockEntity>> FEEDTHROUGH = REGISTER.register(
			"feedthrough", makeType(FeedthroughBlockEntity::new, Connectors.FEEDTHROUGH)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CapacitorBlockEntity>> CAPACITOR_LV = REGISTER.register(
			"capacitorlv", makeType((pos, state) -> new CapacitorBlockEntity(IEServerConfig.MACHINES.lvCapConfig, pos, state), MetalDevices.CAPACITOR_LV)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CapacitorBlockEntity>> CAPACITOR_MV = REGISTER.register(
			"capacitormv", makeType((pos, state) -> new CapacitorBlockEntity(IEServerConfig.MACHINES.mvCapConfig, pos, state), MetalDevices.CAPACITOR_MV)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CapacitorBlockEntity>> CAPACITOR_HV = REGISTER.register(
			"capacitorhv", makeType((pos, state) -> new CapacitorBlockEntity(IEServerConfig.MACHINES.hvCapConfig, pos, state), MetalDevices.CAPACITOR_HV)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CapacitorCreativeBlockEntity>> CAPACITOR_CREATIVE = REGISTER.register(
			"capacitorcreative", makeType(CapacitorCreativeBlockEntity::new, MetalDevices.CAPACITOR_CREATIVE)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MetalBarrelBlockEntity>> METAL_BARREL = REGISTER.register(
			"metalbarrel", makeType(MetalBarrelBlockEntity::new, MetalDevices.BARREL)
	);
	public static final MultiblockBEType<FluidPumpBlockEntity> FLUID_PUMP = makeMultiblock(
			"fluidpump", FluidPumpBlockEntity::new, MetalDevices.FLUID_PUMP
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPlacerBlockEntity>> FLUID_PLACER = REGISTER.register(
			"fluidplacer", makeType(FluidPlacerBlockEntity::new, MetalDevices.FLUID_PLACER)
	);
	public static final MultiblockBEType<BlastFurnacePreheaterBlockEntity> BLASTFURNACE_PREHEATER = makeMultiblock(
			"blastfurnacepreheater", BlastFurnacePreheaterBlockEntity::new, MetalDevices.BLAST_FURNACE_PREHEATER
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FurnaceHeaterBlockEntity>> FURNACE_HEATER = REGISTER.register(
			"furnaceheater", makeType(FurnaceHeaterBlockEntity::new, MetalDevices.FURNACE_HEATER)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DynamoBlockEntity>> DYNAMO = REGISTER.register(
			"dynamo", makeType(DynamoBlockEntity::new, MetalDevices.DYNAMO)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThermoelectricGenBlockEntity>> THERMOELECTRIC_GEN = REGISTER.register(
			"thermoelectricgen", makeType(ThermoelectricGenBlockEntity::new, MetalDevices.THERMOELECTRIC_GEN)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricLanternBlockEntity>> ELECTRIC_LANTERN = REGISTER.register(
			"electriclantern", makeType(ElectricLanternBlockEntity::new, MetalDevices.ELECTRIC_LANTERN)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChargingStationBlockEntity>> CHARGING_STATION = REGISTER.register(
			"chargingstation", makeType(ChargingStationBlockEntity::new, MetalDevices.CHARGING_STATION)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE = REGISTER.register(
			"fluidpipe", makeType(FluidPipeBlockEntity::new, MetalDevices.FLUID_PIPE)
	);
	public static final MultiblockBEType<SampleDrillBlockEntity> SAMPLE_DRILL = makeMultiblock(
			"sampledrill", SampleDrillBlockEntity::new, MetalDevices.SAMPLE_DRILL
	);
	public static final MultiblockBEType<TeslaCoilBlockEntity> TESLACOIL = makeMultiblock(
			"teslacoil", TeslaCoilBlockEntity::new, MetalDevices.TESLA_COIL
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FloodlightBlockEntity>> FLOODLIGHT = REGISTER.register(
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
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChuteBlockEntity>> CHUTE = REGISTER.register(
			"chute",
			makeTypeMultipleBlocks(ChuteBlockEntity::new, MetalDevices.CHUTES.values(), MetalDevices.DYED_CHUTES.values())
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectromagnetBlockEntity>> ELECTROMAGNET = REGISTER.register(
			"electromagnet", makeType(ElectromagnetBlockEntity::new, MetalDevices.ELECTROMAGNET)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FakeLightBlockEntity>> FAKE_LIGHT = REGISTER.register(
			"fakelight", makeType(FakeLightBlockEntity::new, Misc.FAKE_LIGHT)
	);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PipeValveBlockEntity>> PIPE_VALVE = REGISTER.register(
			"pipevalve", makeType(PipeValveBlockEntity::new, MetalDevices.PIPE_VALVE)
	);

	static
	{
		EnergyConnectorBlockEntity.registerConnectorTEs(REGISTER);
	}

	public static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeType(BlockEntityType.BlockEntitySupplier<T> create, Supplier<? extends Block> valid)
	{
		return makeTypeMultipleBlocks(create, ImmutableSet.of(valid));
	}

	@SafeVarargs
	public static <T extends BlockEntity> Supplier<BlockEntityType<T>> makeTypeMultipleBlocks(
			BlockEntityType.BlockEntitySupplier<T> create, Collection<? extends Supplier<? extends Block>>... valid
	)
	{
		return () -> new BlockEntityType<>(
				create,
				Arrays.stream(valid)
						.flatMap(Collection::stream)
						.map(Supplier::get)
						.collect(Collectors.toSet()),
				null
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
