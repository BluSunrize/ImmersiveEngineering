/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.client.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MessageMineralListSync implements IMessage
{
	HashMap<MineralMix, Integer> map = new HashMap<MineralMix, Integer>();

	public MessageMineralListSync(HashMap<MineralMix, Integer> map)
	{
		this.map = map;
	}

	public MessageMineralListSync(PacketBuffer buf)
	{
		int size = buf.readInt();
		for(int i = 0; i < size; i++)
		{
			CompoundNBT tag = buf.readCompoundTag();
			assert tag!=null;
			MineralMix mix = MineralMix.readFromNBT(tag);
			if(mix!=null)
				map.put(mix, tag.getInt("weight"));
		}
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(map.size());
		for(Map.Entry<MineralMix, Integer> e : map.entrySet())
		{
			CompoundNBT tag = e.getKey().writeToNBT();
			tag.setInt("weight", e.getValue());
			buf.writeCompoundTag(tag);
		}
	}

	@Override
	public void process(Supplier<Context> context)
	{
		Minecraft.getInstance().addScheduledTask(this::onMessageMain);
	}

	private void onMessageMain()
	{
		ExcavatorHandler.mineralList.clear();
		for(MineralMix min : map.keySet())
			ExcavatorHandler.mineralList.put(min, map.get(min));
		ClientProxy.handleMineralManual();
	}
}