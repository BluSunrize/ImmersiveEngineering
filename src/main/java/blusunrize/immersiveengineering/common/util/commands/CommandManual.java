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
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nonnull;

public class CommandManual extends CommandTreeBase
{
	{
		addSubcommand(new CommandReload());
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "manual";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender)
	{
		return "";
	}

	private class CommandReload extends CommandBase
	{

		@Nonnull
		@Override
		public String getName()
		{
			return "reload";
		}

		@Nonnull
		@Override
		public String getUsage(@Nonnull ICommandSender sender)
		{
			return "reload: Reloads the IE manual";
		}

		@Override
		public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
		{
			ImmersiveEngineering.proxy.reloadManual();
		}
	}
}
