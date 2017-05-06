package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler.IESubCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.Locale;

public class CommandMineral extends IESubCommand
{
	@Override
	public String getIdent()
	{
		return "mineral";
	}

	@Override
	public void perform(CommandHandler handler, MinecraftServer server, ICommandSender sender, String[] args)
	{
		if(args.length>1)
		{
			DimensionChunkCoords coords = new DimensionChunkCoords(sender.getEntityWorld().provider.getDimension(), (sender.getPosition().getX()>>4), (sender.getPosition().getZ()>>4));
			switch(args[1])
			{
			case "list":
				String s = "";
				int i=0;
				for(MineralMix mm : ExcavatorHandler.mineralList.keySet())
					s += ((i++)>0?", ":"")+mm.name;
				sender.sendMessage(new TextComponentString(s));
				break;
			case "get":
				MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getEntityWorld(), coords.chunkXPos, coords.chunkZPos);
				sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".get", TextFormatting.GOLD+(info.mineral!=null?info.mineral.name:"null")+ TextFormatting.RESET, TextFormatting.GOLD+(info.mineralOverride!=null?info.mineralOverride.name:"null")+ TextFormatting.RESET, TextFormatting.GOLD+(""+info.depletion)+ TextFormatting.RESET));
				break;
			case "set":
				info = ExcavatorHandler.getMineralWorldInfo(sender.getEntityWorld(),coords.chunkXPos,coords.chunkZPos);
				if(args.length<3)
				{
					sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".set.clear",info.mineralOverride!=null?info.mineralOverride.name:"null"));
					info.mineralOverride=null;
					return;
				}

				MineralMix mineral = null;
				for(MineralMix mm : ExcavatorHandler.mineralList.keySet())
					if(mm.name.equalsIgnoreCase(args[2]))
						mineral=mm;
				if(mineral==null)
				{
					sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".set.invalidMineral",args[2]));
					return;
				}
				info.mineralOverride = mineral;
				sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".set.sucess",mineral.name));
				IESaveData.setDirty(sender.getEntityWorld().provider.getDimension());
				break;
			case "setDepletion":
				info = ExcavatorHandler.getMineralWorldInfo(sender.getEntityWorld(),coords.chunkXPos,coords.chunkZPos);
				if(args.length<3)
				{
					String h = I18n.translateToLocal(getHelp(".setDepletion"));
					for(String str : h.split("<br>"))
						sender.sendMessage(new TextComponentString(str));
					return;
				}
				int depl = 0;
				try{
					depl = Integer.parseInt(args[2].trim());
				}catch(Exception e)
				{
					sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".setDepletion.NFE",args[2].trim()));
					return;
				}
				info.depletion = depl;
				sender.sendMessage(new TextComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".setDepletion.sucess",(depl<0? I18n.translateToLocal(Lib.CHAT_INFO+"coreDrill.infinite"):Integer.toString(depl))));
				IESaveData.setDirty(sender.getEntityWorld().provider.getDimension());
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
	public ArrayList<String> getSubCommands(CommandHandler h, MinecraftServer server, ICommandSender sender, String[]args)
	{
		ArrayList<String> list = new ArrayList<String>();
		// subcommand argument autocomplete
		if(args.length>1)
		{
			switch (args[0])
			{
				case "set":
					if(args.length>2)
						break;
					for(MineralMix mineralMix : ExcavatorHandler.mineralList.keySet())
						if(args[1].isEmpty()||mineralMix.name.toLowerCase(Locale.ENGLISH).startsWith(args[1].toLowerCase(Locale.ENGLISH)))
							list.add(mineralMix.name);
					break;
			}
			return list;
		}

		for(String s : new String[]{"list","get","set","setDepletion"})
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