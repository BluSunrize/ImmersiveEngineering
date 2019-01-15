/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.energy.wires;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

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
			public NBTBase writeNBT(Capability<GlobalWireNetwork> capability, GlobalWireNetwork instance, EnumFacing side)
			{
				return instance.writeToNBT();
			}

			@Override
			public void readNBT(Capability<GlobalWireNetwork> capability, GlobalWireNetwork instance, EnumFacing side, NBTBase nbt)
			{
				instance.readFromNBT((NBTTagCompound)nbt);
			}
		}, GlobalWireNetwork::new);
	}

	public static class Provider implements ICapabilityProvider, INBTSerializable<NBTTagCompound>
	{
		private final GlobalWireNetwork net = new GlobalWireNetwork();
		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
		{
			return capability==NET_CAPABILITY;
		}

		@Nullable
		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
		{
			if (capability==NET_CAPABILITY)
				return NET_CAPABILITY.cast(net);
			return null;
		}

		@Override
		public NBTTagCompound serializeNBT()
		{
			return net.writeToNBT();
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt)
		{
			net.readFromNBT(nbt);
		}
	}
}
