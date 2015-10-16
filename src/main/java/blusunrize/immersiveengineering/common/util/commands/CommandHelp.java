package blusunrize.immersiveengineering.common.util.commands;

import java.util.ArrayList;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler.IESubCommand;

public class CommandHelp extends IESubCommand
{
	@Override
	public String getIdent()
	{
		return "help";
	}

	@Override
	public void perform(ICommandSender sender, String[] args)
	{
		if(args.length>1)
		{
			String sub = "";
			for(int i=2;i<args.length;i++)
				sub += "."+args[i];
			for(IESubCommand com : CommandHandler.commands)
			{
				if(com.getIdent().equalsIgnoreCase(args[1]))
				{
					String h = StatCollector.translateToLocal(com.getHelp(sub));
					for(String s : h.split("<br>"))
						sender.addChatMessage(new ChatComponentText(s));
				}
			}
		}
		else
		{
			String h = StatCollector.translateToLocal(getHelp(""));
			for(String s : h.split("<br>"))
				sender.addChatMessage(new ChatComponentText(s));
			String sub = "";
			int i=0;
			for(IESubCommand com : CommandHandler.commands)
				sub += ((i++)>0?", ":"")+com.getIdent();
			sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+"available",sub));
		}
	}

	@Override
	public ArrayList<String> getSubCommands(String[] args)
	{
		return null;
	}
}