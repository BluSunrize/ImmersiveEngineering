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

public class BottlingMachineMultiblock extends IETemplateMultiblock
{
	//TODO pump seems to be a bit weird
	public BottlingMachineMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/bottling_machine"),
				new BlockPos(1, 1, 0), new BlockPos(1, 1, 1), new BlockPos(3, 3, 2),
				IEMultiblockLogic.BOTTLING_MACHINE);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}
