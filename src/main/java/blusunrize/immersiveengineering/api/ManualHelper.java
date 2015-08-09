package blusunrize.immersiveengineering.api;

import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualInstance;

/**
 * @author BluSunrize - 04.07.2015
 *
 */
public class ManualHelper
{
	public static String CAT_GENERAL = "general";
	public static String CAT_ENERGY = "energy";
	public static String CAT_MACHINES = "machines";
	
	public static ManualInstance ieManualInstance;

	public static ManualInstance getManual()
	{
		return ieManualInstance;
	}
	/**
	 * Adds a new entry to the manual. if the Category is new, it will be added to the list of categories automatically 
	 */
	public static void addEntry(String name, String category, IManualPage... pages)
	{
		ieManualInstance.addEntry(name, category, pages);
	}
}
