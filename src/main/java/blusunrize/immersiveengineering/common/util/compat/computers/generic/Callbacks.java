/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.MultiblockBEType;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CrusherLogic;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.owners.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
		final var wrapped = new MultiblockCallbackWrapper<>(owner, type, name, valid);
		CALLBACKS.put(type.masterBE().getId(), wrapped);
		CALLBACKS.put(type.dummyBE().getId(), wrapped);
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
		registerMB(IEMultiblockLogic.CRUSHER, new CrusherCallbacks(), "crusher", CrusherLogic.REDSTONE_POS);
		registerMB(IEBlockEntities.ARC_FURNACE, new ArcFurnaceCallbacks());
		registerMB(IEBlockEntities.BOTTLING_MACHINE, new BottlingMachineCallbacks());
		register(IEBlockEntities.CAPACITOR_LV, new CapacitorCallbacks("lv"));
		register(IEBlockEntities.CAPACITOR_MV, new CapacitorCallbacks("mv"));
		register(IEBlockEntities.CAPACITOR_HV, new CapacitorCallbacks("hv"));
		registerMB(IEBlockEntities.DIESEL_GENERATOR, new DieselGenCallbacks());
		registerMB(IEBlockEntities.ENERGY_METER, new EnergyMeterCallbacks());
		registerMB(IEBlockEntities.EXCAVATOR, new ExcavatorCallbacks());
		registerMB(IEBlockEntities.FERMENTER, new FermenterCallbacks());
		registerMB(IEBlockEntities.SQUEEZER, new SqueezerCallbacks());
		registerMB(IEBlockEntities.MIXER, new MixerCallbacks());
		registerMB(IEBlockEntities.REFINERY, new RefineryCallbacks());
		register(IEBlockEntities.FLOODLIGHT, new FloodlightCallbacks());
		registerMB(IEBlockEntities.SAMPLE_DRILL, new SampleDrillCallbacks());
		registerMB(IEBlockEntities.TESLACOIL, new TeslaCoilCallbacks());
		registerMB(IEBlockEntities.ASSEMBLER, new AssemblerCallbacks());
		registerMB(IEBlockEntities.AUTO_WORKBENCH, new AutoWorkbenchCallbacks());
		registerMB(IEBlockEntities.SILO, new SiloCallbacks());
		registerMB(IEBlockEntities.SAWMILL, new SawmillCallbacks());
		initialized = true;
	}

	public static Map<ResourceLocation, CallbackOwner<?>> getCallbacks()
	{
		ensureInitialized();
		return Collections.unmodifiableMap(CALLBACKS);
	}
}
