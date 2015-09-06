package blusunrize.immersiveengineering.common.util.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
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
				MineralMix min = ExcavatorHandler.getRandomMineral(sender.getEntityWorld(), coords.chunkXPos, coords.chunkZPos);
				sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".get",(min!=null?min.name:"null")));
				break;
			case "set":
				if(args.length<3)
				{
					MineralMix mineral = ExcavatorHandler.mineralOverrides.get(coords);
					ExcavatorHandler.mineralOverrides.remove(coords);
					sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".set.clear",mineral!=null?mineral.name:"null"));
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
				ExcavatorHandler.mineralOverrides.put(coords,mineral);
				sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".set.sucess",mineral.name));
				break;
			case "setDepletion":
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
				ExcavatorHandler.mineralDepletion.put(coords, depl);
				sender.addChatMessage(new ChatComponentTranslation(Lib.CHAT_COMMAND+getIdent()+".setDepletion.sucess",(depl<0?StatCollector.translateToLocal(Lib.CHAT_INFO+"coreDrill.infinite"):Integer.toString(depl))));
				break;
			}
		}
		else
			sender.addChatMessage(new ChatComponentTranslation(getHelp("")));

	}
}