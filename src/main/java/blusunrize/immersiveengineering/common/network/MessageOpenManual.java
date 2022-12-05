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
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.function.Supplier;

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
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(ImmersiveEngineering.proxy::openManual);
	}
}
