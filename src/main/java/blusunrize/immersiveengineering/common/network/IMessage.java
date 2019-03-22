package blusunrize.immersiveengineering.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public interface IMessage
{
	void toBytes(PacketBuffer buf);

	void process(Supplier<Context> context);
}
