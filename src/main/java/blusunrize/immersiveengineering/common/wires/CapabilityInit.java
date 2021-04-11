package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.NetHandlerCapability;
import blusunrize.immersiveengineering.api.wires.proxy.DefaultProxyProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityInit
{
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
		}, () -> {
			throw new IllegalStateException("Can not create global wire network without a world");
		});
	}

	public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundNBT>
	{
		private final GlobalWireNetwork net;
		private final LazyOptional<GlobalWireNetwork> netOpt;

		public Provider(World w)
		{
			net = new GlobalWireNetwork(w.isRemote, new DefaultProxyProvider(w), new WireSyncManager(w));
			netOpt = CapabilityUtils.constantOptional(net);
		}

		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
		{
			if(capability==NetHandlerCapability.NET_CAPABILITY)
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
