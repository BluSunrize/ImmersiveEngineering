package blusunrize.immersiveengineering.api.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;

public class PlayerUtils
{
	public static void resetFloatingState(@Nullable Entity player)
	{
		if(player instanceof ServerPlayerEntity)
		{
			ConnectionAccess access = (ConnectionAccess)((ServerPlayerEntity)player).connection;
			access.setFloating(false);
			access.setFloatingTickCount(0);
		}
	}

	public interface ConnectionAccess
	{
		void setFloating(boolean shouldFloat);

		void setFloatingTickCount(int ticks);
	}
}
