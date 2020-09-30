/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 17.02.2019
 */
public class DropCoveredConveyor extends DropConveyor
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "droppercovered");

	public DropCoveredConveyor(TileEntity tile)
	{
		super(tile);
	}

	@Override
	public boolean isCovered()
	{
		return true;
	}
}
