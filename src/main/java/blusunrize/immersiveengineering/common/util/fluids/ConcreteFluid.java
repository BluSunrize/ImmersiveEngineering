/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fluids;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes.Builder;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class ConcreteFluid extends IEFluid
{
	public ConcreteFluid()
	{
		this("concrete", new ResourceLocation("immersiveengineering:block/fluid/concrete_still"),
				new ResourceLocation("immersiveengineering:block/fluid/concrete_flow"), createBuilder(2400, 4000),
				true);
	}

	public ConcreteFluid(String fluidName, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<Builder> buildAttributes, boolean isSource)
	{
		super(fluidName, stillTex, flowingTex, buildAttributes, isSource);
	}

	@Override
	protected boolean ticksRandomly()
	{
		return true;
	}

	@Override
	public int getTickRate(IWorldReader p_205569_1_)
	{
		return 20;
	}

	boolean hasFlownInTick = false;

	@Override
	public void tick(World world, BlockPos pos, IFluidState state)
	{
		hasFlownInTick = false;
		super.tick(world, pos, state);
		int timer = state.get(IEProperties.INT_16);
		int level = getLevelFromState(state);
		int quantaRemaining = 16-level;
		if(timer >= Math.min(14, quantaRemaining))
		{
			Block solidBlock;
			if(level >= 14)
				solidBlock = StoneDecoration.concreteSheet;
			else if(level >= 10)
				solidBlock = StoneDecoration.concreteQuarter;
			else if(level >= 6)
				solidBlock = IEBlocks.toSlab.get(StoneDecoration.concrete);
			else if(level >= 2)
				solidBlock = StoneDecoration.concreteThreeQuarter;
			else
				solidBlock = StoneDecoration.concrete;
			world.setBlockState(pos, solidBlock.getDefaultState());
			for(LivingEntity living : world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(pos, pos.add(1, 1, 1))))
				living.addPotionEffect(new EffectInstance(IEPotions.concreteFeet, Integer.MAX_VALUE));
		}
		else if(world.getBlockState(pos).getBlock()==block)
		{
			BlockState newState = world.getBlockState(pos).with(IEProperties.INT_16, timer+(hasFlownInTick?1: 2));
			world.setBlockState(pos, newState);
		}
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Fluid, IFluidState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(IEProperties.INT_16);
	}

	@Override
	protected BlockState getBlockState(IFluidState state)
	{
		return super.getBlockState(state).with(IEProperties.INT_16, state.get(IEProperties.INT_16));
	}

	@Override
	protected ConcreteFluid createFlowingVariant()
	{
		ConcreteFluid ret = new ConcreteFluid(fluidName, stillTex, flowingTex, buildAttributes, false)
		{
			@Override
			protected void fillStateContainer(StateContainer.Builder<Fluid, IFluidState> builder)
			{
				super.fillStateContainer(builder);
				builder.add(LEVEL_1_8);
			}
		};
		ret.source = this;
		ret.bucket = bucket;
		ret.block = block;
		ret.setDefaultState(ret.getStateContainer().getBaseState().with(LEVEL_1_8, 7));
		return ret;
	}

	private static Method doesSideHaveHoles = ObfuscationReflectionHelper.findMethod(FlowingFluid.class, "func_212751_a",
			Direction.class, IBlockReader.class, BlockPos.class, BlockState.class, BlockPos.class, BlockState.class);

	private boolean doesSideHaveHoles(Direction side, IBlockReader world, BlockPos pos, BlockState state, BlockPos pos2, BlockState state2)
	{
		try
		{
			return (boolean)doesSideHaveHoles.invoke(this, side, world, pos, state, pos2, state2);
		} catch(IllegalAccessException|InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Nonnull
	@Override
	protected IFluidState calculateCorrectFlowingState(IWorldReader worldIn, BlockPos pos, @Nonnull BlockState blockStateIn)
	{
		//Based on super version, respects timer/decay
		int maxNeighborLevel = 0;
		int correspondingTimer = 0;

		for(Direction neighborSide : Direction.Plane.HORIZONTAL)
		{
			BlockPos neighborPos = pos.offset(neighborSide);
			BlockState neighborState = worldIn.getBlockState(neighborPos);
			IFluidState fluidAtNeighbor = neighborState.getFluidState();
			if(fluidAtNeighbor.getFluid().isEquivalentTo(this)
					&&this.doesSideHaveHoles(neighborSide, worldIn, pos, blockStateIn, neighborPos, neighborState)
					&&fluidAtNeighbor.getLevel() > maxNeighborLevel
			)
			{
				correspondingTimer = fluidAtNeighbor.get(IEProperties.INT_16);
				maxNeighborLevel = fluidAtNeighbor.getLevel();
			}
		}

		BlockPos abovePos = pos.up();
		BlockState aboveState = worldIn.getBlockState(abovePos);
		IFluidState aboveFluid = aboveState.getFluidState();
		IFluidState currFluid = blockStateIn.getFluidState();
		if(!aboveFluid.isEmpty()&&aboveFluid.getFluid().isEquivalentTo(this)&&this.doesSideHaveHoles(Direction.UP, worldIn, pos, blockStateIn, abovePos, aboveState))
			return this.getFlowingFluidState(8, true, currFluid, Math.max(correspondingTimer, aboveFluid.get(IEProperties.INT_16)));
		else
		{
			int newLevel = maxNeighborLevel-this.getLevelDecreasePerBlock(worldIn);
			if(newLevel <= 0)
				return Fluids.EMPTY.getDefaultState();
			else
				return this.getFlowingFluidState(newLevel, false, currFluid, correspondingTimer);
		}
	}

	public IFluidState getFlowingFluidState(int level, boolean falling, IFluidState currentState, int baseDecay)
	{
		IFluidState baseState = super.getFlowingFluidState(level, falling);
		if(isEquivalentTo(currentState.getFluid()))
			baseDecay = Math.max(currentState.get(IEProperties.INT_16), baseDecay);
		baseState = baseState.with(IEProperties.INT_16, baseDecay);
		return baseState;
	}

	protected void flowInto(@Nonnull IWorld worldIn, @Nonnull BlockPos pos, BlockState blockStateIn, Direction direction,
							@Nonnull IFluidState fluidStateIn)
	{
		if(blockStateIn.getBlock() instanceof ILiquidContainer)
			((ILiquidContainer)blockStateIn.getBlock()).receiveFluid(worldIn, pos, blockStateIn, fluidStateIn);
		else
		{
			if(!blockStateIn.isAir())
				this.beforeReplacingBlock(worldIn, pos, blockStateIn);

			worldIn.setBlockState(pos, fluidStateIn.getBlockState(), 3);
		}
		hasFlownInTick = true;
	}
}
