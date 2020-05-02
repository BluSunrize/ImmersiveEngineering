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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class MessageMineralListSync implements IMessage
{
	Collection<MineralMix> list = new ArrayList<>();

	public MessageMineralListSync(Collection<MineralMix> list)
	{
		this.list = list;
	}

	public MessageMineralListSync(PacketBuffer buf)
	{
		int size = buf.readInt();
		for(int i = 0; i < size; i++)
		{
			CompoundNBT tag = buf.readCompoundTag();
			assert tag!=null;
			list.add(MineralMix.readFromNBT(tag));
		}
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(list.size());
		for(MineralMix e : list)
			buf.writeCompoundTag(e.writeToNBT());
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(this::onMessageMain);
	}

	private void onMessageMain()
	{
		ExcavatorHandler.mineralList.clear();
		for(MineralMix min : list)
			ExcavatorHandler.mineralList.put(min.getId(), min);
	}
}