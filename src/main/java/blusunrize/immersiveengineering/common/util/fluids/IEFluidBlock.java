/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fluids;

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
import net.minecraft.world.level.material.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class IEFluidBlock extends LiquidBlock
{
	private final IEFluid ieFluid;
	@Nullable
	private MobEffect effect;
	private int duration;
	private int level;
	private static IEFluid tempFluid;

	private static Supplier<IEFluid> supply(IEFluid fluid)
	{
		tempFluid = fluid;
		return () -> fluid;
	}

	public IEFluidBlock(IEFluid ieFluid)
	{
		super(supply(ieFluid), Properties.of(Material.WATER));
		this.ieFluid = ieFluid;
	}

	@Override
	protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		IEFluid f;
		if(ieFluid!=null)
			f = ieFluid;
		else
			f = tempFluid;
		builder.add(f.getStateDefinition().getProperties().toArray(new Property[0]));
	}

	@Nonnull
	@Override
	public FluidState getFluidState(@Nonnull BlockState state)
	{
		FluidState baseState = super.getFluidState(state);
		for(Property<?> prop : ieFluid.getStateDefinition().getProperties())
			if(prop!=LiquidBlock.LEVEL)
				baseState = withCopiedValue(prop, baseState, state);
		return baseState;
	}

	private <T extends StateHolder<?, T>, S extends Comparable<S>>
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
