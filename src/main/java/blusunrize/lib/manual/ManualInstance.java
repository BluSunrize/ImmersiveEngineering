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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

	public Multimap<String, ManualEntry> contentsByCategory = ArrayListMultimap.create();
	public Map<ResourceLocation, ManualEntry> contentsByName = new HashMap<>();

	public void addEntry(ManualEntry entry)
	{
		contentsByCategory.put(entry.getCategory(), entry);
		contentsByName.put(entry.getLocation(), entry);
	}

	@Nullable
	public ManualEntry getEntry(ResourceLocation loc)
	{
		return contentsByName.get(loc);
	}

	public HashMap<Integer, ManualLink> itemLinks = Maps.newHashMap();
	public void indexRecipes()
	{
		itemLinks.clear();
		for(ManualEntry entry : contentsByCategory.values())
		{
			final int[] iP = {0};
			entry.getSpecials().forEach((p)->
			{
				p.recalculateCraftingRecipes();
				for(ItemStack s : p.getProvidedRecipes())
					itemLinks.put(getItemHash(s), new ManualLink(entry, iP[0], 0));
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
		return contentsByCategory.entries().stream().map(Map.Entry::getValue);
	}

	public static class ManualLink
	{
		@Nonnull
		private final ManualEntry key;
		private final int anchor;
		private final int offset;

		public ManualLink(@Nonnull ManualEntry key, int anchor, int offset)
		{
			this.key = key;
			this.anchor = anchor;
			this.offset = offset;
		}

		@Nonnull
		public ManualEntry getKey()
		{
			return key;
		}

		public int getAnchor()
		{
			return anchor;
		}

		public int getOffset()
		{
			return offset;
		}

		public void changePage(GuiManual guiManual)
		{
			guiManual.previousSelectedEntry.push(guiManual.getSelectedEntry());
			guiManual.setSelectedEntry(this.key);
			guiManual.page = getPage();
			guiManual.initGui();
		}

		public int getPage()
		{
			return getKey().getPageForAnchor(getAnchor())+getOffset();
		}
	}
}