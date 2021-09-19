/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.fluids.IEFluid;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import java.util.List;

import static blusunrize.immersiveengineering.api.client.TextUtils.applyFormat;
import static blusunrize.immersiveengineering.client.ClientUtils.getSprite;
import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class GuiHelper
{
	public static void drawColouredRect(int x, int y, int w, int h, int colour, MultiBufferSource buffers,
										PoseStack transform)
	{
		Matrix4f mat = transform.last().pose();
		VertexConsumer worldrenderer = buffers.getBuffer(IERenderTypes.TRANSLUCENT_POSITION_COLOR);
		worldrenderer.vertex(mat, x, y+h, 0).color(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255).endVertex();
		worldrenderer.vertex(mat, x+w, y+h, 0).color(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255).endVertex();
		worldrenderer.vertex(mat, x+w, y, 0).color(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255).endVertex();
		worldrenderer.vertex(mat, x, y, 0).color(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255).endVertex();
	}

	public static void drawTexturedColoredRect(
			VertexConsumer builder, PoseStack transform,
			float x, float y, float w, float h,
			float r, float g, float b, float alpha,
			float u0, float u1, float v0, float v1
	)
	{
		TransformingVertexBuilder innerBuilder = new TransformingVertexBuilder(builder, transform);
		innerBuilder.setColor(r, g, b, alpha);
		innerBuilder.setLight(LightTexture.pack(15, 15));
		innerBuilder.setOverlay(OverlayTexture.NO_OVERLAY);
		innerBuilder.setNormal(1, 1, 1);
		innerBuilder.vertex(x, y+h, 0).uv(u0, v1).endVertex();
		innerBuilder.vertex(x+w, y+h, 0).uv(u1, v1).endVertex();
		innerBuilder.vertex(x+w, y, 0).uv(u1, v0).endVertex();
		innerBuilder.vertex(x, y, 0).uv(u0, v0).endVertex();
	}

	public static void drawTexturedRect(VertexConsumer builder, PoseStack transform, int x, int y, int w, int h, float picSize,
										int u0, int u1, int v0, int v1)
	{
		drawTexturedColoredRect(builder, transform, x, y, w, h, 1, 1, 1, 1, u0/picSize, u1/picSize, v0/picSize, v1/picSize);
	}

	public static void drawRepeatedFluidSpriteGui(MultiBufferSource buffer, PoseStack transform, FluidStack fluid, float x, float y, float w, float h)
	{
		RenderType renderType = IERenderTypes.getGui(InventoryMenu.BLOCK_ATLAS);
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

	public static void handleGuiTank(PoseStack transform, IFluidTank tank, int x, int y, int w, int h, int oX, int oY, int oW, int oH, int mX, int mY, ResourceLocation originalTexture, List<Component> tooltip)
	{
		handleGuiTank(transform, tank.getFluid(), tank.getCapacity(), x, y, w, h, oX, oY, oW, oH, mX, mY, originalTexture, tooltip);
	}

	public static void handleGuiTank(PoseStack transform, FluidStack fluid, int capacity, int x, int y, int w, int h, int oX, int oY, int oW, int oH, int mX, int mY, ResourceLocation originalTexture, List<Component> tooltip)
	{
		if(tooltip==null)
		{
			transform.pushPose();
			MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			if(fluid!=null&&fluid.getFluid()!=null)
			{
				int fluidHeight = (int)(h*(fluid.getAmount()/(float)capacity));
				drawRepeatedFluidSpriteGui(buffer, transform, fluid, x, y+h-fluidHeight, w, fluidHeight);
				RenderSystem.color3f(1, 1, 1);
			}
			int xOff = (w-oW)/2;
			int yOff = (h-oH)/2;
			RenderType renderType = IERenderTypes.getGui(originalTexture);
			drawTexturedRect(buffer.getBuffer(renderType), transform, x+xOff, y+yOff, oW, oH, 256f, oX, oX+oW, oY, oY+oH);
			buffer.endBatch(renderType);
			transform.popPose();
		}
		else
		{
			if(mX >= x&&mX < x+w&&mY >= y&&mY < y+h)
				addFluidTooltip(fluid, tooltip, capacity);
		}
	}

	public static void addFluidTooltip(FluidStack fluid, List<Component> tooltip, int tankCapacity)
	{
		if(!fluid.isEmpty())
			tooltip.add(applyFormat(
					fluid.getDisplayName(),
					fluid.getFluid().getAttributes().getRarity(fluid).color
			));
		else
			tooltip.add(new TranslatableComponent("gui.immersiveengineering.empty"));
		if(fluid.getFluid() instanceof IEFluid)
			((IEFluid)fluid.getFluid()).addTooltipInfo(fluid, null, tooltip);

		if(mc().options.advancedItemTooltips&&!fluid.isEmpty())
		{
			if(!Screen.hasShiftDown())
				tooltip.add(new TranslatableComponent(Lib.DESC_INFO+"holdShiftForInfo"));
			else
			{
				//TODO translation keys
				tooltip.add(applyFormat(new TextComponent("Fluid Registry: "+fluid.getFluid().getRegistryName()), ChatFormatting.DARK_GRAY));
				tooltip.add(applyFormat(new TextComponent("Density: "+fluid.getFluid().getAttributes().getDensity(fluid)), ChatFormatting.DARK_GRAY));
				tooltip.add(applyFormat(new TextComponent("Temperature: "+fluid.getFluid().getAttributes().getTemperature(fluid)), ChatFormatting.DARK_GRAY));
				tooltip.add(applyFormat(new TextComponent("Viscosity: "+fluid.getFluid().getAttributes().getViscosity(fluid)), ChatFormatting.DARK_GRAY));
				tooltip.add(applyFormat(new TextComponent("NBT Data: "+fluid.getTag()), ChatFormatting.DARK_GRAY));
			}
		}

		if(tankCapacity > 0)
			tooltip.add(applyFormat(new TextComponent(fluid.getAmount()+"/"+tankCapacity+"mB"), ChatFormatting.GRAY));
		else
			tooltip.add(applyFormat(new TextComponent(fluid.getAmount()+"mB"), ChatFormatting.GRAY));
	}

	public static void renderItemWithOverlayIntoGUI(MultiBufferSource buffer, PoseStack transform,
													ItemStack stack, int x, int y)
	{
		buffer = IERenderTypes.disableLighting(buffer);
		transform.pushPose();
		transform.translate(x, y, 100);
		transform.pushPose();
		transform.translate(8, 8, 0);
		transform.scale(1, -1, 1);
		transform.scale(16, 16, 16);
		BatchingRenderTypeBuffer batchBuffer = new BatchingRenderTypeBuffer();
		mc().getItemRenderer().renderStatic(stack, TransformType.GUI, 0xf000f0, OverlayTexture.NO_OVERLAY,
				transform, batchBuffer);
		batchBuffer.pipe(buffer);
		transform.popPose();
		renderDurabilityBar(stack, buffer, transform);
		transform.popPose();
	}

	public static void renderDurabilityBar(ItemStack stack, MultiBufferSource buffer, PoseStack transform)
	{
		if(!stack.isEmpty()&&stack.getItem().showDurabilityBar(stack))
		{
			double health = stack.getItem().getDurabilityForDisplay(stack);
			int i = Math.round(13.0F-(float)health*13.0F);
			int j = stack.getItem().getRGBDurabilityForDisplay(stack);
			draw(transform, buffer, 2, 13, 13, 2, 0, 0, 0);
			draw(transform, buffer, 2, 13, i, 1, j >> 16&255, j >> 8&255, j&255);
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
