/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.common.entities.IEExplosiveEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

public class IEExplosiveRenderer extends EntityRenderer<IEExplosiveEntity>
{
	public IEExplosiveRenderer(EntityRenderDispatcher renderManager)
	{
		super(renderManager);
		this.shadowRadius = .5f;
	}

	@Override
	public void render(IEExplosiveEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
		if(entity.block==null)
			return;
		BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, 0.5F, 0);
		if(entity.getLife()-partialTicks+1 < 10)
		{
			float f = 1.0F-((float)entity.getLife()-partialTicks+1.0F)/10.0F;
			f = Mth.clamp(f, 0.0F, 1.0F);
			f = f*f;
			f = f*f;
			float f1 = 1.0F+f*0.3F;
			matrixStackIn.scale(f1, f1, f1);
		}
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
		matrixStackIn.translate(-0.5D, -0.5D, 0.5D);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90.0F));

		int overlay;
		if(entity.getLife()/5%2==0)
			overlay = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
		else
			overlay = OverlayTexture.NO_OVERLAY;
		blockrendererdispatcher.renderSingleBlock(entity.block, matrixStackIn, bufferIn, packedLightIn, overlay);

		matrixStackIn.popPose();
		super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	@Override
	public ResourceLocation getTextureLocation(IEExplosiveEntity entity)
	{
		return InventoryMenu.BLOCK_ATLAS;
	}
}