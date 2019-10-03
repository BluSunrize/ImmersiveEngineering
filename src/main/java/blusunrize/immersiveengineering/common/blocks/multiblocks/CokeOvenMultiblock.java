/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CokeOvenMultiblock extends IETemplateMultiblock
{
	public CokeOvenMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/coke_oven"),
				new BlockPos(1, 1, 1), new BlockPos(1, 1, 2), () -> Multiblocks.cokeOven.getDefaultState());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure()
	{
	}

	@Override
	public float getManualScale()
	{
		return 16;
	}
}