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
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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
		entityID = entity.getEntityId();
		connection = entity.getConnection();
		linePos = entity.linePos;
		start = entity.start;
		speed = entity.horizontalSpeed;
	}

	public MessageSkyhookSync(PacketBuffer buf)
	{
		entityID = buf.readInt();
		CompoundNBT tag = buf.readCompoundTag();
		connection = new Connection(tag);
		linePos = buf.readDouble();
		speed = buf.readDouble();
		start = new ConnectionPoint(buf.readCompoundTag());
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(entityID);
		buf.writeCompoundTag(connection.toNBT());
		buf.writeDouble(linePos);
		buf.writeDouble(speed);
		buf.writeCompoundTag(start.createTag());
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> {
			World world = ImmersiveEngineering.proxy.getClientWorld();
			if(world!=null)
			{
				Entity ent = world.getEntityByID(entityID);
				if(ent instanceof SkylineHookEntity)
				{
					connection.generateCatenaryData(world);
					((SkylineHookEntity)ent).setConnectionAndPos(connection, start, linePos, speed);
				}
			}
		});
	}
}