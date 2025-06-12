/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher.Result;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;

public class SiloMultiblock extends IETemplateMultiblock
{
	public SiloMultiblock()
	{
		super(IEApi.ieLoc("multiblocks/silo"),
				new BlockPos(1, 0, 1), new BlockPos(1, 1, 2), new BlockPos(3, 7, 3),
				IEMultiblockLogic.SILO, ImmutableList.of((expected, found, world, pos) -> {
					if(expected.getBlock()!=found.getBlock())
						if(expected.is(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)&&found.is(IETags.sheetmetals))
							return Result.allow(2);
					return Result.DEFAULT;
				})
		);
	}

	@Override
	public float getManualScale()
	{
		return 10;
	}
}