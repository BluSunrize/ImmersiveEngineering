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

public class AlloySmelterMultiblock extends StoneMultiblock
{
	public AlloySmelterMultiblock()
	{
		super(IEApi.ieLoc("multiblocks/alloy_smelter"),
				new BlockPos(0, 0, 1), new BlockPos(1, 1, 1), new BlockPos(2, 2, 2),
				IEMultiblockLogic.ALLOY_SMELTER);
	}

	@Override
	public float getManualScale()
	{
		return 20;
	}
}