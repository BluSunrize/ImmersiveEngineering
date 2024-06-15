/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CrusherLogic;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.core.BlockPos;
public class CrusherMultiblock extends IETemplateMultiblock
{
	public CrusherMultiblock()
	{
		super(IEApi.ieLoc("multiblocks/crusher"),
				CrusherLogic.MASTER_OFFSET, new BlockPos(2, 1, 2), new BlockPos(5, 3, 3),
				IEMultiblockLogic.CRUSHER);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}