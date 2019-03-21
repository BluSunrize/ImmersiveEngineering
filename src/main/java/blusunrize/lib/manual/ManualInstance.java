/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualElementImage.ManualImage;
import blusunrize.lib.manual.gui.GuiManual;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class ManualInstance implements ISelectiveResourceReloadListener
{
	public FontRenderer fontRenderer;
	public String texture;
	private Map<ResourceLocation, Function<JsonObject, SpecialManualElement>> specialElements = new HashMap<>();
	public final int pageWidth;
	public final int pageHeight;

	public ManualInstance(FontRenderer fontRenderer, String texture, int pageWidth, int pageHeight, ResourceLocation name)
	{
		this.fontRenderer = fontRenderer;
		this.texture = texture;
		this.pageHeight = pageHeight;
		this.pageWidth = pageWidth;
		contentTree = new Tree<>(name);
		((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
		registerSpecialElement(new ResourceLocation(name.getNamespace(), "crafting"), s -> {
			Object[] stacksAndRecipes;
			if(JsonUtils.isJsonArray(s, "recipes"))
			{
				JsonArray data = JsonUtils.getJsonArray(s, "recipes");
				stacksAndRecipes = new Object[data.size()];
				for(int i = 0; i < data.size(); i++)
				{
					JsonElement el = data.get(i);
					if(el.isJsonArray())
					{
						JsonArray inner = el.getAsJsonArray();
						Object[] innerSaR = new Object[inner.size()];
						for(int j = 0; j < inner.size(); ++j)
						{
							innerSaR[j] = ManualUtils.getRecipeObjFromJson(this, inner.get(j));
						}
						stacksAndRecipes[i] = innerSaR;
					}
					else if(el.isJsonObject())
						stacksAndRecipes[i] = ManualUtils.getRecipeObjFromJson(this, el.getAsJsonObject());
				}
			}
			else
			{
				stacksAndRecipes = new Object[1];
				stacksAndRecipes[0] = ManualUtils.getRecipeObjFromJson(this, s);
			}
			return new ManualElementCrafting(this, stacksAndRecipes);
		});
		registerSpecialElement(new ResourceLocation(name.getNamespace(), "image"),
				s -> {
					JsonArray data = JsonUtils.getJsonArray(s, "images");
					ManualImage[] images = new ManualImage[data.size()];
					for(int i = 0; i < data.size(); i++)
					{
						JsonObject img = data.get(i).getAsJsonObject();
						ResourceLocation loc = ManualUtils.getLocationForManual(
								JsonUtils.getString(img, "location"), this);
						int uMin = JsonUtils.getInt(img, "uMin");
						int vMin = JsonUtils.getInt(img, "vMin");
						int uSize = JsonUtils.getInt(img, "uSize");
						int vSize = JsonUtils.getInt(img, "vSize");
						images[i] = new ManualImage(loc, uMin, uSize, vMin, vSize);
					}
					return new ManualElementImage(this, images);
				}
		);
		//TODO test these
		registerSpecialElement(new ResourceLocation(name.getNamespace(), "item_display"),
				s -> {
					JsonElement items = s.get("items");
					NonNullList<ItemStack> stacks;
					JsonContext ctx = new JsonContext(name.getNamespace());
					if(items.isJsonObject())
					{
						stacks = NonNullList.withSize(1, CraftingHelper.getItemStack(items.getAsJsonObject(), ctx));
					}
					else
					{
						JsonArray arr = items.getAsJsonArray();
						stacks = NonNullList.withSize(arr.size(), ItemStack.EMPTY);
						for(int i = 0; i < arr.size(); i++)
							stacks.set(i, CraftingHelper.getItemStack(arr.get(i).getAsJsonObject(), ctx));
					}
					return new ManualElementItem(this, stacks);
				}
		);
		registerSpecialElement(new ResourceLocation(name.getNamespace(), "table"),
				s -> {
					JsonArray arr = JsonUtils.getJsonArray(s, "table");
					String[][] table = new String[arr.size()][];
					for(int i = 0; i < table.length; i++)
					{
						JsonArray row = arr.get(i).getAsJsonArray();
						table[i] = new String[row.size()];
						for(int j = 0; j < row.size(); j++)
							table[i][j] = row.get(j).getAsString();
					}
					return new ManualElementTable(this, table, JsonUtils.getBoolean(s,
							"horizontal_bars", false));
				}
		);
	}

	public void registerSpecialElement(ResourceLocation resLoc, Function<JsonObject, SpecialManualElement> factory)
	{
		if(specialElements.containsKey(resLoc))
			throw new IllegalArgumentException("Tried adding manual element type "+resLoc+" twice!");
		specialElements.put(resLoc, factory);
	}

	public Function<JsonObject, SpecialManualElement> getElementFactory(ResourceLocation loc)
	{
		Function<JsonObject, SpecialManualElement> ret = specialElements.get(loc);
		if(ret==null)
			throw new IllegalArgumentException("No element type found for "+loc);
		return ret;
	}

	public abstract String getDefaultResourceDomain();

	public abstract String getManualName();

	public abstract String formatCategoryName(ResourceLocation s);

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

	public void addEntry(Tree.Node<ResourceLocation, ManualEntry> node, ResourceLocation source)
	{
		ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
		builder.readFromFile(source);
		addEntry(node, builder.create());
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
			TIntObjectMap<SpecialManualElement> specials = entry.getSpecials();
			TIntIterator it = specials.keySet().iterator();
			while(it.hasNext())
			{
				int page = it.next();
				SpecialManualElement p = specials.get(page);
				p.recalculateCraftingRecipes();
				for(ItemStack s : p.getProvidedRecipes())
					itemLinks.put(getItemHash(s), new ManualLink(entry, -1, page));
			}
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
			if(!nbt.isEmpty())
				ret = ret*31+nbt.hashCode();
		}
		return ret;
	}

	public Stream<ManualEntry> getAllEntries()
	{
		return contentTree.leafStream();
	}

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, @Nonnull Predicate<IResourceType> resourcePredicate)
	{
		if(resourcePredicate.test(VanillaResourceType.LANGUAGES))
		{
			getAllEntries().forEach(ManualEntry::refreshPages);
		}
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
			if(addCurrentToStack)// && guiManual.getCurrentPage()!=key)
				guiManual.previousSelectedEntry.push(new ManualLink(guiManual.getCurrentPage(), -1, guiManual.page));
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