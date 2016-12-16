package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommandHandler extends CommandBase
{
	ArrayList<IESubCommand> commands = new ArrayList<>();
	final String name;
	public CommandHandler(boolean client)
	{
		commands.add(new CommandHelp());
		if (client)
		{
			commands.add(new CommandResetRenders());
			name = "cie";
		}
		else
		{
			commands.add(new CommandMineral());
			commands.add(new CommandShaders());
			name = "ie";
		}
	}

	@Override
	public String getCommandName()
	{
		return name;
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		ArrayList<String> list = new ArrayList<String>();
		if(args.length>0)
			for(IESubCommand sub : commands)
			{
				if(args.length==1)
				{
					if(args[0].isEmpty() || sub.getIdent().startsWith(args[0].toLowerCase(Locale.ENGLISH)))
						list.add(sub.getIdent());
				}
				else if(sub.getIdent().equalsIgnoreCase(args[0]))
				{
					String[] redArgs = new String[args.length-1];
					System.arraycopy(args,1, redArgs,0, redArgs.length);
					ArrayList<String> subCommands = sub.getSubCommands(this, server, redArgs);
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
		for(IESubCommand com : commands)
			sub += ((i++)>0?"|":"")+com.getIdent();
		return "/"+name+" <"+sub+">";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
	{
		if(args.length>0)
			for(IESubCommand com : commands)
			{
				if(com.getIdent().equalsIgnoreCase(args[0]))
					com.perform(this, server, sender, args);
			}
		else
		{
			String sub = "";
			int i=0;
			for(IESubCommand com : commands)
				sub += ((i++)>0?", ":"")+com.getIdent();
			sender.addChatMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+"available",sub));
		}
	}

	public abstract static class IESubCommand
	{
		public abstract String getIdent();
		public abstract void perform(CommandHandler h, MinecraftServer server, ICommandSender sender, String[] args);
		public String getHelp(String subIdent)
		{
			return Lib.CHAT_COMMAND+getIdent()+subIdent+".help";
		}
		public abstract ArrayList<String> getSubCommands(CommandHandler h, MinecraftServer server, String[] args);
	}
}