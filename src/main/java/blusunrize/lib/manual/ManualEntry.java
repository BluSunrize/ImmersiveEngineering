/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.GuiManual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

//TODO links
public class ManualEntry
{
	private final ManualInstance manual;
	private List<ManualPage> pages;
	private final TextSplitter splitter;
	private final String fullText;
	private String title;
	private String subtext;
	private String category;

	public ManualEntry(ManualInstance m, TextSplitter splitter, String fullText, String title, String subtext, String category)
	{
		this.manual = m;
		this.splitter = splitter;
		this.fullText = fullText;
		this.title = title;
		this.subtext = subtext;
		this.category = category;
		refreshPages();
	}

	public void refreshPages()
	{
		boolean oldUni = manual.fontRenderer.getUnicodeFlag();
		manual.fontRenderer.setUnicodeFlag(true);
		manual.entryRenderPre();
		splitter.split(fullText);
		Map<Integer, SpecialManualElement> specials = splitter.getSpecials();
		List<List<String>> text = splitter.getEntryText();
		pages = new ArrayList<>(text.size());
		for (int i = 0; i < text.size(); i++)
		{
			SpecialManualElement special = specials.getOrDefault(i, NOT_SPECIAL);
			pages.add(new ManualPage(text.get(i), special));
		}
		manual.fontRenderer.setUnicodeFlag(false);
		manual.entryRenderPost();
	}

	public void renderPage(GuiManual gui, int x, int y, int mouseX, int mouseY)
	{
		int page = gui.page;//TODO is is 0 or 1 based?
		ManualPage toRender = pages.get(page);
		int offsetText = 0;
		int offsetSpecial = toRender.text.size()*manual.fontRenderer.FONT_HEIGHT;
		ManualInstance manual = gui.getManual();
		if (toRender.special.isAbove())
		{
			offsetText = manual.fontRenderer.FONT_HEIGHT*toRender.special.getLinesTaken();
			offsetSpecial = 0;
		}
		ManualUtils.drawSplitString(manual.fontRenderer, toRender.text, x, y+offsetText,
				manual.getTextColour());
		toRender.special.render(gui, x, y+offsetSpecial, mouseX, mouseY);
	}

	public String getCategory()
	{
		return category;
	}

	public String getTitle()
	{
		return title;
	}

	public Stream<SpecialManualElement> getSpecials()
	{
		return pages.stream().map((p)->p.special);
	}

	public void addButtons(GuiManual guiManual, int x, int y, List<GuiButton> pageButtons)
	{

	}

	public String getSubtext()
	{
		return subtext;
	}

	public int getPageCount()
	{
		return pages.size();
	}

	public ItemStack getHighlightedStack(int page)
	{
		return pages.get(page).special.getHighlightedStack();
	}

	public boolean listForSearch(String search)
	{
		for (ManualPage p:pages)
			if (p.special.listForSearch(search))
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

	private class ManualPage {
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
		ManualInstance manual = null;
		TextSplitter splitter = null;
		String rawText = null;
		private String title;
		private String subtext;
		private String category;

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

		public void addSpecialElement(int anchor, int offset, SpecialManualElement element)
		{
			splitter.addSpecialPage(anchor, offset, element);
		}

		public void setCategory(String category)
		{
			this.category = category;
		}

		public void setText(String text)
		{
			this.rawText = rawText;
		}

		public void setSubtext(String subtext)
		{
			this.subtext = subtext;
		}

		public void readFromFile(ResourceLocation name)
		{
			ResourceLocation realLoc = new ResourceLocation(name.getResourceDomain(),
					"manual/" + Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode()
							+ "/" + name.getResourcePath()+".txt");
			IResource res = getResourceNullable(realLoc);
			if (res == null)
				res = getResourceNullable(new ResourceLocation(name.getResourceDomain(),
						"manual/en_us/" + name.getResourcePath()+".txt"));
			if (res==null)
				return;
			try
			{
				byte[] bytes = IOUtils.toByteArray(res.getInputStream());
				String content = new String(bytes);
				int titleEnd = content.indexOf('\n');
				title = content.substring(0, titleEnd);
				content = content.substring(titleEnd+1);
				int subtitleEnd = content.indexOf('\n');
				subtext = content.substring(0, subtitleEnd);
				content = content.substring(subtitleEnd+1);
				rawText = content;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		private static IResource getResourceNullable(ResourceLocation rl)
		{
			try
			{
				return Minecraft.getMinecraft().getResourceManager().getResource(rl);
			}
			catch (IOException e)
			{
				return null;
			}
		}

		public ManualEntry create()
		{
			return new ManualEntry(manual, splitter, rawText, title, subtext, category);
		}
	}
	private static final SpecialManualElement NOT_SPECIAL = new SpecialManualElement()
	{

		@Override
		public void onOpened(GuiManual m, int x, int y, List<GuiButton> buttons)
		{}

		@Override
		public int getLinesTaken()
		{
			return 0;
		}

		@Override
		public void render(GuiManual m, int x, int y, int mouseX, int mouseY)
		{}

		@Override
		public void buttonPressed(GuiManual gui, GuiButton button)
		{}

		@Override
		public void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY,
								 GuiButton button)
		{}

		@Override
		public boolean listForSearch(String searchTag)
		{
			return false;
		}

		@Override
		public void recalculateCraftingRecipes()
		{}
	};
}
