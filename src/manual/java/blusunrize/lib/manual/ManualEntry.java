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
import blusunrize.lib.manual.links.Link;
import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ManualEntry implements Comparable<ManualEntry>
{
	private boolean initialized = false;

	private final ManualInstance manual;
	private final Supplier<EntryData> getContent;
	private final ResourceLocation location;
	private final ResourceLocation requiredAdvancement;

	// in basic init
	private String title;
	private String subtext;
	private List<SpecialElementData> specialElements;
	private Supplier<EntryWithLinks> withLinks;
	private Supplier<Set<String>> anchors;

	// in full init/ensureInitialized
	private List<ManualPage> pages;
	private Int2ObjectMap<SpecialManualElement> specials;
	private Object2IntMap<String> anchorPoints;

	private ManualEntry(ManualInstance m, Supplier<EntryData> getContent, ResourceLocation location, ResourceLocation requiredAdvancement)
	{
		this.manual = m;
		this.getContent = getContent;
		this.location = location;
		this.requiredAdvancement = requiredAdvancement;
	}

	public void initBasic()
	{
		EntryData data = getContent.get();
		title = data.title;
		subtext = data.subtext;
		String formattedText = manual.formatText(data.content);
		specialElements = data.specialElements;
		withLinks = Suppliers.memoize(() -> new EntryWithLinks(formattedText, manual));
		anchors = Suppliers.memoize(() -> {
			Set<String> anchors = new HashSet<>();
			for(Either<String, Link> e : withLinks.get().getUnsplitTokens())
			{
				Optional<String> left = e.left();
				if(left.isPresent())
				{
					String s = left.get();
					if(s.startsWith("<&")&&s.endsWith(">"))
						anchors.add(s.substring(2, s.length()-1));
				}
			}
			anchors.add(TextSplitter.START);
			return anchors;
		});
		initialized = false;
	}

	private void ensureInitialized()
	{
		if(initialized)
			return;
		try
		{
			manual.entryRenderPre();
			TextSplitter splitter = new TextSplitter(manual);
			for(SpecialElementData special : specialElements)
			{
				splitter.addSpecialPage(special.anchor, special.offset, special.getElement());
			}
			SplitResult result = splitter.split(withLinks.get().getUnsplitTokens());
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
		initialized = true;
	}

	public void renderPage(GuiGraphics graphics, ManualScreen gui, int x, int y, int mouseX, int mouseY)
	{
		ensureInitialized();
		int page = gui.page;
		ManualPage toRender = pages.get(page);
		int offsetText = 0;
		int offsetSpecial = ((toRender.renderText.size()*manual.fontRenderer().lineHeight+1)+
				manual.pageHeight-toRender.special.getPixelsTaken())/2;
		ManualInstance manual = gui.getManual();
		if(toRender.special.isAbove())
		{
			offsetText = toRender.special.getPixelsTaken();
			offsetSpecial = 0;
		}
		ManualUtils.drawSplitString(graphics, manual.fontRenderer(), toRender.renderText, x, y+offsetText,
				manual.getTextColour());
		graphics.pose().pushPose();
		graphics.pose().translate(x, y+offsetSpecial, 0);
		toRender.special.render(graphics, gui, 0, 0, mouseX, mouseY);
		graphics.pose().popPose();
	}

	public String getTitle()
	{
		return title;
	}

	public List<SpecialElementData> getSpecialData()
	{
		return specialElements;
	}

	public Int2ObjectMap<SpecialManualElement> getSpecialsByPage()
	{
		ensureInitialized();
		return specials;
	}

	public void addButtons(ManualScreen gui, int x, int y, int page, List<Button> pageButtons)
	{
		ensureInitialized();
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
		pages.get(gui.page).special.onOpened(gui, x, y, tempButtons);
		pageButtons.addAll(tempButtons);
	}

	public String getSubtext()
	{
		return subtext;
	}

	public int getPageCount()
	{
		ensureInitialized();
		return pages.size();
	}

	public ResourceLocation getLocation()
	{
		return location;
	}

	public Optional<ResourceLocation> getRequiredAdvancement()
	{
		return Optional.ofNullable(requiredAdvancement);
	}

	public ItemStack getHighlightedStack(int page)
	{
		ensureInitialized();
		return pages.get(page).special.getHighlightedStack();
	}

	public boolean listForSearch(String search)
	{
		for(SpecialElementData d : specialElements)
			if(d.getElement().listForSearch(search))
				return true;
		return false;
	}

	public void mouseDragged(ManualScreen gui, int x, int y, double clickX, double clickY, double mx, double my,
							 double lastX, double lastY, int button)
	{
		ensureInitialized();
		pages.get(gui.page).special.mouseDragged(x, y, clickX, clickY, mx, my, lastX, lastY, button);
	}

	public int getPageForAnchor(String anchor)
	{
		ensureInitialized();
		return anchorPoints.getInt(anchor);
	}

	public boolean hasAnchor(String anchor)
	{
		return anchors.get().contains(anchor);
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
		private final ManualInstance manual;
		private Supplier<EntryData> getContent = null;
		private ResourceLocation location;
		private final List<SpecialElementData> hardcodedSpecials = new ArrayList<>();
		private ResourceLocation requiredAdvancement;

		public ManualEntryBuilder(ManualInstance manual)
		{
			this.manual = manual;
		}

		public ManualEntryBuilder(@Nonnull ManualInstance manual, @Nonnull TextSplitter splitter)
		{
			this.manual = manual;
		}

		public void addSpecialElement(SpecialElementData data)
		{
			hardcodedSpecials.add(data);
		}

		public void setContent(Supplier<EntryData> get)
		{
			getContent = () -> {
				EntryData base = get.get();
				List<SpecialElementData> allSpecials = new ArrayList<>(base.specialElements);
				allSpecials.addAll(hardcodedSpecials);
				return new EntryData(base.title, base.subtext, base.content, allSpecials);
			};
		}

		public void setContent(Supplier<String> title, Supplier<String> subText, Supplier<String> mainText)
		{
			getContent = () -> new EntryData(title.get(), subText.get(), mainText.get(), hardcodedSpecials);
		}

		public void setContent(String title, String subText, String mainText)
		{
			setContent(() -> title, () -> subText, () -> mainText);
		}

		private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

		public void appendText(Supplier<Pair<String, List<SpecialElementData>>> text)
		{
			Supplier<EntryData> old = getContent;
			setContent(() -> {
				EntryData base = old.get();
				Pair<String, List<SpecialElementData>> toAdd = text.get();
				List<SpecialElementData> allSpecials = new ArrayList<>(base.specialElements);
				allSpecials.addAll(toAdd.getSecond());
				return new EntryData(base.title, base.subtext, base.content+toAdd.getFirst(), allSpecials);
			});
		}

		public void readFromFile(ResourceLocation name)
		{
			location = name;

			// Load JSON, fetching required advancement if necessary.
			// The rest of the JSON is consumed in the getContent supplier.
			Resource resData = Minecraft.getInstance().getResourceManager().getResource(
					name.withPath("manual/"+name.getPath()+".json")
			).orElseThrow();
			final JsonObject json = ManualUtils.loadFromStream(
					resData::openAsReader,
					dataStream -> GsonHelper.fromJson(GSON, dataStream, JsonObject.class, true),
					() -> "Failed to load manual entry from "+name
			);
			if(json.has("require_advancement"))
				requiredAdvancement = ResourceLocation.parse(json.remove("require_advancement").getAsString());

			getContent = () -> {
				ResourceLocation langLoc = name.withPath("manual/"+Minecraft.getInstance().getLanguageManager().getSelected()
						+"/"+name.getPath()+".txt");
				Resource resLang = getResourceNullable(langLoc);
				if(resLang==null)
					resLang = getResourceNullable(name.withPath("manual/en_us/"+name.getPath()+".txt"));
				if(resLang==null)
					return new EntryData(
							"ERROR", "This is not a good thing", "Could not find the file for "+name, ImmutableList.of()
					);
				return ManualUtils.loadFromStream(
						resLang::open,
						langStream -> {
							byte[] bytesLang = IOUtils.toByteArray(langStream);
							String content = new String(bytesLang, StandardCharsets.UTF_8);
							List<SpecialElementData> allSpecials = new ArrayList<>(hardcodedSpecials);
							ManualUtils.parseSpecials(json, manual, allSpecials);
							int titleEnd = content.indexOf('\n');
							String title = content.substring(0, titleEnd).trim();
							content = content.substring(titleEnd+1);
							int subtitleEnd = content.indexOf('\n');
							String subtext = content.substring(0, subtitleEnd).trim();
							content = content.substring(subtitleEnd+1).trim();
							Pattern backslashNewline = Pattern.compile("[^\\\\][\\\\][\r]?\n[\r]?");
							String rawText = backslashNewline.matcher(content).replaceAll("").replace("\\\\", "\\");
							return new EntryData(title, subtext, rawText, allSpecials);
						},
						() -> "Failed to load manual entry from "+name
				);
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
			return new ManualEntry(manual, getContent, location, requiredAdvancement);
		}

		private static Resource getResourceNullable(ResourceLocation rl)
		{
			return Minecraft.getInstance().getResourceManager().getResource(rl).orElse(null);
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
		public void render(GuiGraphics graphics, ManualScreen m, int x, int y, int mouseX, int mouseY)
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
		private final List<SpecialElementData> specialElements;

		public EntryData(String title, String subtext, String content, List<SpecialElementData> specialElements)
		{
			this.title = title;
			this.subtext = subtext;
			this.content = content;
			this.specialElements = specialElements;
		}
	}

	public static class SpecialElementData
	{
		private final String anchor;
		private final int offset;
		private final Supplier<? extends SpecialManualElement> element;

		public SpecialElementData(String anchor, int offset, SpecialManualElement element)
		{
			this(anchor, offset, () -> element);
		}

		public SpecialElementData(String anchor, int offset, Supplier<? extends SpecialManualElement> element)
		{
			this.anchor = anchor;
			this.offset = offset;
			//TODO reset
			this.element = Suppliers.memoize(element::get);
		}

		public SpecialManualElement getElement()
		{
			return element.get();
		}

		public String getAnchor()
		{
			return anchor;
		}

		public int getOffset()
		{
			return offset;
		}
	}
}
