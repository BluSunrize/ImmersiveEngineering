/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.ManualEntry.SpecialElementData;
import blusunrize.lib.manual.SplitResult.Token;
import blusunrize.lib.manual.Tree.AbstractNode;
import blusunrize.lib.manual.gui.GuiButtonManualLink;
import blusunrize.lib.manual.gui.ManualScreen;
import blusunrize.lib.manual.links.Link;
import blusunrize.lib.manual.utils.ManualRecipeRef;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.FontContext;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import static blusunrize.lib.manual.utils.ManualLogger.LOGGER;
import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
import static com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE;
import static com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA;

public class ManualUtils
{
	public static boolean stackMatchesObject(ItemStack stack, ItemStack o)
	{
		return ItemStack.isSameItemSameComponents(stack, o);
	}

	public static String getTitleForNode(AbstractNode<ResourceLocation, ManualEntry> node, ManualInstance inst)
	{
		if(node.isLeaf())
			return inst.formatEntryName(node.getLeafData().getTitle());
		else
			return inst.formatCategoryName(node.getNodeData());
	}

	public static void drawTexturedRect(GuiGraphics graphics, ResourceLocation texture, int x, int y, int w, int h, float... uv)
	{
		// TODO replace by graphics.blit?
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, texture);
		Matrix4f mat = graphics.pose().last().pose();
		BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		buffer.addVertex(mat, x, y+h, 0)
				.setColor(1F, 1F, 1F, 1F)
				.setUv(uv[0], uv[3]);
		buffer.addVertex(mat, x+w, y+h, 0)
				.setColor(1F, 1F, 1F, 1F)
				.setUv(uv[1], uv[3]);
		buffer.addVertex(mat, x+w, y, 0)
				.setColor(1F, 1F, 1F, 1F)
				.setUv(uv[1], uv[2]);
		buffer.addVertex(mat, x, y, 0)
				.setColor(1F, 1F, 1F, 1F)
				.setUv(uv[0], uv[2]);
		BufferUploader.drawWithShader(buffer.buildOrThrow());
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

	public static final String THIS = "this";

	public static void addLinkButtons(ManualEntry entry, ManualInstance manual, ManualScreen gui, List<List<Token>> text, int x, int y,
									  List<Button> pageButtons)
	{
		final Map<Link, List<GuiButtonManualLink>> partButtons = new IdentityHashMap<>();
		MutableInt lineId = new MutableInt(0);
		for(List<Token> line : text)
		{
			Mutable<String> textUpToHere = new MutableObject<>("");
			for(Token token : line)
			{
				token.getContent().ifRight(linkPart -> {
					int bx = manual.fontRenderer().width(textUpToHere.getValue());
					int by = lineId.intValue()*manual.fontRenderer().lineHeight;
					Link link = linkPart.getParent();
					String linkText = linkPart.getText();
					ResourceLocation bkey = link.getTarget(entry);
					int bw = manual.fontRenderer().width(linkText);
					ManualInstance.ManualLink outputLink;
					ManualEntry bEntry = manual.getEntry(bkey);
					if(bEntry!=null&&bEntry.hasAnchor(link.getTargetAnchor()))
						outputLink = new ManualInstance.ManualLink(bEntry, link.getTargetAnchor(), link.getTargetOffset());
					else
					{
						if(bEntry==null)
							LOGGER.error("Unknown manual entry: {} (link from {})", bkey, entry.getLocation());
						else if(!bEntry.hasAnchor(link.getTargetAnchor()))
							LOGGER.error("Unknown anchor {} in entry {} (link from {})", link.getTargetAnchor(), bkey,
									entry.getLocation());
						outputLink = null;
					}
					GuiButtonManualLink btn = new GuiButtonManualLink(gui, x+bx, y+by, bw, (int)(manual.fontRenderer().lineHeight*1.5),
							outputLink, linkText);
					partButtons.computeIfAbsent(link, l -> new ArrayList<>())
							.add(btn);
				});
				textUpToHere.setValue(textUpToHere.getValue()+token.getText());
			}
			lineId.increment();
		}
		for(List<GuiButtonManualLink> parts : partButtons.values())
			for(GuiButtonManualLink btn : parts)
			{
				btn.otherParts = parts;
				pageButtons.add(btn);
			}
	}

