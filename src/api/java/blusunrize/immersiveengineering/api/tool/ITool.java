/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.item.ItemStack;

/**
 * Items that implement this will be allowed in the toolbox
 */
public interface ITool
{
	boolean isTool(ItemStack item);
}
