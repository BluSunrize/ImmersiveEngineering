package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSkyhookSync implements IMessage
{
	int entityID;
	Connection connection;
	BlockPos target;
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
		target = new BlockPos(buf.readInt(),buf.readInt(),buf.readInt());
		int l = buf.readInt();
		subPoints = new Vec3[l];
		for(int i=0; i<l; i++)
			subPoints[i] = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
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