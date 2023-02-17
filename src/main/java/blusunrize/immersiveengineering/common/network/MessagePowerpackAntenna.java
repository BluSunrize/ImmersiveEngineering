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
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public class MessagePowerpackAntenna implements IMessage
{
	UUID player;
	boolean remove = false;
	BlockPos from;
	BlockPos to;

	public MessagePowerpackAntenna(Player player, @Nullable Connection connection)
	{
		this.player = player.getUUID();
		if(connection==null)
			this.remove = true;
		else
		{
			this.from = connection.getEndA().position();
			this.to = connection.getEndB().position();
		}
	}

	public MessagePowerpackAntenna(FriendlyByteBuf buf)
	{
		this.player = buf.readUUID();
		this.remove = buf.readBoolean();
		if(!this.remove)
		{
			this.from = buf.readBlockPos();
			this.to = buf.readBlockPos();
		}
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.player);
		buf.writeBoolean(this.remove);
		if(!this.remove)
		{
			buf.writeBlockPos(this.from);
			buf.writeBlockPos(this.to);
		}
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> {
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
}