package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.EntityChemthrowerShot;
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
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		Tessellator tessellator = ClientUtils.tes();

		GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

		TextureAtlasSprite sprite = ClientUtils.mc().getTextureMapBlocks().getAtlasSprite(((EntityChemthrowerShot)entity).getFluid().getStill().toString());
		if(sprite!=null)
		{
			ClientUtils.bindAtlas();
			GL11.glScalef(.25f, .25f, .25f);
			WorldRenderer worldrenderer = ClientUtils.tes().getWorldRenderer();
			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			
			worldrenderer.pos(-.25,-.25, 0).tex(sprite.getInterpolatedU(4), sprite.getInterpolatedV(4)).endVertex();
			worldrenderer.pos( .25,-.25, 0).tex(sprite.getInterpolatedU(0), sprite.getInterpolatedV(4)).endVertex();
			worldrenderer.pos( .25, .25, 0).tex(sprite.getInterpolatedU(0), sprite.getInterpolatedV(0)).endVertex();
			worldrenderer.pos(-.25, .25, 0).tex(sprite.getInterpolatedU(4), sprite.getInterpolatedV(0)).endVertex();
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
