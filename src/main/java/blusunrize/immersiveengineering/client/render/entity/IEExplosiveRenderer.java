/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.common.entities.IEExplosiveEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class IEExplosiveRenderer extends EntityRenderer<IEExplosiveEntity>
{
	public IEExplosiveRenderer(EntityRendererManager renderManager)
	{
		super(renderManager);
		this.shadowSize = .5f;
	}

	@Override
	public void render(IEExplosiveEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		if(entity.block==null)
			return;
		BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		matrixStackIn.push();
		matrixStackIn.translate(0, 0.5F, 0);
		if(entity.getFuse()-partialTicks+1 < 10)
		{
			float f = 1.0F-((float)entity.getFuse()-partialTicks+1.0F)/10.0F;
			f = MathHelper.clamp(f, 0.0F, 1.0F);
			f = f*f;
			f = f*f;
			float f1 = 1.0F+f*0.3F;
			matrixStackIn.scale(f1, f1, f1);
		}

		int overlay;
		if(entity.getFuse()/5%2==0)
		{
			overlay = OverlayTexture.getPackedUV(OverlayTexture.getU(1.0F), 10);
		}
		else
		{
			overlay = OverlayTexture.NO_OVERLAY;
		}
		blockrendererdispatcher.renderBlock(entity.block, matrixStackIn, bufferIn, packedLightIn, overlay);

		matrixStackIn.pop();
		super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getEntityTexture(IEExplosiveEntity entity)
	{
		return PlayerContainer.LOCATION_BLOCKS_TEXTURE;
	}
}