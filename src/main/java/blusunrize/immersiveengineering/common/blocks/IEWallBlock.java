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
import net.minecraft.world.level.block.WallBlock;

import java.util.function.Supplier;

public class IEWallBlock extends WallBlock implements IIEBlock
{
	private final Supplier<? extends IIEBlock> base;

	public <T extends Block & IIEBlock> IEWallBlock(Properties properties, Supplier<T> base)
	{
		super(properties);
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
