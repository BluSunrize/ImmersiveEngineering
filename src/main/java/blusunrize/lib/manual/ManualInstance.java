/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.GuiManual;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;

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

	public abstract String formatLink(ManualLink link);

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

	public void openManual()
	{
	}

	public void closeManual()
	{
	}

	public void openEntry(String entry)
	{
	}

	public void titleRenderPre()
	{
	}

	public void titleRenderPost()
	{
	}

	public void entryRenderPre()
	{
	}

	public void entryRenderPost()
	{
	}

	public void tooltipRenderPre()
	{
	}

	public void tooltipRenderPost()
	{
	}

	public GuiManual getGui()
	{
		if(GuiManual.activeManual!=null&&GuiManual.activeManual.getManual()==this)
			return GuiManual.activeManual;
		return new GuiManual(this, texture);
	}

	public ArrayListMultimap<String, ManualEntry> manualContents = ArrayListMultimap.create();

	public void addEntry(String name, String category, IManualPage... pages)
	{
		manualContents.put(category, new ManualEntry(name, category, pages));
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
			this.name = name;
			this.category = category;
			this.pages = pages;
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

	public HashMap<Integer, ManualLink> itemLinks = Maps.newHashMap();

	public void indexRecipes()
	{
		itemLinks.clear();
		for(ManualEntry entry : manualContents.values())
		{
			int iP = 0;
			for(IManualPage p : entry.getPages())
			{
				p.recalculateCraftingRecipes();
				for(ItemStack s : p.getProvidedRecipes())
					itemLinks.put(getItemHash(s), new ManualLink(entry.getName(), iP));
				iP++;
			}
		}
	}

	public ManualLink getManualLink(ItemStack stack)
	{
		int hash = getItemHash(stack);
		return itemLinks.get(hash);
	}

	int getItemHash(ItemStack stack)
	{
		if(stack.isEmpty())
			return 0;
		int ret = ForgeRegistries.ITEMS.getKey(stack.getItem()).hashCode();
		if(stack.getHasSubtypes())
			ret = ret*31+stack.getMetadata();
		if(stack.hasTagCompound())
		{
			NBTTagCompound nbt = stack.getTagCompound();
			if(!nbt.isEmpty())
				ret = ret*31+nbt.hashCode();
		}
		return ret;
	}

	public static class ManualLink
	{
		private final String key;
		private final int page;

		public ManualLink(String key, int page)
		{
			this.key = key;
			this.page = page;
		}

		public String getKey()
		{
			return key;
		}

		public int getPage()
		{
			return page;
		}

		public void changePage(GuiManual guiManual)
		{
			guiManual.previousSelectedEntry.push(guiManual.getSelectedEntry());
			guiManual.setSelectedEntry(this.key);
			guiManual.page = this.page;
			guiManual.initGui();
		}
	}
}