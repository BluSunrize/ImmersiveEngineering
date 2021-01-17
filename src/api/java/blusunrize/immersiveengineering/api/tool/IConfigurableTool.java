/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigBoolean;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigFloat;
import net.minecraft.item.ItemStack;

/**
 * @author BluSunrize - 10.03.2016
 * <p>
 * Similar to upgradeable tools, configurable tools can be put in the workbench to change their options<br>
 */
public interface IConfigurableTool
{
	boolean canConfigure(ItemStack stack);

	/**
	 * Boolean options are checkboxes where checked equals true
	 */
	ToolConfigBoolean[] getBooleanOptions(ItemStack stack);

	/**
	 * Float options are sliders
	 */
	ToolConfigFloat[] getFloatOptions(ItemStack stack);

	/**
	 * Apply and store the config option on the given stack
	 */
	void applyConfigOption(ItemStack stack, String key, Object value);

	/**
	 * @return a TRANSLATED name for the config option. Try to keep this short.
	 */
	String fomatConfigName(ItemStack stack, ToolConfig config);

	/**
	 * @return a TRANSLATED name for the config option, displayed when hovering over it
	 */
	String fomatConfigDescription(ItemStack stack, ToolConfig config);


	abstract class ToolConfig
	{
		public int x;
		public int y;
		public String name;

		protected ToolConfig(String name, int x, int y)
		{
			this.name = name;
			this.x = x;
			this.y = y;
		}

		public static class ToolConfigBoolean extends ToolConfig
		{
			public boolean value;

			public ToolConfigBoolean(String name, int x, int y, boolean value)
			{
				super(name, x, y);
				this.value = value;
			}
		}

		public static class ToolConfigFloat extends ToolConfig
		{
			public float value;

			public ToolConfigFloat(String name, int x, int y, float value)
			{
				super(name, x, y);
				this.value = value;
			}
		}
	}
}