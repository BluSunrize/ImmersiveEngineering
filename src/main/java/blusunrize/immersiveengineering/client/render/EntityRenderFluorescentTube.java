/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.EntityFluorescentTube;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class EntityRenderFluorescentTube extends Render<EntityFluorescentTube>
{
	static double sqrt2Half = Math.sqrt(2)/2;
	public static final double[][] octagon = {
			{1, 0}, {sqrt2Half, sqrt2Half}, {0, 1}, {-sqrt2Half, sqrt2Half},
			{-1, 0}, {-sqrt2Half, -sqrt2Half}, {0, -1}, {sqrt2Half, -sqrt2Half}	
	};
	private static Random r = new Random();
	ResourceLocation modelLocation = new ResourceLocation("immersiveengineering:fluorescent_tube.obj");
	TextureAtlasSprite tex;
	public EntityRenderFluorescentTube(RenderManager renderManager)
	{
		super(renderManager);
		shadowOpaque = 0;
		shadowSize = 0;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityFluorescentTube entity)
	{
		return null;
	}
	@Override
	public void doRender(EntityFluorescentTube entity, double x, double y, double z, float entityYaw,
			float partialTicks)
	{
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder wr = tes.getBuffer();
		GlStateManager.enableRescaleNormal();
		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y+1, z);
		GlStateManager.rotate(entityYaw+90, 0, 1, 0);
		GlStateManager.disableTexture2D();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, .03125);
		GlStateManager.rotate(entity.angleHorizontal, 1, 0, 0);
		GlStateManager.translate(0, -entity.tubeLength/2, 0);
		drawTube(entity.active, entity.rgb, entity.tubeLength, wr, tes);
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
		GlStateManager.translate(-0.25, -1, 0);
		GlStateManager.color(1, 1, 1);
		if (tex==null)
			tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/iron_block");

		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		ClientUtils.renderTexturedBox(wr, 0, 0, 0, .0625, 1, .0625, tex.getMinU(), tex.getMinV(), tex.getMaxU(), tex.getMaxV());
		ClientUtils.renderTexturedBox(wr, .0625, .9375, 0, .25, 1, .0625, tex.getMinU(), tex.getMinV(), tex.getMaxU(), tex.getMaxV());
		tes.draw();

		GlStateManager.popMatrix();
	}

	static void drawTube(boolean active, float[] rgb, double length, BufferBuilder wr, Tessellator tes)
	{
		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.enableRescaleNormal();
		GlStateManager.scale(.0625*length, 1, .0625*length);
		boolean wasLightmapEnabled, wasLightingEnabled;
		{
			GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			wasLightmapEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		}
		wasLightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
		if (wasLightingEnabled)
			GlStateManager.disableLighting();
		if (active&&wasLightmapEnabled)
			ClientUtils.setLightmapDisabled(true);
		if (rgb!=null&&rgb.length>=3)
		{
			float min = .6F;
			float mult = min+(active?r.nextFloat()*(1-min):0);
			GlStateManager.color(rgb[0]*mult, rgb[1]*mult, rgb[2]*mult);
		}
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		//sides
		for (int i = 0;i<8;i++)
		{
			wr.pos(octagon[i][0], length, octagon[i][1]).endVertex();
			wr.pos(octagon[(i+1)%8][0], length, octagon[(i+1)%8][1]).endVertex();
			wr.pos(octagon[(i+1)%8][0], 0, octagon[(i+1)%8][1]).endVertex();
			wr.pos(octagon[i][0], 0, octagon[i][1]).endVertex();
		}
		tes.draw();
		if (wasLightingEnabled)
			GlStateManager.enableLighting();
		if (wasLightmapEnabled)
			ClientUtils.setLightmapDisabled(false);
		//caps
		GlStateManager.color(0, 0, 0);
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		wr.pos(octagon[0][0], 0, octagon[0][1]).endVertex();
		wr.pos(octagon[1][0], 0, octagon[1][1]).endVertex();
		wr.pos(octagon[2][0], 0, octagon[2][1]).endVertex();
		wr.pos(octagon[3][0], 0, octagon[3][1]).endVertex();

		wr.pos(octagon[3][0], 0, octagon[3][1]).endVertex();
		wr.pos(octagon[4][0], 0, octagon[4][1]).endVertex();
		wr.pos(octagon[7][0], 0, octagon[7][1]).endVertex();
		wr.pos(octagon[0][0], 0, octagon[0][1]).endVertex();

		wr.pos(octagon[4][0], 0, octagon[4][1]).endVertex();
		wr.pos(octagon[5][0], 0, octagon[5][1]).endVertex();
		wr.pos(octagon[6][0], 0, octagon[6][1]).endVertex();
		wr.pos(octagon[7][0], 0, octagon[7][1]).endVertex();


		wr.pos(octagon[3][0], length, octagon[3][1]).endVertex();
		wr.pos(octagon[2][0], length, octagon[2][1]).endVertex();
		wr.pos(octagon[1][0], length, octagon[1][1]).endVertex();
		wr.pos(octagon[0][0], length, octagon[0][1]).endVertex();

		wr.pos(octagon[0][0], length, octagon[0][1]).endVertex();
		wr.pos(octagon[7][0], length, octagon[7][1]).endVertex();
		wr.pos(octagon[4][0], length, octagon[4][1]).endVertex();
		wr.pos(octagon[3][0], length, octagon[3][1]).endVertex();

		wr.pos(octagon[7][0], length, octagon[7][1]).endVertex();
		wr.pos(octagon[6][0], length, octagon[6][1]).endVertex();
		wr.pos(octagon[5][0], length, octagon[5][1]).endVertex();
		wr.pos(octagon[4][0], length, octagon[4][1]).endVertex();

		tes.draw();
		GlStateManager.color(1, 1, 1);
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}
}
