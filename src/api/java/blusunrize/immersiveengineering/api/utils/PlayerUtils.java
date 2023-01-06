/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public class PlayerUtils
{
	public static void resetFloatingState(@Nullable Entity player)
	{
		if(player instanceof ServerPlayer)
		{
			ConnectionAccess access = (ConnectionAccess)((ServerPlayer)player).connection;
			access.setClientIsFloating(false);
			access.setAboveGroundTickCount(0);
		}
	}

	public interface ConnectionAccess
	{
		void setClientIsFloating(boolean shouldFloat);

		void setAboveGroundTickCount(int ticks);
	}
}
