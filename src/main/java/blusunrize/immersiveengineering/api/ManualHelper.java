/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.client.manual.IEManualInstance;

/**
 * @author BluSunrize - 04.07.2015
 */
public class ManualHelper
{
	public static String CAT_GENERAL = "general";
	public static String CAT_CONSTRUCTION = "construction";
	public static String CAT_ENERGY = "energy";
	public static String CAT_MACHINES = "machines";
	public static String CAT_TOOLS = "tools";
	public static String CAT_HEAVYMACHINES = "heavymachines";
	public static String CAT_UPDATE = "update";

	public static IEManualInstance ieManualInstance;

	public static IEManualInstance getManual()
	{
		return ieManualInstance;
	}
}
