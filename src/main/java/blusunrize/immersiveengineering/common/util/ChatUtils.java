/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.network.MessageNoSpamChat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ChatUtils
{
	public static final MessageSignature NO_SPAM_SIGNATURE = new MessageSignature(
			Arrays.copyOf((Lib.MODID+"nospam").getBytes(StandardCharsets.UTF_8), MessageSignature.BYTES)
	);

	public static void sendServerNoSpamMessages(Player player, Component message)
	{
		if(!(player instanceof ServerPlayer serverPlayer))
			return;
		ImmersiveEngineering.packetHandler.send(
				PacketDistributor.PLAYER.with(() -> serverPlayer), new MessageNoSpamChat(message)
		);
	}
}
