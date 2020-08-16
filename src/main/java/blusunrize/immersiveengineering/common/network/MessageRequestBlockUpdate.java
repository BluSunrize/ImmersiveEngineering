/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.EventHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Objects;
import java.util.function.Supplier;

public class MessageRequestBlockUpdate implements IMessage
{
	private BlockPos pos;

	public MessageRequestBlockUpdate(BlockPos pos)
	{
		this.pos = pos;
	}

	public MessageRequestBlockUpdate(PacketBuffer buf)
	{
		pos = buf.readBlockPos();
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeBlockPos(pos);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Context ctx = context.get();
		ServerWorld world = Objects.requireNonNull(ctx.getSender()).getServerWorld();
		ctx.enqueueWork(() -> {
			if(world.isAreaLoaded(pos, 1))
				EventHandler.requestedBlockUpdates.offer(new ImmutablePair<>(world.getDimensionKey(), pos));
		});
	}
}