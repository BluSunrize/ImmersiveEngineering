package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
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
	public int getRequiredPermissionLevel()
	{
		return 4;
	}

	@Override
	public List<String> getCommandAliases()
	{
		return Collections.emptyList();
	}

//	/**
//	 * Check if the given ICommandSender has permission to execute this command
//	 */
//	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
//	{
//	}

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
					ArrayList<String> subCommands = sub.getSubCommands(this, server, sender, redArgs);
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
				{
					if(!sender.canCommandSenderUseCommand(com.getPermissionLevel(), this.getCommandName()))
					{
						TextComponentTranslation msg = new TextComponentTranslation("commands.generic.permission");
						msg.getStyle().setColor(TextFormatting.RED);
						sender.addChatMessage(msg);
					}
					else
						com.perform(this, server, sender, args);
				}
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
		public abstract ArrayList<String> getSubCommands(CommandHandler h, MinecraftServer server,  ICommandSender sender, String[] args);
		public abstract int getPermissionLevel();
	}
}