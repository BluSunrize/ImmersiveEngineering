package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class EntityRenderRailgunShot extends Render
{
	public EntityRenderRailgunShot()
	{
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1)
	{
		GL11.glPushMatrix();
//		this.bindEntityTexture(entity);
		GL11.glTranslated(x, y, z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		Tessellator tessellator = Tessellator.instance;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * f1 - 90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * f1, 0.0F, 0.0F, 1.0F);

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
		tessellator.addVertex(1.375, .125,0);
		tessellator.addVertex(0, .125,0);
		tessellator.addVertex(0, .375,0);
		tessellator.addVertex(1.375,.375,0);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(.375, .25,-.25, 8/32d, 5/32d);
		tessellator.addVertexWithUV(0, .25,-.25, 0/32d, 5/32d);
		tessellator.addVertexWithUV(0, .25, .25, 0/32d, 0/32d);
		tessellator.addVertexWithUV(.375,.25, .25, 8/32d, 0/32d);
		tessellator.draw();

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
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
