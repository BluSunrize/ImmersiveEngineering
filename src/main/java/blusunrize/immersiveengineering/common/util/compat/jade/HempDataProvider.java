/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.compat.jade;

import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class HempDataProvider implements IComponentProvider
{
	@Override
	public void appendBody(List<Component> tooltip, IDataAccessor accessor, IPluginConfig config)
	{
		BlockState blockState = accessor.getBlockState();
		EnumHempGrowth growth = blockState.getValue(HempBlock.GROWTH);
		EnumHempGrowth min = growth.getMin();
		EnumHempGrowth max = growth.getMax();
		float relativeGrowth;
		if(min==max)
			relativeGrowth = 1;
		else
			relativeGrowth = ((growth.ordinal()-min.ordinal())/(float)(max.ordinal()-min.ordinal()));
		Component growthText;
		if(relativeGrowth < 1)
			growthText = new TextComponent((int)(100*relativeGrowth)+"%");
		else
			growthText = new TranslatableComponent("tooltip.waila.crop_mature");
		tooltip.add(new TranslatableComponent("tooltip.waila.crop_growth").append(growthText));
	}
}
