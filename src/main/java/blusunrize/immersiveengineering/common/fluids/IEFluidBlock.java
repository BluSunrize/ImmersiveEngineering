/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.fluids;

import blusunrize.immersiveengineering.common.fluids.IEFluids.FluidEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.StateHolder;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IEFluidBlock extends FlowingFluidBlock
{
	private static FluidEntry entryStatic;
	private final FluidEntry entry;
	@Nullable
	private Effect effect;
	private int duration;
	private int level;

	public IEFluidBlock(IEFluids.FluidEntry entry, Properties props)
	{
		super(entry.getStillGetter(), Util.make(props, $ -> entryStatic = entry));
		this.entry = entry;
		entryStatic = null;
	}

	@Override
	protected void fillStateContainer(@Nonnull Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		for(Property<?> p : (entry==null?entryStatic: entry).getProperties())
			builder.add(p);
	}

	@Nonnull
	@Override
	public FluidState getFluidState(@Nonnull BlockState state)
	{
		FluidState baseState = super.getFluidState(state);
		for(Property<?> prop : getFluid().getStateContainer().getProperties())
			if(prop!=FlowingFluidBlock.LEVEL)
				baseState = withCopiedValue(prop, baseState, state);
		return baseState;
	}

	public static <T extends StateHolder<?, T>, S extends Comparable<S>>
	T withCopiedValue(Property<S> prop, T oldState, StateHolder<?, ?> copyFrom)
	{
		return oldState.with(prop, copyFrom.get(prop));
	}

	public void setEffect(@Nonnull Effect effect, int duration, int level)
	{
		this.effect = effect;
		this.duration = duration;
		this.level = level;
	}

	@Override
	public void onEntityCollision(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn)
	{
		super.onEntityCollision(state, worldIn, pos, entityIn);
		if(effect!=null&&entityIn instanceof LivingEntity)
			((LivingEntity)entityIn).addPotionEffect(new EffectInstance(effect, duration, level));
	}
}
