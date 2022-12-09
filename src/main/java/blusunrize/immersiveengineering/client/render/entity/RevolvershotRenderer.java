/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class RevolvershotRenderer extends EntityRenderer<RevolvershotEntity>
{
	public RevolvershotRenderer(Context renderManager)
	{
		super(renderManager);
	}

	@Override
	public void render(@Nonnull RevolvershotEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn,
						 MultiBufferSource bufferIn, int packedLightIn)
	{
		matrixStackIn.pushPose();
		VertexConsumer builder = bufferIn.getBuffer(IERenderTypes.getPositionTex(getTextureLocation(entity)));
		matrixStackIn.mulPose(
				new Quaternionf()
						.rotateY(Mth.DEG_TO_RAD * (entity.yRotO+(entity.getYRot()-entity.yRotO)*partialTicks-90.0F))
						.rotateZ(Mth.DEG_TO_RAD * (entity.xRotO+(entity.getXRot()-entity.xRotO)*partialTicks))
		);
		matrixStackIn.scale(0.25F, 0.25F, 0.25F);
		Matrix4f mat = matrixStackIn.last().pose();

		builder.vertex(mat, 0, 0, -.25f).uv(5/32f, 10/32f).endVertex();
		builder.vertex(mat, 0, 0, .25f).uv(0/32f, 10/32f).endVertex();
		builder.vertex(mat, 0, .5f, .25f).uv(0/32f, 5/32f).endVertex();
		builder.vertex(mat, 0, .5f, -.25f).uv(5/32f, 5/32f).endVertex();

		builder.vertex(mat, .375f, .125f, 0).uv(8/32f, 5/32f).endVertex();
		builder.vertex(mat, 0, .125f, 0).uv(0/32f, 5/32f).endVertex();
		builder.vertex(mat, 0, .375f, 0).uv(0/32f, 0/32f).endVertex();
		builder.vertex(mat, .375f, .375f, 0).uv(8/32f, 0/32f).endVertex();

		builder.vertex(mat, .375f, .25f, -.25f).uv(8/32f, 5/32f).endVertex();
		builder.vertex(mat, 0, .25f, -.25f).uv(0/32f, 5/32f).endVertex();
		builder.vertex(mat, 0, .25f, .25f).uv(0/32f, 0/32f).endVertex();
		builder.vertex(mat, .375f, .25f, .25f).uv(8/32f, 0/32f).endVertex();

		matrixStackIn.popPose();
	}

	@Override
	@Nonnull
	public ResourceLocation getTextureLocation(@Nonnull RevolvershotEntity entity)
	{
		return new ResourceLocation("immersiveengineering:textures/models/bullet.png");
	}

}
