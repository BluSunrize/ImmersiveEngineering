/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RefineryMultiblock extends IETemplateMultiblock
{
	public RefineryMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/refinery"),
				new BlockPos(2, 1, 2), new BlockPos(2, 1, 2), () -> Multiblocks.refinery.getDefaultState());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	private static ItemStack renderStack;

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack==null)
			renderStack = new ItemStack(Multiblocks.refinery);
		RenderSystem.translated(1.5, 1.5, 1.5);
		RenderSystem.rotatef(-45, 0, 1, 0);
		RenderSystem.rotatef(-20, 1, 0, 0);
		RenderSystem.scaled(4, 4, 4);

		RenderSystem.disableCull();
		ClientUtils.mc().getItemRenderer().renderItemIntoGUI(renderStack, 0, 0);
		RenderSystem.enableCull();
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}
