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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

import java.util.function.Function;

public class BlockIESlab<T extends Block & IIEBlock> extends SlabBlock implements IIEBlock
{
	private final T base;

	public BlockIESlab(String name, Properties props, Function<Block, Item> itemBlock, T base)
	{
		super(props);
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
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity)
	{
		double relativeEntityPosition = entity.getPosition().getY()-pos.getY();
		switch(state.get(SlabBlock.TYPE))
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
	public BlockRenderLayer getRenderLayer()
	{
		return base.getRenderLayer();
	}

	@Override
	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer)
	{
		return base.canRenderInLayer(base.getDefaultState(), layer);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return Math.min(base.getOpacity(state, worldIn, pos), super.getOpacity(state, worldIn, pos));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
	{
		return super.propagatesSkylightDown(state, reader, pos)||base.propagatesSkylightDown(state, reader, pos);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return super.causesSuffocation(state, worldIn, pos)&&base.causesSuffocation(state, worldIn, pos);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return super.isNormalCube(state, worldIn, pos)&&base.isNormalCube(state, worldIn, pos);
	}
}
