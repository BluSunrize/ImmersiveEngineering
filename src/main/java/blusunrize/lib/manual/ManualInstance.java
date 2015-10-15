package blusunrize.lib.manual;

import net.minecraft.client.gui.FontRenderer;
import blusunrize.lib.manual.gui.GuiManual;

import com.google.common.collect.ArrayListMultimap;

public abstract class ManualInstance
{
	public FontRenderer fontRenderer;
	public String texture;
	public ManualInstance(FontRenderer fontRenderer, String texture)
	{
		this.fontRenderer = fontRenderer;
		this.texture = texture;
	}

	public abstract String getManualName();

	public abstract String[] getSortedCategoryList();
	public abstract String formatCategoryName(String s);
	public abstract String formatEntryName(String s);
	public abstract String formatEntrySubtext(String s);
	public abstract String formatText(String s);
	public abstract boolean showCategoryInList(String category);
	public abstract boolean showEntryInList(ManualEntry entry);

	public abstract int getTitleColour();
	public abstract int getSubTitleColour();
	public abstract int getTextColour();
	public abstract int getHighlightColour();
	public abstract int getPagenumberColour();

	public GuiManual getGui()
	{
		return new GuiManual(this, texture);
	}

	public ArrayListMultimap<String, ManualEntry> manualContents = ArrayListMultimap.create();

	public void addEntry(String name, String category, IManualPage... pages)
	{
		manualContents.put(category, new ManualEntry(name,category,pages));
	}
	public ManualEntry getEntry(String name)
	{
		for(ManualEntry e : manualContents.values())
			if(e.name.equalsIgnoreCase(name))
				return e;
		return null;
	}
	public static class ManualEntry
	{
		String name;
		String category;
		IManualPage[] pages;
		public ManualEntry(String name, String category, IManualPage... pages)
		{
			this.name=name;
			this.category=category;
			this.pages=pages;
		}

		public String getName()
		{
			return name;
		}
		public String getCategory()
		{
			return category;
		}
		public IManualPage[] getPages()
		{
			return pages;
		}
		public void setPages(IManualPage[] pages)
		{
			this.pages = pages;
		}
	}

}