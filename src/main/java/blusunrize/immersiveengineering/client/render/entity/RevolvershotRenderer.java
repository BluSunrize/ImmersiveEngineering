/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.dummy.GlStateManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

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
		GlStateManager.pushMatrix();
		this.bindEntityTexture(entity);
		GlStateManager.enableRescaleNormal();
		Tessellator tessellator = ClientUtils.tes();
		BufferBuilder worldrenderer = ClientUtils.tes().getBuffer();

		GlStateManager.disableCull();
		GlStateManager.rotatef(entity.prevRotationYaw+(entity.rotationYaw-entity.prevRotationYaw)*partialTicks-90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(entity.prevRotationPitch+(entity.rotationPitch-entity.prevRotationPitch)*partialTicks, 0.0F, 0.0F, 1.0F);

		GlStateManager.scalef(.25f, .25f, .25f);

		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(0, .0, -.25).tex(5/32f, 10/32f).endVertex();
		worldrenderer.pos(0, .0, .25).tex(0/32f, 10/32f).endVertex();
		worldrenderer.pos(0, .5, .25).tex(0/32f, 5/32f).endVertex();
		worldrenderer.pos(0, .5, -.25).tex(5/32f, 5/32f).endVertex();
		tessellator.draw();

		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(.375, .125, 0).tex(8/32f, 5/32f).endVertex();
		worldrenderer.pos(0, .125, 0).tex(0/32f, 5/32f).endVertex();
		worldrenderer.pos(0, .375, 0).tex(0/32f, 0/32f).endVertex();
		worldrenderer.pos(.375, .375, 0).tex(8/32f, 0/32f).endVertex();
		tessellator.draw();

		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(.375, .25, -.25).tex(8/32f, 5/32f).endVertex();
		worldrenderer.pos(0, .25, -.25).tex(0/32f, 5/32f).endVertex();
		worldrenderer.pos(0, .25, .25).tex(0/32f, 0/32f).endVertex();
		worldrenderer.pos(.375, .25, .25).tex(8/32f, 0/32f).endVertex();
		tessellator.draw();

		GlStateManager.enableCull();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}

	@Override
	@Nonnull
	public ResourceLocation getEntityTexture(@Nonnull RevolvershotEntity p_110775_1_)
	{
		return new ResourceLocation("immersiveengineering:textures/models/bullet.png");
	}

}
