/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.compat.jade;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.impl.ui.ItemStackElement;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class MultiblockIconProvider implements IComponentProvider
{
	@Override
	public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig)
	{
	}

	@Nullable
	@Override
	public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon)
	{
		return ItemStackElement.of(new ItemStack(accessor.getBlock()));
	}
}
