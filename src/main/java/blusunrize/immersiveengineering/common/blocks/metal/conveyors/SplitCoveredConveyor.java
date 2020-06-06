/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.VoxelShape;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

public class SplitCoveredConveyor extends SplitConveyor
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "splittercovered");

	public SplitCoveredConveyor(TileEntity tile)
	{
		super(tile);
	}

	@Override
	protected boolean allowCovers()
	{
		return true;
	}

	@Override
	public VoxelShape getSelectionShape()
	{
		return FULL_BLOCK;
	}
}