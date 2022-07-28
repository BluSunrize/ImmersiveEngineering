/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ChatUtils
{
	public static void sendServerNoSpamMessages(Player player, Component message)
	{
		// TODO currently not no-spam, need to see if no-spam is still feasible with 1.19.1+
		player.sendSystemMessage(message);
	}
}
