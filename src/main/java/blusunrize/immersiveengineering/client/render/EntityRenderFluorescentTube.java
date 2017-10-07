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
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
	Random r = new Random();
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
		if (entity.active)
		{
			GlStateManager.disableRescaleNormal();
			GlStateManager.pushAttrib();
			GlStateManager.enableBlend();
			GlStateManager.disableLighting();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
		if (entity.rgb!=null&&entity.rgb.length>=3)
		{
			float mult = .5F+(entity.active?r.nextFloat()*.5F:0);
			GlStateManager.color(entity.rgb[0]*mult, entity.rgb[1]*mult, entity.rgb[2]*mult);
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y+1, z);
		GlStateManager.rotate(entityYaw+90, 0, 1, 0);
		GlStateManager.disableTexture2D();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, .03125);
		GlStateManager.rotate(entity.angleHorizontal, 1, 0, 0);
		GlStateManager.scale(.0625, 1, .0625);
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		float size = entity.tubeLength/2;
		//sides
		for (int i = 0;i<8;i++)
		{
			wr.pos(octagon[i][0], size, octagon[i][1]).endVertex();
			wr.pos(octagon[(i+1)%8][0], size, octagon[(i+1)%8][1]).endVertex();
			wr.pos(octagon[(i+1)%8][0], -size, octagon[(i+1)%8][1]).endVertex();
			wr.pos(octagon[i][0], -size, octagon[i][1]).endVertex();
		}
		tes.draw();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		if (!entity.active)
			GlStateManager.disableRescaleNormal();
		//caps
		GlStateManager.color(0, 0, 0);
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		wr.pos(octagon[0][0], -size, octagon[0][1]).endVertex();
		wr.pos(octagon[1][0], -size, octagon[1][1]).endVertex();
		wr.pos(octagon[2][0], -size, octagon[2][1]).endVertex();
		wr.pos(octagon[3][0], -size, octagon[3][1]).endVertex();

		wr.pos(octagon[3][0], -size, octagon[3][1]).endVertex();
		wr.pos(octagon[4][0], -size, octagon[4][1]).endVertex();
		wr.pos(octagon[7][0], -size, octagon[7][1]).endVertex();
		wr.pos(octagon[0][0], -size, octagon[0][1]).endVertex();

		wr.pos(octagon[4][0], -size, octagon[4][1]).endVertex();
		wr.pos(octagon[5][0], -size, octagon[5][1]).endVertex();
		wr.pos(octagon[6][0], -size, octagon[6][1]).endVertex();
		wr.pos(octagon[7][0], -size, octagon[7][1]).endVertex();


		wr.pos(octagon[3][0], size, octagon[3][1]).endVertex();
		wr.pos(octagon[2][0], size, octagon[2][1]).endVertex();
		wr.pos(octagon[1][0], size, octagon[1][1]).endVertex();
		wr.pos(octagon[0][0], size, octagon[0][1]).endVertex();

		wr.pos(octagon[0][0], size, octagon[0][1]).endVertex();
		wr.pos(octagon[7][0], size, octagon[7][1]).endVertex();
		wr.pos(octagon[4][0], size, octagon[4][1]).endVertex();
		wr.pos(octagon[3][0], size, octagon[3][1]).endVertex();

		wr.pos(octagon[7][0], size, octagon[7][1]).endVertex();
		wr.pos(octagon[6][0], size, octagon[6][1]).endVertex();
		wr.pos(octagon[5][0], size, octagon[5][1]).endVertex();
		wr.pos(octagon[4][0], size, octagon[4][1]).endVertex();

		tes.draw();
		if (entity.active)
			GlStateManager.popAttrib();
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
}
