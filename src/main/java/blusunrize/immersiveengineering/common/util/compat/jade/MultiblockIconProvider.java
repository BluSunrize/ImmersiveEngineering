/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.compat.jade;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import javax.annotation.Nullable;

public class MultiblockIconProvider implements IBlockComponentProvider
{
	public static final ResourceLocation ID = ImmersiveEngineering.rl("multiblock_icon");

	@Override
	public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig)
	{
	}

	@Nullable
	@Override
	public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon)
	{
		return IElementHelper.get().item(new ItemStack(accessor.getBlock()));
	}

	@Override
	public ResourceLocation getUid()
	{
		return ID;
	}
}
