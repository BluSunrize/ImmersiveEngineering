/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class ChemthrowerShotRenderer extends EntityRenderer<ChemthrowerShotEntity>
{
	public ChemthrowerShotRenderer(EntityRendererProvider.Context renderManager)
	{
		super(renderManager);
	}

	@Override
	public void render(ChemthrowerShotEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
		FluidStack f = entity.getFluid();
		if(f==null||f.isEmpty())
		{
			f = entity.getFluidSynced();
			if(f==null||f.isEmpty())
				return;
		}

		matrixStackIn.pushPose();

		matrixStackIn.mulPose(new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), 180.0F-this.entityRenderDispatcher.camera.getYRot(), true));
		matrixStackIn.mulPose(new Quaternion(new Vector3f(1.0F, 0.0F, 0.0F), -this.entityRenderDispatcher.camera.getXRot(), true));

		TextureAtlasSprite sprite = ClientUtils.mc().getModelManager()
				.getAtlas(InventoryMenu.BLOCK_ATLAS)
				.getSprite(f.getFluid().getAttributes().getStillTexture(f));
		int colour = f.getFluid().getAttributes().getColor(f);
		float a = (colour >> 24&255)/255f;
		float r = (colour >> 16&255)/255f;
		float g = (colour >> 8&255)/255f;
		float b = (colour&255)/255f;
		int lightAll = entity.getBrightnessForRender();
		int blockLight = Math.max(
				LightTexture.block(lightAll),
				LightTexture.block(packedLightIn)
		);
		int skyLight = Math.max(
				LightTexture.sky(lightAll),
				LightTexture.sky(packedLightIn)
		);
		packedLightIn = LightTexture.pack(blockLight, skyLight);
		matrixStackIn.scale(.25f, .25f, .25f);
		Matrix4f mat = matrixStackIn.last().pose();
		VertexConsumer builder = bufferIn.getBuffer(IERenderTypes.POSITION_COLOR_TEX_LIGHTMAP);
		builder.vertex(mat, -.25f, -.25f, 0)
				.color(r, g, b, a)
				.uv(sprite.getU(4), sprite.getV(4))
				.uv2(packedLightIn)
				.endVertex();
		builder.vertex(mat, .25f, -.25f, 0)
				.color(r, g, b, a)
				.uv(sprite.getU(0), sprite.getV(4))
				.uv2(packedLightIn)
				.endVertex();
		builder.vertex(mat, .25f, .25f, 0)
				.color(r, g, b, a)
				.uv(sprite.getU(0), sprite.getV(0))
				.uv2(packedLightIn)
				.endVertex();
		builder.vertex(mat, -.25f, .25f, 0)
				.color(r, g, b, a)
				.uv(sprite.getU(4), sprite.getV(0))
				.uv2(packedLightIn)
				.endVertex();
		matrixStackIn.popPose();
	}

	@Override
	@Nonnull
	public ResourceLocation getTextureLocation(@Nonnull ChemthrowerShotEntity chemthrowerShotEntity)
	{
		return new ResourceLocation("immersiveengineering:textures/models/bullet.png");
	}

}
