/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class RadioTowerMultiblock extends IETemplateMultiblock
{
	public static final BlockPos MASTER_OFFSET = new BlockPos(2, 8, 2);

	public RadioTowerMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/radio_tower"),
				MASTER_OFFSET, new BlockPos(2, 1, 5), new BlockPos(5, 19, 6),
				IEMultiblockLogic.RADIO_TOWER);
	}

	@Override
	public float getManualScale()
	{
		return 6;
	}
}