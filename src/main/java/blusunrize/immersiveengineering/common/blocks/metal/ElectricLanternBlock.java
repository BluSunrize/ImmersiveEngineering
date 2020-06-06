/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntityType;

import java.util.function.Supplier;

public class ElectricLanternBlock extends MiscConnectorBlock
{

	public ElectricLanternBlock(String name, Supplier<TileEntityType<?>> tileType, IProperty<?>... extraProperties)
	{
		super(name, tileType, extraProperties);
	}

	@Override
	public int getLightValue(BlockState state)
	{
		return state.get(IEProperties.ACTIVE)?15: 0;
	}
}
