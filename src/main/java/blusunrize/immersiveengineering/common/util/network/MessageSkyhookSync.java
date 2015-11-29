package blusunrize.immersiveengineering.common.util.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageSkyhookSync implements IMessage
{
	int entityID;
	Connection connection;
	ChunkCoordinates target;
	Vec3[] subPoints;
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
		target = new ChunkCoordinates(buf.readInt(),buf.readInt(),buf.readInt());
		int l = buf.readInt();
		subPoints = new Vec3[l];
		for(int i=0; i<l; i++)
			subPoints[i] = Vec3.createVectorHelper(buf.readDouble(), buf.readDouble(), buf.readDouble());
		targetPoint = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(entityID);
		ByteBufUtils.writeTag(buf,connection.writeToNBT()); 
		buf.writeInt(target.posX);
		buf.writeInt(target.posY);
		buf.writeInt(target.posZ);
		buf.writeInt(subPoints.length);
		for(Vec3 v : subPoints)
		{
			buf.writeDouble(v.xCoord);
			buf.writeDouble(v.yCoord);
			buf.writeDouble(v.zCoord);
		}
		buf.writeInt(targetPoint);
	}

	public static class Handler implements IMessageHandler<MessageSkyhookSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageSkyhookSync message, MessageContext ctx)
		{
			Entity ent = ClientUtils.mc().theWorld.getEntityByID(message.entityID);
			if(ent instanceof EntitySkylineHook)
			{
				((EntitySkylineHook)ent).connection = message.connection;
				((EntitySkylineHook)ent).target = message.target;
				((EntitySkylineHook)ent).subPoints = message.subPoints;
				((EntitySkylineHook)ent).targetPoint = message.targetPoint;
			}
			return null;
		}
	}
}