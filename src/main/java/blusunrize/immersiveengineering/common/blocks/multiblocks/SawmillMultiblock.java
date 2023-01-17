/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class SawmillMultiblock extends IETemplateMultiblock
{
	public SawmillMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/sawmill"),
				new BlockPos(2, 1, 1), new BlockPos(2, 0, 2), new BlockPos(5, 3, 3),
				IEMultiblockLogic.SAWMILL);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}