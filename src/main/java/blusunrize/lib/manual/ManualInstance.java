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
import java.util.Map;
import java.util.stream.Stream;

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

	public void openManual(){}
	public void closeManual(){}
	public void openEntry(ManualEntry entry){}
	public void titleRenderPre(){}
	public void titleRenderPost(){}
	public void entryRenderPre(){}
	public void entryRenderPost(){}
	public void tooltipRenderPre(){}
	public void tooltipRenderPost(){}

	public GuiManual getGui()
	{
		if(GuiManual.activeManual!=null && GuiManual.activeManual.getManual()==this)
			return GuiManual.activeManual;
		return new GuiManual(this, texture);
	}

	public ArrayListMultimap<String, ManualEntry> manualContents = ArrayListMultimap.create();

	public void addEntry(ManualEntry entry)
	{
		manualContents.put(entry.getCategory(), entry);
	}
	public ManualEntry getEntry(String category, String name)
	{
		for(ManualEntry e : manualContents.get(category))
			if(e.getTitle().equalsIgnoreCase(name))
				return e;
		return null;
	}

	public HashMap<Integer, ManualLink> itemLinks = Maps.newHashMap();
	public void indexRecipes()
	{
		itemLinks.clear();
		for(ManualEntry entry : manualContents.values())
		{
			final int[] iP = {0};
			entry.getSpecials().forEach((p)->
			{
				p.recalculateCraftingRecipes();
				for(ItemStack s : p.getProvidedRecipes())
					itemLinks.put(getItemHash(s), new ManualLink(entry, iP[0]));
				iP[0]++;
			});
		}
	}
	public ManualLink getManualLink(ItemStack stack)
	{
		int hash = getItemHash(stack);
		return itemLinks.get(hash);
	}

	int getItemHash(ItemStack stack)
	{
		if (stack.isEmpty())
			return 0;
		int ret = ForgeRegistries.ITEMS.getKey(stack.getItem()).hashCode();
		if (stack.getHasSubtypes())
			ret = ret*31+stack.getMetadata();
		if (stack.hasTagCompound())
		{
			NBTTagCompound nbt = stack.getTagCompound();
			if (!nbt.hasNoTags())
				ret = ret * 31 + nbt.hashCode();
		}
		return ret;
	}

	public Stream<ManualEntry> getAllEntries()
	{
		return manualContents.entries().stream().map(Map.Entry::getValue);
	}

	public static class ManualLink
	{
		private final ManualEntry key;
		private final int page;

		public ManualLink(ManualEntry key, int page)
		{
			this.key = key;
			this.page = page;
		}

		public ManualEntry getKey()
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