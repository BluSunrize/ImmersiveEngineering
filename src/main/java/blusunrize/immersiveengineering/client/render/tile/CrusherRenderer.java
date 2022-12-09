/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

public class CrusherRenderer extends IEBlockEntityRenderer<CrusherBlockEntity>
{
	public static String NAME_LEFT = "crusher_barrel_left";
	public static String NAME_RIGHT = "crusher_barrel_right";
	public static DynamicModel BARREL_LEFT;
	public static DynamicModel BARREL_RIGHT;

	@Override
	public void render(CrusherBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getLevelNonnull().hasChunkAt(te.getBlockPos()))
			return;

		Direction dir = te.getFacing();

		boolean b = te.shouldRenderAsActive();
		float angle = te.animation_barrelRotation+(b?18*partialTicks: 0);

		matrixStack.pushPose();

		matrixStack.translate(.5, 1.5, .5);
		matrixStack.translate(te.getFacing().getStepX()*.5, 0, te.getFacing().getStepZ()*.5);

		matrixStack.pushPose();
		matrixStack.mulPose(new Quaternionf().rotateAxis(angle *Mth.DEG_TO_RAD, new Vector3f(-te.getFacing().getStepZ(), 0, te.getFacing().getStepX())));
		renderBarrel(BARREL_LEFT, matrixStack, bufferIn, dir, combinedLightIn, combinedOverlayIn);
		matrixStack.popPose();

		matrixStack.pushPose();
		matrixStack.translate(te.getFacing().getStepX()*-1, 0, te.getFacing().getStepZ()*-1);
		matrixStack.mulPose(new Quaternionf().rotateAxis(-angle * Mth.DEG_TO_RAD, new Vector3f(-te.getFacing().getStepZ(), 0, te.getFacing().getStepX())));
		renderBarrel(BARREL_RIGHT, matrixStack, bufferIn, dir, combinedLightIn, combinedOverlayIn);
		matrixStack.popPose();

		matrixStack.popPose();
	}

	private void renderBarrel(DynamicModel barrel, PoseStack matrix, MultiBufferSource buffer, Direction facing, int light, int overlay)
	{
		matrix.pushPose();
		matrix.translate(-.5, -.5, -.5);
		List<BakedQuad> quads = barrel.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
		rotateForFacing(matrix, facing);
		RenderUtils.renderModelTESRFast(quads, buffer.getBuffer(RenderType.solid()), matrix, light, overlay);
		matrix.popPose();
	}

}