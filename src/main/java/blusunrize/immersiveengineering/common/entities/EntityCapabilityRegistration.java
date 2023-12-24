/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(bus = Bus.MOD, modid = Lib.MODID)
public class EntityCapabilityRegistration
{
	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent ev)
	{
		CrateMinecartEntity.registerCapabilities(ev, IEEntityTypes.CRATE_MINECART);
		CrateMinecartEntity.registerCapabilities(ev, IEEntityTypes.REINFORCED_CRATE_CART);
		BarrelMinecartEntity.registerCapabilities(ev, IEEntityTypes.BARREL_MINECART);
		BarrelMinecartEntity.registerCapabilities(ev, IEEntityTypes.METAL_BARREL_CART);
	}
}
