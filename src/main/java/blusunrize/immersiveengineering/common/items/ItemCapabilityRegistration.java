/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Machines.CapacitorConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.register.IEItems.Weapons;
import blusunrize.immersiveengineering.common.util.EnergyHelper.ItemEnergyStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Function;
import java.util.function.Supplier;

@EventBusSubscriber(bus = Bus.MOD, modid = Lib.MODID)
public class ItemCapabilityRegistration
{
	@SubscribeEvent
	public static void registerBlockCapabilities(RegisterCapabilitiesEvent event)
	{
		registerCapacitorCapabilities(event, MetalDevices.CAPACITOR_LV, IEServerConfig.MACHINES.lvCapConfig);
		registerCapacitorCapabilities(event, MetalDevices.CAPACITOR_MV, IEServerConfig.MACHINES.mvCapConfig);
		registerCapacitorCapabilities(event, MetalDevices.CAPACITOR_HV, IEServerConfig.MACHINES.hvCapConfig);
		DieselToolItem.registerCapabilities(forType(event, Tools.DRILL));
		DieselToolItem.registerCapabilities(forType(event, Tools.BUZZSAW));
		IEShieldItem.registerCapabilities(forType(event, Misc.SHIELD));
		JerrycanItem.registerCapabilities(forType(event, Misc.JERRYCAN));
		PotionBucketItem.registerCapabilities(forType(event, Misc.POTION_BUCKET));
		PowerpackItem.registerCapabilities(forType(event, Misc.POWERPACK));
		RailgunItem.registerCapabilities(forType(event, Weapons.RAILGUN));
		RevolverItem.registerCapabilities(forType(event, Weapons.REVOLVER));
		ChemthrowerItem.registerCapabilities(forType(event, Weapons.CHEMTHROWER));

		InternalStorageItem.registerCapabilitiesISI(forType(event, Tools.TOOLBOX));
		InternalStorageItem.registerCapabilitiesISI(forType(event, Weapons.SPEEDLOADER));
		InternalStorageItem.registerCapabilitiesISI(forType(event, Misc.SKYHOOK));

		IEFluids.registerBucketCapabilities(event);
	}

	private static void registerCapacitorCapabilities(
			RegisterCapabilitiesEvent ev, Supplier<? extends ItemLike> capItem, CapacitorConfig config
	)
	{
		ev.registerItem(
				EnergyStorage.ITEM,
				(stack, $) -> new ItemEnergyStorage(stack, value -> config.storage.getAsInt()),
				capItem.get()
		);
	}

	private static ItemCapabilityRegistrar forType(RegisterCapabilitiesEvent ev, Supplier<? extends ItemLike> type)
	{
		return new ItemCapabilityRegistrar()
		{
			@Override
			public <C, T> void register(ItemCapability<T, C> capability, ICapabilityProvider<ItemStack, C, T> provider)
			{
				ev.registerItem(capability, provider, type.get());
			}
		};
	}

	public interface ItemCapabilityRegistrar
	{
		<C, T> void register(ItemCapability<T, C> capability, ICapabilityProvider<ItemStack, C, T> provider);

		default <T> void register(ItemCapability<T, Void> capability, Function<ItemStack, T> provider)
		{
			register(capability, (stack, $) -> provider.apply(stack));
		}
	}
}
