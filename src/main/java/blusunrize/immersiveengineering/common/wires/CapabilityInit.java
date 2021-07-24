package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.NetHandlerCapability;
import blusunrize.immersiveengineering.api.wires.proxy.DefaultProxyProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
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
		CapabilityManager.INSTANCE.register(GlobalWireNetwork.class);
	}

	public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag>
	{
		private final GlobalWireNetwork net;
		private final LazyOptional<GlobalWireNetwork> netOpt;

		public Provider(Level w)
		{
			net = new GlobalWireNetwork(w.isClientSide, new DefaultProxyProvider(w), new WireSyncManager(w));
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
}
