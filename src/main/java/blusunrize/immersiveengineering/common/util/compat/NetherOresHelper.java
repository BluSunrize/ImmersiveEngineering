package blusunrize.immersiveengineering.common.util.compat;

import java.lang.reflect.Method;

import cpw.mods.fml.common.Loader;


public class NetherOresHelper
{
	static Class c_Ores;
	static Method m_getMaceCount;
	
	public static int getCrushingResult(String ore)
	{
		if(!Loader.isModLoaded("NetherOres"))
			return 4;
		try{
			if(c_Ores==null)
				c_Ores = Class.forName("powercrystals.netherores.ores.Ores");
			if(c_Ores!=null && m_getMaceCount==null)
				m_getMaceCount = c_Ores.getDeclaredMethod("getMaceCount");
			if(c_Ores!=null && m_getMaceCount!=null)
			{
				Enum e = Enum.valueOf(c_Ores, ore);
				if(e!=null)
					return (int)m_getMaceCount.invoke(e);
			}
		}catch(Exception e){}
		return 4;
	}
}