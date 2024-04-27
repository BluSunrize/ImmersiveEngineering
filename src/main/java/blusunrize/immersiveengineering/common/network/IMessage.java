/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.IEApi;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface IMessage extends CustomPacketPayload
{
	void process(IPayloadContext context);

	static ServerPlayer serverPlayer(IPayloadContext ctx)
	{
		return (ServerPlayer)ctx.player();
	}

	static <T extends CustomPacketPayload> Type<T> createType(String path)
	{
		return new Type<>(IEApi.ieLoc(path));
	}
}
