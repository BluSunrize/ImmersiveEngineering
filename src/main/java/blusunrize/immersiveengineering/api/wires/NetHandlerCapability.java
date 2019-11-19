/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NetHandlerCapability
{
	@CapabilityInject(GlobalWireNetwork.class)
	public static Capability<GlobalWireNetwork> NET_CAPABILITY = null;

	public static void register()
	{
		CapabilityManager.INSTANCE.register(GlobalWireNetwork.class, new Capability.IStorage<GlobalWireNetwork>()
		{
			@Override
			public INBT writeNBT(Capability<GlobalWireNetwork> capability, GlobalWireNetwork instance, Direction side)
			{
				return instance.writeToNBT();
			}

			@Override
			public void readNBT(Capability<GlobalWireNetwork> capability, GlobalWireNetwork instance, Direction side, INBT nbt)
			{
				instance.readFromNBT((CompoundNBT)nbt);
			}
			//TODO whatb is this used for? How will it work in 1.13?
		}, () -> new GlobalWireNetwork(null));
	}

	public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundNBT>
	{
		private final GlobalWireNetwork net;
		private final LazyOptional<GlobalWireNetwork> netOpt;

		public Provider(World w)
		{
			net = new GlobalWireNetwork(w);
			netOpt = ApiUtils.constantOptional(net);
		}

		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
		{
			if (capability==NET_CAPABILITY)
				return netOpt.cast();
			return LazyOptional.empty();
		}

		@Override
		public CompoundNBT serializeNBT()
		{
			return net.writeToNBT();
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt)
		{
			net.readFromNBT(nbt);
		}
	}
}
