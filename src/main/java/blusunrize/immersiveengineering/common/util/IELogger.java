package blusunrize.immersiveengineering.common.util;

import org.apache.logging.log4j.Level;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import cpw.mods.fml.common.FMLLog;

public class IELogger
{
	public static boolean debug = false;
	public static void log(Level logLevel, Object object)
	{
		FMLLog.log(ImmersiveEngineering.MODID, logLevel, String.valueOf(object), new Object[0]);
	}

	public static void error(Object object)
	{
		log(Level.ERROR, object);
	}

	public static void info(Object object)
	{
		log(Level.INFO, object);
	}

	public static void warn(Object object)
	{
		log(Level.WARN, object);
	}

	public static void debug(Object object)
	{
//		if(debug)
//			log(Level.INFO, "[DEBUG:] "+object);
	}
}