/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.SplitResult.Token;
import blusunrize.lib.manual.gui.ManualScreen;
import blusunrize.lib.manual.links.EntryWithLinks;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ManualEntry implements Comparable<ManualEntry>
{
	private final ManualInstance manual;
	private List<ManualPage> pages;
	private final Function<TextSplitter, EntryData> getContent;
	private String title;
	private String subtext;
	private final ResourceLocation location;
	private Int2ObjectMap<SpecialManualElement> specials;
	private Object2IntMap<String> anchorPoints;

	private ManualEntry(ManualInstance m, Function<TextSplitter, EntryData> getContent,
						ResourceLocation location)
	{
		this.manual = m;
		this.getContent = getContent;
		this.location = location;
	}

	public void refreshPages()
	{
		try
		{
			manual.entryRenderPre();
			TextSplitter splitter = new TextSplitter(manual);
			EntryData data = getContent.apply(splitter);
			title = data.title;
			subtext = data.subtext;
			EntryWithLinks withLinks = new EntryWithLinks(manual.formatText(data.content), manual);
			SplitResult result = splitter.split(withLinks.getUnsplitTokens());
			specials = result.specialByPage;
			anchorPoints = result.pageByAnchor;
			List<List<List<Token>>> text = result.entry;
			pages = new ArrayList<>(text.size());
			for(int i = 0; i < text.size(); i++)
			{
				SpecialManualElement special = specials.get(i);
				if(special==null)
					special = NOT_SPECIAL;
				pages.add(new ManualPage(text.get(i), special));
			}
			manual.entryRenderPost();
		} catch(Exception x)
		{
			throw new RuntimeException(
					"Exception while refreshing manual entry "+location+" for manual "+manual.getManualName(),
					x);
		}
	}

	public void renderPage(ManualScreen gui, int x, int y, int mouseX, int mouseY)
	{
		int page = gui.page;
		ManualPage toRender = pages.get(page);
		int offsetText = 0;
		int offsetSpecial = ((toRender.renderText.size()*manual.fontRenderer().FONT_HEIGHT+1)+
				manual.pageHeight-toRender.special.getPixelsTaken())/2;
		ManualInstance manual = gui.getManual();
		if(toRender.special.isAbove())
		{
			offsetText = toRender.special.getPixelsTaken();
			offsetSpecial = 0;
		}
		ManualUtils.drawSplitString(manual.fontRenderer(), toRender.renderText, x, y+offsetText,
				manual.getTextColour());
		GlStateManager.pushMatrix();
		GlStateManager.translatef(x, y+offsetSpecial, 0);
		toRender.special.render(gui, 0, 0, mouseX, mouseY);
		GlStateManager.popMatrix();
	}

	public String getTitle()
	{
		return title;
	}

	public Int2ObjectMap<SpecialManualElement> getSpecials()
	{
		return specials;
	}

	public void addButtons(ManualScreen gui, int x, int y, int page, List<Button> pageButtons)
	{
		ManualPage p = pages.get(page);
		p.renderText = p.text.stream()
				.map(
						l -> l.stream()
								.map(Token::getText)
								.collect(Collectors.joining())
				)
				.collect(Collectors.toList());
		ManualUtils.addLinkButtons(this, manual, gui, p.text, x,
				y+p.special.getPixelsTaken(), pageButtons);
		List<Button> tempButtons = new ArrayList<>();
		pages.get(gui.page).special.onOpened(gui, 0, 0, tempButtons);
		for(Button btn : tempButtons)
		{
			btn.x += x;
			btn.y += y;
			pageButtons.add(btn);
		}
	}

	public String getSubtext()
	{
		return subtext;
	}

	public int getPageCount()
	{
		return pages.size();
	}

	public ResourceLocation getLocation()
	{
		return location;
	}

	public ItemStack getHighlightedStack(int page)
	{
		return pages.get(page).special.getHighlightedStack();
	}

	public boolean listForSearch(String search)
	{
		for(ManualPage p : pages)
			if(p.special.listForSearch(search))
				return true;
		return false;
	}

	public void mouseDragged(ManualScreen gui, int x, int y, double clickX, double clickY, double mx, double my,
							 double lastX, double lastY, int button)
	{
		pages.get(gui.page).special.mouseDragged(x, y, clickX, clickY, mx, my, lastX, lastY, button);
	}

	public int getPageForAnchor(String anchor)
	{
		return anchorPoints.getInt(anchor);
	}

	public boolean hasAnchor(String anchor)
	{
		return anchorPoints.containsKey(anchor);
	}

	public Tree.AbstractNode<ResourceLocation, ManualEntry> getTreeNode()
	{
		return manual.getAllEntriesAndCategories()
				.filter((e) -> e.getLeafData()==this).findAny().orElse(null);
	}

	@Override
	public int compareTo(ManualEntry o)
	{
		return title.compareTo(o.title);
	}

	private static class ManualPage
	{
		public List<String> renderText;
		List<List<Token>> text;
		@Nonnull
		SpecialManualElement special;

		public ManualPage(List<List<Token>> text, @Nonnull SpecialManualElement special)
		{
			this.text = text;
			this.special = special;
		}
	}

	public static class ManualEntryBuilder
	{
		ManualInstance manual;
		Function<TextSplitter, EntryData> getContent = null;
		private ResourceLocation location;
		private List<Triple<String, Integer, Supplier<? extends SpecialManualElement>>> hardcodedSpecials = new ArrayList<>();

		public ManualEntryBuilder(ManualInstance manual)
		{
			this.manual = manual;
		}

		public ManualEntryBuilder(@Nonnull ManualInstance manual, @Nonnull TextSplitter splitter)
		{
			this.manual = manual;
		}

		public void addSpecialElement(String anchor, int offset, Supplier<? extends SpecialManualElement> element)
		{
			hardcodedSpecials.add(new ImmutableTriple<>(anchor, offset, element));
		}

		public void addSpecialElement(String anchor, int offset, SpecialManualElement element)
		{
			hardcodedSpecials.add(new ImmutableTriple<>(anchor, offset, () -> element));
		}

		public void setContent(Function<TextSplitter, EntryData> get)
		{
			getContent = splitter -> {
				addHardcodedSpecials(splitter);
				return get.apply(splitter);
			};
		}

		public void setContent(Supplier<String> title, Supplier<String> subText, Supplier<String> mainText)
		{
			getContent = splitter -> {
				addHardcodedSpecials(splitter);
				return new EntryData(title.get(), subText.get(), mainText.get());
			};
		}

		private void addHardcodedSpecials(TextSplitter splitter)
		{
			for(Triple<String, Integer, Supplier<? extends SpecialManualElement>> special : hardcodedSpecials)
				splitter.addSpecialPage(
						special.getLeft(),
						special.getMiddle(),
						special.getRight().get()
				);
		}

		public void setContent(String title, String subText, String mainText)
		{
			setContent(() -> title, () -> subText, () -> mainText);
		}

		private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

		public void appendText(Function<TextSplitter, String> text)
		{
			Function<TextSplitter, EntryData> old = getContent;
			setContent(splitter -> {
				EntryData base = old.apply(splitter);
				return new EntryData(
						base.title,
						base.subtext,
						base.content+text.apply(splitter)
				);
			});
		}

		public void readFromFile(ResourceLocation name)
		{
			location = name;
			getContent = (splitter) -> {
				ResourceLocation langLoc = new ResourceLocation(name.getNamespace(),
						"manual/"+Minecraft.getInstance().getLanguageManager().getCurrentLanguage().getCode()
								+"/"+name.getPath()+".txt");
				ResourceLocation dataLoc = new ResourceLocation(name.getNamespace(),
						"manual/"+name.getPath()+".json");
				IResource resLang = getResourceNullable(langLoc);
				IResourceManager manager = Minecraft.getInstance().getResourceManager();
				IResource resData;
				try
				{
					resData = FastResourceAccess.getResource(manager, dataLoc);
				} catch(IOException e)
				{
					throw new RuntimeException(e);
				}
				if(resLang==null)
					resLang = getResourceNullable(new ResourceLocation(name.getNamespace(),
							"manual/en_us/"+name.getPath()+".txt"));
				if(resLang==null)
					return new EntryData("ERROR", "This is not a good thing", "Could not find the file for "+name);
				try
				{
					JsonObject json = JSONUtils.fromJson(GSON, new InputStreamReader(resData.getInputStream()),
							JsonObject.class, true);
					byte[] bytesLang = IOUtils.toByteArray(resLang.getInputStream());
					String content = new String(bytesLang, StandardCharsets.UTF_8);
					addHardcodedSpecials(splitter);
					assert json!=null;
					ManualUtils.parseSpecials(json, splitter, manual);
					int titleEnd = content.indexOf('\n');
					String title = content.substring(0, titleEnd).trim();
					content = content.substring(titleEnd+1);
					int subtitleEnd = content.indexOf('\n');
					String subtext = content.substring(0, subtitleEnd).trim();
					content = content.substring(subtitleEnd+1).trim();
					Pattern backslashNewline = Pattern.compile("[^\\\\][\\\\][\r]?\n[\r]?");
					String rawText = backslashNewline.matcher(content).replaceAll("").replace("\\\\", "\\");
					return new EntryData(title, subtext, rawText);
				} catch(Exception e)
				{
					throw new RuntimeException("Failed to load manual entry from "+name, e);
				}
			};
		}

		public void setLocation(ResourceLocation location)
		{
			this.location = location;
		}

		public ManualEntry create()
		{
			Preconditions.checkNotNull(manual);
			Preconditions.checkNotNull(getContent);
			Preconditions.checkNotNull(location);
			return new ManualEntry(manual, getContent, location);
		}

		private static IResource getResourceNullable(ResourceLocation rl)
		{
			try
			{
				return FastResourceAccess.getResource(Minecraft.getInstance().getResourceManager(), rl);
			} catch(IOException e)
			{
				return null;
			}
		}
	}

	public static final SpecialManualElement NOT_SPECIAL = new SpecialManualElement()
	{

		@Override
		public void onOpened(ManualScreen m, int x, int y, List<Button> buttons)
		{
		}

		@Override
		public int getPixelsTaken()
		{
			return 0;
		}

		@Override
		public void render(ManualScreen m, int x, int y, int mouseX, int mouseY)
		{
		}

		@Override
		public void mouseDragged(int x, int y, double clickX, double clickY, double mx, double my, double lastX, double lastY, int mouseButton)
		{
		}

		@Override
		public boolean listForSearch(String searchTag)
		{
			return false;
		}

		@Override
		public void recalculateCraftingRecipes()
		{
		}
	};

	public static class EntryData
	{
		private final String title;
		private final String subtext;
		private final String content;

		public EntryData(String title, String subtext, String content)
		{
			this.title = title;
			this.subtext = subtext;
			this.content = content;
		}
	}
}
