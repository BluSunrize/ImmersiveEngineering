/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.font;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.fonts.Font;
import net.minecraft.client.gui.fonts.IGlyph;
import net.minecraft.client.gui.fonts.TexturedGlyph;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

@OnlyIn(Dist.CLIENT)
public class IEFontRender extends FontRenderer
{
	private static final HashMap<Character, TexturedGlyph> unicodeReplacements = new HashMap<>();
	private static final ResourceLocation UNICODE = new ResourceLocation(ImmersiveEngineering.MODID, "unicode");
	private static final ResourceLocation NORMAL = Minecraft.DEFAULT_FONT_RENDERER_NAME;

	static
	{
		unicodeReplacements.put((char)Integer.parseInt("260E", 16),
				new TexturedGlyph(new ResourceLocation(ImmersiveEngineering.MODID, "textures/gui/hud_elements.png"),
						.5f, .75f, .5625f, .8125f, 0, 7.99F, 0, 7.99F));
	}

	public float customSpaceWidth = 4f;
	public boolean verticalBoldness = false;
	protected final TextureManager texManager;
	private final boolean unicode;

	public IEFontRender(boolean unicode, ResourceLocation id)
	{
		super(mc().textureManager, new Font(mc().textureManager, id));
		texManager = mc().textureManager;
		this.unicode = unicode;
	}

	public ResourceLocation getBaseID()
	{
		return unicode?UNICODE: NORMAL;
	}

	@Override
	public float renderStringAtPos(String text, float x, float y, int color, boolean isShadow)
	{
		float ret = renderStringAtPosImpl(text, x, y, color, isShadow);
		if(verticalBoldness)
		{
			float yOffset = unicode?.5f: 1;
			renderStringAtPosImpl(text, x, y+yOffset, color, isShadow);
		}
		return ret;
	}

	private float renderStringAtPosImpl(String text, float x, float y, int color, boolean isShadow)
	{
		final float colorScale = isShadow?0.25F: 1.0F;
		final float argRed = (float)(color >> 16&255)/255.0F*colorScale;
		final float argGreen = (float)(color >> 8&255)/255.0F*colorScale;
		final float argBlue = (float)(color&255)/255.0F*colorScale;
		float currentRed = argRed;
		float currentGreen = argGreen;
		float currentBlue = argBlue;
		float alpha = (float)(color >> 24&255)/255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		ResourceLocation lastGlyphTexture = null;
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		boolean obfuscated = false;
		boolean bold = false;
		boolean italic = false;
		boolean underline = false;
		boolean strikethrough = false;
		List<Entry> lineSegments = Lists.newArrayList();
		int resetColorAt = -1;
		FloatList charPositions = new FloatArrayList(text.length());

		for(int i = 0; i < text.length(); ++i)
		{
			char currentChar = text.charAt(i);
			if(currentChar=='\u00a7'&&i+1 < text.length())
			{
				TextFormatting newFormatting = TextFormatting.fromFormattingCode(text.charAt(i+1));
				if(newFormatting!=null)
				{
					if(newFormatting.isNormalStyle())
					{
						obfuscated = false;
						bold = false;
						strikethrough = false;
						underline = false;
						italic = false;
						currentRed = argRed;
						currentGreen = argGreen;
						currentBlue = argBlue;
					}

					if(newFormatting.getColor()!=null)
					{
						int j = newFormatting.getColor();
						currentRed = (float)(j >> 16&255)/255.0F*colorScale;
						currentGreen = (float)(j >> 8&255)/255.0F*colorScale;
						currentBlue = (float)(j&255)/255.0F*colorScale;
					}
					else if(newFormatting==TextFormatting.OBFUSCATED)
						obfuscated = true;
					else if(newFormatting==TextFormatting.BOLD)
						bold = true;
					else if(newFormatting==TextFormatting.STRIKETHROUGH)
						strikethrough = true;
					else if(newFormatting==TextFormatting.UNDERLINE)
						underline = true;
					else if(newFormatting==TextFormatting.ITALIC)
						italic = true;
				}

				++i;
			}
			else if(text.substring(i).startsWith("<hexcol="))
			{
				int end = text.indexOf(">", i);
				String s = text.substring(i, end+1);
				int formatEnd = s.indexOf(":");
				if(formatEnd >= 0)
				{
					String hex = s.substring("<hexcol=".length(), formatEnd);
					int hexColour = Integer.parseInt(hex, 16);
					resetColorAt = end;
					currentRed = ((hexColour >> 16)&255)/255F*colorScale;
					currentGreen = ((hexColour >> 8)&255)/255F*colorScale;
					currentBlue = (hexColour&255)/255F*colorScale;
					i += formatEnd;
				}
			}
			else if(i==resetColorAt)
			{
				currentRed = argRed;
				currentGreen = argGreen;
				currentBlue = argBlue;
			}
			else
			{
				IGlyph currentGlyph = this.font.findGlyph(currentChar);
				TexturedGlyph texturedglyph = obfuscated&&currentChar!=' '?this.font.obfuscate(currentGlyph): this.font.getGlyph(currentChar);
				TexturedGlyph replacement = unicodeReplacements.get(currentChar);
				if(replacement!=null)
					texturedglyph = replacement;
				ResourceLocation glyphLocation = texturedglyph.getTextureLocation();
				if(glyphLocation!=null)
				{
					if(lastGlyphTexture!=glyphLocation)
					{
						tessellator.draw();
						this.texManager.bindTexture(glyphLocation);
						bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
						lastGlyphTexture = glyphLocation;
					}

					float boldOffset = bold?currentGlyph.getBoldOffset(): 0.0F;
					float shadowOffset = isShadow?currentGlyph.getShadowOffset(): 0.0F;
					this.renderGlyph(texturedglyph, bold, italic, boldOffset, x+shadowOffset,
							y+shadowOffset, bufferbuilder, currentRed, currentGreen, currentBlue, alpha, currentChar);
				}

				float advance = getCharWidthIE(currentChar, bold);
				float shadowOffset = isShadow?1.0F: 0.0F;
				if(strikethrough)
					lineSegments.add(new Entry(x+shadowOffset-1.0F, y+shadowOffset+4.5F,
							x+shadowOffset+advance, y+shadowOffset+4.5F-1.0F,
							currentRed, currentGreen, currentBlue, alpha));

				if(underline)
					lineSegments.add(new Entry(x+shadowOffset-1.0F, y+shadowOffset+9.0F,
							x+shadowOffset+advance, y+shadowOffset+9.0F-1.0F,
							currentRed, currentGreen, currentBlue, alpha));

				charPositions.add(x);
				x += advance;
			}
		}

		tessellator.draw();
		if(!lineSegments.isEmpty())
		{
			GlStateManager.disableTexture();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

			for(Entry entry : lineSegments)
				entry.pipe(bufferbuilder);

			tessellator.draw();
			GlStateManager.enableTexture();
		}
		postStringRender(text, charPositions, bufferbuilder, tessellator, y);

		return x;
	}

