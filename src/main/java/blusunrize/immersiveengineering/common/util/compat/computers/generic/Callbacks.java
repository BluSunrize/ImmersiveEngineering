/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.owners.*;
import com.google.common.base.Preconditions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fmllegacy.RegistryObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Callbacks
{
	private static final Map<BlockEntityType<?>, CallbackOwner<?>> CALLBACKS = new HashMap<>();
	private static boolean initialized = false;

	private static <T extends BlockEntity> void register(RegistryObject<BlockEntityType<T>> type, CallbackOwner<T> owner)
	{
		Preconditions.checkState(!CALLBACKS.containsKey(type.get()));
		CALLBACKS.put(type.get(), owner);
	}

	private static void ensureInitialized()
	{
		if(initialized)
			return;
		register(IETileTypes.CRUSHER, new CrusherCallbacks());
		register(IETileTypes.ARC_FURNACE, new ArcFurnaceCallbacks());
		register(IETileTypes.BOTTLING_MACHINE, new BottlingMachineCallbacks());
		register(IETileTypes.CAPACITOR_LV, new CapacitorCallbacks("lv"));
		register(IETileTypes.CAPACITOR_MV, new CapacitorCallbacks("mv"));
		register(IETileTypes.CAPACITOR_HV, new CapacitorCallbacks("hv"));
		register(IETileTypes.DIESEL_GENERATOR, new DieselGenCallbacks());
		register(IETileTypes.ENERGY_METER, new EnergyMeterCallbacks());
		register(IETileTypes.EXCAVATOR, new ExcavatorCallbacks());
		register(IETileTypes.FERMENTER, new FermenterCallbacks());
		register(IETileTypes.SQUEEZER, new SqueezerCallbacks());
		register(IETileTypes.MIXER, new MixerCallbacks());
		register(IETileTypes.REFINERY, new RefineryCallbacks());
		register(IETileTypes.FLOODLIGHT, new FloodlightCallbacks());
		register(IETileTypes.SAMPLE_DRILL, new SampleDrillCallbacks());
		register(IETileTypes.TESLACOIL, new TeslaCoilCallbacks());
		register(IETileTypes.ASSEMBLER, new AssemblerCallbacks());
		register(IETileTypes.AUTO_WORKBENCH, new AutoWorkbenchCallbacks());
		register(IETileTypes.SILO, new SiloCallbacks());
		register(IETileTypes.SAWMILL, new SawmillCallbacks());
		initialized = true;
	}

	public static Map<BlockEntityType<?>, CallbackOwner<?>> getCallbacks()
	{
		ensureInitialized();
		return Collections.unmodifiableMap(CALLBACKS);
	}
}
