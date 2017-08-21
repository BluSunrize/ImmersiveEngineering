package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.common.items.ItemChemthrower;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageChemthrowerSwitch implements IMessage
{
	String player;
	boolean forward;
	public MessageChemthrowerSwitch(String player, boolean forward)
	{
		this.player = player;
		this.forward = forward;
	}
	public MessageChemthrowerSwitch()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.player = ByteBufUtils.readUTF8String(buf);
		this.forward = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeUTF8String(buf, this.player);
		buf.writeBoolean(this.forward);
	}

	public static class Handler implements IMessageHandler<MessageChemthrowerSwitch, IMessage>
	{
		@Override
		public IMessage onMessage(MessageChemthrowerSwitch message, MessageContext ctx)
		{
			EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(message.player);
			if(player!=null)
			{
				ItemStack equipped = player.getHeldItem(EnumHand.MAIN_HAND);
				if(equipped.getItem() instanceof ItemChemthrower&& ((ItemChemthrower)equipped.getItem()).getUpgrades(equipped).getBoolean("multitank"))
					((ItemChemthrower)equipped.getItem()).switchTank(equipped, message.forward);
			}
			return null;
		}
	}
}