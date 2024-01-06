/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public interface IMessage extends CustomPacketPayload
{
	void process(PlayPayloadContext context);

	default ServerPlayer serverPlayer(PlayPayloadContext ctx)
	{
		return (ServerPlayer)ctx.player().orElseThrow();
	}
}
