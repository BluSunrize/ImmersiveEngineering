package blusunrize.immersiveengineering.common.util.network;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class MessageMineralListSync implements IMessage
{
	HashMap<MineralMix,Integer> map = new HashMap<MineralMix,Integer>();
	public MessageMineralListSync()
	{
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
			for(Map.Entry<MineralMix,Integer> e: ExcavatorHandler.mineralList.entrySet())
				if(e.getKey()!=null && e.getValue()!=null)
					map.put(e.getKey(), e.getValue());
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		int size = buf.readInt();
		for(int i=0; i<size; i++)
		{
			NBTTagCompound tag = ByteBufUtils.readTag(buf);
			MineralMix mix = MineralMix.readFromNBT(tag);
			if(mix!=null)
				map.put(mix, tag.getInteger("weight"));
		}		

	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(map.size());
		for(Map.Entry<MineralMix,Integer> e: map.entrySet())
		{
			NBTTagCompound tag = e.getKey().writeToNBT();
			tag.setInteger("weight", e.getValue());
			ByteBufUtils.writeTag(buf,tag); 
		}
	}

	public static class Handler implements IMessageHandler<MessageMineralListSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageMineralListSync message, MessageContext ctx)
		{
			ExcavatorHandler.mineralList.clear();
			for(MineralMix min : message.map.keySet())
				ExcavatorHandler.mineralList.put(min, message.map.get(min));
			ExcavatorHandler.handleMineralManual();
			return null;
		}
	}
}