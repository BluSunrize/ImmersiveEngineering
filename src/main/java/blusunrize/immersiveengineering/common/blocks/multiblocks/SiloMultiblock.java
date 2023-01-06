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

public class SiloMultiblock extends IETemplateMultiblock
{
	public SiloMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/silo"),
				new BlockPos(1, 0, 1), new BlockPos(1, 1, 2), new BlockPos(3, 7, 3),
				IEMultiblockLogic.SILO.block());
	}

	@Override
	public float getManualScale()
	{
		return 10;
	}
}