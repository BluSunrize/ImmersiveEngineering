/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.network.MessageNoSpamChatComponents;
import blusunrize.immersiveengineering.mixin.accessors.client.NewChatGuiAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

public class ChatUtils
{
	private static final int DELETION_ID = 3718126;
	private static int lastAdded;

	@OnlyIn(Dist.CLIENT)//Credit goes to WayOfFlowingTime
	public static void sendClientNoSpamMessages(Component[] messages)
	{
		ChatComponent chat = Minecraft.getInstance().gui.getChat();
		NewChatGuiAccess chatAccess = (NewChatGuiAccess)chat;
		for(int i = DELETION_ID+messages.length-1; i <= lastAdded; i++)
			chatAccess.callRemoveById(i);
		for(int i = 0; i < messages.length; i++)
			chatAccess.callAddMessage(messages[i], DELETION_ID+i);
		lastAdded = DELETION_ID+messages.length-1;
	}

	public static void sendServerNoSpamMessages(Player player, Component... messages)
	{
		if(messages.length > 0&&player instanceof ServerPlayer)
			ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player),
					new MessageNoSpamChatComponents(messages));
	}
}
