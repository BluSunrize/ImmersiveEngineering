/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import static blusunrize.immersiveengineering.client.ClientUtils.getSprite;
import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class GuiHelper
{
	public static void drawColouredRect(int x, int y, int w, int h, int colour, MultiBufferSource buffers, PoseStack transform)
	{
		Matrix4f mat = transform.last().pose();
		VertexConsumer worldrenderer = buffers.getBuffer(IERenderTypes.TRANSLUCENT_POSITION_COLOR);
		worldrenderer.defaultColor(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255);
		worldrenderer.vertex(mat, x, y+h, 0).endVertex();
		worldrenderer.vertex(mat, x+w, y+h, 0).endVertex();
		worldrenderer.vertex(mat, x+w, y, 0).endVertex();
		worldrenderer.vertex(mat, x, y, 0).endVertex();
		worldrenderer.unsetDefaultColor();
	}

	public static void drawColouredRect(PoseStack transform, int x, int y, int w, int h, DyeColor dyeColor)
	{
		Matrix4f mat = transform.last().pose();
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		float[] color = dyeColor.getTextureDiffuseColors();
		bufferbuilder.vertex(mat, x, y+h, 0).color(color[0], color[1], color[2], 1).endVertex();
		bufferbuilder.vertex(mat, x+w, y+h, 0).color(color[0], color[1], color[2], 1).endVertex();
		bufferbuilder.vertex(mat, x+w, y, 0).color(color[0], color[1], color[2], 1).endVertex();
		bufferbuilder.vertex(mat, x, y, 0).color(color[0], color[1], color[2], 1).endVertex();
		bufferbuilder.unsetDefaultColor();
		bufferbuilder.end();
		BufferUploader.end(bufferbuilder);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	public static void drawTexturedColoredRect(
			VertexConsumer builder, PoseStack transform,
			float x, float y, float w, float h,
			float r, float g, float b, float alpha,
			float u0, float u1, float v0, float v1
	)
	{
		TransformingVertexBuilder innerBuilder = new TransformingVertexBuilder(builder, transform, DefaultVertexFormat.BLOCK);
		innerBuilder.defaultColor((int)(255*r), (int)(255*g), (int)(255*b), (int)(255*alpha));
		innerBuilder.setLight(LightTexture.pack(15, 15));
		innerBuilder.setOverlay(OverlayTexture.NO_OVERLAY);
		innerBuilder.setNormal(1, 1, 1);
		innerBuilder.vertex(x, y+h, 0).uv(u0, v1).endVertex();
		innerBuilder.vertex(x+w, y+h, 0).uv(u1, v1).endVertex();
		innerBuilder.vertex(x+w, y, 0).uv(u1, v0).endVertex();
		innerBuilder.vertex(x, y, 0).uv(u0, v0).endVertex();
		innerBuilder.unsetDefaultColor();
	}

	public static void drawTexturedRect(VertexConsumer builder, PoseStack transform, int x, int y, int w, int h, float picSize,
										int u0, int u1, int v0, int v1)
	{
		drawTexturedColoredRect(builder, transform, x, y, w, h, 1, 1, 1, 1, u0/picSize, u1/picSize, v0/picSize, v1/picSize);
	}

	public static void drawRepeatedFluidSpriteGui(MultiBufferSource buffer, PoseStack transform, FluidStack fluid, float x, float y, float w, float h)
	{
		RenderType renderType = IERenderTypes.getGuiTranslucent(InventoryMenu.BLOCK_ATLAS);
		VertexConsumer builder = buffer.getBuffer(renderType);
		drawRepeatedFluidSprite(builder, transform, fluid, x, y, w, h);
	}

	public static void drawRepeatedFluidSprite(VertexConsumer builder, PoseStack transform, FluidStack fluid, float x, float y, float w, float h)
	{
		TextureAtlasSprite sprite = getSprite(fluid.getFluid().getAttributes().getStillTexture(fluid));
		int col = fluid.getFluid().getAttributes().getColor(fluid);
		int iW = sprite.getWidth();
		int iH = sprite.getHeight();
		if(iW > 0&&iH > 0)
			drawRepeatedSprite(builder, transform, x, y, w, h, iW, iH,
					sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(),
					(col >> 16&255)/255.0f, (col >> 8&255)/255.0f, (col&255)/255.0f, 1);
	}

	public static void drawRepeatedSprite(VertexConsumer builder, PoseStack transform, float x, float y, float w,
										  float h, int iconWidth, int iconHeight, float uMin, float uMax, float vMin, float vMax,
										  float r, float g, float b, float alpha)
	{
		int iterMaxW = (int)(w/iconWidth);
		int iterMaxH = (int)(h/iconHeight);
		float leftoverW = w%iconWidth;
		float leftoverH = h%iconHeight;
		float leftoverWf = leftoverW/(float)iconWidth;
		float leftoverHf = leftoverH/(float)iconHeight;
		float iconUDif = uMax-uMin;
		float iconVDif = vMax-vMin;
		for(int ww = 0; ww < iterMaxW; ww++)
		{
			for(int hh = 0; hh < iterMaxH; hh++)
				drawTexturedColoredRect(builder, transform, x+ww*iconWidth, y+hh*iconHeight, iconWidth, iconHeight,
						r, g, b, alpha, uMin, uMax, vMin, vMax);
			drawTexturedColoredRect(builder, transform, x+ww*iconWidth, y+iterMaxH*iconHeight, iconWidth, leftoverH,
					r, g, b, alpha, uMin, uMax, vMin, (vMin+iconVDif*leftoverHf));
		}
		if(leftoverW > 0)
		{
			for(int hh = 0; hh < iterMaxH; hh++)
				drawTexturedColoredRect(builder, transform, x+iterMaxW*iconWidth, y+hh*iconHeight, leftoverW, iconHeight,
						r, g, b, alpha, uMin, (uMin+iconUDif*leftoverWf), vMin, vMax);
			drawTexturedColoredRect(builder, transform, x+iterMaxW*iconWidth, y+iterMaxH*iconHeight, leftoverW, leftoverH,
					r, g, b, alpha, uMin, (uMin+iconUDif*leftoverWf), vMin, (vMin+iconVDif*leftoverHf));
		}
	}

	public static void drawSlot(int x, int y, int w, int h, PoseStack transform)
	{
		drawSlot(x, y, w, h, 0xff, transform);
	}

	public static void drawSlot(PoseStack transform, int x, int y, int w, int h, int dark, int main, int light)
	{
		final int minX = x+8-w/2;
		final int minY = y+8-h/2;
		final int maxX = minX+w;
		final int maxY = minY+h;
		GuiComponent.fill(transform, minX, minY-1, maxX, minY, dark);
		GuiComponent.fill(transform, minX-1, minY-1, minX, maxY, dark);
		GuiComponent.fill(transform, minX, minY, maxX, maxY, main);
		GuiComponent.fill(transform, minX, maxY, maxX+1, maxY+1, light);
		GuiComponent.fill(transform, maxX, minY, maxX+1, maxY, light);
	}

	public static void drawSlot(int x, int y, int w, int h, int alpha, PoseStack transform)
	{
		drawSlot(transform, x, y, w, h, (alpha<<24)|0x373737, (alpha<<24)|0x8b8b8b, (alpha<<24)|0xffffff);
	}

	public static void drawDarkSlot(PoseStack transform, int x, int y, int w, int h)
	{
		drawSlot(transform, x, y, w, h, 0x77222222, 0x77111111, 0x77999999);
	}

	public static void renderItemWithOverlayIntoGUI(MultiBufferSource buffer, PoseStack transform,
													ItemStack stack, int x, int y)
	{
		ItemRenderer itemRenderer = mc().getItemRenderer();
		BakedModel bakedModel = itemRenderer.getModel(stack, null, mc().player, 0);
		if(!bakedModel.usesBlockLight())
			Lighting.setupForFlatItems();
		transform.pushPose();
		transform.translate(x, y, 100);
		transform.pushPose();
		transform.translate(8, 8, 0);
		transform.scale(1, -1, 1);
		transform.scale(16, 16, 16);
		BatchingRenderTypeBuffer batchBuffer = new BatchingRenderTypeBuffer();
		itemRenderer.renderStatic(stack, TransformType.GUI, 0xf000f0, OverlayTexture.NO_OVERLAY,
				transform, batchBuffer, 0);
		batchBuffer.pipe(buffer);
		transform.popPose();
		renderDurabilityBar(stack, buffer, transform);
		transform.popPose();
	}

	public static void renderDurabilityBar(ItemStack stack, MultiBufferSource buffer, PoseStack transform)
	{
		if(!stack.isEmpty()&&stack.getItem().isBarVisible(stack))
		{
			int width = stack.getItem().getBarWidth(stack);
			int color = stack.getItem().getBarColor(stack);
			draw(transform, buffer, 2, 13, 13, 2, 0, 0, 0);
			draw(transform, buffer, 2, 13, width, 1, (color >> 16)&255, (color >> 8)&255, color&255);
		}
	}

	private static void draw(PoseStack transform, MultiBufferSource buffer, int x, int y, int width, int height, int red, int green, int blue)
	{
		VertexConsumer builder = buffer.getBuffer(IERenderTypes.ITEM_DAMAGE_BAR);
		transform.pushPose();
		transform.translate(x, y, 0);
		Matrix4f mat = transform.last().pose();
		builder.vertex(mat, 0, 0, 0).color(red, green, blue, 255).endVertex();
		builder.vertex(mat, 0, height, 0).color(red, green, blue, 255).endVertex();
		builder.vertex(mat, width, height, 0).color(red, green, blue, 255).endVertex();
		builder.vertex(mat, width, 0, 0).color(red, green, blue, 255).endVertex();
		transform.popPose();
	}
}
