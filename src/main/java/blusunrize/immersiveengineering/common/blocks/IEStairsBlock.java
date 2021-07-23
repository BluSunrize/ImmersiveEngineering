/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;

import java.util.function.Supplier;

public class IEStairsBlock extends StairBlock implements IIEBlock
{
	private final Supplier<? extends IIEBlock> base;

	public <T extends Block & IIEBlock> IEStairsBlock(Properties properties, Supplier<T> base)
	{
		super(() -> base.get().defaultBlockState(), properties);
		this.base = base;
	}

	@Override
	public boolean hasFlavour()
	{
		return base.get().hasFlavour();
	}

	@Override
	public String getNameForFlavour()
	{
		return base.get().getNameForFlavour();
	}
}
