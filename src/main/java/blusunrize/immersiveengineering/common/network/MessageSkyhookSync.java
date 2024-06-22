/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageSkyhookSync(
		SyncedConnection connection,
		int entityID,
		ConnectionPoint start,
		double linePos,
		double speed
) implements IMessage
{
	public static final Type<MessageSkyhookSync> ID = IMessage.createType("skyhook_sync");
	public static final StreamCodec<ByteBuf, MessageSkyhookSync> CODEC = StreamCodec.composite(
			SyncedConnection.CODEC, MessageSkyhookSync::connection,
			ByteBufCodecs.INT, MessageSkyhookSync::entityID,
			ConnectionPoint.CODECS.streamCodec(), MessageSkyhookSync::start,
			ByteBufCodecs.DOUBLE, MessageSkyhookSync::linePos,
			ByteBufCodecs.DOUBLE, MessageSkyhookSync::speed,
			MessageSkyhookSync::new
	);

	public MessageSkyhookSync(SkylineHookEntity entity)
	{
		this(
				new SyncedConnection(entity.getConnection()),
				entity.getId(),
				entity.start,
				entity.linePos,
				entity.horizontalSpeed
		);
	}

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> {
			Level world = ImmersiveEngineering.proxy.getClientWorld();
			if(world!=null)
			{
				Entity ent = world.getEntity(entityID);
				if(ent instanceof SkylineHookEntity hook)
					hook.setConnectionAndPos(connection.toConnection(), start, linePos, speed);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}