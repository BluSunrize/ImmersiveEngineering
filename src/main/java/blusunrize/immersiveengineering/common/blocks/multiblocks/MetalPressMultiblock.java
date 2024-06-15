/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.core.BlockPos;
public class MetalPressMultiblock extends IETemplateMultiblock
{
	public MetalPressMultiblock()
	{
		super(IEApi.ieLoc("multiblocks/metal_press"),
				new BlockPos(1, 1, 0), new BlockPos(1, 1, 0), new BlockPos(3, 3, 1),
				IEMultiblockLogic.METAL_PRESS);
	}

	@Override
	public float getManualScale()
	{
		return 13;
	}
}