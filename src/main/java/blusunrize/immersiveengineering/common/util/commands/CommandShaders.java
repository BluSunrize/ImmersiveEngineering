/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CommandShaders extends CommandBase
{
	@Nonnull
	@Override
	public String getName()
	{
		return "clearshaders";
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender)
	{
		return "/ie clearshaders [player name]";
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args)
	{
		String player = args.length > 0?args[0].trim(): sender.getName();
		if(ShaderRegistry.receivedShaders.containsKey(player))
			ShaderRegistry.receivedShaders.get(player).clear();
		ShaderRegistry.recalculatePlayerTotalWeight(player);
		sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+"shaders.clear.sucess", player));
	}

	@Nonnull
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
	{
		return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}
}