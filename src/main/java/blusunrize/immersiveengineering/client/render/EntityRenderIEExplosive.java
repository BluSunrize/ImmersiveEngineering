/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.common.entities.EntityIEExplosive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class EntityRenderIEExplosive extends Render<EntityIEExplosive>
{
	public EntityRenderIEExplosive(RenderManager renderManager)
	{
		super(renderManager);
		this.shadowSize = .5f;
	}

	@Override
	public void doRender(EntityIEExplosive entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		if(entity.block==null)
			return;
		BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x, (float)y+0.5F, (float)z);

		if(entity.getFuse()-partialTicks+1 < 10)
		{
			float f = 1.0F-((float)entity.getFuse()-partialTicks+1.0F)/10.0F;
			f = MathHelper.clamp(f, 0.0F, 1.0F);
			f = f*f;
			f = f*f;
			float f1 = 1.0F+f*0.3F;
			GlStateManager.scale(f1, f1, f1);
		}

		float f2 = (1-(entity.getFuse()-partialTicks+1)/100F)*.8F;
		this.bindEntityTexture(entity);
		GlStateManager.translate(-0.5F, -0.5F, 0.5F);
		blockrendererdispatcher.renderBlockBrightness(entity.block, entity.getBrightness());
		GlStateManager.translate(0.0F, 0.0F, 1.0F);

		if(entity.getFuse()/5%2==0)
		{
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(770, 772);
			GlStateManager.color(1.0F, 1.0F, 1.0F, f2);
			GlStateManager.doPolygonOffset(-3.0F, -3.0F);
			GlStateManager.enablePolygonOffset();
			blockrendererdispatcher.renderBlockBrightness(entity.block, 1.0F);
			GlStateManager.doPolygonOffset(0.0F, 0.0F);
			GlStateManager.disablePolygonOffset();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
		}

		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityIEExplosive entity)
	{
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}
}