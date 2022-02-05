/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.oc2;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.config.CachedConfig.BooleanValue;
import blusunrize.immersiveengineering.common.util.compat.IECompatModules.EarlyIECompatModule;
import li.cil.oc2.api.bus.device.provider.BlockDeviceProvider;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

public class OC2CompatModule extends EarlyIECompatModule
{
	public OC2CompatModule(BooleanValue enabled)
	{
		DeferredRegister<BlockDeviceProvider> register = DeferredRegister.create(BlockDeviceProvider.class, Lib.MODID);
		register.register("generic", () -> new DeviceProvider(enabled));
		register.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
}
