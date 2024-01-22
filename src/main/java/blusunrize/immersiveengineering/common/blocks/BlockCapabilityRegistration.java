/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.cloth.BalloonBlockEntity;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.VanillaFurnaceHeater;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@EventBusSubscriber(bus = Bus.MOD, modid = Lib.MODID)
public class BlockCapabilityRegistration
{
	@SubscribeEvent
	public static void registerBlockCapabilities(RegisterCapabilitiesEvent event)
	{
		// Cloth
		BalloonBlockEntity.registerCapabilities(forType(event, IEBlockEntities.BALLOON));
		ShaderBannerBlockEntity.registerCapabilities(forType(event, IEBlockEntities.SHADER_BANNER));

		// Metal
		BlastFurnacePreheaterBlockEntity.registerCapabilities(forType(event, IEBlockEntities.BLASTFURNACE_PREHEATER));
		CapacitorBlockEntity.registerCapabilities(forType(event, IEBlockEntities.CAPACITOR_LV));
		CapacitorBlockEntity.registerCapabilities(forType(event, IEBlockEntities.CAPACITOR_MV));
		CapacitorBlockEntity.registerCapabilities(forType(event, IEBlockEntities.CAPACITOR_HV));
		ChargingStationBlockEntity.registerCapabilities(forType(event, IEBlockEntities.CHARGING_STATION));
		ChuteBlockEntity.registerCapabilities(forType(event, IEBlockEntities.CHUTE));
		ClocheBlockEntity.registerCapabilities(forType(event, IEBlockEntities.CLOCHE));
		for(Supplier<BlockEntityType<?>> beType : ConveyorBeltBlockEntity.BE_TYPES.values())
			ConveyorBeltBlockEntity.registerCapabilities(
					forType(event, () -> (BlockEntityType<ConveyorBeltBlockEntity<?>>)beType.get())
			);
		DynamoBlockEntity.registerCapabilities(forType(event, IEBlockEntities.DYNAMO));
		EnergyConnectorBlockEntity.registerCapabilities(event);
		FluidPipeBlockEntity.registerCapabilities(forType(event, IEBlockEntities.FLUID_PIPE));
		FluidPumpBlockEntity.registerCapabilities(forType(event, IEBlockEntities.FLUID_PUMP));
		FluidPlacerBlockEntity.registerCapabilities(forType(event, IEBlockEntities.FLUID_PLACER));
		ElectromagnetBlockEntity.registerCapabilities(forType(event, IEBlockEntities.ELECTROMAGNET));
		MetalBarrelBlockEntity.registerCapabilities(forType(event, IEBlockEntities.METAL_BARREL));
		SampleDrillBlockEntity.registerCapabilities(forType(event, IEBlockEntities.SAMPLE_DRILL));
		TeslaCoilBlockEntity.registerCapabilities(forType(event, IEBlockEntities.TESLACOIL));
		ThermoelectricGenBlockEntity.registerCapabilities(forType(event, IEBlockEntities.THERMOELECTRIC_GEN));
		TurretChemBlockEntity.registerCapabilities(forType(event, IEBlockEntities.TURRET_CHEM));
		TurretGunBlockEntity.registerCapabilities(forType(event, IEBlockEntities.TURRET_GUN));

		// Wood
		CircuitTableBlockEntity.registerCapabilities(forType(event, IEBlockEntities.CIRCUIT_TABLE));
		CraftingTableBlockEntity.registerCapabilities(forType(event, IEBlockEntities.CRAFTING_TABLE));
		FluidSorterBlockEntity.registerCapabilities(forType(event, IEBlockEntities.FLUID_SORTER));
		ItemBatcherBlockEntity.registerCapabilities(forType(event, IEBlockEntities.ITEM_BATCHER));
		LogicUnitBlockEntity.registerCapabilities(forType(event, IEBlockEntities.LOGIC_UNIT));
		SorterBlockEntity.registerCapabilities(forType(event, IEBlockEntities.SORTER));
		WoodenBarrelBlockEntity.registerCapabilities(forType(event, IEBlockEntities.WOODEN_BARREL));
		WoodenCrateBlockEntity.registerCapabilities(forType(event, IEBlockEntities.WOODEN_CRATE));

		// Vanilla
		event.registerBlockEntity(
				ExternalHeaterHandler.CAPABILITY, BlockEntityType.FURNACE, (be, side) -> new VanillaFurnaceHeater(be)
		);
	}

	private static <BE extends BlockEntity> BECapabilityRegistrar<BE> forType(
			RegisterCapabilitiesEvent ev, Supplier<BlockEntityType<BE>> type
	)
	{
		return new BECapabilityRegistrar<>()
		{
			@Override
			public <C, T> void register(BlockCapability<T, C> capability, ICapabilityProvider<? super BE, C, T> provider)
			{
				ev.registerBlockEntity(capability, type.get(), provider);
			}
		};
	}

	private static <BE extends BlockEntity & IGeneralMultiblock> BECapabilityRegistrar<BE> forType(
			RegisterCapabilitiesEvent ev, MultiblockBEType<BE> type
	)
	{
		return new BECapabilityRegistrar<>()
		{
			@Override
			public <C, T> void register(BlockCapability<T, C> capability, ICapabilityProvider<? super BE, C, T> provider)
			{
				ev.registerBlockEntity(capability, type.dummy(), provider);
				ev.registerBlockEntity(capability, type.master(), provider);
			}
		};
	}

	public interface BECapabilityRegistrar<BE>
	{
		<C, T> void register(BlockCapability<T, C> capability, ICapabilityProvider<? super BE, C, T> provider);

		default <C, T> void registerOnContext(
				BlockCapability<T, C> capability, Function<? super BE, T> getValue, C onContext
		)
		{
			register(capability, (be, ctx) -> Objects.equals(onContext, ctx)?getValue.apply(be): null);
		}

		default <T> void registerAllContexts(BlockCapability<T, ?> capability, Function<? super BE, T> getValue)
		{
			register(capability, (be, ctx) -> getValue.apply(be));
		}
	}
}
