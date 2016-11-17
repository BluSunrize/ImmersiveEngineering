package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.GuiManual;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.client.gui.FontRenderer;

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
	public abstract boolean allowGuiRescale();
	public abstract boolean improveReadability();

	public void openManual(){}
	public void closeManual(){}
	public void openEntry(String entry){}
	public void titleRenderPre(){}
	public void titleRenderPost(){}
	public void entryRenderPre(){}
	public void entryRenderPost(){}
	public void tooltipRenderPre(){}
	public void tooltipRenderPost(){}

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

	public void recalculateAllRecipes()
	{
		for(ManualEntry entry : manualContents.values())
			for(IManualPage p : entry.getPages())
				p.recalculateCraftingRecipes();
	}
}