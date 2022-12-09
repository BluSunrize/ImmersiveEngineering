/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
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
		if(f.isEmpty())
		{
			f = entity.getFluidSynced();
			if(f.isEmpty())
				return;
		}

		matrixStackIn.pushPose();

		matrixStackIn.mulPose(new Quaternionf()
				.rotateXYZ(0.0F, (float) Math.toRadians(180.0F-this.entityRenderDispatcher.camera.getYRot()), 0)
				.rotateXYZ((float) Math.toRadians(-this.entityRenderDispatcher.camera.getXRot()), 0, 0)
		);

		IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(f.getFluid());
		TextureAtlasSprite sprite = ClientUtils.mc().getModelManager()
				.getAtlas(InventoryMenu.BLOCK_ATLAS)
				.getSprite(props.getStillTexture(f));
		int colour = props.getTintColor(f);
		float a = (colour>>24&255)/255f;
		float r = (colour>>16&255)/255f;
		float g = (colour>>8&255)/255f;
		float b = (colour&255)/255f;
		int lightAll = entity.getBrightnessForRender();
		int blockLight = Math.max(LightTexture.block(lightAll), LightTexture.block(packedLightIn));
		int skyLight = Math.max(LightTexture.sky(lightAll), LightTexture.sky(packedLightIn));
		packedLightIn = LightTexture.pack(blockLight, skyLight);
		matrixStackIn.scale(.25f, .25f, .25f);
		TransformingVertexBuilder builder = new TransformingVertexBuilder(
				bufferIn, RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS), matrixStackIn
		);
		builder.defaultColor(r, g, b, a);
		builder.setNormal(0, 1, 0);
		builder.setLight(packedLightIn);
		builder.setOverlay(OverlayTexture.NO_OVERLAY);
		builder.vertex(-.25f, -.25f, 0)
				.uv(sprite.getU(4), sprite.getV(4))
				.endVertex();
		builder.vertex(.25f, -.25f, 0)
				.uv(sprite.getU(0), sprite.getV(4))
				.endVertex();
		builder.vertex(.25f, .25f, 0)
				.uv(sprite.getU(0), sprite.getV(0))
				.endVertex();
		builder.vertex(-.25f, .25f, 0)
				.uv(sprite.getU(4), sprite.getV(0))
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
