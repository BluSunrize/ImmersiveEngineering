package blusunrize.immersiveengineering.common.util.commands;

import java.util.ArrayList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;
import blusunrize.immersiveengineering.common.util.Lib;

public class CommandHandler extends CommandBase
{
	static ArrayList<IESubCommand> commands = new ArrayList();
	static
	{
		commands.add(new CommandHelp());
		commands.add(new CommandMineral());
	}

	@Override
	public String getCommandName()
	{
		return "ie";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		String sub = "";
		int i=0;
		for(IESubCommand com : CommandHandler.commands)
			sub += ((i++)>0?"|":"")+com.getIdent();
		return "/ie <"+sub+">";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		if(args.length>0)
			for(IESubCommand com : commands)
			{
				if(com.getIdent().equalsIgnoreCase(args[0]))
					com.perform(sender, args);
			}
		else
		{
			String sub = "";
			int i=0;
			for(IESubCommand com : CommandHandler.commands)
				sub += ((i++)>0?", ":"")+com.getIdent();
			sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+"available",sub));
		}
	}

	public static abstract class IESubCommand
	{
		public abstract String getIdent();
		public abstract void perform(ICommandSender sender, String[] args);
		public String getHelp(String subIdent)
		{
			return Lib.CHAT_COMMAND+getIdent()+subIdent+".help";
		}
	}
}