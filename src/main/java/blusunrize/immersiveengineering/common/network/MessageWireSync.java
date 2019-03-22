/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.energy.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageWireSync implements IMessage
{
	private ConnectionPoint start;
	private ConnectionPoint end;
	private WireType type;
	private boolean added;

	public MessageWireSync(Connection conn, boolean added)
	{
		this.start = conn.getEndA();
		this.end = conn.getEndB();
		this.type = conn.type;
		this.added = added;
	}

	public MessageWireSync()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		PacketBuffer pb = new PacketBuffer(buf);
		added = pb.readBoolean();
		start = readConnPoint(pb);
		end = readConnPoint(pb);
		if(added)
			type = WireType.getValue(pb.readString(128));
		else
			type = null;
	}

	private ConnectionPoint readConnPoint(PacketBuffer buf)
	{
		return new ConnectionPoint(buf.readBlockPos(), buf.readInt());
	}

	private void writeConnPoint(ConnectionPoint cp, PacketBuffer buf)
	{
		buf.writeBlockPos(cp.getPosition());
		buf.writeInt(cp.getIndex());
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		PacketBuffer pb = new PacketBuffer(buf);
		pb.writeBoolean(added);
		writeConnPoint(start, pb);
		writeConnPoint(end, pb);
		if(added)
			pb.writeString(type.getUniqueName());
	}

	public static class Handler implements IMessageHandler<MessageWireSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageWireSync message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() -> {
				EntityPlayer player = ImmersiveEngineering.proxy.getClientPlayer();
				World w = player.world;
				GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(w);
				if(message.added)
				{
					//TODO handle connections loading before the connectors do!
					globalNet.addConnection(new Connection(message.type, message.start, message.end));
				}
				else
					globalNet.removeConnection(new Connection(WireType.STEEL, message.start, message.end));
				IBlockState state = w.getBlockState(message.start.getPosition());
				w.notifyBlockUpdate(message.start.getPosition(), state, state, 3);
				state = w.getBlockState(message.end.getPosition());
				w.notifyBlockUpdate(message.end.getPosition(), state, state, 3);
			});
			return null;
		}
	}
}