	public static String attemptStringTranslation(String tranlationKey, String arg)
	{
		String untranslated = String.format(tranlationKey, arg);
		String translated = I18n.get(untranslated);
		if(!untranslated.equals(translated))
			return translated;
		return arg;
	}

	private static final Map<String, ResourceLocation> resourceMap = new HashMap<>();

	public static Tesselator tes()
	{
		return Tesselator.getInstance();
	}

	public static Minecraft mc()
	{
		return Minecraft.getInstance();
	}

	//TODO properly fix usages
	@Deprecated
	public static void bindTexture(ResourceLocation path)
	{
		RenderSystem.setShaderTexture(0, path);
	}

	/**
	 * Custom implementation of drawing a split string because Mojang's doesn't reset text colour between lines >___>
	 */
	public static void drawSplitString(GuiGraphics graphics, Font fontRenderer, List<String> text, int x, int y, int colour)
	{
		for(String s : text)
		{
			graphics.drawString(fontRenderer, s, x, y, colour, false);
			y += fontRenderer.lineHeight;
		}
	}

	private static void parseSpecial(
			JsonObject obj, String anchor, ManualInstance instance, List<SpecialElementData> out
	)
	{
		String type = GsonHelper.getAsString(obj, "type");
		int offset = GsonHelper.getAsInt(obj, "offset", 0);
		ResourceLocation resLoc = getLocationForManual(type, instance);
		try
		{
			Function<JsonObject, SpecialManualElement> createElement = instance.getElementFactory(resLoc);
			out.add(new SpecialElementData(anchor, offset, () -> createElement.apply(obj)));
		} catch(Exception x)
		{
			x.printStackTrace();
		}
	}

	public static void parseSpecials(JsonObject data, ManualInstance instance, List<SpecialElementData> out)
	{
		for(Entry<String, JsonElement> entry : data.entrySet())
		{
			JsonElement currData = entry.getValue();
			if(currData.isJsonObject())
				parseSpecial(currData.getAsJsonObject(), entry.getKey(), instance, out);
			else
				for(JsonElement inner : currData.getAsJsonArray())
					parseSpecial(inner.getAsJsonObject(), entry.getKey(), instance, out);
		}
	}

	public static ResourceLocation getLocationForManual(String s, ManualInstance instance)
	{
		if(s.indexOf(':') >= 0)
			return ResourceLocation.parse(s);
		else
			return ResourceLocation.fromNamespaceAndPath(instance.getDefaultResourceDomain(), s);
	}

	public static boolean isNumber(JsonObject main, String name)
	{
		return main.has(name)&&main.get(name).isJsonPrimitive()&&main.get(name).getAsJsonPrimitive().isNumber();
	}

	@Nullable
	public static PositionedItemStack parsePosItemStack(JsonElement ele)
	{
		JsonObject json = ele.getAsJsonObject();
		if(!isNumber(json, "x"))
			return null;
		int x = GsonHelper.getAsInt(json, "x");
		if(!isNumber(json, "y"))
			return null;
		int y = GsonHelper.getAsInt(json, "y");
		if(GsonHelper.isStringValue(json, "id"))
			return new PositionedItemStack(readItemStack(json), x, y);
		else if(GsonHelper.isArrayNode(json, "stacks"))
		{
			JsonArray arr = json.getAsJsonArray("stacks");
			List<ItemStack> stacks = new ArrayList<>(arr.size());
			for(JsonElement stack : arr)
				stacks.add(readItemStack(stack.getAsJsonObject()));
			return new PositionedItemStack(stacks, x, y);
		}
		else
			try
			{
				return new PositionedItemStack(
						Ingredient.CODEC.decode(JsonOps.INSTANCE, json).result().get().getFirst(), x, y
				);
			} catch(JsonSyntaxException xcp)
			{
				return null;
			}
	}

