/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import blusunrize.immersiveengineering.dummy.GlStateManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class ChemthrowerShotRenderer extends EntityRenderer<ChemthrowerShotEntity>
{
	public ChemthrowerShotRenderer(EntityRendererManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public void render(ChemthrowerShotEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		FluidStack f = entity.getFluid();
		if(f==null)
		{
			f = entity.getFluidSynced();
			if(f==null)
				return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderHelper.disableStandardItemLighting();

		Tessellator tessellator = ClientUtils.tes();

		GlStateManager.rotatef(180.0F-this.renderManager.info.getYaw(), 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(-this.renderManager.info.getPitch(), 1.0F, 0.0F, 0.0F);

		TextureAtlasSprite sprite = ClientUtils.mc().getTextureMap().getAtlasSprite(f.getFluid().getAttributes().getStillTexture(f).toString());
		int colour = f.getFluid().getAttributes().getColor(f);
		float a = (colour >> 24&255)/255f;
		float r = (colour >> 16&255)/255f;
		float g = (colour >> 8&255)/255f;
		float b = (colour&255)/255f;
		ClientUtils.bindAtlas();
		int lightAll = entity.getBrightnessForRender();
		int lightA = (lightAll >> 0x10)&0xffff;
		int lightB = lightAll&0xffff;
		GlStateManager.scalef(.25f, .25f, .25f);
		BufferBuilder worldrenderer = ClientUtils.tes().getBuffer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		worldrenderer.pos(-.25, -.25, 0).tex(sprite.getInterpolatedU(4), sprite.getInterpolatedV(4)).lightmap(lightA, lightB).color(r, g, b, a).endVertex();
		worldrenderer.pos(.25, -.25, 0).tex(sprite.getInterpolatedU(0), sprite.getInterpolatedV(4)).lightmap(lightA, lightB).color(r, g, b, a).endVertex();
		worldrenderer.pos(.25, .25, 0).tex(sprite.getInterpolatedU(0), sprite.getInterpolatedV(0)).lightmap(lightA, lightB).color(r, g, b, a).endVertex();
		worldrenderer.pos(-.25, .25, 0).tex(sprite.getInterpolatedU(4), sprite.getInterpolatedV(0)).lightmap(lightA, lightB).color(r, g, b, a).endVertex();
		tessellator.draw();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}

	@Override
	@Nonnull
	public ResourceLocation getEntityTexture(@Nonnull ChemthrowerShotEntity chemthrowerShotEntity)
	{
		return new ResourceLocation("immersiveengineering:textures/models/bullet.png");
	}

}
