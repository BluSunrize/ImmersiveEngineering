/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.compat.jade;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.world.item.ItemStack;

public class MultiblockIconProvider implements IComponentProvider
{
	@Override
	public ItemStack getStack(IDataAccessor accessor, IPluginConfig config)
	{
		return new ItemStack(accessor.getBlock());
	}
}
