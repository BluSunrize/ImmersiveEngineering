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
		default void setFloating(boolean shouldFloat) {
			setClientIsFloating(shouldFloat);
		}

		default void setFloatingTickCount(int ticks) {
			setAboveGroundTickCount(ticks);
		}

		void setClientIsFloating(boolean shouldFloat);

		void setAboveGroundTickCount(int ticks);
	}
}
