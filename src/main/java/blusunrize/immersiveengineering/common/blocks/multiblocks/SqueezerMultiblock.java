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
import net.minecraft.resources.ResourceLocation;

public class SqueezerMultiblock extends IETemplateMultiblock
{
	public SqueezerMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/squeezer"),
				new BlockPos(1, 1, 1), new BlockPos(1, 1, 1), new BlockPos(3, 3, 3),
				IEMultiblockLogic.SQUEEZER);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}
