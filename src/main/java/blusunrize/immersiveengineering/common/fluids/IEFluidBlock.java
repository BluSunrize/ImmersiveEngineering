/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.fluids;

import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEFluids.FluidEntry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IEFluidBlock extends LiquidBlock
{
	private static FluidEntry entryStatic;
	private final FluidEntry entry;
	@Nullable
	private MobEffect effect;
	private int duration;
	private int level;

	public IEFluidBlock(IEFluids.FluidEntry entry, Properties props)
	{
		super(entry.getStillGetter(), Util.make(props, $ -> entryStatic = entry));
		this.entry = entry;
		entryStatic = null;
	}

	@Override
	protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		for(Property<?> p : (entry==null?entryStatic: entry).properties())
			builder.add(p);
	}

	@Nonnull
	@Override
	public FluidState getFluidState(@Nonnull BlockState state)
	{
		FluidState baseState = super.getFluidState(state);
		for(Property<?> prop : baseState.getProperties())
			if(state.hasProperty(prop))
				baseState = withCopiedValue(prop, baseState, state);
		return baseState;
	}

	public static <T extends StateHolder<?, T>, S extends Comparable<S>>
	T withCopiedValue(Property<S> prop, T oldState, StateHolder<?, ?> copyFrom)
	{
		return oldState.setValue(prop, copyFrom.getValue(prop));
	}

	public void setEffect(@Nonnull MobEffect effect, int duration, int level)
	{
		this.effect = effect;
		this.duration = duration;
		this.level = level;
	}

	@Override
	public void entityInside(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn)
	{
		super.entityInside(state, worldIn, pos, entityIn);
		if(effect!=null&&entityIn instanceof LivingEntity)
			((LivingEntity)entityIn).addEffect(new MobEffectInstance(effect, duration, level));
	}
}
