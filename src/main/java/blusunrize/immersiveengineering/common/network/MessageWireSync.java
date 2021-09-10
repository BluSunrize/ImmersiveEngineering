/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

import java.util.function.Supplier;

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

	public MessageWireSync(FriendlyByteBuf buf)
	{
		added = buf.readBoolean();
		start = readConnPoint(buf);
		end = readConnPoint(buf);
		type = WireType.getValue(buf.readUtf(128));
	}

	private ConnectionPoint readConnPoint(FriendlyByteBuf buf)
	{
		return new ConnectionPoint(buf.readBlockPos(), buf.readInt());
	}

	private void writeConnPoint(ConnectionPoint cp, FriendlyByteBuf buf)
	{
		buf.writeBlockPos(cp.getPosition());
		buf.writeInt(cp.getIndex());
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		FriendlyByteBuf pb = new FriendlyByteBuf(buf);
		pb.writeBoolean(added);
		writeConnPoint(start, pb);
		writeConnPoint(end, pb);
		pb.writeUtf(type.getUniqueName());
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> {
			WireLogger.logger.debug("Processing sync for connection from {} to {}, type {}, adding {}",
					start, end, type, added);
			Player player = ImmersiveEngineering.proxy.getClientPlayer();
			Level w = player.level;

			GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(w);
			if(added)
				globalNet.addConnection(new Connection(type, start, end));
			else if(globalNet.getNullableLocalNet(start)!=null&&globalNet.getNullableLocalNet(end)!=null)
			{
				globalNet.removeConnection(new Connection(type, start, end));
				removeProxyIfNoWires(start, globalNet);
				removeProxyIfNoWires(end, globalNet);
			}
			BlockEntity startTE = w.getBlockEntity(start.getPosition());
			if(startTE!=null)
				startTE.requestModelDataUpdate();
			BlockEntity endTE = w.getBlockEntity(end.getPosition());
			if(endTE!=null)
				endTE.requestModelDataUpdate();
			BlockState state = w.getBlockState(start.getPosition());
			w.sendBlockUpdated(start.getPosition(), state, state, 3);
			state = w.getBlockState(end.getPosition());
			w.sendBlockUpdated(end.getPosition(), state, state, 3);
		});
		context.get().setPacketHandled(true);
	}

	private void removeProxyIfNoWires(ConnectionPoint point, GlobalWireNetwork globalNet)
	{
		LocalWireNetwork localNet = globalNet.getLocalNet(point);
		IImmersiveConnectable iic = localNet.getConnector(point);
		if(iic.isProxy()&&!WireUtils.hasAnyConnections(globalNet, iic))
			globalNet.removeConnector(iic);
	}
}
