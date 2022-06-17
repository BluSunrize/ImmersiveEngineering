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
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class HempDataProvider implements IBlockComponentProvider
{
	public static final ResourceLocation ID = ImmersiveEngineering.rl("hemp");

	@Override
	public void appendTooltip(ITooltip iTooltip, BlockAccessor accessor, IPluginConfig iPluginConfig)
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
			growthText = Component.literal((int)(100*relativeGrowth)+"%");
		else
			growthText = Component.translatable("tooltip.waila.crop_mature");
		iTooltip.append(Component.translatable("tooltip.waila.crop_growth").append(growthText));
	}

	@Override
	public ResourceLocation getUid()
	{
		return ID;
	}
}
