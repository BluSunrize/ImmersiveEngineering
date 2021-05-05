/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.entities.FluorescentTubeEntity;
import blusunrize.immersiveengineering.common.items.FluorescentTubeItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;

public class FluorescentTubeRenderer extends EntityRenderer<FluorescentTubeEntity>
{
	static double sqrt2Half = Math.sqrt(2)/2;
	public static final double[][] octagon = {
			{1, 0}, {sqrt2Half, sqrt2Half}, {0, 1}, {-sqrt2Half, sqrt2Half},
			{-1, 0}, {-sqrt2Half, -sqrt2Half}, {0, -1}, {sqrt2Half, -sqrt2Half}
	};
	TextureAtlasSprite tex;

	public FluorescentTubeRenderer(EntityRendererManager renderManager)
	{
		super(renderManager);
		shadowOpaque = 0;
		shadowSize = 0;
	}

	@Override
	public ResourceLocation getEntityTexture(FluorescentTubeEntity entity)
	{
		return null;
	}

	@Override
	public void render(FluorescentTubeEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		matrixStackIn.push();
		matrixStackIn.translate(0, 1, 0);
		matrixStackIn.rotate(new Quaternion(0, entityYaw+90, 0, true));
		matrixStackIn.push();
		matrixStackIn.translate(0, 0, .03125);
		matrixStackIn.rotate(new Quaternion(entity.angleHorizontal, 0, 0, true));
		matrixStackIn.translate(0, -entity.TUBE_LENGTH/2, 0);
		drawTube(entity.active, entity.rgb, matrixStackIn, bufferIn, packedLightIn, 0);
		matrixStackIn.pop();
		matrixStackIn.translate(-0.25, -1, 0);
		if(tex==null)
			tex = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
					.apply(new ResourceLocation("minecraft:block/iron_block"));

		IVertexBuilder builder = bufferIn.getBuffer(IERenderTypes.getPositionTex(PlayerContainer.LOCATION_BLOCKS_TEXTURE));
		RenderUtils.renderTexturedBox(builder, matrixStackIn,
				0, 0, 0,
				.0625F, 1, .0625F,
				tex.getMinU(), tex.getMinV(), tex.getMaxU(), tex.getMaxV());
		RenderUtils.renderTexturedBox(builder, matrixStackIn,
				.0625F, .9375F, 0,
				.25F, 1, .0625F,
				tex.getMinU(), tex.getMinV(), tex.getMaxU(), tex.getMaxV());

		matrixStackIn.pop();
	}

	private static ItemStack tube = ItemStack.EMPTY;
	private static ItemStack tubeActive = ItemStack.EMPTY;

	static void drawTube(boolean active, float[] rgb, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light,
						 int overlay)
	{
		if(tube.isEmpty())
			tube = new ItemStack(Misc.fluorescentTube);
		if(tubeActive.isEmpty())
		{
			tubeActive = new ItemStack(Misc.fluorescentTube);
			FluorescentTubeItem.setLit(tubeActive, 1);
		}
		matrixStack.translate(-.5, .25, -.5);
		ItemStack renderStack = active?tubeActive: tube;
		FluorescentTubeItem.setRGB(renderStack, rgb);
		IEOBJItemRenderer.INSTANCE.func_239207_a_(renderStack, TransformType.FIXED, matrixStack, buffer, light, overlay);
	}
}
