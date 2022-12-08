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
import blusunrize.immersiveengineering.common.blocks.metal.DieselGeneratorBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

public class DieselGeneratorRenderer extends IEBlockEntityRenderer<DieselGeneratorBlockEntity>
{
	public static final String NAME = "diesel_gen_fan";
	public static DynamicModel FAN;

	@Override
	public void render(DieselGeneratorBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getLevelNonnull().hasChunkAt(te.getBlockPos()))
			return;

		BlockPos blockPos = te.getBlockPos();
		BlockState state = te.getLevel().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.DIESEL_GENERATOR.get())
			return;

		matrixStack.pushPose();
		matrixStack.translate(0, .6875, 0);
		matrixStack.translate(0.5, 0, 0.5);

		matrixStack.mulPose(new Quaternionf(new Vector3f(Vec3.atLowerCornerOf(te.getFacing().getNormal())),
				te.animation_fanRotation+(te.animation_fanRotationStep*partialTicks), true));
		matrixStack.translate(-0.5, 0, -0.5);

		List<BakedQuad> quads = FAN.get().getQuads(state, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
		rotateForFacing(matrixStack, te.getFacing());
		RenderUtils.renderModelTESRFast(quads, bufferIn.getBuffer(RenderType.solid()), matrixStack, combinedLightIn,
				combinedOverlayIn);

		matrixStack.popPose();
	}
}