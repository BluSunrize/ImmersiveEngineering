/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.plant;

import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.function.Supplier;

public class HempBlock extends CropBlock implements BonemealableBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.PLANT)
			.pushReaction(PushReaction.DESTROY)
			.sound(SoundType.CROP)
			.noCollission()
			.instabreak()
			.randomTicks();

	public final static IntegerProperty AGE = BlockStateProperties.AGE_4;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;


	public HempBlock(Properties props)
	{
		super(props);
		this.registerDefaultState(this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(AGE).add(HALF);
	}

	@Override
	protected IntegerProperty getAgeProperty()
	{
		return AGE;
	}

	@Override
	public int getMaxAge()
	{
		return 4;
	}

	@Override
	protected ItemLike getBaseSeedId()
	{
		return Misc.HEMP_SEEDS.asItem();
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
	{
		boolean b = super.canSurvive(state, world, pos);
		if(state.getBlock().equals(this)&&state.getValue(HALF)==DoubleBlockHalf.UPPER)
		{
			BlockState stateBelow = world.getBlockState(pos.below());
			b = stateBelow.getBlock().equals(this)&&this.getAge(stateBelow)==this.getMaxAge();
		}
		return b;
	}

	private static final VoxelShape[] shapesByAge = {
			Shapes.create(new AABB(0, 0, 0, 1, .375f, 1)),
			Shapes.create(new AABB(0, 0, 0, 1, .625f, 1)),
			Shapes.create(new AABB(0, 0, 0, 1, .875f, 1)),
			Shapes.block(),
			Shapes.block()
	};

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		if(state.getValue(HALF)==DoubleBlockHalf.UPPER)
			return Shapes.block();
		return shapesByAge[this.getAge(state)];
	}

	private boolean canGrowTop(LevelReader world, BlockPos pos, BlockState state)
	{
		if(state.getValue(HALF)==DoubleBlockHalf.UPPER)
			return false;
		if(!world.isEmptyBlock(pos.above()))
			return false;
		return !world.getBlockState(pos.above()).getBlock().equals(this);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state)
	{
		return state.getValue(HALF)==DoubleBlockHalf.LOWER;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand)
	{
		if(!world.isAreaLoaded(pos, 1))
			return; // Forge: prevent loading unloaded chunks when checking neighbor's light
		if(world.getRawBrightness(pos, 0) >= 9)
		{
			int i = this.getAge(state);
			boolean notMaxAge = i < this.getMaxAge();
			boolean canGrowTop = this.canGrowTop(world, pos, state);

			if(notMaxAge||canGrowTop)
			{
				float f = getGrowthSpeed(state, world, pos);
				if(CommonHooks.canCropGrow(world, pos, state, rand.nextInt((int)(25.0F/f)+1)==0))
				{
					if(notMaxAge)
					{
						world.setBlock(pos, this.getStateForAge(i+1), 2);
						CommonHooks.fireCropGrowPost(world, pos, state);
					}
					else if(canGrowTop)
					{
						BlockPos above = pos.above();
						BlockState aboveState = this.getStateForAge(getMaxAge()).setValue(HALF, DoubleBlockHalf.UPPER);
						world.setBlockAndUpdate(above, aboveState);
						CommonHooks.fireCropGrowPost(world, above, aboveState);
					}
				}
			}
		}

	}

	@Override
	public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state)
	{
		return (!this.isMaxAge(state)&&state.getValue(HALF)==DoubleBlockHalf.LOWER)||canGrowTop(world, pos, state);
	}

	@Override
	public void growCrops(Level world, BlockPos pos, BlockState state)
	{
		int newAge = this.getAge(state)+this.getBonemealAgeIncrease(world);
		boolean growTop = false;
		if(newAge > getMaxAge())
		{
			newAge = getMaxAge();
			growTop = canGrowTop(world, pos, state);
		}

		world.setBlock(pos, this.getStateForAge(newAge), 2);
		if(growTop)
			world.setBlockAndUpdate(pos.above(), this.getStateForAge(getMaxAge()).setValue(HALF, DoubleBlockHalf.UPPER));
	}
}