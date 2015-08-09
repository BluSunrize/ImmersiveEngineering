package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class EntityRenderRevolvershot extends Render
{
	public EntityRenderRevolvershot()
	{
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1)
	{
		GL11.glPushMatrix();
		this.bindEntityTexture(entity);
		GL11.glTranslated(x, y, z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		Tessellator tessellator = Tessellator.instance;

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

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(0, .0,-.25, 5/32d, 10/32d);
		tessellator.addVertexWithUV(0, .0, .25, 0/32d, 10/32d);
		tessellator.addVertexWithUV(0, .5, .25, 0/32d,  5/32d);
		tessellator.addVertexWithUV(0, .5,-.25, 5/32d,  5/32d);
		tessellator.draw();

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(.375, .125,0, 8/32d, 5/32d);
		tessellator.addVertexWithUV(0, .125,0, 0/32d, 5/32d);
		tessellator.addVertexWithUV(0, .375,0, 0/32d, 0/32d);
		tessellator.addVertexWithUV(.375,.375,0, 8/32d, 0/32d);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(.375, .25,-.25, 8/32d, 5/32d);
		tessellator.addVertexWithUV(0, .25,-.25, 0/32d, 5/32d);
		tessellator.addVertexWithUV(0, .25, .25, 0/32d, 0/32d);
		tessellator.addVertexWithUV(.375,.25, .25, 8/32d, 0/32d);
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
