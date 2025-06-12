/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.core.BlockPos;

public class ChunkLoaderMultiblock extends IETemplateMultiblock
{
	public static final BlockPos MASTER_OFFSET = new BlockPos(1, 2, 1);

	public ChunkLoaderMultiblock()
	{
		super(IEApi.ieLoc("multiblocks/chunk_loader"),
				MASTER_OFFSET, new BlockPos(1, 1, 2), new BlockPos(3, 5, 3),
				IEMultiblockLogic.CHUNK_LOADER);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}