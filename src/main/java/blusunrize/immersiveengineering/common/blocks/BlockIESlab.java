/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public class BlockIESlab<T extends Block & IIEBlock> extends SlabBlock implements IIEBlock
{
	private final T base;

	public BlockIESlab(String name, Properties props, Function<Block, Item> itemBlock, T base)
	{
		super(props
				.isSuffocating(causesSuffocation(base))
				.isRedstoneConductor(isNormalCube(base)));
		ResourceLocation registryName = new ResourceLocation(ImmersiveEngineering.MODID, name);
		setRegistryName(registryName);

		IEContent.registeredIEBlocks.add(this);
		try
		{
			IEContent.registeredIEItems.add(itemBlock.apply(this));
		} catch(Exception e)
		{
			//TODO e.printStackTrace();
			throw new RuntimeException(e);
		}
		this.base = base;
	}

	@Override
	public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity)
	{
		double relativeEntityPosition = entity.position().y()-pos.getY();
		switch(state.getValue(SlabBlock.TYPE))
		{
			case TOP:
				return 0.5 < relativeEntityPosition&&relativeEntityPosition < 1;
			case BOTTOM:
				return 0 < relativeEntityPosition&&relativeEntityPosition < 0.5;
			case DOUBLE:
				return true;
		}
		return false;
	}

	@Override
	public boolean hasFlavour()
	{
		return base.hasFlavour();
	}

	@Override
	public String getNameForFlavour()
	{
		return base.getNameForFlavour();
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos)
	{
		return Math.min(base.getLightBlock(state, worldIn, pos), super.getLightBlock(state, worldIn, pos));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos)
	{
		return super.propagatesSkylightDown(state, reader, pos)||base.propagatesSkylightDown(state, reader, pos);
	}

	public static BlockBehaviour.StatePredicate causesSuffocation(Block base)
	{
		return (state, world, pos) ->
			base.defaultBlockState().isSuffocating(world, pos) && state.getValue(TYPE) == SlabType.DOUBLE;
	}

	public static BlockBehaviour.StatePredicate isNormalCube(Block base)
	{
		return (state, world, pos) ->
				base.defaultBlockState().isRedstoneConductor(world, pos) && state.getValue(TYPE) == SlabType.DOUBLE;
	}
}
