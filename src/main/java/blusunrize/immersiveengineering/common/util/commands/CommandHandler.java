package blusunrize.immersiveengineering.common.util.commands;

import java.util.ArrayList;
import java.util.List;

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
	public List addTabCompletionOptions(ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		for(String a : args)
			System.out.println("|"+a+"|");
		if(args.length>0)
			for(IESubCommand sub : commands)
			{
				if(args.length==1)
				{
					if(args[0].isEmpty() || sub.getIdent().startsWith(args[0].toLowerCase()))
						list.add(sub.getIdent());
				}
				else if(sub.getIdent().equalsIgnoreCase(args[0]))
				{
					String[] redArgs = new String[args.length-1];
					System.arraycopy(args,1, redArgs,0, redArgs.length);
					ArrayList<String> subCommands = sub.getSubCommands(redArgs);	
					if(subCommands!=null)
						list.addAll(subCommands);
				}
			}
		return list;
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
		public abstract ArrayList<String> getSubCommands(String[] args);
	}
}