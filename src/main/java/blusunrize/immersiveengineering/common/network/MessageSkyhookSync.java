/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.wires.old.ImmersiveNetHandler;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class MessageSkyhookSync implements IMessage
{
	private int entityID;
	private ImmersiveNetHandler.Connection connection;
	private double linePos;
	private double speed;

	public MessageSkyhookSync(EntitySkylineHook entity)
	{
		entityID = entity.getEntityId();
		connection = entity.getConnection();
		linePos = entity.linePos;
		speed = entity.horizontalSpeed;
	}

	public MessageSkyhookSync(PacketBuffer buf)
	{
		entityID = buf.readInt();
		CompoundNBT tag = buf.readCompoundTag();
		connection = ImmersiveNetHandler.Connection.readFromNBT(tag);
		linePos = buf.readDouble();
		speed = buf.readDouble();
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(entityID);
		buf.writeCompoundTag(connection.writeToNBT());
		buf.writeDouble(linePos);
		buf.writeDouble(speed);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Minecraft.getInstance().addScheduledTask(() -> {
			World world = ImmersiveEngineering.proxy.getClientWorld();
			if(world!=null)
			{
				Entity ent = world.getEntityByID(entityID);
				if(ent instanceof EntitySkylineHook)
				{
					connection.getSubVertices(world);
					((EntitySkylineHook)ent).setConnectionAndPos(connection, linePos, speed);
				}
			}
		});
	}
}