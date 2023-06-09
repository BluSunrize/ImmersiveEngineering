/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

public class CushionBlock extends IEBaseBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.WOOL)
			.ignitedByLava()
			.sound(SoundType.WOOL)
			.strength(0.8F);

	public CushionBlock(Properties props)
	{
		super(props);
	}

	@Override
	public void fallOn(Level w, BlockState state, BlockPos pos, Entity entity, float fallStrength)
	{
		entity.fallDistance = 0;
	}
}
