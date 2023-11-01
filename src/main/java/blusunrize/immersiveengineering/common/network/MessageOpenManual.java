/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent.Context;

public class MessageOpenManual implements IMessage
{
	public MessageOpenManual()
	{
	}

	public MessageOpenManual(FriendlyByteBuf buf)
	{
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
	}

	@Override
	public void process(Context context)
	{
		context.enqueueWork(ImmersiveEngineering.proxy::openManual);
	}
}
