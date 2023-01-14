/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.LightningRodLogic;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class LightningRodMultiblock extends IETemplateMultiblock
{
	public LightningRodMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/lightning_rod"),
				LightningRodLogic.MASTER_OFFSET, new BlockPos(1, 1, 2), new BlockPos(3, 3, 3),
				IEMultiblockLogic.LIGHTNING_ROD);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}
