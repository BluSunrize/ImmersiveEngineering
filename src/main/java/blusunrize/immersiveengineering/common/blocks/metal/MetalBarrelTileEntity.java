/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelTileEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fluids.FluidStack;

public class MetalBarrelTileEntity extends WoodenBarrelTileEntity
{
	public MetalBarrelTileEntity(BlockEntityType<? extends WoodenBarrelTileEntity> type)
	{
		super(type);
	}

	public MetalBarrelTileEntity()
	{
		this(IETileTypes.METAL_BARREL.get());
	}

	@Override
	public void tick()
	{
		if(!level.isClientSide&&!isRSPowered())
			super.tick();
	}

	@Override
	public boolean isFluidValid(FluidStack fluid)
	{
		return fluid!=null&&fluid.getFluid()!=null;
	}
}
