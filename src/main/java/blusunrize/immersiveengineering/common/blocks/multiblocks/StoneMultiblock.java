/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public abstract class StoneMultiblock extends IETemplateMultiblock
{
	public StoneMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, BlockPos size, MultiblockRegistration<?> logic)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, size, logic);
	}

	@Override
	public boolean canBeMirrored()
	{
		return false;
	}
}
