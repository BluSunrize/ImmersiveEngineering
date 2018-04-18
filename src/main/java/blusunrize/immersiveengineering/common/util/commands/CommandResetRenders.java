/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;

/**
 * @author BluSunrize - 05.09.2016
 */
public class CommandResetRenders extends CommandBase
{
	@Nonnull
	@Override
	public String getName()
	{
		return "resetrender";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender)
	{
		return "Reset the render caches of Immersive Engineering and its addons";
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args)
	{
		ImmersiveEngineering.proxy.clearRenderCaches();
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}
}
