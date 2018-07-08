/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.*;

public class ManualUtils
{
	public static boolean stackMatchesObject(ItemStack stack, Object o)
	{
		if(o instanceof String)
			return compareToOreName(stack, (String)o);
		if(o instanceof ItemStack)
		{
			if(!OreDictionary.itemMatches((ItemStack)o, stack, false))
				return false;
			if(stack.getItemDamage()==OreDictionary.WILDCARD_VALUE)
				return true;
			if(((ItemStack)o).hasTagCompound())
				return ((ItemStack)o).getTagCompound().equals(stack.getTagCompound());
			return true;
		}
		return false;
	}

	public static boolean compareToOreName(ItemStack stack, String oreName)
	{
		for(int oid : OreDictionary.getOreIDs(stack))
			if(OreDictionary.getOreName(oid).equals(oreName))
				return true;
		return false;
	}

	public static boolean isExistingOreName(String name)
	{
		if(!OreDictionary.doesOreNameExist(name))
			return false;
		else
			return !OreDictionary.getOres(name).isEmpty();
	}

	public static void drawTexturedRect(int x, int y, int w, int h, double... uv)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x, y+h, 0).tex(uv[0], uv[3]).endVertex();
		worldrenderer.pos(x+w, y+h, 0).tex(uv[1], uv[3]).endVertex();
		worldrenderer.pos(x+w, y, 0).tex(uv[1], uv[2]).endVertex();
		worldrenderer.pos(x, y, 0).tex(uv[0], uv[2]).endVertex();
		tessellator.draw();
	}

	public static ArrayList<String> getPrimitiveSpellingCorrections(String query, String[] valid, int maxDistance)
	{
		ArrayList<String> list = new ArrayList<String>();
		for(String s : valid)
			if(s!=null&&!s.trim().isEmpty())
				if(getSpellingDistanceBetweenStrings(query, s) < maxDistance)
					list.add(s);

		Collections.sort(list, new Comparator<String>()
		{
			@Override
			public int compare(String s0, String s1)
			{
				return getSpellingDistanceBetweenStrings(s1, s0);
			}
		});

		return list;
	}

	public static int getSpellingDistanceBetweenStrings(String query, String target)
	{
		query = query.toLowerCase(Locale.ENGLISH);
		target = target.toLowerCase(Locale.ENGLISH);

		String[] queryWords = query.split(" ");
		String[] targetWords = target.split(" ");
		int distance = 0;
		for(int iWord = 0; iWord < queryWords.length; iWord++)
		{
			if(iWord >= targetWords.length)
				distance++;
			else
			{
				int wordDistance = 0;
				for(int iChar = 0; iChar < queryWords[iWord].length(); iChar++)
					if(iChar >= targetWords[iWord].length())
						distance++;
					else
					{
						if(queryWords[iWord].charAt(iChar)!=targetWords[iWord].charAt(iChar))
						{
							wordDistance++;
							if(iChar > 0&&queryWords[iWord].charAt(iChar-1)==targetWords[iWord].charAt(iChar)&&queryWords[iWord].charAt(iChar)==targetWords[iWord].charAt(iChar-1))
								wordDistance -= 2;//switched letters don't increase distance
						}
					}
				if(wordDistance > 0)
					wordDistance += targetWords[iWord].length()-queryWords[iWord].length();
				distance += wordDistance;
			}
		}
		return distance;
	}

	/**
	 * Custom implementation of drawing a split string because Mojang's doesn't reset text colour between lines >___>
	 */
	public static void drawSplitString(FontRenderer fontRenderer, String string, int x, int y, int width, int colour)
	{
		fontRenderer.resetStyles();
		fontRenderer.textColor = colour;
		List<String> list = fontRenderer.listFormattedStringToWidth(string, width);
		FloatBuffer currentGLColor = BufferUtils.createFloatBuffer(16);
		int line = 0;
		for(Iterator<String> iterator = list.iterator(); iterator.hasNext(); y += fontRenderer.FONT_HEIGHT)
		{
			String next = iterator.next();
			if(line > 0)
			{
				int currentColour = fontRenderer.textColor;
				GL11.glGetFloat(GL11.GL_CURRENT_COLOR, currentGLColor);
				//Resetting colour if GL colour differs from textColor
				//that case happens because the formatting reset does not reset textColor
				int glColourRGBA = ((int)(currentGLColor.get(0)*255)<<16)+((int)(currentGLColor.get(1)*255)<<8)+((int)(currentGLColor.get(2)*255));
				if(glColourRGBA!=currentColour)
				{
					int j = 0;
					for(; j < fontRenderer.colorCode.length; j++)
						if(fontRenderer.colorCode[j]==glColourRGBA)
						{
							String code = Integer.toHexString(j%16);
							next = '\u00a7'+code+next;
							break;
						}
				}
			}
			fontRenderer.drawString(next, x, y, colour, false);
		}
	}

	public static String attemptStringTranslation(String tranlationKey, String arg)
	{
		String untranslated = String.format(tranlationKey, arg);
		String translated = I18n.format(untranslated);
		if(!untranslated.equals(translated))
			return translated;
		return arg;
	}

	static HashMap<String, ResourceLocation> resourceMap = new HashMap<String, ResourceLocation>();

	public static Tessellator tes()
	{
		return Tessellator.getInstance();
	}

	public static Minecraft mc()
	{
		return Minecraft.getMinecraft();
	}

	public static void bindTexture(String path)
	{
		mc().getTextureManager().bindTexture(getResource(path));
	}

	public static ResourceLocation getResource(String path)
	{
		ResourceLocation rl = resourceMap.containsKey(path)?resourceMap.get(path): new ResourceLocation(path);
		if(!resourceMap.containsKey(path))
			resourceMap.put(path, rl);
		return rl;
	}

	public static RenderItem renderItem()
	{
		return mc().getRenderItem();
	}
}
