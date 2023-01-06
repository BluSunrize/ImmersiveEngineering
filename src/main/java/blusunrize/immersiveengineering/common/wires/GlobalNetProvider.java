/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.NetHandlerCapability;
import blusunrize.immersiveengineering.api.wires.proxy.DefaultProxyProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GlobalNetProvider implements ICapabilityProvider, INBTSerializable<CompoundTag>
{
	private final GlobalWireNetwork net;
	private final LazyOptional<GlobalWireNetwork> netOpt;

	public GlobalNetProvider(Level w)
	{
		net = new GlobalWireNetwork(w.isClientSide, new DefaultProxyProvider(w), new WireSyncManager(w));
		netOpt = CapabilityUtils.constantOptional(net);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		return NetHandlerCapability.NET_CAPABILITY.orEmpty(capability, netOpt);
	}

	@Override
	public CompoundTag serializeNBT()
	{
		return net.writeToNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		net.readFromNBT(nbt);
	}
}
