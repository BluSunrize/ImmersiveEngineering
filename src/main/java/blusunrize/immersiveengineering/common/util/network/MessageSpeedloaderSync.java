package blusunrize.immersiveengineering.common.util.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.common.IEContent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class MessageSpeedloaderSync implements IMessage
{
	int slot;
	public MessageSpeedloaderSync(int slot)
	{
		this.slot = slot;
	}
	public MessageSpeedloaderSync()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		slot = buf.readByte();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeByte(slot);
	}

	public static class Handler implements IMessageHandler<MessageSpeedloaderSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageSpeedloaderSync message, MessageContext ctx)
		{
			if (FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT) {
				Minecraft.getMinecraft().thePlayer.inventory.setInventorySlotContents(message.slot, new ItemStack(IEContent.itemRevolver, 1, 1));
			}
			return null;
		}
	}
}