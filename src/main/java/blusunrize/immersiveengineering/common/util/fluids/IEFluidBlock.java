/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.IProperty;
import net.minecraft.state.IStateHolder;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class IEFluidBlock extends FlowingFluidBlock
{
	private final IEFluid ieFluid;
	@Nullable
	private Effect effect;
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
		super(supply(ieFluid), Properties.create(Material.WATER));
		this.ieFluid = ieFluid;
	}

	@Override
	protected void fillStateContainer(@Nonnull Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		IEFluid f;
		if(ieFluid!=null)
			f = ieFluid;
		else
			f = tempFluid;
		builder.add(f.getStateContainer().getProperties().toArray(new IProperty[0]));
	}

	@Nonnull
	@Override
	public IFluidState getFluidState(@Nonnull BlockState state)
	{
		IFluidState baseState = super.getFluidState(state);
		for(IProperty<?> prop : ieFluid.getStateContainer().getProperties())
			if(prop!=FlowingFluidBlock.LEVEL)
				baseState = withCopiedValue(prop, baseState, state);
		return baseState;
	}

	private <T extends IStateHolder<T>, S extends Comparable<S>>
	T withCopiedValue(IProperty<S> prop, T oldState, IStateHolder<?> copyFrom)
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
