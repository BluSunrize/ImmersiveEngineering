package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.common.items.ItemIEShield;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageMagnetEquip implements IMessage
{
	String player;
	int fetchSlot;
	public MessageMagnetEquip(String player, int fetch)
	{
		this.player = player;
		this.fetchSlot = fetch;
	}
	public MessageMagnetEquip()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.player = ByteBufUtils.readUTF8String(buf);
		this.fetchSlot = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, this.player);
		buf.writeInt(this.fetchSlot);
	}

	public static class Handler implements IMessageHandler<MessageMagnetEquip, IMessage>
	{
		@Override
		public IMessage onMessage(MessageMagnetEquip message, MessageContext ctx)
		{
			EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(message.player);
			if(player!=null)
			{
				ItemStack held = player.getHeldItem(EnumHand.OFF_HAND);
				if(message.fetchSlot>=0)
				{
					ItemStack s = player.inventory.mainInventory.get(message.fetchSlot);
					if(!s.isEmpty()&&s.getItem() instanceof ItemIEShield&&((ItemIEShield)s.getItem()).getUpgrades(s).getBoolean("magnet"))
					{
						((ItemIEShield)s.getItem()).getUpgrades(s).setInteger("prevSlot", message.fetchSlot);
						player.inventory.mainInventory.set(message.fetchSlot, held);
						player.setHeldItem(EnumHand.OFF_HAND, s);
					}
				}
				else
				{
					int prevSlot = ((ItemIEShield)held.getItem()).getUpgrades(held).getInteger("prevSlot");
					ItemStack s = player.inventory.mainInventory.get(prevSlot);
					player.inventory.mainInventory.set(prevSlot, held);
					player.setHeldItem(EnumHand.OFF_HAND, s);
					((ItemIEShield)held.getItem()).getUpgrades(held).removeTag("prevSlot");
				}
			}
			return null;
		}
	}
}