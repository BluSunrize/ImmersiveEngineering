/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEMultiblockBlock;
import blusunrize.immersiveengineering.common.blocks.MultiblockBEType;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;

import java.util.function.Supplier;

public class StoneMultiBlock<T extends MultiblockPartBlockEntity<? super T>> extends IEMultiblockBlock<T>
{
	public static Supplier<Properties> properties(boolean solid)
	{
		return () -> {
			Properties base = Properties.of(Material.STONE)
					.strength(2, 20);
			if(!solid)
				base.noOcclusion();
			return base;
		};
	}

	public StoneMultiBlock(Properties props, MultiblockBEType<T> type)
	{
		super(props, type);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.ACTIVE);
	}
}
