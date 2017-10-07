/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler.IESubCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;

/**
 * @author BluSunrize - 05.09.2016
 */
public class CommandResetRenders extends IESubCommand
{
	@Override
	public String getIdent()
	{
		return "resetrender";
	}

	@Override
	public void perform(CommandHandler h, MinecraftServer server, ICommandSender sender, String[] args)
	{
		ImmersiveEngineering.proxy.clearRenderCaches();
	}

	@Override
	public ArrayList<String> getSubCommands(CommandHandler h, MinecraftServer server, ICommandSender sender, String[] args)
	{
		return null;
	}

	@Override
	public int getPermissionLevel()
	{
		return 0;
	}
}
