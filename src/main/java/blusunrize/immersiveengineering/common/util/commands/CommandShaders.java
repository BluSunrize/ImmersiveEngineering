package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler.IESubCommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.Locale;

public class CommandShaders extends IESubCommand
{
	@Override
	public String getIdent()
	{
		return "shaders";
	}

	@Override
	public void perform(CommandHandler h, MinecraftServer server, ICommandSender sender, String[] args)
	{
		if(args.length>1)
		{
			switch(args[1])
			{
			case "clear":
				String player = args.length>2?args[2].trim():sender.getName();
				if(ShaderRegistry.receivedShaders.containsKey(player))
					ShaderRegistry.receivedShaders.get(player).clear();
				ShaderRegistry.recalculatePlayerTotalWeight(player);
				sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".clear.sucess",player));
				break;
			default:
				sender.sendMessage(new TextComponentTranslation(getHelp("")));
				break;
			}
		}
		else
			sender.sendMessage(new TextComponentTranslation(getHelp("")));

	}

	@Override
	public ArrayList<String> getSubCommands(CommandHandler h, MinecraftServer server, ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		if(args.length>1)
		{
			switch (args[0])
			{
				case "clear":
					if(args.length>2)
						break;
					list.addAll(CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()));
					break;
			}
			return list;
		}
		
		for(String s : new String[]{"clear"})
		{
			if(args.length==0)
				list.add(s);
			else if(s.toLowerCase(Locale.ENGLISH).startsWith(args[0].toLowerCase(Locale.ENGLISH)))
				list.add(s);
		}
		return list;
	}

	@Override
	public int getPermissionLevel()
	{
		return 4;
	}
}