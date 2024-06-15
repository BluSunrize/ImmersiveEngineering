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
public class DieselGeneratorMultiblock extends IETemplateMultiblock
{
	public DieselGeneratorMultiblock()
	{
		super(IEApi.ieLoc("multiblocks/diesel_generator"),
				new BlockPos(1, 1, 2), new BlockPos(1, 1, 4), new BlockPos(3, 3, 5),
				IEMultiblockLogic.DIESEL_GENERATOR);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}