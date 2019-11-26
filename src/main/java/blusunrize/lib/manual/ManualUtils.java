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
import blusunrize.lib.manual.gui.ManualScreen;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class ManualUtils
{
	public static boolean stackMatchesObject(ItemStack stack, Object o)
	{
		if(o instanceof ResourceLocation)
			return isInTag(stack, (ResourceLocation)o);
		if(o instanceof ItemStack)
		{
			if(!ItemStack.areItemsEqual((ItemStack)o, stack))
				return false;
			if(((ItemStack)o).hasTag())
				return ((ItemStack)o).getTag().equals(stack.getTag());
			return true;
		}
		return false;
	}

	public static boolean isInTag(ItemStack stack, ResourceLocation tag)
	{
		Tag<Item> itemTag = ItemTags.getCollection().get(tag);
		if(itemTag!=null&&itemTag.contains(stack.getItem()))
			return true;
		Tag<Block> blockTag = BlockTags.getCollection().get(tag);
		return blockTag!=null&&blockTag.contains(Block.getBlockFromItem(stack.getItem()));
	}

	public static boolean isNonemptyItemTag(ResourceLocation name)
	{
		Tag<Item> t = ItemTags.getCollection().getTagMap().get(name);
		return t!=null&&!t.getAllElements().isEmpty();
	}

	public static boolean isNonemptyBlockTag(ResourceLocation name)
	{
		Tag<Block> t = BlockTags.getCollection().getTagMap().get(name);
		return t!=null&&!t.getAllElements().isEmpty();
	}

	public static boolean isNonemptyBlockOrItemTag(ResourceLocation name)
	{
		return isNonemptyBlockTag(name)||isNonemptyItemTag(name);
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
				Comparator.comparingInt(s -> getSpellingDistanceBetweenStrings(query, getTitle.apply(s)))
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
							if(iChar > 0
									&&queryWords[iWord].charAt(iChar-1)==targetWords[iWord].charAt(iChar)
									&&queryWords[iWord].charAt(iChar)==targetWords[iWord].charAt(iChar-1))
								wordDistance -= 2;//switched letters don't increase distance
						}
					}
				if(wordDistance > 0)
					wordDistance += Math.abs(targetWords[iWord].length()-queryWords[iWord].length());
				distance += wordDistance;
			}
		}
		return distance;
	}

	private static final String THIS = "this";

	//Pass a single-element String array, to allow multiple outputs
	public static List<String[]> prepareEntryForLinks(String[] entryA)
	{
		final String format = TextFormatting.ITALIC.toString()+TextFormatting.UNDERLINE.toString();
		String entry = entryA[0];
		List<String[]> repList = new ArrayList<>();
		int linksAdded = 0;
		int start;
		while((start = entry.indexOf("<link")) >= 0&&linksAdded < 50)
		{
			linksAdded++;
			int end = entry.indexOf(">", start);
			String rep = entry.substring(start, end+1);
			String[] segment = rep.substring(0, rep.length()-1).split(";");
			if(segment.length < 3)
				break;
			String anchor = segment.length > 3?segment[3]: "-1";
			String[] resultParts = segment[2].split("(?<= )");// Split and keep the whitespace at the end of the tokens
			StringBuilder result = new StringBuilder();
			List<String> forCompleteLink = new ArrayList<>(3*resultParts.length);
			for(String resultPart : resultParts)
			{
				//prefixing replacements with MC's formatting character and an unused char to keep them unique, but not counted for size
				String part = format+'\u00a7'+String.valueOf((char)(128+repList.size()))+resultPart;
				forCompleteLink.add(part);
				forCompleteLink.add(segment[1]);
				forCompleteLink.add(anchor);
				result.append(part);
			}
			repList.add(forCompleteLink.toArray(new String[0]));
			entry = entry.substring(0, start)+result+TextFormatting.RESET.toString()+entry.substring(end+1);
		}
		entryA[0] = entry;
		return repList;
	}

	public static void addLinks(ManualEntry entry, ManualInstance manual, ManualScreen gui, List<String> text, int x, int y,
								List<Button> pageButtons, List<String[]> repList)
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
						String linkText = rep[0].substring(2);
						if(!s.substring(start).startsWith(rep[0]))//This can happen when whitespace is cut off at the end of a line
							linkText = linkText.trim();
						int bx = manual.fontRenderer().getStringWidth(s.substring(0, start));
						int by = line*manual.fontRenderer().FONT_HEIGHT;
						ResourceLocation bkey = THIS.equals(rep[1])?entry.getLocation(): getLocationForManual(rep[1], manual);
						int bw = manual.fontRenderer().getStringWidth(linkText);
						String bAnchor = TextSplitter.START;
						int bOffset = 0;
						try
						{
							if(rep[2].contains("+"))
							{
								int plus = rep[2].indexOf('+');
								bAnchor = rep[2].substring(0, plus);
								bOffset = Integer.parseInt(rep[2].substring(plus+1));
							}
							else
								bAnchor = rep[2];
						} catch(Exception e)
						{
							e.printStackTrace();
							throw new RuntimeException(e);
						}
						ManualEntry bEntry = Objects.requireNonNull(manual.getEntry(bkey), bkey+" is not a known entry!");
						Preconditions.checkArgument(bEntry.hasAnchor(bAnchor), "Entry "+bkey+" does not contain anchor "+bAnchor);
						GuiButtonManualLink btn = new GuiButtonManualLink(gui, x+bx, y+by, bw, (int)(manual.fontRenderer().FONT_HEIGHT*1.5),
								new ManualInstance.ManualLink(bEntry, bAnchor, bOffset), linkText);
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

	static HashMap<String, ResourceLocation> resourceMap = new HashMap<>();

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

	public static ItemRenderer renderItem()
	{
		return mc().getItemRenderer();
	}

	/**
	 * Custom implementation of drawing a split string because Mojang's doesn't reset text colour between lines >___>
	 */
	public static void drawSplitString(FontRenderer fontRenderer, List<String> text, int x, int y, int colour)
	{
		/*
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
		 */

		//TODO Do we still need all of ^ or does this simplistic implementation (based on the 1.14 Mojang version) work?
		for(String s : text)
		{
			fontRenderer.drawString(s, x, y, colour);
			y += 9;
		}
	}

	private static void parseSpecial(JsonObject obj, String anchor, TextSplitter splitter, ManualInstance instance)
	{
		String type = JSONUtils.getString(obj, "type");
		int offset = JSONUtils.getInt(obj, "offset", 0);
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

	public static PositionedItemStack parsePosItemStack(JsonElement ele)
	{
		JsonObject json = ele.getAsJsonObject();
		int x = JSONUtils.getInt(json, "x");
		int y = JSONUtils.getInt(json, "y");
		if(JSONUtils.isString(json, "item"))
			return new PositionedItemStack(CraftingHelper.getItemStack(json, true), x, y);
		else if(JSONUtils.isJsonArray(json, "stacks"))
		{
			JsonArray arr = json.getAsJsonArray("stacks");
			List<ItemStack> stacks = new ArrayList<>(arr.size());
			for(JsonElement stack : arr)
				stacks.add(CraftingHelper.getItemStack(stack.getAsJsonObject(), true));
			return new PositionedItemStack(stacks, x, y);
		}
		else
			return new PositionedItemStack(CraftingHelper.getIngredient(json), x, y);
	}

	public static Object getRecipeObjFromJson(ManualInstance m, JsonElement jsonEle)
	{
		if(jsonEle.isJsonObject())
		{
			JsonObject json = jsonEle.getAsJsonObject();
			if(JSONUtils.isString(json, "recipe"))
				return ManualUtils.getLocationForManual(JSONUtils.getString(json, "recipe"), m);
			else if(JSONUtils.isString(json, "orename"))
				return json.get("orename").getAsString();
			else if(JSONUtils.isString(json, "item"))
				return CraftingHelper.getItemStack(json, true);
		}
		else if(jsonEle.isJsonArray())
		{
			JsonArray json = jsonEle.getAsJsonArray();
			PositionedItemStack[] stacks = new PositionedItemStack[json.size()];
			for(int i = 0; i < json.size(); i++)
				stacks[i] = parsePosItemStack(json.get(i));
			return stacks;
		}
		throw new RuntimeException("Could not find recipe for "+jsonEle);
	}

	public static boolean listStack(String search, ItemStack stack)
	{
		return stack.getDisplayName().getFormattedText().toLowerCase(Locale.ENGLISH).contains(search);
	}
}
