/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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

	public MessageWireSync(PacketBuffer buf)
	{
		added = buf.readBoolean();
		start = readConnPoint(buf);
		end = readConnPoint(buf);
		if(added)
			type = WireType.getValue(buf.readString(128));
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
	public void toBytes(PacketBuffer buf)
	{
		PacketBuffer pb = new PacketBuffer(buf);
		pb.writeBoolean(added);
		writeConnPoint(start, pb);
		writeConnPoint(end, pb);
		if(added)
			pb.writeString(type.getUniqueName());
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> {
			IELogger.logger.debug("Processing sync for connection from {} to {}, type {}, adding {}",
					start, end, type, added);
			PlayerEntity player = ImmersiveEngineering.proxy.getClientPlayer();
			World w = player.world;
			IChunk startChunk = w.getChunk(start.getPosition().getX() >> 4, start.getPosition().getZ() >> 4, ChunkStatus.FULL, false);
			if(startChunk==null)
				IELogger.logger.debug("Start chunk is null");

			GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(w);
			if(added)
				globalNet.addConnection(new Connection(type, start, end));
			else
				globalNet.removeConnection(new Connection(WireType.STEEL, start, end));
			TileEntity startTE = w.getTileEntity(start.getPosition());
			if(startTE!=null)
				startTE.requestModelDataUpdate();
			TileEntity endTE = w.getTileEntity(end.getPosition());
			if(endTE!=null)
				endTE.requestModelDataUpdate();
			BlockState state = w.getBlockState(start.getPosition());
			w.notifyBlockUpdate(start.getPosition(), state, state, 3);
			state = w.getBlockState(end.getPosition());
			w.notifyBlockUpdate(end.getPosition(), state, state, 3);
		});
	}
}