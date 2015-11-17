package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.EntityChemthrowerShot;

public class EntityRenderChemthrowerShot extends Render
{
	public EntityRenderChemthrowerShot()
	{
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1)
	{
		if(((EntityChemthrowerShot)entity).getFluid()==null)
			return;
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		Tessellator tessellator = Tessellator.instance;

		GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

		IIcon icon = ((EntityChemthrowerShot)entity).getFluid().getStillIcon();
		if(icon!=null)
		{
			ClientUtils.bindAtlas(0);
			GL11.glScalef(.25f, .25f, .25f);
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 1.0F, 0.0F);
			tessellator.addVertexWithUV(-.25,-.25,0, icon.getInterpolatedU(4), icon.getInterpolatedV(4));
			tessellator.addVertexWithUV( .25,-.25,0, icon.getInterpolatedU(0), icon.getInterpolatedV(4));
			tessellator.addVertexWithUV( .25, .25,0, icon.getInterpolatedU(0), icon.getInterpolatedV(0));
			tessellator.addVertexWithUV(-.25, .25,0, icon.getInterpolatedU(4), icon.getInterpolatedV(0));
			tessellator.draw();
		}
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
