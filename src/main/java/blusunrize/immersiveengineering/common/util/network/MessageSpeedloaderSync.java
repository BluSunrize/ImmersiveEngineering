package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.common.IEContent;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

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
			if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
				Minecraft.getMinecraft().player.inventory.setInventorySlotContents(message.slot, new ItemStack(IEContent.itemRevolver, 1, 1));
			return null;
		}
	}
}