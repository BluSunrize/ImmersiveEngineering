/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.GuiManual;
import com.google.common.collect.Maps;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class ManualInstance
{
	public FontRenderer fontRenderer;
	public String texture;
	private Map<ResourceLocation, Function<String, SpecialManualElement>> specialElements = new HashMap<>();

	public ManualInstance(FontRenderer fontRenderer, String texture, ResourceLocation name)
	{
		this.fontRenderer = fontRenderer;
		this.texture = texture;
		contentTree = new Tree<>(name);
	}

	public void registerSpecialElement(ResourceLocation resLoc, Function<String, SpecialManualElement> factory)
	{
		if(specialElements.containsKey(resLoc))
			throw new IllegalArgumentException("Tried adding manual element type "+resLoc+" twice!");
		specialElements.put(resLoc, factory);
	}

	public Function<String, SpecialManualElement> getElementFactory(ResourceLocation loc)
	{
		Function<String, SpecialManualElement> ret = specialElements.get(loc);
		if(ret==null)
			throw new IllegalArgumentException("No element type found for "+loc);
		return ret;
	}

	public abstract String getDefaultResourceDomain();

	public abstract String getManualName();

	public abstract String formatCategoryName(String s);

	public abstract String formatEntryName(String s);

	public abstract String formatEntrySubtext(String s);

	public abstract String formatLink(ManualLink link);

	public abstract String formatText(String s);

	public abstract boolean showCategoryInList(String category);

	public abstract boolean showNodeInList(Tree.AbstractNode<ResourceLocation, ManualEntry> node);

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

	public void openEntry(ManualEntry entry)
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

	public final Tree<ResourceLocation, ManualEntry> contentTree;
	public Map<ResourceLocation, ManualEntry> contentsByName = new HashMap<>();

	public void addEntry(Tree.Node<ResourceLocation, ManualEntry> node, ManualEntry entry)
	{
		node.addNewLeaf(entry);
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
		getAllEntries().forEach((entry) ->
		{
			final int[] iP = {0};
			entry.getSpecials().forEach((p) ->
			{
				p.recalculateCraftingRecipes();
				for(ItemStack s : p.getProvidedRecipes())
					itemLinks.put(getItemHash(s), new ManualLink(entry, iP[0], 0));
				iP[0]++;
			});
		});
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
			if(!nbt.hasNoTags())
				ret = ret*31+nbt.hashCode();
		}
		return ret;
	}

	public Stream<ManualEntry> getAllEntries()
	{
		return contentTree.leafStream();
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

		public void changePage(GuiManual guiManual, boolean addCurrentToStack)
		{
			if(addCurrentToStack)
				guiManual.previousSelectedEntry.push(new ManualLink(key, -1, guiManual.page));
			guiManual.setCurrentNode(this.key.getTreeNode());
			guiManual.page = getPage();
			guiManual.initGui();
		}

		public int getPage()
		{
			return getKey().getPageForAnchor(getAnchor())+getOffset();
		}
	}
}