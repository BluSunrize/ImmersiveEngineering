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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fluids.FluidStack;

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

		matrixStackIn.push();

		matrixStackIn.rotate(new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), 180.0F-this.renderManager.info.getYaw(), true));
		matrixStackIn.rotate(new Quaternion(new Vector3f(1.0F, 0.0F, 0.0F), -this.renderManager.info.getPitch(), true));

		TextureAtlasSprite sprite = ClientUtils.mc().getModelManager()
				.getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
				.getSprite(f.getFluid().getAttributes().getStillTexture(f));
		int colour = f.getFluid().getAttributes().getColor(f);
		float a = (colour >> 24&255)/255f;
		float r = (colour >> 16&255)/255f;
		float g = (colour >> 8&255)/255f;
		float b = (colour&255)/255f;
		int lightAll = entity.getBrightnessForRender();
		int blockLight = Math.max(
				LightTexture.getLightBlock(lightAll),
				LightTexture.getLightBlock(packedLightIn)
		);
		int skyLight = Math.max(
				LightTexture.getLightSky(lightAll),
				LightTexture.getLightSky(packedLightIn)
		);
		packedLightIn = LightTexture.packLight(blockLight, skyLight);
		matrixStackIn.scale(.25f, .25f, .25f);
		Matrix4f mat = matrixStackIn.getLast().getMatrix();
		IVertexBuilder builder = bufferIn.getBuffer(IERenderTypes.POSITION_COLOR_TEX_LIGHTMAP);
		builder.pos(mat, -.25f, -.25f, 0)
				.color(r, g, b, a)
				.tex(sprite.getInterpolatedU(4), sprite.getInterpolatedV(4))
				.lightmap(packedLightIn)
				.endVertex();
		builder.pos(mat, .25f, -.25f, 0)
				.color(r, g, b, a)
				.tex(sprite.getInterpolatedU(0), sprite.getInterpolatedV(4))
				.lightmap(packedLightIn)
				.endVertex();
		builder.pos(mat, .25f, .25f, 0)
				.color(r, g, b, a)
				.tex(sprite.getInterpolatedU(0), sprite.getInterpolatedV(0))
				.lightmap(packedLightIn)
				.endVertex();
		builder.pos(mat, -.25f, .25f, 0)
				.color(r, g, b, a)
				.tex(sprite.getInterpolatedU(4), sprite.getInterpolatedV(0))
				.lightmap(packedLightIn)
				.endVertex();
		matrixStackIn.pop();
	}

	@Override
	@Nonnull
	public ResourceLocation getEntityTexture(@Nonnull ChemthrowerShotEntity chemthrowerShotEntity)
	{
		return new ResourceLocation("immersiveengineering:textures/models/bullet.png");
	}

}
