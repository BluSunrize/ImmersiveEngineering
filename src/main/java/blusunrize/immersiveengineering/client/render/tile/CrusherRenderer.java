/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CrusherLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CrusherLogic.State;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class CrusherRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<CrusherLogic.State>>
{
	public static String NAME_LEFT = "crusher_barrel_left";
	public static String NAME_RIGHT = "crusher_barrel_right";
	public static DynamicModel BARREL_LEFT;
	public static DynamicModel BARREL_RIGHT;

	@Override
	public void render(
			MultiblockBlockEntityMaster<CrusherLogic.State> te,
			float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn
	)
	{
		final IMultiblockContext<State> ctx = te.getHelper().getContext();
		final State state = ctx.getState();
		final MultiblockOrientation orientation = ctx.getLevel().getOrientation();
		Direction dir = orientation.front();

		boolean active = state.shouldRenderActive();
		float angle = state.getBarrelAngle()+(active?18*partialTicks: 0);

		matrixStack.pushPose();

		matrixStack.translate(.5, 1.5, .5);
		matrixStack.translate(dir.getStepX()*.5, 0, dir.getStepZ()*.5);

		matrixStack.pushPose();
		matrixStack.mulPose(new Quaternionf().rotateAxis(angle *Mth.DEG_TO_RAD, new Vector3f(-dir.getStepZ(), 0, dir.getStepX())));
		renderBarrel(BARREL_LEFT, matrixStack, bufferIn, dir, combinedLightIn, combinedOverlayIn);
		matrixStack.popPose();

		matrixStack.pushPose();
		matrixStack.translate(dir.getStepX()*-1, 0, dir.getStepZ()*-1);
		matrixStack.mulPose(new Quaternionf().rotateAxis(-angle * Mth.DEG_TO_RAD, new Vector3f(-dir.getStepZ(), 0, dir.getStepX())));
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