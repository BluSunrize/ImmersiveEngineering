package blusunrize.immersiveengineering.common.util.commands;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandHandler extends CommandBase
{
	static ArrayList<IESubCommand> commands = new ArrayList();
	static
	{
		commands.add(new CommandHelp());
		commands.add(new CommandMineral());
		commands.add(new CommandShaders());
	}

	@Override
	public String getCommandName()
	{
		return "ie";
	}

	@Override
	public List getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		ArrayList<String> list = new ArrayList<String>();
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
					ArrayList<String> subCommands = sub.getSubCommands(server, redArgs);
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
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
	{
		if(args.length>0)
			for(IESubCommand com : commands)
			{
				if(com.getIdent().equalsIgnoreCase(args[0]))
					com.perform(server, sender, args);
			}
		else
		{
			String sub = "";
			int i=0;
			for(IESubCommand com : CommandHandler.commands)
				sub += ((i++)>0?", ":"")+com.getIdent();
			sender.addChatMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+"available",sub));
		}
	}

	public abstract static class IESubCommand
	{
		public abstract String getIdent();
		public abstract void perform(MinecraftServer server, ICommandSender sender, String[] args);
		public String getHelp(String subIdent)
		{
			return Lib.CHAT_COMMAND+getIdent()+subIdent+".help";
		}
		public abstract ArrayList<String> getSubCommands(MinecraftServer server, String[] args);
	}
}