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
	public void perform(MinecraftServer server, ICommandSender sender, String[] args)
	{
		ImmersiveEngineering.proxy.clearRenderCaches();
	}

	@Override
	public ArrayList<String> getSubCommands(MinecraftServer server, String[] args)
	{
		return null;
	}
}
