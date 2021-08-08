/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.common.blocks.generic.GenericEntityBlock;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;

import java.util.function.Supplier;

public class BalloonBlock extends GenericEntityBlock<BalloonBlockEntity>
{
	public static final Supplier<Properties> PROPERTIES = () -> Properties.of(Material.WOOL)
			.sound(SoundType.WOOL)
			.strength(0.8F)
			.lightLevel(s -> 13)
			.noOcclusion();

	public BalloonBlock(Properties props)
	{
		super(IEBlockEntities.BALLOON, props);
		setHasColours();
		setLightOpacity(0);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	public void fallOn(Level w, BlockState state, BlockPos pos, Entity entity, float fallStrength)
	{
		entity.fallDistance = 0;
	}
}
