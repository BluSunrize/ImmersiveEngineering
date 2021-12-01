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
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageSkyhookSync implements IMessage
{
	private int entityID;
	private Connection connection;
	private ConnectionPoint start;
	private double linePos;
	private double speed;

	public MessageSkyhookSync(SkylineHookEntity entity)
	{
		entityID = entity.getId();
		connection = entity.getConnection();
		linePos = entity.linePos;
		start = entity.start;
		speed = entity.horizontalSpeed;
	}

	public MessageSkyhookSync(FriendlyByteBuf buf)
	{
		entityID = buf.readInt();
		CompoundTag tag = buf.readNbt();
		connection = new Connection(tag);
		linePos = buf.readDouble();
		speed = buf.readDouble();
		start = new ConnectionPoint(buf.readNbt());
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeInt(entityID);
		buf.writeNbt(connection.toNBT());
		buf.writeDouble(linePos);
		buf.writeDouble(speed);
		buf.writeNbt(start.createTag());
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> {
			Level world = ImmersiveEngineering.proxy.getClientWorld();
			if(world!=null)
			{
				Entity ent = world.getEntity(entityID);
				if(ent instanceof SkylineHookEntity hook)
				{
					connection.generateCatenaryData();
					hook.setConnectionAndPos(connection, start, linePos, speed);
				}
			}
		});
	}
}