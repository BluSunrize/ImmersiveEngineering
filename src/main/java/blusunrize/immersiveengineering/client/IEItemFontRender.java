/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.HashMap;

@SideOnly(Side.CLIENT)
public class IEItemFontRender extends FontRenderer
{
	static HashMap<Character, CharReplacement> unicodeReplacements = new HashMap();

	static
	{
		unicodeReplacements.put((char)Integer.parseInt("260E", 16), new CharReplacement("immersiveengineering:textures/gui/hud_elements.png", .5f, .75f, .5625f, .8125f));
	}

	int[] backupColours;
	String colourFormattingKeys = "0123456789abcdef";
	public float customSpaceWidth = 4f;
	public float spacingModifier = 0f;
	public boolean verticalBoldness = false;

	public IEItemFontRender()
	{
		super(ClientUtils.mc().gameSettings, new ResourceLocation("textures/font/ascii.png"), ClientUtils.mc().renderEngine, false);
		if(Minecraft.getMinecraft().gameSettings.language!=null)
		{
			this.setUnicodeFlag(ClientUtils.mc().getLanguageManager().isCurrentLocaleUnicode());
			this.setBidiFlag(ClientUtils.mc().getLanguageManager().isCurrentLanguageBidirectional());
		}
		((IReloadableResourceManager)ClientUtils.mc().getResourceManager()).registerReloadListener(this);
		createColourBackup();
	}

	/**
	 * This should be called again if the colour array was modified after instantiation
	 */
	public void createColourBackup()
	{
		this.backupColours = Arrays.copyOf(this.colorCode, 32);
	}

	@Override
	public void renderStringAtPos(String text, boolean shadow)
	{
		int idx = -1;
		int loop = 0;
		HashMap<Integer, Integer> formattingReplacements = new HashMap<Integer, Integer>();
		while((idx = text.indexOf("<hexcol=")) >= 0&&loop++ < 20)
		{
			int end = text.indexOf(">", idx);
			if(end >= 0)
			{
				String rep = "ERROR";
				String s = text.substring(idx, end+1);
				int formatEnd = s.indexOf(":");
				if(formatEnd >= 0)
				{
					rep = s.substring(formatEnd+1, s.length()-1);
					String hex = s.substring("<hexcol=".length(), formatEnd);
					try
					{
						int hexColour = Integer.parseInt(hex, 16);
						int formatting = 0;
						if(formattingReplacements.containsKey(hexColour))
							formatting = formattingReplacements.get(hexColour);
						else
							while(formatting < 16&&text.contains("\u00A7"+colourFormattingKeys.charAt(formatting)))
								formatting++;
						if(formatting < 16)
						{
							rep = "\u00A7"+colourFormattingKeys.charAt(formatting)+rep+"\u00A7r";
							this.colorCode[formatting] = hexColour;
							this.colorCode[16+formatting] = ClientUtils.getDarkenedTextColour(hexColour);
						}
						formattingReplacements.put(hexColour, formatting);
					} catch(Exception e)
					{
					}
				}
				text = text.replace(s, rep);
			}
		}
		if(verticalBoldness)
		{
			float startX = this.posX;
			float startY = this.posY;
			float yOffset = this.getUnicodeFlag()?.5f: 1;

			super.renderStringAtPos(text, shadow);
			this.posY = startY+yOffset;
			this.posX = startX;
			super.renderStringAtPos(text, shadow);
			this.posY -= yOffset;
		}
		else
			super.renderStringAtPos(text, shadow);

		this.colorCode = Arrays.copyOf(backupColours, 32);
	}

	@Override
	protected float renderUnicodeChar(char ch, boolean italic)
	{
		CharReplacement cr = unicodeReplacements.get(ch);
		if(cr!=null)
			return cr.replaceChar(posX, posY);
		return super.renderUnicodeChar(ch, italic);
	}

