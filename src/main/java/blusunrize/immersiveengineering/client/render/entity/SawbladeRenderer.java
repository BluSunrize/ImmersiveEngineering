/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel;
import blusunrize.immersiveengineering.common.entities.SawbladeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.AmbientOcclusionStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class SawbladeRenderer extends EntityRenderer<SawbladeEntity>
{
	public static final String NAME = "sawblade_entity";
	public static DynamicModel MODEL;

	public static final ResourceLocation SAWBLADE = new ResourceLocation(ImmersiveEngineering.MODID, "item/sawblade_blade");

	public SawbladeRenderer(Context renderManager)
	{
		super(renderManager);
	}

	@Override
	public void render(SawbladeEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
		VertexConsumer builder = bufferIn.getBuffer(RenderType.translucent());

		final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		BlockPos blockPos = entity.blockPosition();
		BlockState state = entity.getCommandSenderWorld().getBlockState(blockPos);
		BakedModel model = this.MODEL.get();
		IEObjState objState = new IEObjState(VisibilityList.show("blade"));

		matrixStackIn.pushPose();
		matrixStackIn.scale(.75f, .75f, .75f);

		double yaw = entity.yRotO+(entity.getYRot()-entity.yRotO)*partialTicks-90.0F;
		double pitch = entity.xRotO+(entity.getXRot()-entity.xRotO)*partialTicks;
		matrixStackIn.mulPose(new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), (float)yaw, true));
		matrixStackIn.mulPose(new Quaternion(new Vector3f(0.0F, 0.0F, 1.0F), (float)pitch, true));

		if(!entity.inGround)
		{
			float spin = ((entity.tickCount+partialTicks)%10)/10f*360;
			matrixStackIn.mulPose(new Quaternion(new Vector3f(0, 1, 0), spin, true));
		}

		AmbientOcclusionStatus aoStat = ClientUtils.mc().options.ambientOcclusion;
		ClientUtils.mc().options.ambientOcclusion = AmbientOcclusionStatus.OFF;

		blockRenderer.getModelRenderer().renderModel(
				matrixStackIn.last(), builder, state, model,
				// Tint color
				1, 1, 1,
				packedLightIn, OverlayTexture.NO_OVERLAY, new SinglePropertyModelData<>(objState, Model.IE_OBJ_STATE)
		);

		ClientUtils.mc().options.ambientOcclusion = aoStat;

		matrixStackIn.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(@Nonnull SawbladeEntity entity)
	{
		return SAWBLADE;
	}

}
