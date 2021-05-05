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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import java.util.List;

import static blusunrize.immersiveengineering.api.client.TextUtils.applyFormat;
import static blusunrize.immersiveengineering.client.ClientUtils.getSprite;
import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class GuiHelper
{
	public static void drawColouredRect(int x, int y, int w, int h, int colour, IRenderTypeBuffer buffers,
										MatrixStack transform)
	{
		Matrix4f mat = transform.getLast().getMatrix();
		IVertexBuilder worldrenderer = buffers.getBuffer(IERenderTypes.TRANSLUCENT_POSITION_COLOR);
		worldrenderer.pos(mat, x, y+h, 0).color(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255).endVertex();
		worldrenderer.pos(mat, x+w, y+h, 0).color(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255).endVertex();
		worldrenderer.pos(mat, x+w, y, 0).color(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255).endVertex();
		worldrenderer.pos(mat, x, y, 0).color(colour >> 16&255, colour >> 8&255, colour&255, colour >> 24&255).endVertex();
	}

	public static void drawTexturedColoredRect(
			IVertexBuilder builder, MatrixStack transform,
			float x, float y, float w, float h,
			float r, float g, float b, float alpha,
			float u0, float u1, float v0, float v1
	)
	{
		TransformingVertexBuilder innerBuilder = new TransformingVertexBuilder(builder, transform);
		innerBuilder.setColor(r, g, b, alpha);
		innerBuilder.setLight(LightTexture.packLight(15, 15));
		innerBuilder.setOverlay(OverlayTexture.NO_OVERLAY);
		innerBuilder.setNormal(1, 1, 1);
		innerBuilder.pos(x, y+h, 0).tex(u0, v1).endVertex();
		innerBuilder.pos(x+w, y+h, 0).tex(u1, v1).endVertex();
		innerBuilder.pos(x+w, y, 0).tex(u1, v0).endVertex();
		innerBuilder.pos(x, y, 0).tex(u0, v0).endVertex();
	}

	public static void drawTexturedRect(IVertexBuilder builder, MatrixStack transform, int x, int y, int w, int h, float picSize,
										int u0, int u1, int v0, int v1)
	{
		drawTexturedColoredRect(builder, transform, x, y, w, h, 1, 1, 1, 1, u0/picSize, u1/picSize, v0/picSize, v1/picSize);
	}

	public static void drawRepeatedFluidSpriteGui(IRenderTypeBuffer buffer, MatrixStack transform, FluidStack fluid, float x, float y, float w, float h)
	{
		RenderType renderType = IERenderTypes.getGui(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
		IVertexBuilder builder = buffer.getBuffer(renderType);
		drawRepeatedFluidSprite(builder, transform, fluid, x, y, w, h);
	}

	public static void drawRepeatedFluidSprite(IVertexBuilder builder, MatrixStack transform, FluidStack fluid, float x, float y, float w, float h)
	{
		TextureAtlasSprite sprite = getSprite(fluid.getFluid().getAttributes().getStillTexture(fluid));
		int col = fluid.getFluid().getAttributes().getColor(fluid);
		int iW = sprite.getWidth();
		int iH = sprite.getHeight();
		if(iW > 0&&iH > 0)
			drawRepeatedSprite(builder, transform, x, y, w, h, iW, iH,
					sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(),
					(col >> 16&255)/255.0f, (col >> 8&255)/255.0f, (col&255)/255.0f, 1);
	}

	public static void drawRepeatedSprite(IVertexBuilder builder, MatrixStack transform, float x, float y, float w,
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

	public static void drawSlot(int x, int y, int w, int h, MatrixStack transform)
	{
		drawSlot(x, y, w, h, 0xff, transform);
	}

	public static void drawSlot(MatrixStack transform, int x, int y, int w, int h, int dark, int main, int light)
	{
		final int minX = x+8-w/2;
		final int minY = y+8-h/2;
		final int maxX = minX+w;
		final int maxY = minY+h;
		AbstractGui.fill(transform, minX, minY-1, maxX, minY, dark);
		AbstractGui.fill(transform, minX-1, minY-1, minX, maxY, dark);
		AbstractGui.fill(transform, minX, minY, maxX, maxY, main);
		AbstractGui.fill(transform, minX, maxY, maxX+1, maxY+1, light);
		AbstractGui.fill(transform, maxX, minY, maxX+1, maxY, light);
	}

	public static void drawSlot(int x, int y, int w, int h, int alpha, MatrixStack transform)
	{
		drawSlot(transform, x, y, w, h, (alpha<<24)|0x373737, (alpha<<24)|0x8b8b8b, (alpha<<24)|0xffffff);
	}

	public static void drawDarkSlot(MatrixStack transform, int x, int y, int w, int h)
	{
		drawSlot(transform, x, y, w, h, 0x77222222, 0x77111111, 0x77999999);
	}

	public static void handleGuiTank(MatrixStack transform, IFluidTank tank, int x, int y, int w, int h, int oX, int oY, int oW, int oH, int mX, int mY, ResourceLocation originalTexture, List<ITextComponent> tooltip)
	{
		handleGuiTank(transform, tank.getFluid(), tank.getCapacity(), x, y, w, h, oX, oY, oW, oH, mX, mY, originalTexture, tooltip);
	}

	public static void handleGuiTank(MatrixStack transform, FluidStack fluid, int capacity, int x, int y, int w, int h, int oX, int oY, int oW, int oH, int mX, int mY, ResourceLocation originalTexture, List<ITextComponent> tooltip)
	{
		if(tooltip==null)
		{
			transform.push();
			IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
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
			buffer.finish(renderType);
			transform.pop();
		}
		else
		{
			if(mX >= x&&mX < x+w&&mY >= y&&mY < y+h)
				addFluidTooltip(fluid, tooltip, capacity);
		}
	}

	public static void addFluidTooltip(FluidStack fluid, List<ITextComponent> tooltip, int tankCapacity)
	{
		if(!fluid.isEmpty())
			tooltip.add(applyFormat(
					fluid.getDisplayName(),
					fluid.getFluid().getAttributes().getRarity(fluid).color
			));
		else
			tooltip.add(new TranslationTextComponent("gui.immersiveengineering.empty"));
		if(fluid.getFluid() instanceof IEFluid)
			((IEFluid)fluid.getFluid()).addTooltipInfo(fluid, null, tooltip);

		if(mc().gameSettings.advancedItemTooltips&&!fluid.isEmpty())
		{
			if(!Screen.hasShiftDown())
				tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"holdShiftForInfo"));
			else
			{
				//TODO translation keys
				tooltip.add(applyFormat(new StringTextComponent("Fluid Registry: "+fluid.getFluid().getRegistryName()), TextFormatting.DARK_GRAY));
				tooltip.add(applyFormat(new StringTextComponent("Density: "+fluid.getFluid().getAttributes().getDensity(fluid)), TextFormatting.DARK_GRAY));
				tooltip.add(applyFormat(new StringTextComponent("Temperature: "+fluid.getFluid().getAttributes().getTemperature(fluid)), TextFormatting.DARK_GRAY));
				tooltip.add(applyFormat(new StringTextComponent("Viscosity: "+fluid.getFluid().getAttributes().getViscosity(fluid)), TextFormatting.DARK_GRAY));
				tooltip.add(applyFormat(new StringTextComponent("NBT Data: "+fluid.getTag()), TextFormatting.DARK_GRAY));
			}
		}

		if(tankCapacity > 0)
			tooltip.add(applyFormat(new StringTextComponent(fluid.getAmount()+"/"+tankCapacity+"mB"), TextFormatting.GRAY));
		else
			tooltip.add(applyFormat(new StringTextComponent(fluid.getAmount()+"mB"), TextFormatting.GRAY));
	}

	public static void renderItemWithOverlayIntoGUI(IRenderTypeBuffer buffer, MatrixStack transform,
													ItemStack stack, int x, int y)
	{
		buffer = IERenderTypes.disableLighting(buffer);
		transform.push();
		transform.translate(x, y, 100);
		transform.push();
		transform.translate(8, 8, 0);
		transform.scale(1, -1, 1);
		transform.scale(16, 16, 16);
		BatchingRenderTypeBuffer batchBuffer = new BatchingRenderTypeBuffer();
		mc().getItemRenderer().renderItem(stack, TransformType.GUI, 0xf000f0, OverlayTexture.NO_OVERLAY,
				transform, batchBuffer);
		batchBuffer.pipe(buffer);
		transform.pop();
		renderDurabilityBar(stack, buffer, transform);
		transform.pop();
	}

	public static void renderDurabilityBar(ItemStack stack, IRenderTypeBuffer buffer, MatrixStack transform)
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

	private static void draw(MatrixStack transform, IRenderTypeBuffer buffer, int x, int y, int width, int height, int red, int green, int blue)
	{
		IVertexBuilder builder = buffer.getBuffer(IERenderTypes.ITEM_DAMAGE_BAR);
		transform.push();
		transform.translate(x, y, 0);
		Matrix4f mat = transform.getLast().getMatrix();
		builder.pos(mat, 0, 0, 0).color(red, green, blue, 255).endVertex();
		builder.pos(mat, 0, height, 0).color(red, green, blue, 255).endVertex();
		builder.pos(mat, width, height, 0).color(red, green, blue, 255).endVertex();
		builder.pos(mat, width, 0, 0).color(red, green, blue, 255).endVertex();
		transform.pop();
	}
}
