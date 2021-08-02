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
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BucketWheelMultiblock extends IETemplateMultiblock
{
	public BucketWheelMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/bucket_wheel"),
				new BlockPos(3, 3, 0), new BlockPos(3, 3, 0), new BlockPos(7, 7, 1),
				() -> Multiblocks.bucketWheel.defaultBlockState());
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
	public void renderFormedStructure(PoseStack transform, MultiBufferSource buffer)
	{
		if(renderStack==null)
			renderStack = new ItemStack(Multiblocks.bucketWheel);
		transform.translate(3.5, 3.5, 0.5);
		transform.mulPose(new Quaternion(0, 45, 0, true));
		transform.mulPose(new Quaternion(-20, 0, 0, true));
		transform.scale(6.875F, 6.875F, 6.875F);

		ClientUtils.mc().getItemRenderer().renderStatic(
				renderStack,
				TransformType.GUI,
				0xf000f0,
				OverlayTexture.NO_OVERLAY,
				transform, buffer,
				0
		);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
}