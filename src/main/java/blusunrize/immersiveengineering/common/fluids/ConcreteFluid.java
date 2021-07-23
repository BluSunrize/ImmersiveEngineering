/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.fluids;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.fluids.IEFluids.FluidEntry;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.mixin.accessors.FlowingFluidAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidAttributes.Builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ConcreteFluid extends IEFluid
{
	public ConcreteFluid(IEFluids.FluidEntry entry, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<Builder> buildAttributes)
	{
		super(entry, stillTex, flowingTex, buildAttributes);
	}

	@Override
	protected boolean isRandomlyTicking()
	{
		return true;
	}

	@Override
	public int getTickDelay(LevelReader p_205569_1_)
	{
		return 20;
	}

	boolean hasFlownInTick = false;

	@Override
	public void tick(Level world, BlockPos pos, FluidState state)
	{
		hasFlownInTick = false;
		super.tick(world, pos, state);
		int timer = state.getValue(IEProperties.INT_16);
		int level = getLegacyLevel(state);
		int quantaRemaining = 16-level;
		if(timer >= Math.min(14, quantaRemaining))
		{
			BlockEntry<? extends Block> solidBlock;
			if(level >= 14)
				solidBlock = StoneDecoration.concreteSheet;
			else if(level >= 10)
				solidBlock = StoneDecoration.concreteQuarter;
			else if(level >= 6)
				solidBlock = IEBlocks.toSlab.get(StoneDecoration.concrete.getId());
			else if(level >= 2)
				solidBlock = StoneDecoration.concreteThreeQuarter;
			else
				solidBlock = StoneDecoration.concrete;
			world.setBlockAndUpdate(pos, solidBlock.get().defaultBlockState());
			for(LivingEntity living : world.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos.offset(1, 1, 1))))
				living.addEffect(new MobEffectInstance(IEPotions.concreteFeet.get(), Integer.MAX_VALUE));
		}
		else if(world.getBlockState(pos).getBlock()==entry.getBlock())
		{
			BlockState newState = world.getBlockState(pos).setValue(IEProperties.INT_16, timer+(hasFlownInTick?1: 2));
			world.setBlockAndUpdate(pos, newState);
		}
	}

	public static class Flowing extends ConcreteFluid
	{
		public Flowing(FluidEntry entry, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<Builder> buildAttributes)
		{
			super(entry, stillTex, flowingTex, buildAttributes);
			registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
		}

		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
		{
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}
	}

	@Nonnull
	@Override
	protected FluidState getNewLiquid(LevelReader worldIn, BlockPos pos, @Nonnull BlockState blockStateIn)
	{
		//Based on super version, respects timer/decay
		int maxNeighborLevel = 0;
		int correspondingTimer = 0;

		for(Direction neighborSide : Direction.Plane.HORIZONTAL)
		{
			BlockPos neighborPos = pos.relative(neighborSide);
			BlockState neighborState = worldIn.getBlockState(neighborPos);
			FluidState fluidAtNeighbor = neighborState.getFluidState();
			if(fluidAtNeighbor.getType().isSame(this)
					&&((FlowingFluidAccess) this).callCanPassThroughWall(neighborSide, worldIn, pos, blockStateIn, neighborPos, neighborState)
					&&fluidAtNeighbor.getAmount() > maxNeighborLevel
			)
			{
				correspondingTimer = fluidAtNeighbor.getValue(IEProperties.INT_16);
				maxNeighborLevel = fluidAtNeighbor.getAmount();
			}
		}

		BlockPos abovePos = pos.above();
		BlockState aboveState = worldIn.getBlockState(abovePos);
		FluidState aboveFluid = aboveState.getFluidState();
		FluidState currFluid = blockStateIn.getFluidState();
		if(!aboveFluid.isEmpty()&&aboveFluid.getType().isSame(this)&&((FlowingFluidAccess) this).callCanPassThroughWall(Direction.UP, worldIn, pos, blockStateIn, abovePos, aboveState))
			return this.getFlowingFluidState(8, true, currFluid, Math.max(correspondingTimer, aboveFluid.getValue(IEProperties.INT_16)));
		else
		{
			int newLevel = maxNeighborLevel-this.getDropOff(worldIn);
			if(newLevel <= 0)
				return Fluids.EMPTY.defaultFluidState();
			else
				return this.getFlowingFluidState(newLevel, false, currFluid, correspondingTimer);
		}
	}

	public FluidState getFlowingFluidState(int level, boolean falling, FluidState currentState, int baseDecay)
	{
		FluidState baseState = super.getFlowing(level, falling);
		if(isSame(currentState.getType()))
			baseDecay = Math.max(currentState.getValue(IEProperties.INT_16), baseDecay);
		baseState = baseState.setValue(IEProperties.INT_16, baseDecay);
		return baseState;
	}

	protected void spreadTo(@Nonnull LevelAccessor worldIn, @Nonnull BlockPos pos, BlockState blockStateIn, Direction direction,
							@Nonnull FluidState fluidStateIn)
	{
		if(blockStateIn.getBlock() instanceof LiquidBlockContainer)
			((LiquidBlockContainer)blockStateIn.getBlock()).placeLiquid(worldIn, pos, blockStateIn, fluidStateIn);
		else
		{
			if(!blockStateIn.isAir())
				this.beforeDestroyingBlock(worldIn, pos, blockStateIn);

			worldIn.setBlock(pos, fluidStateIn.createLegacyBlock(), 3);
		}
		hasFlownInTick = true;
	}
}
