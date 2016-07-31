package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.VertexBuffer;
import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.EntityGrapplingHook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class EntityRenderGrapplingHook extends Render<EntityGrapplingHook>
{
	public EntityRenderGrapplingHook(RenderManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public void doRender(EntityGrapplingHook entity, double x, double y, double z, float f0, float f1)
	{
		if(entity.getShooter()==null || entity.isDead)
			return;
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.enableRescaleNormal();
		Tessellator tes = ClientUtils.tes();
		VertexBuffer worldrenderer = ClientUtils.tes().getBuffer();

		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GlStateManager.disableCull();
		GlStateManager.disableLighting();

		ClientUtils.bindAtlas();

		double targetX = entity.getShooter().prevPosX+(entity.getShooter().posX-entity.getShooter().prevPosX)*f1;
		double targetY = entity.getShooter().prevPosY+(entity.getShooter().posY-entity.getShooter().prevPosY)*f1+entity.getShooter().height/2;
		double targetZ = entity.getShooter().prevPosZ+(entity.getShooter().posZ-entity.getShooter().prevPosZ)*f1;
		double distanceX = targetX-entity.posX;
		double distanceY = targetY-entity.posY;
		double distanceZ = targetZ-entity.posZ;
		double dw = Math.sqrt(distanceX*distanceX + distanceZ*distanceZ);
		double d = 1;//Math.sqrt(distanceX*distanceX + distanceY*distanceY + distanceZ*distanceZ);

		double rmodx = distanceZ/dw;
		double rmodz = distanceX/dw;
		double uMin = WireType.iconDefaultWire.getMinU();
		double uMax = WireType.iconDefaultWire.getMaxU();
		double vMin = WireType.iconDefaultWire.getMinV();
		double vMax = WireType.iconDefaultWire.getMaxV();
		double radius = .03125;
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		int[] col = {0x77,0x77,0x77};
		double tx = 0;
		double ty = 0; 
		double tz = 0;
		for(int i=0; i<d; i++)
		{
			worldrenderer.setTranslation(tx, ty, tz);
			double dx = distanceX/d*(i+1);
			double dy = distanceY/d*(i+1); 
			double dz = distanceZ/d*(i+1);
			worldrenderer.pos(0, 0+radius, 0).tex(uMin,vMax).color(col[0],col[1],col[2],255).endVertex();
			worldrenderer.pos(dx, dy+radius, dz).tex(uMax,vMax).color(col[0],col[1],col[2],255).endVertex();
			worldrenderer.pos(dx, dy-radius, dz).tex(uMax,vMin).color(col[0],col[1],col[2],255).endVertex();
			worldrenderer.pos(0, 0-radius, 0).tex(uMin,vMin).color(col[0],col[1],col[2],255).endVertex();

			worldrenderer.pos(0-radius*rmodx, 0, 0+radius*rmodz).tex(uMin,vMax).color(col[0],col[1],col[2],255).endVertex();
			worldrenderer.pos(dx-radius*rmodx, dy, dz+radius*rmodz).tex(uMax,vMax).color(col[0],col[1],col[2],255).endVertex();
			worldrenderer.pos(dx+radius*rmodx, dy, dz-radius*rmodz).tex(uMax,vMin).color(col[0],col[1],col[2],255).endVertex();
			worldrenderer.pos(0+radius*rmodx, 0, 0-radius*rmodz).tex(uMin,vMin).color(col[0],col[1],col[2],255).endVertex();
			tx += dx;
			ty += dy;
			tz += dz;
		}
		worldrenderer.setTranslation(0,0,0);
		tes.draw();

		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.enableCull();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGrapplingHook entity)
	{
		return null;
	}
}