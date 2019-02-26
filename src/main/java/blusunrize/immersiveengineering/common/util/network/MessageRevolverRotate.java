package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.common.items.ItemRevolver;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageRevolverRotate implements IMessage
{
    boolean forward;

    public MessageRevolverRotate(boolean forward)
    {
        this.forward = forward;
    }

    public MessageRevolverRotate()
    {
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.forward = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeBoolean(this.forward);
    }

    public static class Handler implements IMessageHandler<MessageRevolverRotate, IMessage>
    {

        @Override
        public IMessage onMessage(MessageRevolverRotate message, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack equipped = player.getHeldItemMainhand();
                if(equipped.getItem() instanceof ItemRevolver)
                    ((ItemRevolver)equipped.getItem()).rotateCylinder(equipped, player, message.forward);
            });
            return null;
        }
    }
}
