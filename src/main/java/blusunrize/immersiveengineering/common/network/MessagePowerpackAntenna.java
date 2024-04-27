/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.client.models.ModelPowerpack;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.UUID;

public record MessagePowerpackAntenna(UUID player, boolean remove, BlockPos from, BlockPos to) implements IMessage
{
	public static final Type<MessagePowerpackAntenna> ID = IMessage.createType("powerpack_antenna");
	public static final StreamCodec<ByteBuf, MessagePowerpackAntenna> CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, MessagePowerpackAntenna::player,
			ByteBufCodecs.BOOL, MessagePowerpackAntenna::remove,
			BlockPos.STREAM_CODEC, MessagePowerpackAntenna::from,
			BlockPos.STREAM_CODEC, MessagePowerpackAntenna::to,
			MessagePowerpackAntenna::new
	);

	public static MessagePowerpackAntenna create(Player player, @Nullable Connection connection)
	{
		if(connection==null)
			return new MessagePowerpackAntenna(player.getUUID(), true, BlockPos.ZERO, BlockPos.ZERO);
		else
			return new MessagePowerpackAntenna(
					player.getUUID(), true, connection.getEndA().position(), connection.getEndB().position()
			);
	}

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> {
			Level world = ImmersiveEngineering.proxy.getClientWorld();
			if(world!=null) // This can happen if the task is scheduled right before leaving the world
			{
				if(this.remove)
					ModelPowerpack.PLAYER_ATTACHED_TO.remove(this.player);
				else
				{
					GlobalWireNetwork global = GlobalWireNetwork.getNetwork(world);
					global.getLocalNet(this.from).getConnections(this.from).stream().filter(conn ->
							// filter to connections matching both points
							(conn.getEndA().position().equals(this.from)&&conn.getEndB().position().equals(this.to))
									||(conn.getEndB().position().equals(this.from)&&conn.getEndA().position().equals(this.to))
					).findFirst().ifPresent(
							// add to map
							c -> ModelPowerpack.PLAYER_ATTACHED_TO.put(this.player, c)
					);
				}
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}