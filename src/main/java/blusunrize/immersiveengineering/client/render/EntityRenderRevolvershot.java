package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import blusunrize.immersiveengineering.client.ClientUtils;

public class EntityRenderRevolvershot extends Render
{
	public EntityRenderRevolvershot(RenderManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1)
	{
		GL11.glPushMatrix();
		this.bindEntityTexture(entity);
		GL11.glTranslated(x, y, z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		Tessellator tessellator = ClientUtils.tes();
		WorldRenderer worldrenderer = ClientUtils.tes().getWorldRenderer();

		//		float f2 = 0.0F;
		//		float f3 = 0.5F;
		//		float f4 = 0/32f;
		//		float f5 = 5/32f;
		//		float f6 = 0.0F;
		//		float f7 = 0.15625F;
		//		float f8 = 5/32F;
		//		float f9 = 10/32F;
		//		float f10 = 0.05625F;
		//		        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		//		        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * f1 - 90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * f1, 0.0F, 0.0F, 1.0F);

		//		tessellator.startDrawingQuads();
		//        tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, (double)f6, (double)f8);
		//        tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, (double)f7, (double)f8);
		//        tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, (double)f7, (double)f9);
		//        tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, (double)f6, (double)f9);
		//        tessellator.draw();
		//        GL11.glNormal3f(-f10, 0.0F, 0.0F);
		//        tessellator.startDrawingQuads();
		//        tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, (double)f6, (double)f8);
		//        tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, (double)f7, (double)f8);
		//        tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, (double)f7, (double)f9);
		//        tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, (double)f6, (double)f9);
		//        tessellator.draw();

		GL11.glScalef(.25f, .25f, .25f);

		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(0, .0,-.25).tex(5/32d, 10/32d).endVertex();
		worldrenderer.pos(0, .0, .25).tex(0/32d, 10/32d).endVertex();
		worldrenderer.pos(0, .5, .25).tex(0/32d, 5/32d).endVertex();
		worldrenderer.pos(0, .5,-.25).tex(5/32d, 5/32d).endVertex();
		tessellator.draw();

		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(.375,	.125,0).tex(8/32d, 5/32d).endVertex();
		worldrenderer.pos(0,	.125,0).tex(0/32d, 5/32d).endVertex();
		worldrenderer.pos(0,	.375,0).tex(0/32d, 0/32d).endVertex();
		worldrenderer.pos(.375,	.375,0).tex(8/32d, 0/32d).endVertex();
		tessellator.draw();

		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(.375,	.25,-.25).tex(8/32d, 5/32d).endVertex();
		worldrenderer.pos(0,	.25,-.25).tex(0/32d, 5/32d).endVertex();
		worldrenderer.pos(0,	.25, .25).tex(0/32d, 0/32d).endVertex();
		worldrenderer.pos(.375,	.25, .25).tex(8/32d, 0/32d).endVertex();
		tessellator.draw();
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_)
	{
		return new ResourceLocation("immersiveengineering:textures/models/bullet.png");
	}

}
