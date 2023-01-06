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

public class RefineryMultiblock extends IETemplateMultiblock
{
	public RefineryMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/refinery"),
				new BlockPos(2, 1, 2), new BlockPos(2, 1, 2), new BlockPos(5, 3, 3),
				IEMultiblockLogic.REFINERY.block());
	}

	@Override
	public float getManualScale()
	{
		return 13;
	}
}
