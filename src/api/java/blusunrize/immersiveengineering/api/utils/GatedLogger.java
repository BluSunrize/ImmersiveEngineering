/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import org.apache.logging.log4j.Logger;

public class GatedLogger
{
	private final Logger baseLogger;
	private boolean enabled;

	public GatedLogger(Logger baseLogger, boolean enabled)
	{
		this.baseLogger = baseLogger;
		this.enabled = enabled;
	}

	public void setEnabled(boolean enable)
	{
		this.enabled = enable;
	}

	//GATED

	public void debug(String s)
	{
		if(enabled)
			baseLogger.debug(s);
	}

	public void debug(String s, Object o)
	{
		if(enabled)
			baseLogger.debug(s, o);
	}

	public void debug(String s, Object o1, Object o2)
	{
		if(enabled)
			baseLogger.debug(s, o1, o2);
	}

	public void debug(String s, Object o1, Object o2, Object o3)
	{
		if(enabled)
			baseLogger.debug(s, o1, o2, o3);
	}

	public void debug(String s, Object o1, Object o2, Object o3, Object o4)
	{
		if(enabled)
			baseLogger.debug(s, o1, o2, o3, o4);
	}

	public void debug(String s, Object... args)
	{
		if(enabled)
			baseLogger.debug(s, args);
	}

	public void info(String s)
	{
		if(enabled)
			baseLogger.info(s);
	}

	public void info(String s, Object o)
	{
		if(enabled)
			baseLogger.info(s, o);
	}

	public void info(String s, Object o1, Object o2)
	{
		if(enabled)
			baseLogger.info(s, o1, o2);
	}

	public void info(String s, Object o1, Object o2, Object o3)
	{
		if(enabled)
			baseLogger.info(s, o1, o2, o3);
	}

	public void info(String s, Object o1, Object o2, Object o3, Object o4)
	{
		if(enabled)
			baseLogger.info(s, o1, o2, o3, o4);
	}

	public void info(String s, Object... args)
	{
		if(enabled)
			baseLogger.info(s, args);
	}

	//NON-GATED

	public void warn(String s)
	{
		baseLogger.warn(s);
	}

	public void warn(String s, Object o)
	{
		baseLogger.warn(s, o);
	}

	public void warn(String s, Object o1, Object o2)
	{
		baseLogger.warn(s, o1, o2);
	}

	public void warn(String s, Object o1, Object o2, Object o3)
	{
		baseLogger.warn(s, o1, o2, o3);
	}

	public void warn(String s, Object o1, Object o2, Object o3, Object o4)
	{
		baseLogger.warn(s, o1, o2, o3, o4);
	}

	public void warn(String s, Object... args)
	{
		baseLogger.warn(s, args);
	}

	public void error(String s)
	{
		baseLogger.error(s);
	}

	public void error(String s, Object o1)
	{
		baseLogger.error(s, o1);
	}

	public void error(String s, Object o1, Object o2)
	{
		baseLogger.error(s, o1, o2);
	}

	public void error(String s, Object o1, Object o2, Object o3)
	{
		baseLogger.error(s, o1, o2, o3);
	}

	public void error(String s, Object o1, Object o2, Object o3, Object o4)
	{
		baseLogger.error(s, o1, o2, o3, o4);
	}

	public void error(String s, Object... args)
	{
		baseLogger.error(s, args);
	}
}
