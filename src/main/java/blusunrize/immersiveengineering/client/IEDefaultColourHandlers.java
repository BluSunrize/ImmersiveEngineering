/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IColouredBlock;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import javax.annotation.Nullable;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author BluSunrize - 03.10.2016
 */
public class IEDefaultColourHandlers implements ItemColor, BlockColor
{
	public static IEDefaultColourHandlers INSTANCE = new IEDefaultColourHandlers();

	@Override
	public int getColor(BlockState state, @Nullable BlockAndTintGetter worldIn, @Nullable BlockPos pos, int tintIndex)
	{
		if(state.getBlock() instanceof IColouredBlock)
			return ((IColouredBlock)state.getBlock()).getRenderColour(state, worldIn, pos, tintIndex);
		return 0xffffff;
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex)
	{
		if(stack.getItem() instanceof IColouredItem)
			return ((IColouredItem)stack.getItem()).getColourForIEItem(stack, tintIndex);
		return 0xffffff;
	}
}