	@Override
	protected float renderDefaultChar(int ch, boolean italic)
	{
		if(ch==32)
			return customSpaceWidth;
		return super.renderDefaultChar(ch, italic)+spacingModifier;
	}

	public float getCharWidthIEFloat(char character)
	{
		if(character==32)
			return customSpaceWidth;
		return super.getCharWidth(character)+spacingModifier;
	}

	@Override
	public int getCharWidth(char character)
	{
		return (int)this.getCharWidthIEFloat(character);
	}

	@Override
	public int getStringWidth(String text)
	{
		if(text==null)
			return 0;
		else
		{
			float i = 0;
			boolean flag = false;
			for(int j = 0; j < text.length(); ++j)
			{
				char c0 = text.charAt(j);
				float k = this.getCharWidthIEFloat(c0);
				if(k < 0&&j < text.length()-1)
				{
					++j;
					c0 = text.charAt(j);

					if(c0!=108&&c0!=76)
					{
						if(c0==114||c0==82)
							flag = false;
					}
					else
						flag = true;
					k = 0;
				}

				i += k;
				if(flag&&k > 0)
					++i;
			}
			return (int)i;
		}
	}

	@Override
	public int sizeStringToWidth(String str, int wrapWidth)
	{
		int i = str.length();
		float j = 0;
		int k = 0;
		int l = -1;

		for(boolean flag = false; k < i; ++k)
		{
			char c0 = str.charAt(k);
			switch(c0)
			{
				case '\n':
					--k;
					break;
				case ' ':
					l = k;
				default:
					j += this.getCharWidthIEFloat(c0);
					if(flag)
						++j;
					break;
				case '\u00a7':
					if(k < i-1)
					{
						++k;
						char c1 = str.charAt(k);

						if(c1!=108&&c1!=76)
						{
							if(c1==114||c1==82||(c1 >= 48&&c1 <= 57||c1 >= 97&&c1 <= 102||c1 >= 65&&c1 <= 70))
								flag = false;
						}
						else
							flag = true;
					}
			}
			if(c0==10)
			{
				++k;
				l = k;
				break;
			}
			if(j > wrapWidth)
				break;
		}
		return k!=i&&l!=-1&&l < k?l: k;
	}

	static class CharReplacement
	{
		private final String textureSheet;
		private final float uMin;
		private final float vMin;
		private final float uMax;
		private final float vMax;

		public CharReplacement(String textureSheet, float uMin, float vMin, float uMax, float vMax)
		{
			this.textureSheet = textureSheet;
			this.uMin = uMin;
			this.vMin = vMin;
			this.uMax = uMax;
			this.vMax = vMax;
		}

		float replaceChar(float posX, float posY)
		{
			ClientUtils.bindTexture(textureSheet);
//			int j = ch / 256;
//			this.loadGlyphTexture(j);
//			int k = i >>> 4;
//			int l = i & 15;
//			float f = (float)k;
//			float f1 = (float)(l + 1);
//			float f2 = (float)(ch % 16 * 88816) + f;
//			float f3 = (float)((ch & 255) / 16 * 16);
//			float f4 = f1 - f - 0.02F;
//			float f5 = italic ? 1.0F : 0.0F;
			GlStateManager.glBegin(5);
			GlStateManager.glTexCoord2f(uMin, vMin);
			GlStateManager.glVertex3f(posX, posY, 0.0F);
			GlStateManager.glTexCoord2f(uMin, vMax);
			GlStateManager.glVertex3f(posX, posY+7.99F, 0.0F);
			GlStateManager.glTexCoord2f(uMax, vMin);
			GlStateManager.glVertex3f(posX+7.99f, posY, 0.0F);
			GlStateManager.glTexCoord2f(uMax, vMax);
			GlStateManager.glVertex3f(posX+7.99f, posY+7.99F, 0.0F);
			GlStateManager.glEnd();
			return 8.02f;
		}
	}
}
