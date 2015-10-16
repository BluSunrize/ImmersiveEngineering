package blusunrize.immersiveengineering.client;

import java.util.LinkedHashSet;

import net.minecraft.util.StatCollector;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualInstance;

public class IEManualInstance extends ManualInstance
{
	public IEManualInstance()
	{
		super(ClientUtils.font(), "immersiveengineering:textures/gui/manual.png");
	}

	@Override
	public String formatText(String s)
	{
		s = StatCollector.translateToLocal("ie.manual.entry."+s);
		String splitKey = ";";

		s = s.replaceAll("<br>", "\n");
		int start;
		int overflow=0;
		while( (start=s.indexOf("<config"))>=0 && overflow<50)
		{
			overflow++;
			int end = s.indexOf(">", start);
			String rep = s.substring(start, end+1);
			String[] segment = rep.substring(0,rep.length()-1).split(splitKey);
			if(segment.length<3)
				break;
			String result = "";
			if(segment[1].equalsIgnoreCase("b"))
			{
				if(segment.length>3)
					result = (Config.getBoolean(segment[2])?segment[3]: segment.length>4?segment[4]:"");
				else
					result = ""+Config.getBoolean(segment[2]);
			}
			else if(segment[1].equalsIgnoreCase("i"))
				result = ""+Config.getInt(segment[2]);
			else if(segment[1].equalsIgnoreCase("iA"))
			{
				int[] iA = Config.getIntArray(segment[2]);
				if(segment.length>3)
					try{
						if(segment[3].startsWith("l"))
						{
							int limiter = Integer.parseInt(segment[3].substring(1));
							for(int i=0; i<limiter; i++)
								result += (i>0?", ":"")+iA[i];
						}
						else
						{
							int idx = Integer.parseInt(segment[3]);
							result = ""+iA[idx];
						}
					}catch(Exception ex){
						break;
					}
				else
					for(int i=0; i<iA.length; i++)
						result += (i>0?", ":"")+iA[i];
			}
			else if(segment[1].equalsIgnoreCase("dA"))
			{
				double[] iD = Config.getDoubleArray(segment[2]);
				if(segment.length>3)
					try{
						int idx = Integer.parseInt(segment[3]);
						result = ""+Utils.formatDouble(iD[idx], "#.***");
					}catch(Exception ex){
						break;
					}
				else
					for(int i=0; i<iD.length; i++)
						result += (i>0?", ":"")+Utils.formatDouble(iD[i], "#.***");
			}

			s = s.replaceFirst(rep, result);
		}
		return s;
	}

	@Override
	public String getManualName()
	{
		return StatCollector.translateToLocal("item.ImmersiveEngineering.tool.manual.name");
	}
	@Override
	public void addEntry(String name, String category, IManualPage... pages)
	{
		super.addEntry(name, category, pages);
		if(!categorySet.contains(category))
			categorySet.add(category);
	}
	LinkedHashSet<String> categorySet = new LinkedHashSet<String>();
	@Override
	public String[] getSortedCategoryList()
	{
		return categorySet.toArray(new String[categorySet.size()]);
	}
	@Override
	public String formatCategoryName(String s)
	{
		return StatCollector.translateToLocal("ie.manual.category."+s+".name");
	}
	@Override
	public String formatEntryName(String s)
	{
		return StatCollector.translateToLocal("ie.manual.entry."+s+".name");
	}
	@Override
	public String formatEntrySubtext(String s)
	{
		return StatCollector.translateToLocal("ie.manual.entry."+s+".subtext");
	}
	@Override
	public boolean showEntryInList(ManualEntry entry)
	{
		if(entry.getCategory().equalsIgnoreCase(ManualHelper.CAT_UPDATE))
			return Config.getBoolean("showUpdateNews");
		
		return true;
	}
	@Override
	public boolean showCategoryInList(String category)
	{
		return true;
	}

	@Override
	public int getTitleColour()
	{
		return 0xf78034;
	}
	@Override
	public int getSubTitleColour()
	{
		return 0xf78034;
	}
	@Override
	public int getTextColour()
	{
		return 0x555555;
	}
	@Override
	public int getHighlightColour()
	{
		return 0xd4804a;
	}
	@Override
	public int getPagenumberColour()
	{
		return 0x9c917c;
	}
}