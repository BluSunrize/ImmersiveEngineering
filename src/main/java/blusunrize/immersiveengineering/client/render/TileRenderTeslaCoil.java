/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil.LightningAnimation;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;
import java.util.List;

public class TileRenderTeslaCoil extends TileEntitySpecialRenderer<TileEntityTeslaCoil>
{
	@Override
	public void render(TileEntityTeslaCoil tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(tile.isDummy()||!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		Iterator<LightningAnimation> animationIt = TileEntityTeslaCoil.effectMap.get(tile.getPos()).iterator();

		setLightmapDisabled(true);
		boolean wasLightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
		GlStateManager.disableLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		while(animationIt.hasNext())
		{
			LightningAnimation animation = animationIt.next();
			if(animation.shoudlRecalculateLightning())
				animation.createLightning(Utils.RAND);

			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);

			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();


			double tx = tile.getPos().getX();
			double ty = tile.getPos().getY();
			double tz = tile.getPos().getZ();
			float curWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
			drawAnimation(animation, tx, ty, tz, new float[]{77/255f, 74/255f, 152/255f, .75f}, 4f);
			drawAnimation(animation, tx, ty, tz, new float[]{1, 1, 1, 1}, 1f);
			GL11.glLineWidth(curWidth);

			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();

			GlStateManager.popMatrix();
		}
		if(wasLightingEnabled)
			GlStateManager.enableLighting();
		else
			GlStateManager.disableLighting();
		setLightmapDisabled(false);
	}

	public static void drawAnimation(LightningAnimation animation, double tileX, double tileY, double tileZ, float[] rgba, float lineWidth)
	{
		GlStateManager.color(rgba[0], rgba[1], rgba[2], rgba[3]);
		GL11.glLineWidth(lineWidth);
		Tessellator tes = ClientUtils.tes();
		BufferBuilder worldrenderer = tes.getBuffer();
		worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
		List<Vec3d> subs = animation.subPoints;
		worldrenderer.pos(animation.startPos.x-tileX, animation.startPos.y-tileY, animation.startPos.z-tileZ).endVertex();

		for(int i = 0; i < subs.size(); i++)
			worldrenderer.pos(subs.get(i).x-tileX, subs.get(i).y-tileY, subs.get(i).z-tileZ).endVertex();

		Vec3d end = (animation.targetEntity!=null?animation.targetEntity.getPositionVector(): animation.targetPos).add(-tileX, -tileY, -tileZ);
		worldrenderer.pos(end.x, end.y, end.z).endVertex();

		tes.draw();
	}
}