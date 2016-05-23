package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.EntityChemthrowerShot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class EntityRenderChemthrowerShot extends Render
{
	public EntityRenderChemthrowerShot(RenderManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1)
	{
		if(((EntityChemthrowerShot)entity).getFluid()==null)
			return;

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.enableRescaleNormal();
		Tessellator tessellator = ClientUtils.tes();

		GlStateManager.disableCull();
        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

		TextureAtlasSprite sprite = ClientUtils.mc().getTextureMapBlocks().getAtlasSprite(((EntityChemthrowerShot)entity).getFluid().getStill().toString());
		if(sprite!=null)
		{
			ClientUtils.bindAtlas();
			GlStateManager.scale(.25f, .25f, .25f);
			WorldRenderer worldrenderer = ClientUtils.tes().getWorldRenderer();
			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			
			worldrenderer.pos(-.25,-.25, 0).tex(sprite.getInterpolatedU(4), sprite.getInterpolatedV(4)).endVertex();
			worldrenderer.pos( .25,-.25, 0).tex(sprite.getInterpolatedU(0), sprite.getInterpolatedV(4)).endVertex();
			worldrenderer.pos( .25, .25, 0).tex(sprite.getInterpolatedU(0), sprite.getInterpolatedV(0)).endVertex();
			worldrenderer.pos(-.25, .25, 0).tex(sprite.getInterpolatedU(4), sprite.getInterpolatedV(0)).endVertex();
			tessellator.draw();
		}
		GlStateManager.enableCull();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_)
	{
		return new ResourceLocation("immersiveengineering:textures/models/bullet.png");
	}

}
