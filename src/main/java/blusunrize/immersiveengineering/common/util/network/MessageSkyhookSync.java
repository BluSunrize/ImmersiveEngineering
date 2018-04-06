/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSkyhookSync implements IMessage
{
	int entityID;
	Connection connection;
	BlockPos target;
	Vec3d[] subPoints;
	int targetPoint;

	public MessageSkyhookSync(EntitySkylineHook entity)
	{
		entityID = entity.getEntityId();
		connection = entity.connection;
		target = entity.target;
		subPoints = entity.subPoints;
		targetPoint = entity.targetPoint;
	}
	public MessageSkyhookSync()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		entityID = buf.readInt();
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		connection = Connection.readFromNBT(tag);
		target = new BlockPos(buf.readInt(),buf.readInt(),buf.readInt());
		int l = buf.readInt();
		subPoints = new Vec3d[l];
		for(int i=0; i<l; i++)
			subPoints[i] = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		targetPoint = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(entityID);
		ByteBufUtils.writeTag(buf,connection.writeToNBT());
		buf.writeInt(target.getX());
		buf.writeInt(target.getY());
		buf.writeInt(target.getZ());
		buf.writeInt(subPoints.length);
		for(Vec3d v : subPoints)
		{
			buf.writeDouble(v.x);
			buf.writeDouble(v.y);
			buf.writeDouble(v.z);
		}
		buf.writeInt(targetPoint);
	}

	public static class Handler implements IMessageHandler<MessageSkyhookSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageSkyhookSync message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() -> {
				World world = ImmersiveEngineering.proxy.getClientWorld();
				if(world!=null)
				{
					Entity ent = world.getEntityByID(message.entityID);
					if(ent instanceof EntitySkylineHook)
					{
						((EntitySkylineHook)ent).connection = message.connection;
						((EntitySkylineHook)ent).target = message.target;
						((EntitySkylineHook)ent).subPoints = message.subPoints;
						((EntitySkylineHook)ent).targetPoint = message.targetPoint;
					}
				}
			});
			return null;
		}
	}
}