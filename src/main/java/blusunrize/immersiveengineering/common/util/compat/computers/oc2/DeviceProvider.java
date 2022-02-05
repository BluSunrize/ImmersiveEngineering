/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.oc2;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.config.CachedConfig.BooleanValue;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callbacks;
import li.cil.oc2.api.bus.device.Device;
import li.cil.oc2.api.bus.device.provider.BlockDeviceQuery;
import li.cil.oc2.api.util.Invalidatable;
import li.cil.oc2.common.bus.device.provider.util.AbstractBlockDeviceProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DeviceProvider extends AbstractBlockDeviceProvider
{
	private final Map<ResourceLocation, WrappedOwner<?>> wrappedOwners;
	private final BooleanValue enabled;

	public DeviceProvider(BooleanValue enabled)
	{
		this.enabled = enabled;
		Map<ResourceLocation, WrappedOwner<?>> map = new HashMap<>();
		try
		{
			for(Entry<ResourceLocation, CallbackOwner<?>> e : Callbacks.getCallbacks().entrySet())
				map.put(e.getKey(), new WrappedOwner<>(e.getValue()));
		} catch(IllegalAccessException ex)
		{
			throw new RuntimeException(ex);
		}
		this.wrappedOwners = Collections.unmodifiableMap(map);
	}

	@Nonnull
	@Override
	public Invalidatable<Device> getDevice(@Nonnull BlockDeviceQuery blockDeviceQuery)
	{
		if(!enabled.get())
			return Invalidatable.empty();
		BlockEntity blockEntity = blockDeviceQuery.getLevel().getBlockEntity(blockDeviceQuery.getQueryPosition());
		if(!(blockEntity instanceof IEBaseBlockEntity ieBE))
			return Invalidatable.empty();
		WrappedOwner<?> owner = wrappedOwners.get(ieBE.getType().getRegistryName());
		Invalidatable<Device> result = getDevice(owner, ieBE);
		if(result.isPresent())
			ieBE.addCapInvalidateHook(result::invalidate);
		return result;
	}

	private <T> Invalidatable<Device> getDevice(WrappedOwner<T> owner, BlockEntity actualBE)
	{
		@SuppressWarnings("unchecked") T asT = (T)actualBE;
		if(owner==null||!owner.getOwner().canAttachTo(asT))
			return Invalidatable.empty();
		return Invalidatable.of(new IEDevice<>(owner, owner.getOwner().preprocess(asT)));
	}
}
