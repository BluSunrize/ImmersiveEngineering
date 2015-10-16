package blusunrize.immersiveengineering.common.util.commands;

import java.util.ArrayList;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler.IESubCommand;

public class CommandMineral extends IESubCommand
{
	@Override
	public String getIdent()
	{
		return "mineral";
	}

	@Override
	public void perform(ICommandSender sender, String[] args)
	{
		if(args.length>1)
		{
			DimensionChunkCoords coords = new DimensionChunkCoords(sender.getEntityWorld().provider.dimensionId, (sender.getPlayerCoordinates().posX>>4), (sender.getPlayerCoordinates().posZ>>4));
			switch(args[1])
			{
			case "list":
				String s = "";
				int i=0;
				for(MineralMix mm : ExcavatorHandler.mineralList.keySet())
					s += ((i++)>0?", ":"")+mm.name;
				sender.addChatMessage(new ChatComponentText(s));
				break;
			case "get":
				MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(sender.getEntityWorld(), coords.chunkXPos, coords.chunkZPos);
				sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".get",EnumChatFormatting.GOLD+(info.mineral!=null?info.mineral.name:"null")+EnumChatFormatting.RESET,EnumChatFormatting.GOLD+(info.mineralOverride!=null?info.mineralOverride.name:"null")+EnumChatFormatting.RESET,EnumChatFormatting.GOLD+(""+info.depletion)+EnumChatFormatting.RESET));
				break;
			case "set":
				info = ExcavatorHandler.getMineralWorldInfo(sender.getEntityWorld(),coords.chunkXPos,coords.chunkZPos);
				if(args.length<3)
				{
					sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".set.clear",info.mineralOverride!=null?info.mineralOverride.name:"null"));
					info.mineralOverride=null;
					return;
				}

				MineralMix mineral = null;
				for(MineralMix mm : ExcavatorHandler.mineralList.keySet())
					if(mm.name.equalsIgnoreCase(args[2]))
						mineral=mm;
				if(mineral==null)
				{
					sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".set.invalidMineral",args[2]));
					return;
				}
				info.mineralOverride = mineral;
				sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".set.sucess",mineral.name));
				IESaveData.setDirty(sender.getEntityWorld().provider.dimensionId);
				break;
			case "setDepletion":
				info = ExcavatorHandler.getMineralWorldInfo(sender.getEntityWorld(),coords.chunkXPos,coords.chunkZPos);
				if(args.length<3)
				{
					sender.addChatMessage(new ChatComponentTranslation(getHelp(".setDepletion")));
					return;
				}
				int depl = 0;
				try{
					depl = Integer.parseInt(args[2].trim());
				}catch(Exception e)
				{
					sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".setDepletion.NFE",args[2].trim()));
					return;
				}
				info.depletion = depl;
				sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".setDepletion.sucess",(depl<0?StatCollector.translateToLocal(Lib.CHAT_INFO+"coreDrill.infinite"):Integer.toString(depl))));
				IESaveData.setDirty(sender.getEntityWorld().provider.dimensionId);
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
		for(String s : new String[]{"list","get","set","setDepletion"})
		{
			if(args.length==0)
				list.add(s);
			else if(s.startsWith(args[0].toLowerCase()))
				list.add(s);
		}
		return list;
	}
}