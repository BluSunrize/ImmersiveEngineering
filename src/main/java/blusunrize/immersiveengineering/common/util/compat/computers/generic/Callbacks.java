/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.MultiblockBEType;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
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

	private static <T extends BlockEntity & IGeneralMultiblock>
	void register(MultiblockBEType<T> type, CallbackOwner<T> owner)
	{
		register(type.dummy(), owner);
		register(type.master(), owner);
	}

	private static <T extends BlockEntity> void register(RegistryObject<BlockEntityType<T>> type, CallbackOwner<T> owner)
	{
		register(type.get(), owner);
	}

	private static <T extends BlockEntity> void register(BlockEntityType<T> type, CallbackOwner<T> owner)
	{
		Preconditions.checkState(!CALLBACKS.containsKey(type));
		CALLBACKS.put(type, owner);
	}

	private static void ensureInitialized()
	{
		if(initialized)
			return;
		register(IEBlockEntities.CRUSHER, new CrusherCallbacks());
		register(IEBlockEntities.ARC_FURNACE, new ArcFurnaceCallbacks());
		register(IEBlockEntities.BOTTLING_MACHINE, new BottlingMachineCallbacks());
		register(IEBlockEntities.CAPACITOR_LV, new CapacitorCallbacks("lv"));
		register(IEBlockEntities.CAPACITOR_MV, new CapacitorCallbacks("mv"));
		register(IEBlockEntities.CAPACITOR_HV, new CapacitorCallbacks("hv"));
		register(IEBlockEntities.DIESEL_GENERATOR, new DieselGenCallbacks());
		register(IEBlockEntities.ENERGY_METER, new EnergyMeterCallbacks());
		register(IEBlockEntities.EXCAVATOR, new ExcavatorCallbacks());
		register(IEBlockEntities.FERMENTER, new FermenterCallbacks());
		register(IEBlockEntities.SQUEEZER, new SqueezerCallbacks());
		register(IEBlockEntities.MIXER, new MixerCallbacks());
		register(IEBlockEntities.REFINERY, new RefineryCallbacks());
		register(IEBlockEntities.FLOODLIGHT, new FloodlightCallbacks());
		register(IEBlockEntities.SAMPLE_DRILL, new SampleDrillCallbacks());
		register(IEBlockEntities.TESLACOIL, new TeslaCoilCallbacks());
		register(IEBlockEntities.ASSEMBLER, new AssemblerCallbacks());
		register(IEBlockEntities.AUTO_WORKBENCH, new AutoWorkbenchCallbacks());
		register(IEBlockEntities.SILO, new SiloCallbacks());
		register(IEBlockEntities.SAWMILL, new SawmillCallbacks());
		initialized = true;
	}

	public static Map<BlockEntityType<?>, CallbackOwner<?>> getCallbacks()
	{
		ensureInitialized();
		return Collections.unmodifiableMap(CALLBACKS);
	}
}
