/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fluids.FluidStack;

public class MetalBarrelTileEntity extends WoodenBarrelTileEntity
{
	public static TileEntityType<MetalBarrelTileEntity> TYPE;

	public MetalBarrelTileEntity(TileEntityType<? extends WoodenBarrelTileEntity> type)
	{
		super(type);
	}

	public MetalBarrelTileEntity()
	{
		this(TYPE);
	}

	@Override
	public void tick()
	{
		if(!world.isRemote&&!isRSPowered())
			super.tick();
	}

	@Override
	public boolean isFluidValid(FluidStack fluid)
	{
		return fluid!=null&&fluid.getFluid()!=null;
	}
}