	public static ItemStack getItemStackFromJson(ManualInstance m, JsonElement jsonEle)
	{
		if(jsonEle.isJsonPrimitive())
		{
			ResourceLocation itemName = getLocationForManual(jsonEle.getAsString(), m);
			return new ItemStack(BuiltInRegistries.ITEM.get(itemName));
		}
		else
			return readItemStack(jsonEle.getAsJsonObject());
	}

	public static ManualRecipeRef getRecipeObjFromJson(ManualInstance m, JsonElement jsonEle)
	{
		if(jsonEle.isJsonObject())
		{
			JsonObject json = jsonEle.getAsJsonObject();
			if(GsonHelper.isStringValue(json, "recipe"))
				return new ManualRecipeRef(ManualUtils.getLocationForManual(GsonHelper.getAsString(json, "recipe"), m));
			else if(GsonHelper.isStringValue(json, "id"))
				return new ManualRecipeRef(readItemStack(json));
		}
		else if(jsonEle.isJsonArray())
		{
			JsonArray json = jsonEle.getAsJsonArray();
			PositionedItemStack[] stacks = new PositionedItemStack[json.size()];
			for(int i = 0; i < json.size(); i++)
			{
				PositionedItemStack posStack = parsePosItemStack(json.get(i));
				if(posStack!=null)
					stacks[i] = posStack;
				else
					throw new RuntimeException("Failed to load positional item stack from "+json.get(i));
			}
			return new ManualRecipeRef(stacks);
		}
		else if(jsonEle.isJsonPrimitive()&&jsonEle.getAsJsonPrimitive().isString())
			return new ManualRecipeRef(ManualUtils.getLocationForManual(jsonEle.getAsString(), m));
		throw new RuntimeException("Could not find recipe for "+jsonEle);
	}

	public static boolean listStack(String search, ItemStack stack)
	{
		return stack.getHoverName().getString().toLowerCase(Locale.ENGLISH).contains(search);
	}

	public static void renderItemStack(GuiGraphics graphics, ItemStack stack, int x, int y, boolean overlay)
	{
		renderItemStack(graphics, stack, x, y, overlay, null);
	}

	public static void renderItemStack(GuiGraphics graphics, ItemStack stack, int x, int y, boolean overlay, String count)
	{
		if(stack.isEmpty())
			return;
		graphics.renderItem(stack, x, y);
		if(overlay)
		{
			// Use the Item's font renderer, if available
			Font font = IClientItemExtensions.of(stack.getItem()).getFont(stack, FontContext.ITEM_COUNT);
			font = font!=null?font: Minecraft.getInstance().font;
			graphics.renderItemDecorations(font, stack, x, y, count);
		}
	}

	private static ItemStack readItemStack(JsonObject json)
	{
		return ItemStack.OPTIONAL_CODEC.decode(JsonOps.INSTANCE, json).result().get().getFirst();
	}

	public static <S extends AutoCloseable, R> R loadFromStream(StreamSupplier<S> streamProvider, StreamHandler<S, R> streamHandler, Supplier<String> exceptionMessage)
	{
		try(S stream = streamProvider.get())
		{
			return streamHandler.apply(stream);
		} catch(Exception e)
		{
			throw new RuntimeException(exceptionMessage.get(), e);
		}
	}

	@FunctionalInterface
	public interface StreamSupplier<S extends AutoCloseable>
	{
		S get() throws Exception;
	}

	@FunctionalInterface
	public interface StreamHandler<S extends AutoCloseable, R>
	{
		R apply(S stream) throws Exception;
	}

}
