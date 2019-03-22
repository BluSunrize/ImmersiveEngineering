/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.Tree.AbstractNode;
import blusunrize.lib.manual.gui.GuiButtonManualLink;
import blusunrize.lib.manual.gui.GuiManual;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

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

	public static String getTitleForNode(AbstractNode<ResourceLocation, ManualEntry> node, ManualInstance inst)
	{
		if(node.isLeaf())
			return inst.formatEntryName(node.getLeafData().getTitle());
		else
			return inst.formatCategoryName(node.getNodeData());
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

	public static <T> List<T> getPrimitiveSpellingCorrections
			(String query, Iterable<T> valid, int maxDistance, Function<T, String> getTitle)
	{
		List<T> ret = new ArrayList<>();
		for(T node : valid)
		{
			String s = getTitle.apply(node);
			if(s!=null&&!s.trim().isEmpty())
				if(getSpellingDistanceBetweenStrings(query, s) < maxDistance)
					ret.add(node);
		}
		ret.sort(
				(s0, s1) -> getSpellingDistanceBetweenStrings(getTitle.apply(s1), getTitle.apply(s0))
		);
		return ret;
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
	public static void drawSplitString(FontRenderer fontRenderer, List<String> text, int x, int y, int colour)
	{
		fontRenderer.resetStyles();
		fontRenderer.textColor = colour;
		FloatBuffer currentGLColor = BufferUtils.createFloatBuffer(16);
		int line = 0;
		//TODO remove
		fontRenderer.drawString("", 0, 0, colour);
		for(Iterator<String> iterator = text.iterator(); iterator.hasNext(); y += fontRenderer.FONT_HEIGHT)
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
			drawJustifiedString(fontRenderer, 120, next, x, y);
			//fontRenderer.drawString(next, x, y, colour, false);
			line++;
		}
	}

	private static Method renderChar;
	private static Method doDraw;
	private static Field posX, posY;

	//TODO make this an AT
	static
	{
		try
		{
			renderChar = FontRenderer.class.getDeclaredMethod("renderChar", char.class, boolean.class);
			renderChar.setAccessible(true);
			doDraw = FontRenderer.class.getDeclaredMethod("doDraw", float.class);
			doDraw.setAccessible(true);
			posX = FontRenderer.class.getDeclaredField("posX");
			posX.setAccessible(true);
			posY = FontRenderer.class.getDeclaredField("posY");
			posY.setAccessible(true);
		} catch(NoSuchMethodException|NoSuchFieldException e)
		{
			e.printStackTrace();
		}
	}

	public static void drawJustifiedString(FontRenderer render, int renderWidth, String text, float xPos, float yPos)
	{
		render.resetStyles();//TODO do I wnt this here?
		text = text.trim();
		float whitespaceLen = 0;
		for(char c : text.toCharArray())
		{
			if(Character.isWhitespace(c))
				whitespaceLen += render.getCharWidth(c);
			else
				++whitespaceLen;
		}
		int textWidth = render.getStringWidth(text);
		float factor;
		if(whitespaceLen==0)
			factor = 1;
		else
		{
			factor = (renderWidth-textWidth)/whitespaceLen;
			if(factor > 1)
				factor = 0;
		}
		for(int i = 0; i < text.length(); ++i)
		{
			char currChar = text.charAt(i);

			if(currChar=='§'&&i+1 < text.length())
			{
				render.renderStringAtPos(String.valueOf(currChar)+String.valueOf(text.charAt(i+1)), false);
				++i;
			}
			else
			{
				final String indexString = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
				int j = indexString.indexOf(currChar);

				/*if (render.randomStyle && j != -1)
				{
					int k = render.getCharWidth(currChar);
					char c1;

					do
					{
						j = render.fontRandom.nextInt(indexString.length());
						c1 = indexString.charAt(j);

					} while(k!=render.getCharWidth(c1));

					currChar = c1;
				}*/

				float boldOffset = j==-1||render.getUnicodeFlag()?0.5f: 1f;
				try
				{
					posX.set(render, (int)xPos);
					posY.set(render, yPos);
					float charWidth = (float)renderChar.invoke(render, currChar, false);
//TODO move to static
					Field bold = FontRenderer.class.getDeclaredField("boldStyle");
					bold.setAccessible(true);
					if((boolean)bold.get(render))
					{
						float tmpX = (int)xPos;
						tmpX += boldOffset;

						posX.set(render, tmpX);
						renderChar.invoke(render, currChar, false);
						posX.set(render, (int)xPos);

						++charWidth;
					}
					//doDraw.invoke(render, f);
					if(Character.isWhitespace(currChar))
						xPos += charWidth*(1+factor);
					else
						xPos += charWidth+factor;
				} catch(Exception x)
				{
					x.printStackTrace();
				}
			}
		}
	}

	private static final String THIS = "this";

	//Pass a single-element String array, to allow multiple outputs
	public static List<String[]> prepareEntryForLinks(String[] entryA)
	{
		String entry = entryA[0];
		List<String[]> repList = new ArrayList<String[]>();
		int overflow = 0;
		int start;
		while((start = entry.indexOf("<link")) >= 0&&overflow < 50)
		{
			overflow++;
			int end = entry.indexOf(">", start);
			String rep = entry.substring(start, end+1);
			String[] segment = rep.substring(0, rep.length()-1).split(";");
			if(segment.length < 3)
				break;
			String anchor = segment.length > 3?segment[3]: "-1";
			String[] resultParts = segment[2].split("(?<= )");// Split and keep the whitespace at the end of the tokens
			StringBuilder result = new StringBuilder();
			List<String> forCompleteLink = new ArrayList<>(3*resultParts.length);
			for(int iPart = 0; iPart < resultParts.length; iPart++)
			{
				//prefixing replacements with MC's formatting character and an unused char to keep them unique, but not counted for size
				String part = '\u00a7'+String.valueOf((char)(128+repList.size()))+resultParts[iPart];
				forCompleteLink.add(part);
				forCompleteLink.add(segment[1]);
				forCompleteLink.add(anchor);
				result.append(part);
			}
			repList.add(forCompleteLink.toArray(new String[0]));
			entry = entry.substring(0, start)+result+entry.substring(end+1);
		}
		entryA[0] = entry;
		return repList;
	}

	public static void addLinks(ManualEntry entry, ManualInstance helper, GuiManual gui, List<String> text, int x, int y,
								List<GuiButton> pageButtons, List<String[]> repList)
	{
		for(int linkIndex = 0; linkIndex < repList.size(); linkIndex++)
		{
			String[] repComplete = repList.get(linkIndex);
			String[] rep = new String[3];
			List<GuiButtonManualLink> parts = new ArrayList<>();
			for(int i = 0; i < repComplete.length/3; i++)
			{
				System.arraycopy(repComplete, 3*i, rep, 0, 3);
				for(int line = 0; line < text.size(); line++)
				{
					String s = text.get(line);
					int start;
					if((start = s.indexOf(rep[0].trim())) >= 0)
					{
						String formatIdent = rep[0].substring(0, 2);
						String element = rep[0].substring(2);
						if(!s.substring(start).startsWith(rep[0]))//This can happen when whitespace is cut off at the end of a line
							element = element.trim();
						int bx = helper.fontRenderer.getStringWidth(s.substring(0, start));
						int by = line*helper.fontRenderer.FONT_HEIGHT;
						ResourceLocation bkey = THIS.equals(element)?new ResourceLocation(rep[1]): entry.getLocation();
						int bw = helper.fontRenderer.getStringWidth(element);
						int bAnchor = -1;
						int bOffset = 0;
						try
						{
							if(rep[2].contains("+"))
							{
								int plus = rep[2].indexOf('+');
								bAnchor = Integer.parseInt(rep[2].substring(0, plus));
								bOffset = Integer.parseInt(rep[2].substring(plus+1));
							}
							else
								bAnchor = Integer.parseInt(rep[2]);
						} catch(Exception e)
						{
							e.printStackTrace();
						}
						GuiButtonManualLink btn = new GuiButtonManualLink(gui, 900+linkIndex, x+bx, y+by, bw, (int)(helper.fontRenderer.FONT_HEIGHT*1.5),
								new ManualInstance.ManualLink(Objects.requireNonNull(helper.getEntry(bkey), bkey+" is not a known entry!"), bAnchor, bOffset), element);
						parts.add(btn);
						pageButtons.add(btn);
						s = s.replaceFirst(formatIdent, "");
						text.set(line, s);
						break;
					}
				}
			}
			for(GuiButtonManualLink btn : parts)
				btn.otherParts = parts;
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
		return Minecraft.getInstance();
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

	/**
	 * Custom implementation of drawing a split string because Mojang's doesn't reset text colour between lines >___>
	 */
	public static int drawSplitString(FontRenderer fontRenderer, String string, int x, int y, int width, int colour)
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
			++line;
		}
		return list.size();
	}

	private static void parseSpecial(JsonObject obj, String anchor, TextSplitter splitter, ManualInstance instance)
	{
		String type = JsonUtils.getString(obj, "type");
		int offset = JsonUtils.getInt(obj, "offset", 0);
		ResourceLocation resLoc = getLocationForManual(type, instance);
		Function<JsonObject, SpecialManualElement> createElement = instance.getElementFactory(resLoc);
		splitter.addSpecialPage(anchor, offset, createElement.apply(obj));
	}

	public static void parseSpecials(JsonObject data, TextSplitter splitter, ManualInstance instance)
	{
		for(Entry<String, JsonElement> entry : data.entrySet())
		{
			JsonElement currData = entry.getValue();
			if(currData.isJsonObject())
				parseSpecial(currData.getAsJsonObject(), entry.getKey(), splitter, instance);
			else
				for(JsonElement inner : currData.getAsJsonArray())
					parseSpecial(inner.getAsJsonObject(), entry.getKey(), splitter, instance);
		}
	}

	public static ResourceLocation getLocationForManual(String s, ManualInstance instance)
	{
		if(s.indexOf(':') >= 0)
			return new ResourceLocation(s);
		else
			return new ResourceLocation(instance.getDefaultResourceDomain(), s);
	}

	public static PositionedItemStack parsePosItemStack(JsonElement ele, JsonContext ctx)
	{
		JsonObject json = ele.getAsJsonObject();
		int x = JsonUtils.getInt(json, "x");
		int y = JsonUtils.getInt(json, "y");
		if(JsonUtils.isString(json, "item"))
			return new PositionedItemStack(CraftingHelper.getItemStack(json, ctx), x, y);
		else if(JsonUtils.isJsonArray(json, "stacks"))
		{
			JsonArray arr = json.getAsJsonArray("stacks");
			List<ItemStack> stacks = new ArrayList<>(arr.size());
			for(JsonElement stack : arr)
				stacks.add(CraftingHelper.getItemStack(stack.getAsJsonObject(), ctx));
			return new PositionedItemStack(stacks, x, y);
		}
		else
			return new PositionedItemStack(CraftingHelper.getIngredient(json, ctx), x, y);
	}

	public static Object getRecipeObjFromJson(ManualInstance m, JsonElement jsonEle)
	{
		JsonContext ctx = new JsonContext(m.getDefaultResourceDomain());
		if(jsonEle.isJsonObject())
		{
			JsonObject json = jsonEle.getAsJsonObject();
			if(JsonUtils.isString(json, "recipe"))
				return ManualUtils.getLocationForManual(JsonUtils.getString(json, "recipe"), m);
			else if(JsonUtils.isString(json, "orename"))
				return json.get("orename").getAsString();
			else if(JsonUtils.isString(json, "item"))
				return CraftingHelper.getItemStack(json, ctx);
		}
		else if(jsonEle.isJsonArray())
		{
			JsonArray json = jsonEle.getAsJsonArray();
			PositionedItemStack[] stacks = new PositionedItemStack[json.size()];
			for(int i = 0; i < json.size(); i++)
				stacks[i] = parsePosItemStack(json.get(i), ctx);
			return stacks;
		}
		throw new RuntimeException("Could not find recipe for "+jsonEle);
	}
}
