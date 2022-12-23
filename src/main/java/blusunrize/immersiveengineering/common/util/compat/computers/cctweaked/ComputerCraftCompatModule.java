/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.compat.computers.cctweaked;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorBundledBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.util.compat.IECompatModules.StandardIECompatModule;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callbacks;
import com.google.common.base.Suppliers;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class ComputerCraftCompatModule extends StandardIECompatModule
{
	public static Capability<IPeripheral> PERIPHERAL_CAPABILITY = CapabilityManager.get(new CapabilityToken<>()
	{
	});

	private final Map<ResourceLocation, PeripheralCreator<?>> knownPeripherals = new HashMap<>();

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
				channelValues[color] = (byte)(15*((output >> color)&1));
			return channelValues;
		});

		MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, this::attachPeripheral);
		try
		{
			for(Entry<ResourceLocation, CallbackOwner<?>> entry : Callbacks.getCallbacks().entrySet())
				knownPeripherals.put(
						entry.getKey(), new PeripheralCreator<>((CallbackOwner<? extends BlockEntity>)entry.getValue())
				);
		} catch(IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static final ResourceLocation CAP_NAME = ImmersiveEngineering.rl("cc_peripheral");

	private void attachPeripheral(AttachCapabilitiesEvent<BlockEntity> ev)
	{
		if(PERIPHERAL_CAPABILITY==null)
			return;
		BlockEntity te = ev.getObject();
		PeripheralCreator<?> creator = knownPeripherals.get(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(te.getType()));
		if(creator!=null)
		{
			ev.addCapability(CAP_NAME, new ICapabilityProvider()
			{
				private final Supplier<LazyOptional<IPeripheral>> realPeripheral = Suppliers.memoize(
						() -> {
							IPeripheral peripheral = creator.make(te);
							if(peripheral!=null)
								return CapabilityUtils.constantOptional(peripheral);
							else
								return LazyOptional.<IPeripheral>empty();
						}
				);

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
				{
					if(cap==PERIPHERAL_CAPABILITY)
						return realPeripheral.get().cast();
					else
						return LazyOptional.empty();
				}
			});
		}
	}
}
