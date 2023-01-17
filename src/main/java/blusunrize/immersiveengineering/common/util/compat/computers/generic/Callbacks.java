/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.MultiblockBEType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine.BottlingMachineLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.sawmill.SawmillLogic;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.owners.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static blusunrize.immersiveengineering.common.register.IEMultiblockLogic.*;

public class Callbacks
{
	private static final Map<ResourceLocation, CallbackOwner<?>> CALLBACKS = new HashMap<>();
	private static boolean initialized = false;

	private static <T extends BlockEntity & IGeneralMultiblock>
	void registerMB(MultiblockBEType<T> type, CallbackOwner<T> owner)
	{
		register(type.dummyHolder(), owner);
		register(type.masterHolder(), owner);
	}

	private static <S extends IMultiblockState>
	void registerMB(MultiblockRegistration<S> type, Callback<S> owner, String name, BlockPos... valid)
	{
		final MultiblockCallbackWrapper<S> wrapped = new MultiblockCallbackWrapper<>(owner, type, name, valid);
		CALLBACKS.put(type.id().withPath(s -> s+MultiblockRegistrationBuilder.MASTER_BE_SUFFIX), wrapped);
		CALLBACKS.put(type.id().withPath(s -> s+MultiblockRegistrationBuilder.DUMMY_BE_SUFFIX), wrapped);
	}

	private static <T extends BlockEntity>
	void register(RegistryObject<BlockEntityType<T>> type, CallbackOwner<T> owner)
	{
		CALLBACKS.put(type.getId(), owner);
	}

	private static void ensureInitialized()
	{
		if(initialized)
			return;
		registerMB(CRUSHER, new CrusherCallbacks(), "crusher", CrusherLogic.REDSTONE_POS);
		registerMB(ARC_FURNACE, new ArcFurnaceCallbacks(), "arc_furnace", ArcFurnaceLogic.REDSTONE_POS);
		registerMB(BOTTLING_MACHINE, new BottlingMachineCallbacks(), "bottling_machine", BottlingMachineLogic.REDSTONE_POS);
		registerMB(DIESEL_GENERATOR, new DieselGenCallbacks(), "diesel_generator", DieselGeneratorLogic.REDSTONE_POS);
		registerMB(EXCAVATOR, new ExcavatorCallbacks(), "exavator", ExcavatorLogic.REDSTONE_POS);
		registerMB(FERMENTER, new FermenterCallbacks(), "fermenter", FermenterLogic.REDSTONE_POS);
		registerMB(SQUEEZER, new SqueezerCallbacks(), "squeezer", SqueezerLogic.REDSTONE_POS);
		registerMB(MIXER, new MixerCallbacks(), "mixer", MixerLogic.REDSTONE_POS);
		registerMB(REFINERY, new RefineryCallbacks(), "refinery", RefineryLogic.REDSTONE_POS);
		registerMB(ASSEMBLER, new AssemblerCallbacks(), "assembler", AssemblerLogic.REDSTONE_PORTS);
		registerMB(AUTO_WORKBENCH, new AutoWorkbenchCallbacks(), "auto_workbench", AutoWorkbenchLogic.REDSTONE_POS);
		registerMB(SILO, new SiloCallbacks(), "silo", SiloLogic.OUTPUT_POS);
		registerMB(SAWMILL, new SawmillCallbacks(), "sawmill", SawmillLogic.REDSTONE_POS);

		register(IEBlockEntities.CAPACITOR_LV, new CapacitorCallbacks("lv"));
		register(IEBlockEntities.CAPACITOR_MV, new CapacitorCallbacks("mv"));
		register(IEBlockEntities.CAPACITOR_HV, new CapacitorCallbacks("hv"));
		register(IEBlockEntities.FLOODLIGHT, new FloodlightCallbacks());

		registerMB(IEBlockEntities.ENERGY_METER, new EnergyMeterCallbacks());
		registerMB(IEBlockEntities.SAMPLE_DRILL, new SampleDrillCallbacks());
		registerMB(IEBlockEntities.TESLACOIL, new TeslaCoilCallbacks());

		initialized = true;
	}

	public static Map<ResourceLocation, CallbackOwner<?>> getCallbacks()
	{
		ensureInitialized();
		return Collections.unmodifiableMap(CALLBACKS);
	}
}