	protected void postStringRender(String text, FloatList charPositions, BufferBuilder bb, Tessellator tes, float y)
	{

	}

	protected void renderGlyph(TexturedGlyph glyph, boolean bold, boolean italic, float boldOffset, float x, float y,
							   BufferBuilder bufferBuilder, float red, float green, float blue, float alpha, char orig)
	{

		glyph.render(this.texManager, italic, x, y, bufferBuilder, red, green, blue, alpha);
		if(bold)
		{
			glyph.render(this.texManager, italic, x+boldOffset, y, bufferBuilder, red, green, blue, alpha);
		}

	}

	public float getCharWidthIE(char character, boolean bold)
	{
		if(character==32)
			return customSpaceWidth;
		return character==167?0.0F: this.font.findGlyph(character).getAdvance(bold);
	}

	@Override
	public float getCharWidth(char character)
	{
		return this.getCharWidthIE(character, false);
	}

	@Override
	public int getStringWidth(String text)
	{
		if(text==null)
			return 0;
		else
		{
			float length = 0.0F;
			boolean bold = false;

			for(int i = 0; i < text.length(); ++i)
			{
				if(text.substring(i).startsWith("<hexcol="))
				{
					int end = text.indexOf(">", i);
					String s = text.substring(i, end+1);
					int formatEnd = s.indexOf(":");
					if(formatEnd >= 0)
						i += formatEnd;
				}
				else
				{
					char currentChar = text.charAt(i);
					if(currentChar==167&&i < text.length()-1)
					{
						++i;
						TextFormatting textformatting = TextFormatting.fromFormattingCode(text.charAt(i));
						if(textformatting==TextFormatting.BOLD)
							bold = true;
						else if(textformatting!=null&&textformatting.isNormalStyle())
							bold = false;
					}
					else
						length += this.getCharWidthIE(currentChar, bold);
				}
			}

			return MathHelper.ceil(length);
		}
	}

	@Override
	public int sizeStringToWidth(String str, int wrapWidth)
	{
		final int strLength = str.length();
		float currentWidth = 0;
		boolean bold = false;
		int lastSpace = -1;
		int currIndex = 0;

		for(; currIndex < strLength; ++currIndex)
		{
			char currentChar = str.charAt(currIndex);
			switch(currentChar)
			{
				case '\n':
					--currIndex;
					break;
				case ' ':
					lastSpace = currIndex;
				default:
					currentWidth += this.getCharWidthIE(currentChar, bold);
					break;
				case '\u00a7':
					if(currIndex < strLength-1)
					{
						++currIndex;
						TextFormatting textformatting = TextFormatting.fromFormattingCode(str.charAt(currIndex));
						if(textformatting==TextFormatting.BOLD)
						{
							bold = true;
						}
						else if(textformatting!=null&&textformatting.isNormalStyle())
						{
							bold = false;
						}
					}
			}
			if(currentChar=='\n')
			{
				++currIndex;
				lastSpace = currIndex;
				break;
			}
			if(currentWidth > wrapWidth)
				break;
		}
		return currIndex!=strLength&&lastSpace!=-1&&lastSpace < currIndex?lastSpace: currIndex;
	}

	public int getFontHeight()
	{
		return FONT_HEIGHT;
	}

	@Override
	public int getWordWrappedHeight(String str, int maxLength)
	{
		return getFontHeight()*this.listFormattedStringToWidth(str, maxLength).size();
	}

	static class Entry
	{
		protected final float x1;
		protected final float y1;
		protected final float x2;
		protected final float y2;
		protected final float red;
		protected final float green;
		protected final float blue;
		protected final float alpha;

		private Entry(float x1, float y1, float x2, float y2, float red, float green, float blue, float alpha)
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.alpha = alpha;
		}

		public void pipe(BufferBuilder buffer)
		{
			buffer.pos(this.x1, this.y1, 0.0D).color(this.red, this.green, this.blue, this.alpha).endVertex();
			buffer.pos(this.x2, this.y1, 0.0D).color(this.red, this.green, this.blue, this.alpha).endVertex();
			buffer.pos(this.x2, this.y2, 0.0D).color(this.red, this.green, this.blue, this.alpha).endVertex();
			buffer.pos(this.x1, this.y2, 0.0D).color(this.red, this.green, this.blue, this.alpha).endVertex();
		}
	}
}
