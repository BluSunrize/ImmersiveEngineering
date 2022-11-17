/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class AlloySmelterMultiblock extends StoneMultiblock
{
	public AlloySmelterMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/alloy_smelter"),
				new BlockPos(0, 0, 1), new BlockPos(1, 1, 1), new BlockPos(2, 2, 2),
				IEMultiblockLogic.ALLOY_SMELTER.block());
	}

	@Override
	public float getManualScale()
	{
		return 20;
	}

	@Override
	public Direction transformDirection(Direction original)
	{
		return original;
	}

	@Override
	public Direction untransformDirection(Direction transformed)
	{
		return transformed;
	}

	@Override
	public BlockPos multiblockToModelPos(BlockPos posInMultiblock)
	{
		return posInMultiblock;
	}
}