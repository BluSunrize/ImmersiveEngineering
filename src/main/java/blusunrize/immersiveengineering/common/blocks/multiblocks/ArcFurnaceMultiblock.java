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

public class ArcFurnaceMultiblock extends IETemplateMultiblock
{
	public static final BlockPos MASTER_OFFSET = new BlockPos(2, 1, 2);

	public ArcFurnaceMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/arcfurnace"),
				MASTER_OFFSET, new BlockPos(2, 0, 4), new BlockPos(5, 5, 5),
				IEMultiblockLogic.ARC_FURNACE);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}