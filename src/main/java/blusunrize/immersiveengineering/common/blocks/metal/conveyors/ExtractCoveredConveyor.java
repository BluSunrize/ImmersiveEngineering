/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import com.google.common.collect.Lists;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 19.05.2017
 */
public class ExtractCoveredConveyor extends ExtractConveyor
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "extractcovered");

	public ExtractCoveredConveyor(TileEntity tile)
	{
		super(tile);
	}

	@Override
	protected boolean allowCovers()
	{
		return true;
	}

	@Override
	public List<AxisAlignedBB> getSelectionBoxes()
	{
		return Lists.newArrayList(FULL_BLOCK);
	}
}