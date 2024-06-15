/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Quaternionf;

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
		builder.setDefaultNormal(0, 1, 0);
		builder.setDefaultLight(packedLightIn);
		builder.setDefaultOverlay(OverlayTexture.NO_OVERLAY);
		builder.addVertex(-.25f, -.25f, 0)
				.setUv(sprite.getU(0.25f), sprite.getV(0.25f));
		builder.addVertex(.25f, -.25f, 0)
				.setUv(sprite.getU(0), sprite.getV(0.25f));
		builder.addVertex(.25f, .25f, 0)
				.setUv(sprite.getU(0), sprite.getV(0));
		builder.addVertex(-.25f, .25f, 0)
				.setUv(sprite.getU(0.25f), sprite.getV(0));
		matrixStackIn.popPose();
	}

	@Override
	@Nonnull
	public ResourceLocation getTextureLocation(@Nonnull ChemthrowerShotEntity chemthrowerShotEntity)
	{
		return IEApi.ieLoc("textures/models/bullet.png");
	}

}
