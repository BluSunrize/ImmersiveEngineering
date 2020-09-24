/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SawmillMultiblock extends IETemplateMultiblock
{
	public SawmillMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/sawmill"),
				new BlockPos(2, 1, 1), new BlockPos(2, 0, 2), () -> Multiblocks.sawmill.getDefaultState());
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
	public void renderFormedStructure(MatrixStack transform, IRenderTypeBuffer buffer)
	{
		if(renderStack==null)
			renderStack = new ItemStack(Multiblocks.sawmill);
		transform.translate(2.5, 1.5, 1.5);
		transform.rotate(new Quaternion(0, 45, 0, true));
		transform.rotate(new Quaternion(-20, 0, 0, true));
		transform.scale(5.5F, 5.5F, 5.5F);

		ClientUtils.mc().getItemRenderer().renderItem(
				renderStack,
				TransformType.GUI,
				0xf000f0,
				OverlayTexture.NO_OVERLAY,
				transform, buffer
		);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}