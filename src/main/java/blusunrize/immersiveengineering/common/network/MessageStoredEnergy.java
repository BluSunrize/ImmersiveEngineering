package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.items.VoltmeterItem;
import blusunrize.immersiveengineering.common.items.VoltmeterItem.RemoteEnergyData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.function.Supplier;

public record MessageStoredEnergy(VoltmeterItem.RemoteEnergyData data) implements IMessage
{
	public MessageStoredEnergy(FriendlyByteBuf in)
	{
		this(RemoteEnergyData.read(in));
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		data.write(buf);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> VoltmeterItem.lastEnergyUpdate = data);
	}
}
