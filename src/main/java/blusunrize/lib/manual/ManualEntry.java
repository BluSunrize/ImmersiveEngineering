/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.lib.manual.gui.GuiManual;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gnu.trove.map.TIntObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.IResource;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess")
public class ManualEntry
{
	private final ManualInstance manual;
	private List<ManualPage> pages;
	private final TextSplitter splitter;
	private final Function<TextSplitter, String[]> getContent;
	private String title;
	private String subtext;
	private final ResourceLocation location;
	private List<String[]> linkData;

	private ManualEntry(ManualInstance m, TextSplitter splitter, Function<TextSplitter, String[]> getContent,
						ResourceLocation location)
	{
		this.manual = m;
		this.splitter = splitter;
		this.getContent = getContent;
		this.location = location;
		refreshPages();
	}

	public void refreshPages()
	{
		try
		{
			boolean oldUni = manual.fontRenderer.getUnicodeFlag();
			manual.fontRenderer.setUnicodeFlag(true);
			manual.entryRenderPre();
			splitter.clearSpecialByAnchor();
			String[] parts = getContent.apply(splitter);
			title = parts[0];
			subtext = parts[1];
			String[] tmp = {parts[2]};//I want pointers... They would make this easier
			linkData = ManualUtils.prepareEntryForLinks(tmp);
			splitter.split(manual.formatText(tmp[0]));
			TIntObjectMap<SpecialManualElement> specials = splitter.getSpecials();
			List<List<String>> text = splitter.getEntryText();
			pages = new ArrayList<>(text.size());
			for(int i = 0; i < text.size(); i++)
			{
				SpecialManualElement special = specials.get(i);
				if(special==null)
					special = NOT_SPECIAL;
				pages.add(new ManualPage(text.get(i), special));
			}
			manual.fontRenderer.setUnicodeFlag(oldUni);
			manual.entryRenderPost();
		} catch(Throwable throwable)
		{
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Refreshing an IE manual entry");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Entry being refreshed:");
			crashreportcategory.addCrashSection("Entry name", location);
			crashreportcategory.addCrashSection("Manual", manual.getManualName());
			throw new ReportedException(crashreport);
		}
	}

	public void renderPage(GuiManual gui, int x, int y, int mouseX, int mouseY)
	{
		int page = gui.page;
		ManualPage toRender = pages.get(page);
		int offsetText = 0;
		int offsetSpecial = toRender.renderText.size()*manual.fontRenderer.FONT_HEIGHT;
		ManualInstance manual = gui.getManual();
		if(toRender.special.isAbove())
		{
			offsetText = toRender.special.getPixelsTaken();
			offsetSpecial = 0;
		}
		ManualUtils.drawSplitString(manual.fontRenderer, toRender.renderText, x, y+offsetText,
				manual.getTextColour());
		toRender.special.render(gui, x, y+offsetSpecial, mouseX, mouseY);
	}

	public String getTitle()
	{
		return title;
	}

	public Stream<SpecialManualElement> getSpecials()
	{
		return pages.stream().map((p) -> p.special);
	}

	public void addButtons(GuiManual guiManual, int x, int y, int page, List<GuiButton> pageButtons)
	{
		boolean uni = manual.fontRenderer.getUnicodeFlag();
		manual.fontRenderer.setUnicodeFlag(true);
		ManualPage p = pages.get(page);
		p.renderText = new ArrayList<>(p.text);
		ManualUtils.addLinks(this, manual, guiManual, p.renderText, x,
				y+p.special.getPixelsTaken(), pageButtons, linkData);
		manual.fontRenderer.setUnicodeFlag(uni);
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

	//TODO range checks everywhere
	public void buttonPressed(GuiManual gui, GuiButton button)
	{
		pages.get(gui.page).special.buttonPressed(gui, button);
	}

	public void mouseDragged(GuiManual gui, int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY,
							 GuiButton button)
	{
		pages.get(gui.page).special.mouseDragged(x, y, clickX, clickY, mx, my, lastX, lastY, button);
	}

	public int getPageForAnchor(int anchor)
	{
		return splitter.getPageForAnchor(anchor);
	}

	public Tree.AbstractNode<ResourceLocation, ManualEntry> getTreeNode()
	{
		return manual.contentTree.fullStream().filter((e) -> e.getLeafData()==this).findAny().orElse(null);
	}

	private class ManualPage
	{
		public List<String> renderText;
		List<String> text;
		@Nonnull
		SpecialManualElement special;

		public ManualPage(List<String> text, @Nonnull SpecialManualElement special)
		{
			this.text = text;
			this.special = special;
		}
	}

	public static class ManualEntryBuilder
	{
		ManualInstance manual;
		TextSplitter splitter;
		Function<TextSplitter, String[]> getContent = null;
		private ResourceLocation location;
		private List<Triple<String, Integer, SpecialManualElement>> hardcodedSpecials = new ArrayList<>();

		public ManualEntryBuilder(ManualInstance manual)
		{
			this.manual = manual;
			splitter = new TextSplitter(manual);
		}

		public ManualEntryBuilder(@Nonnull ManualInstance manual, @Nonnull TextSplitter splitter)
		{
			this.manual = manual;
			this.splitter = splitter;
		}

		public void addSpecialElement(String anchor, int offset, SpecialManualElement element)
		{
			splitter.addSpecialPage(anchor, offset, element);
		}

		public void setContent(String title, String subText, String mainText)
		{
			String[] content = {title, subText, mainText};
			getContent = (splitter) -> {
				for(Triple<String, Integer, SpecialManualElement> special : hardcodedSpecials)
					splitter.addSpecialPage(special.getLeft(), special.getMiddle(), special.getRight());
				return content;
			};
		}

		private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

		public void readFromFile(ResourceLocation name)
		{
			location = name;
			getContent = (splitter) -> {
				ResourceLocation langLoc = new ResourceLocation(name.getResourceDomain(),
						"manual/"+Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode()
								+"/"+name.getResourcePath()+".txt");
				ResourceLocation dataLoc = new ResourceLocation(name.getResourceDomain(),
						"manual/"+name.getResourcePath()+".json");
				IResource resLang = getResourceNullable(langLoc);
				IResource resData;
				try
				{
					resData = Minecraft.getMinecraft().getResourceManager().getResource(dataLoc);
				} catch(IOException e)
				{
					throw new RuntimeException(e);//50d9ee7d986cd28e7ea0b2493e0d902b1d676e75
				}
				if(resLang==null)
					resLang = getResourceNullable(new ResourceLocation(name.getResourceDomain(),
							"manual/en_us/"+name.getResourcePath()+".txt"));
				if(resLang==null)
					return new String[]{"ERROR", "This is not a good thing", "Could not find the file for "+name};
				try
				{
					JsonObject json = JsonUtils.gsonDeserialize(GSON, new InputStreamReader(resData.getInputStream()),
							JsonObject.class, true);
					byte[] bytesLang = IOUtils.toByteArray(resLang.getInputStream());
					String content = new String(bytesLang);
					for(Triple<String, Integer, SpecialManualElement> special : hardcodedSpecials)
						splitter.addSpecialPage(special.getLeft(), special.getMiddle(), special.getRight());
					assert json!=null;
					ManualUtils.parseSpecials(json, splitter, manual);
					int titleEnd = content.indexOf('\n');
					String title = content.substring(0, titleEnd);
					content = content.substring(titleEnd+1);
					int subtitleEnd = content.indexOf('\n');
					String subtext = content.substring(0, subtitleEnd);
					content = content.substring(subtitleEnd+1);
					String rawText = content;
					return new String[]{title, subtext, rawText};
				} catch(IOException e)
				{
					e.printStackTrace();
					return new String[]{"ERROR", "This is not a good thing", "Please check the log file for errors"};
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
			Preconditions.checkNotNull(splitter);
			Preconditions.checkNotNull(getContent);
			Preconditions.checkNotNull(location);
			return new ManualEntry(manual, splitter, getContent, location);
		}

		private static IResource getResourceNullable(ResourceLocation rl)
		{
			try
			{
				return Minecraft.getMinecraft().getResourceManager().getResource(rl);
			} catch(IOException e)
			{
				return null;
			}
		}
	}

	public static final SpecialManualElement NOT_SPECIAL = new SpecialManualElement()
	{

		@Override
		public void onOpened(GuiManual m, int x, int y, List<GuiButton> buttons)
		{
		}

		@Override
		public int getPixelsTaken()
		{
			return 0;
		}

		@Override
		public void render(GuiManual m, int x, int y, int mouseX, int mouseY)
		{
		}

		@Override
		public void buttonPressed(GuiManual gui, GuiButton button)
		{
		}

		@Override
		public void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY,
								 GuiButton button)
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
}
