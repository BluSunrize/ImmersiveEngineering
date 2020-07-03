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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;

import javax.annotation.Nonnull;

public class RevolvershotRenderer extends EntityRenderer<RevolvershotEntity>
{
	public RevolvershotRenderer(EntityRendererManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public void render(@Nonnull RevolvershotEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
						 IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		matrixStackIn.push();
		IVertexBuilder builder = bufferIn.getBuffer(IERenderTypes.getPositionTex(getEntityTexture(entity)));
		matrixStackIn.rotate(new Quaternion(0, entity.prevRotationYaw+(entity.rotationYaw-entity.prevRotationYaw)*partialTicks-90.0F, 0, true));
		matrixStackIn.rotate(new Quaternion(0.0F, 0.0F, entity.prevRotationPitch+(entity.rotationPitch-entity.prevRotationPitch)*partialTicks, true));
		matrixStackIn.scale(0.25F, 0.25F, 0.25F);
		Matrix4f mat = matrixStackIn.getLast().getMatrix();

		builder.pos(mat, 0, 0, -.25f).tex(5/32f, 10/32f).endVertex();
		builder.pos(mat, 0, 0, .25f).tex(0/32f, 10/32f).endVertex();
		builder.pos(mat, 0, .5f, .25f).tex(0/32f, 5/32f).endVertex();
		builder.pos(mat, 0, .5f, -.25f).tex(5/32f, 5/32f).endVertex();

		builder.pos(mat, .375f, .125f, 0).tex(8/32f, 5/32f).endVertex();
		builder.pos(mat, 0, .125f, 0).tex(0/32f, 5/32f).endVertex();
		builder.pos(mat, 0, .375f, 0).tex(0/32f, 0/32f).endVertex();
		builder.pos(mat, .375f, .375f, 0).tex(8/32f, 0/32f).endVertex();

		builder.pos(mat, .375f, .25f, -.25f).tex(8/32f, 5/32f).endVertex();
		builder.pos(mat, 0, .25f, -.25f).tex(0/32f, 5/32f).endVertex();
		builder.pos(mat, 0, .25f, .25f).tex(0/32f, 0/32f).endVertex();
		builder.pos(mat, .375f, .25f, .25f).tex(8/32f, 0/32f).endVertex();

		matrixStackIn.pop();
	}

	@Override
	@Nonnull
	public ResourceLocation getEntityTexture(@Nonnull RevolvershotEntity p_110775_1_)
	{
		return new ResourceLocation("immersiveengineering:textures/models/bullet.png");
	}

}
