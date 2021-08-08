/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class MetalBarrelBlockEntity extends WoodenBarrelBlockEntity
{
	public MetalBarrelBlockEntity(BlockEntityType<? extends WoodenBarrelBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	public MetalBarrelBlockEntity(BlockPos pos, BlockState state)
	{
		this(IEBlockEntities.METAL_BARREL.get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		if(!isRSPowered())
			super.tickServer();
	}

	@Override
	public boolean isFluidValid(FluidStack fluid)
	{
		return !fluid.isEmpty();
	}
}
