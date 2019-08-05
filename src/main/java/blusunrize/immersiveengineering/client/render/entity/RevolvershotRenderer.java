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
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
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
	public void doRender(@Nonnull RevolvershotEntity entity, double x, double y, double z, float f0, float f1)
	{
		GlStateManager.pushMatrix();
		this.bindEntityTexture(entity);
		GlStateManager.translated(x, y, z);
		GlStateManager.enableRescaleNormal();
		Tessellator tessellator = ClientUtils.tes();
		BufferBuilder worldrenderer = ClientUtils.tes().getBuffer();

		GlStateManager.disableCull();
		GlStateManager.rotatef(entity.prevRotationYaw+(entity.rotationYaw-entity.prevRotationYaw)*f1-90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(entity.prevRotationPitch+(entity.rotationPitch-entity.prevRotationPitch)*f1, 0.0F, 0.0F, 1.0F);

		GlStateManager.scalef(.25f, .25f, .25f);

		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(0, .0, -.25).tex(5/32d, 10/32d).endVertex();
		worldrenderer.pos(0, .0, .25).tex(0/32d, 10/32d).endVertex();
		worldrenderer.pos(0, .5, .25).tex(0/32d, 5/32d).endVertex();
		worldrenderer.pos(0, .5, -.25).tex(5/32d, 5/32d).endVertex();
		tessellator.draw();

		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(.375, .125, 0).tex(8/32d, 5/32d).endVertex();
		worldrenderer.pos(0, .125, 0).tex(0/32d, 5/32d).endVertex();
		worldrenderer.pos(0, .375, 0).tex(0/32d, 0/32d).endVertex();
		worldrenderer.pos(.375, .375, 0).tex(8/32d, 0/32d).endVertex();
		tessellator.draw();

		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(.375, .25, -.25).tex(8/32d, 5/32d).endVertex();
		worldrenderer.pos(0, .25, -.25).tex(0/32d, 5/32d).endVertex();
		worldrenderer.pos(0, .25, .25).tex(0/32d, 0/32d).endVertex();
		worldrenderer.pos(.375, .25, .25).tex(8/32d, 0/32d).endVertex();
		tessellator.draw();

		GlStateManager.enableCull();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(@Nonnull RevolvershotEntity p_110775_1_)
	{
		return new ResourceLocation("immersiveengineering:textures/models/bullet.png");
	}

}
