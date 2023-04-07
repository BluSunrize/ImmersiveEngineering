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
import net.minecraft.ChatFormatting;
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
		if(relativeGrowth < 1)
			iTooltip.add(Component.translatable("tooltip.jade.crop_growth", String.format("%.0f%%", relativeGrowth*100)));
		else
			iTooltip.add(Component.translatable("tooltip.jade.crop_growth", Component.translatable("tooltip.jade.crop_mature").withStyle(ChatFormatting.GREEN)));
	}

	@Override
	public ResourceLocation getUid()
	{
		return ID;
	}
}
