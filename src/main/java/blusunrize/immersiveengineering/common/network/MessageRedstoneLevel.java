/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.common.items.VoltmeterItem;
import blusunrize.immersiveengineering.common.items.VoltmeterItem.RemoteRedstoneData;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent.Context;

public record MessageRedstoneLevel(VoltmeterItem.RemoteRedstoneData data) implements IMessage
{

	public MessageRedstoneLevel(FriendlyByteBuf in)
	{
		this(RemoteRedstoneData.read(in));
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		data.write(buf);
	}

	@Override
	public void process(Context context)
	{
		context.enqueueWork(() -> VoltmeterItem.lastRedstoneUpdate = data);
	}
}
