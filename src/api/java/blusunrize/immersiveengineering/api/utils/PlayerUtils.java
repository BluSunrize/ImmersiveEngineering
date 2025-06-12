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
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.scores.PlayerTeam;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
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

	public static boolean isAllied(Entity instance, Entity target)
	{
		if(instance.equals(target)) // You're your own ally
			return true;

		PlayerTeam team = instance.getTeam();
		if(team!=null&&team.isAlliedTo(target.getTeam())) // You're on allied teams
			return true;

		Entity owner;
		if(target instanceof OwnableEntity ownable&&(owner = ownable.getOwner())!=null)
		{
			if(instance.equals(owner)) // You are allied to your tamed entities
				return true;
			// You are allied to your team's tamed entities
			return team!=null&&team.isAlliedTo(owner.getTeam());
		}

		return false;
	}

	public interface ConnectionAccess
	{
		void setClientIsFloating(boolean shouldFloat);

		void setAboveGroundTickCount(int ticks);
	}
}
