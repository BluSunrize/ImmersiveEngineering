/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.EntityChemthrowerShot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class EntityRenderChemthrowerShot extends Render
{
	public EntityRenderChemthrowerShot(RenderManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1)
	{
		FluidStack f = ((EntityChemthrowerShot)entity).getFluid();
		if(f==null)
		{
			f = ((EntityChemthrowerShot)entity).getFluidSynced();
			if(f==null)
				return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderHelper.disableStandardItemLighting();

		Tessellator tessellator = ClientUtils.tes();

		GlStateManager.rotate(180.0F-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

		TextureAtlasSprite sprite = ClientUtils.mc().getTextureMapBlocks().getAtlasSprite(f.getFluid().getStill(f).toString());
		if(sprite!=null)
		{
			int colour = f.getFluid().getColor(f);
			float a = (colour >> 24&255)/255f;
			float r = (colour >> 16&255)/255f;
			float g = (colour >> 8&255)/255f;
			float b = (colour&255)/255f;
			ClientUtils.bindAtlas();
			int lightAll = entity.getBrightnessForRender();
			int lightA = (lightAll >> 0x10)&0xffff;
			int lightB = lightAll&0xffff;
			GlStateManager.scale(.25f, .25f, .25f);
			BufferBuilder worldrenderer = ClientUtils.tes().getBuffer();
			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
			worldrenderer.pos(-.25, -.25, 0).tex(sprite.getInterpolatedU(4), sprite.getInterpolatedV(4)).lightmap(lightA, lightB).color(r, g, b, a).endVertex();
			worldrenderer.pos(.25, -.25, 0).tex(sprite.getInterpolatedU(0), sprite.getInterpolatedV(4)).lightmap(lightA, lightB).color(r, g, b, a).endVertex();
			worldrenderer.pos(.25, .25, 0).tex(sprite.getInterpolatedU(0), sprite.getInterpolatedV(0)).lightmap(lightA, lightB).color(r, g, b, a).endVertex();
			worldrenderer.pos(-.25, .25, 0).tex(sprite.getInterpolatedU(4), sprite.getInterpolatedV(0)).lightmap(lightA, lightB).color(r, g, b, a).endVertex();
			tessellator.draw();
		}
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_)
	{
		return new ResourceLocation("immersiveengineering:textures/models/bullet.png");
	}

}
