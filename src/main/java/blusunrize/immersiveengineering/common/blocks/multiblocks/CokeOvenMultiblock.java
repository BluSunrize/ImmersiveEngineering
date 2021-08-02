/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CokeOvenMultiblock extends StoneMultiblock
{
	public CokeOvenMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/coke_oven"),
				new BlockPos(1, 1, 1), new BlockPos(1, 1, 2), new BlockPos(3, 3, 3),
				() -> Multiblocks.cokeOven.defaultBlockState());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure(PoseStack transform, MultiBufferSource buffer)
	{
	}

	@Override
	public float getManualScale()
	{
		return 16;
	}
}
