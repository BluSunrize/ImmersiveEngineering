/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class IELogger
{
	public static boolean debug = false;
	public static Logger logger;

	public static void log(Level logLevel, Object object)
	{
		logger.log(logLevel, String.valueOf(object));
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

	public static void error(String message, Object... params)
	{
		logger.log(Level.ERROR, message, params);
	}

	public static void info(String message, Object... params)
	{
		logger.log(Level.INFO, message, params);
	}

	public static void warn(String message, Object... params)
	{
		logger.log(Level.WARN, message, params);
	}

	public static void debug(Object object)
	{
//		if(debug)
//			log(Level.INFO, "[DEBUG:] "+object);
	}
}