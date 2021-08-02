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

public class CrusherMultiblock extends IETemplateMultiblock
{
	public CrusherMultiblock()
	{
		super(new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/crusher"),
				new BlockPos(2, 1, 1), new BlockPos(2, 1, 2), new BlockPos(5, 3, 3),
				() -> Multiblocks.crusher.defaultBlockState());
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
			renderStack = new ItemStack(Multiblocks.crusher);
		transform.translate(2.5, 1.5, 1.5);
		transform.mulPose(new Quaternion(0, 45, 0, true));
		transform.mulPose(new Quaternion(-20, 0, 0, true));
		transform.scale(5.5F, 5.5F, 5.5F);

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