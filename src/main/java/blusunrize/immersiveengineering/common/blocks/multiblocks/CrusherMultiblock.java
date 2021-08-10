/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class CrusherMultiblock extends IETemplateMultiblock
{
	public CrusherMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/crusher"),
				new BlockPos(2, 1, 1), new BlockPos(2, 1, 2), new BlockPos(5, 3, 3),
				Multiblocks.crusher);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}