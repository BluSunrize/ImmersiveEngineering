/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.compat.computers.cctweaked;

import blusunrize.immersiveengineering.common.blocks.metal.ConnectorBundledBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.util.compat.IECompatModules.StandardIECompatModule;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callbacks;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.Map.Entry;
import java.util.Objects;

public class ComputerCraftCompatModule extends StandardIECompatModule
{
	@Override
	public void init()
	{
		ComputerCraftAPI.registerBundledRedstoneProvider((world, pos, direction) -> {
			final int doNotHandle = -1;
			BlockState state = world.getBlockState(pos);
			if(state.getBlock()!=Connectors.CONNECTOR_BUNDLED.get())
				return doNotHandle;
			BlockEntity tile = world.getBlockEntity(pos);
			if(!(tile instanceof ConnectorBundledBlockEntity bundled))
				return doNotHandle;
			int bits = 0;
			for(int color = 0; color < 16; ++color)
				if(bundled.getValue(color) > 0)
					bits |= 1<<color;
			return bits;
		});

		ConnectorBundledBlockEntity.EXTRA_SOURCES.add((world, emittingBlock, emittingSide) -> {
			int output = ComputerCraftAPI.getBundledRedstoneOutput(world, emittingBlock, emittingSide);
			if(output==0||output==-1)
			{
				return null;
			}
			byte[] channelValues = new byte[16];
			for(int color = 0; color < 16; ++color)
				channelValues[color] = (byte)(15*((output>>color)&1));
			return channelValues;
		});

		// TODO: modBus.addListener(ComputerCraftCompatModule::registerCapabilities);
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event)
	{
		for(Entry<ResourceLocation, CallbackOwner<?>> entry : Callbacks.getCallbacks().entrySet())
		{

			PeripheralCreator<?> creator;
			try
			{
				creator = new PeripheralCreator<>((CallbackOwner<? extends BlockEntity>)entry.getValue());
			} catch(IllegalAccessException e)
			{
				throw new RuntimeException("Failed to get peripheral methods for "+entry.getKey(), e);
			}

			event.registerBlockEntity(
					PeripheralCapability.get(),
					Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.get(entry.getKey())),
					(be, direction) -> creator.make(be)
			);
		}
	}
}
