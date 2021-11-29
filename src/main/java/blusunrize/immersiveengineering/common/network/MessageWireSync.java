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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageWireSync implements IMessage
{
	private final ConnectionPoint start;
	private final ConnectionPoint end;
	private final WireType type;
	private final boolean added;
	private final Vec3 offsetStart;
	private final Vec3 offsetEnd;

	public MessageWireSync(Connection conn, boolean added)
	{
		this.start = conn.getEndA();
		this.end = conn.getEndB();
		this.type = conn.type;
		this.added = added;
		this.offsetStart = conn.getEndAOffset();
		this.offsetEnd = conn.getEndBOffset();
	}

	public MessageWireSync(FriendlyByteBuf buf)
	{
		added = buf.readBoolean();
		start = readConnPoint(buf);
		end = readConnPoint(buf);
		type = WireType.getValue(buf.readUtf(128));
		offsetStart = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		offsetEnd = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
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
		buf.writeBoolean(added);
		writeConnPoint(start, buf);
		writeConnPoint(end, buf);
		buf.writeUtf(type.getUniqueName());
		buf.writeDouble(offsetStart.x).writeDouble(offsetStart.y).writeDouble(offsetStart.z);
		buf.writeDouble(offsetEnd.x).writeDouble(offsetEnd.y).writeDouble(offsetEnd.z);
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
				globalNet.addConnection(new Connection(type, start, end, offsetStart, offsetEnd));
			else if(globalNet.getNullableLocalNet(start)!=null&&globalNet.getNullableLocalNet(end)!=null)
			{
				globalNet.removeConnection(new Connection(type, start, end, offsetStart, offsetEnd));
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
