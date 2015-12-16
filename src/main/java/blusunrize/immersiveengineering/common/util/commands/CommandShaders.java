package blusunrize.immersiveengineering.common.util.commands;

import java.util.ArrayList;

import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler.IESubCommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

public class CommandShaders extends IESubCommand
{
	@Override
	public String getIdent()
	{
		return "shaders";
	}

	@Override
	public void perform(ICommandSender sender, String[] args)
	{
		if(args.length>1)
		{
			switch(args[1])
			{
			case "clear":
				String player = args.length>2?args[2].trim():sender.getCommandSenderName();
				if(ShaderRegistry.receivedShaders.containsKey(player))
					ShaderRegistry.receivedShaders.get(player).clear();
				ShaderRegistry.recalculatePlayerTotalWeight(player);
				sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".clear.sucess",player));
				break;
			default:
				sender.addChatMessage(new ChatComponentTranslation(getHelp("")));
				break;
			}
		}
		else
			sender.addChatMessage(new ChatComponentTranslation(getHelp("")));

	}

	@Override
	public ArrayList<String> getSubCommands(String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		if(args.length>1)
		{
			switch (args[0])
			{
				case "clear":
					if(args.length>2)
						break;
					list.addAll(CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()));
					break;
			}
			return list;
		}
		
		for(String s : new String[]{"clear"})
		{
			if(args.length==0)
				list.add(s);
			else if(s.toLowerCase().startsWith(args[0].toLowerCase()))
				list.add(s);
		}
		return list;
	}
}