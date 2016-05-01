package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.common.util.ManeuverGearHelper;
import blusunrize.immersiveengineering.common.util.ManeuverGearHelper.ManeuverGearOperator;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageGrapplingHook implements IMessage
{
	int dimension;
	int entityID;
	ManeuverGearOperator operator;
	
	public MessageGrapplingHook(EntityPlayer player, ManeuverGearOperator operator)
	{
		this.dimension = player.worldObj.provider.getDimensionId();
		this.entityID = player.getEntityId();
		this.operator = operator;
	}
	public MessageGrapplingHook()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.dimension = buf.readInt();
		this.entityID = buf.readInt();
		this.operator = ManeuverGearOperator.values()[buf.readInt()];
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.dimension);
		buf.writeInt(this.entityID);
		buf.writeInt(this.operator.ordinal());
	}

	public static class Handler implements IMessageHandler<MessageGrapplingHook, IMessage>
	{
		@Override
		public IMessage onMessage(MessageGrapplingHook message, MessageContext ctx)
		{
			World world = DimensionManager.getWorld(message.dimension);
			if(world!=null)
			{
				Entity entity = world.getEntityByID(message.entityID);
				if(entity!=null && entity instanceof EntityPlayer)
				{
					EntityPlayer player = (EntityPlayer)entity;
					switch(message.operator)
					{
					case PRESS_0:
						ManeuverGearHelper.pressHookButton(player, 0);
						break;
					case PRESS_1:
						ManeuverGearHelper.pressHookButton(player, 1);
						break;
					case RELEASE_0:
						ManeuverGearHelper.releaseHookButton(player, 0);
						break;
					case RELEASE_1:
						ManeuverGearHelper.releaseHookButton(player, 1);
						break;
					case RETRACT_0:
						ManeuverGearHelper.returnHook(player, 0);
						break;
					case RETRACT_1:
						ManeuverGearHelper.returnHook(player, 1);
						break;
					case RETRACT_ALL:
						ManeuverGearHelper.returnHook(player, 0);
						ManeuverGearHelper.returnHook(player, 1);
						break;
					case PRESS_SPACE:
						ManeuverGearHelper.doGasJump(player);
						break;
					default:
						break;
					}
				}
			}
			return null;
		}
	}
}