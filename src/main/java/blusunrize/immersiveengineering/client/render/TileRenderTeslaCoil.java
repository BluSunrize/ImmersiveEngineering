package blusunrize.immersiveengineering.client.render;

import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil.LightningAnimation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Vec3;

public class TileRenderTeslaCoil extends TileEntitySpecialRenderer<TileEntityTeslaCoil>
{
	@Override
	public void renderTileEntityAt(TileEntityTeslaCoil tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(tile.isDummy()||!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		Iterator<LightningAnimation> animationIt = TileEntityTeslaCoil.effectMap.get(tile.getPos()).iterator();

		while(animationIt.hasNext())
		{
			LightningAnimation animation = animationIt.next();
			if(animation.shoudlRecalculateLightning())
				animation.createLightning(tile.getWorld().rand);

			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);

			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GL11.glPushAttrib(GL11.GL_LIGHTING);
			GlStateManager.disableLighting();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);


			double tx = tile.getPos().getX();
			double ty = tile.getPos().getY();
			double tz = tile.getPos().getZ();
			float curWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
			drawAnimation(animation, tx,ty,tz, new float[]{77/255f,74/255f,152/255f, .75f}, 4f);
			drawAnimation(animation, tx,ty,tz, new float[]{1,1,1,1}, 1f);
			GL11.glLineWidth(curWidth);
			
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GL11.glPopAttrib();

			GlStateManager.popMatrix();
			if(animation.timer--<=0)
				animationIt.remove();
		}

	}
	
	public static void drawAnimation(LightningAnimation animation, double tileX, double tileY, double tileZ, float[] rgba, float lineWidth)
	{
		GlStateManager.color(rgba[0],rgba[1],rgba[2],rgba[3]);
		GL11.glLineWidth(lineWidth);
		Tessellator tes = ClientUtils.tes();
		WorldRenderer worldrenderer = tes.getWorldRenderer();
		worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
		List<Vec3> subs = animation.subPoints;
		worldrenderer.pos(animation.startPos.xCoord-tileX,animation.startPos.yCoord-tileY,animation.startPos.zCoord-tileZ).endVertex();
		
		for(int i=0; i<subs.size(); i++)
			worldrenderer.pos(subs.get(i).xCoord-tileX,subs.get(i).yCoord-tileY,subs.get(i).zCoord-tileZ).endVertex();

		Vec3 end = (animation.targetEntity!=null?animation.targetEntity.getPositionVector():animation.targetPos).addVector(-tileX,-tileY,-tileZ);
		worldrenderer.pos(end.xCoord,end.yCoord,end.zCoord).endVertex();

		tes.draw();
	}
}