/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class MessageOpenManual implements IMessage
{
	public static final ResourceLocation ID = IEApi.ieLoc("open_manual");

	public MessageOpenManual()
	{
	}

	public MessageOpenManual(FriendlyByteBuf buf)
	{
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
	}

	@Override
	public void process(PlayPayloadContext context)
	{
		context.workHandler().execute(ImmersiveEngineering.proxy::openManual);
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
