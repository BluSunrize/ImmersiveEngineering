package blusunrize.immersiveengineering.common.util.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.ModelShaderMinecart;
import blusunrize.immersiveengineering.common.entities.EntityPropertiesShaderCart;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageMinecartShaderSync implements IMessage
{
	int dimension;
	int entityID;
	boolean request=false;
	ItemStack shader;
	public MessageMinecartShaderSync(Entity entity, Object o)
	{
		this.dimension = entity.worldObj.provider.dimensionId;
		this.entityID = entity.getEntityId();
		if(o instanceof EntityPropertiesShaderCart)
			shader = ((EntityPropertiesShaderCart)o).getShader();
		else
			request = true;
	}
	public MessageMinecartShaderSync()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.dimension = buf.readInt();
		this.entityID = buf.readInt();
		this.request = buf.readBoolean();
		if(!request)
			this.shader = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.dimension);
		buf.writeInt(this.entityID);
		buf.writeBoolean(this.request);
		if(!request)
			ByteBufUtils.writeItemStack(buf,this.shader); 
	}

	public static class HandlerServer implements IMessageHandler<MessageMinecartShaderSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageMinecartShaderSync message, MessageContext ctx)
		{
			World world = DimensionManager.getWorld(message.dimension);
			if(world!=null)
			{
				Entity entity = world.getEntityByID(message.entityID);
				if(entity!=null)
				{
					EntityPropertiesShaderCart properties = (EntityPropertiesShaderCart)entity.getExtendedProperties(EntityPropertiesShaderCart.PROPERTY_NAME);
					if(properties!=null)
						ImmersiveEngineering.packetHandler.sendToAll(new MessageMinecartShaderSync(entity, properties));
				}
			}
			return null;
		}
	}
	public static class HandlerClient implements IMessageHandler<MessageMinecartShaderSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageMinecartShaderSync message, MessageContext ctx)
		{
			World world = ImmersiveEngineering.proxy.getClientWorld();
			if(world!=null)
			{
				Entity entity = world.getEntityByID(message.entityID);
				if(entity instanceof EntityMinecart)
					ModelShaderMinecart.shadedCarts.put(message.entityID, message.shader);
			}
			return null;
		}
	}
}