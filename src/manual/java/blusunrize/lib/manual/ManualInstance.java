/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.ManualElementImage.ManualImage;
import blusunrize.lib.manual.ManualEntry.ManualEntryBuilder;
import blusunrize.lib.manual.Tree.AbstractNode;
import blusunrize.lib.manual.Tree.InnerNode;
import blusunrize.lib.manual.Tree.Leaf;
import blusunrize.lib.manual.gui.ManualScreen;
import blusunrize.lib.manual.utils.ManualLogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleResource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.packs.DelegatingResourcePack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class ManualInstance implements ISelectiveResourceReloadListener
{
	public ResourceLocation texture;
	private Map<ResourceLocation, Function<JsonObject, SpecialManualElement>> specialElements = new HashMap<>();
	private final Tree<ResourceLocation, ManualEntry> contentTree;
	private final List<Pair<List<ResourceLocation>, ManualEntry>> autoloadedEntries = new ArrayList<>();
	private final List<List<ResourceLocation>> autoloadedSections = new ArrayList<>();
	public Map<ResourceLocation, ManualEntry> contentsByName = new HashMap<>();
	public final int pageWidth;
	public final int pageHeight;
	private int numFailedEntries = 0;

	private boolean initialized = false;

	public ManualInstance(ResourceLocation texture, int pageWidth, int pageHeight, ResourceLocation name)
	{
		this.texture = texture;
		this.pageHeight = pageHeight;
		this.pageWidth = pageWidth;
		contentTree = new Tree<>(name);
		((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(this);
		registerSpecialElement(new ResourceLocation(name.getNamespace(), "crafting"), s -> {
			Object[] stacksAndRecipes;
			if(GsonHelper.isArrayNode(s, "recipes"))
			{
				JsonArray data = GsonHelper.getAsJsonArray(s, "recipes");
				stacksAndRecipes = new Object[data.size()];
				for(int i = 0; i < data.size(); i++)
				{
					JsonElement el = data.get(i);
					if(el.isJsonArray())
					{
						JsonArray inner = el.getAsJsonArray();
						Object[] innerSaR = new Object[inner.size()];
						for(int j = 0; j < inner.size(); ++j)
							innerSaR[j] = ManualUtils.getRecipeObjFromJson(this, inner.get(j));
						stacksAndRecipes[i] = innerSaR;
					}
					else
						stacksAndRecipes[i] = ManualUtils.getRecipeObjFromJson(this, el);
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
					JsonArray data = GsonHelper.getAsJsonArray(s, "images");
					ManualImage[] images = new ManualImage[data.size()];
					for(int i = 0; i < data.size(); i++)
					{
						JsonObject img = data.get(i).getAsJsonObject();
						ResourceLocation loc = ManualUtils.getLocationForManual(
								GsonHelper.getAsString(img, "location"), this);
						int uMin = GsonHelper.getAsInt(img, "uMin");
						int vMin = GsonHelper.getAsInt(img, "vMin");
						int uSize = GsonHelper.getAsInt(img, "uSize");
						int vSize = GsonHelper.getAsInt(img, "vSize");
						images[i] = new ManualImage(loc, uMin, uSize, vMin, vSize);
					}
					return new ManualElementImage(this, images);
				}
		);
		registerSpecialElement(new ResourceLocation(name.getNamespace(), "item_display"),
				s -> {
					NonNullList<ItemStack> stacks;
					if(s.has("item"))
						stacks = NonNullList.withSize(1, ManualUtils.getItemStackFromJson(this, s.get("item")));
					else
					{
						JsonElement items = s.get("items");
						JsonArray arr = items.getAsJsonArray();
						stacks = NonNullList.withSize(arr.size(), ItemStack.EMPTY);
						for(int i = 0; i < arr.size(); i++)
							stacks.set(i, ManualUtils.getItemStackFromJson(this, arr.get(i)));
					}
					return new ManualElementItem(this, stacks);
				}
		);
		registerSpecialElement(new ResourceLocation(name.getNamespace(), "table"),
				s -> {
					JsonArray arr = GsonHelper.getAsJsonArray(s, "table");
					Component[][] table = new Component[arr.size()][];
					for(int i = 0; i < table.length; i++)
					{
						JsonArray row = arr.get(i).getAsJsonArray();
						table[i] = new Component[row.size()];
						for(int j = 0; j < row.size(); j++)
							table[i][j] = Component.nullToEmpty(row.get(j).getAsString());
					}
					return new ManualElementTable(this, table, GsonHelper.getAsBoolean(s,
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

	public Tree.InnerNode<ResourceLocation, ManualEntry> getRoot()
	{
		return contentTree.getRoot();
	}

	public Stream<Tree.AbstractNode<ResourceLocation, ManualEntry>> getAllEntriesAndCategories()
	{
		return contentTree.fullStream();
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

	public abstract int getGuiRescale();

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

	public ManualScreen getGui()
	{
		return this.getGui(true);
	}

	public ManualScreen getGui(boolean useLastActive)
	{
		if(useLastActive&&ManualScreen.lastActiveManual!=null&&ManualScreen.lastActiveManual.getManual()==this)
			return ManualScreen.lastActiveManual;
		if(!initialized)
		{
			long start = System.currentTimeMillis();
			reload();
			ManualLogger.LOGGER.info("Manual reload took {} ms", System.currentTimeMillis()-start);
			if(numFailedEntries > 0)
			{
				Player player = Minecraft.getInstance().player;
				String error = numFailedEntries+" entries failed to load! Please report this as an issue with your log file!";
				if(player!=null)
					player.sendMessage(
							new TextComponent(error).setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED)),
							Util.NIL_UUID
					);
				else
					ManualLogger.LOGGER.error(error);
				return null;
			}
		}
		return new ManualScreen(this, texture, useLastActive);
	}

	public void addEntry(InnerNode<ResourceLocation, ManualEntry> node, ManualEntry entry)
	{
		int nextPrio = node.getChildren().size();
		addEntry(node, entry, nextPrio);
	}

	public void addEntry(InnerNode<ResourceLocation, ManualEntry> node, ManualEntry entry, int priority)
	{
		addEntry(node, entry, () -> priority);
	}

	public void addEntry(InnerNode<ResourceLocation, ManualEntry> node, ManualEntry entry, DoubleSupplier priority)
	{
		node.addNewLeaf(entry, priority);
		reset();
	}

	public void reset()
	{
		initialized = false;
		ManualScreen.lastActiveManual = null;
	}

	public ManualEntry addEntry(InnerNode<ResourceLocation, ManualEntry> node, ResourceLocation source)
	{
		int nextPrio = node.getChildren().size();
		return addEntry(node, source, nextPrio);
	}

	public ManualEntry addEntry(InnerNode<ResourceLocation, ManualEntry> node, ResourceLocation source, int priority)
	{
		return addEntry(node, source, () -> priority);
	}

	public ManualEntry addEntry(InnerNode<ResourceLocation, ManualEntry> node, ResourceLocation source, DoubleSupplier priority)
	{
		ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(this);
		builder.readFromFile(source);
		ManualEntry entry = builder.create();
		addEntry(node, entry, priority);
		return entry;
	}

	public DoubleSupplier atOffsetFrom(InnerNode<ResourceLocation, ManualEntry> node, String baseEntry, double offset)
	{
		return atOffsetFrom(node, ManualUtils.getLocationForManual(baseEntry, this), offset);
	}

	public DoubleSupplier atOffsetFrom(InnerNode<ResourceLocation, ManualEntry> node, ResourceLocation baseEntry, double offset)
	{
		return () -> {
			double baseWeight = findEntry(baseEntry, node).getWeight();
			return baseWeight+offset;
		};
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
			Int2ObjectMap<SpecialManualElement> specials = entry.getSpecials();
			for(int page : specials.keySet())
			{
				SpecialManualElement p = specials.get(page);
				p.recalculateCraftingRecipes();
				for(ItemStack s : p.getProvidedRecipes())
					itemLinks.put(getItemHash(s), new ManualLink(entry, TextSplitter.START, page));
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
		if(stack.hasTag())
		{
			CompoundTag nbt = stack.getTag();
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
	public void onResourceManagerReload(@Nonnull ResourceManager resourceManager, @Nonnull Predicate<IResourceType> resourcePredicate)
	{
		reset();
	}

	public void reload()
	{
		cleanupOldAutoloadedEntries();
		numFailedEntries = 0;
		getAllEntries().forEach(manualEntry -> {
			try
			{
				manualEntry.refreshPages();
			} catch(Exception x)
			{
				x.printStackTrace();
				++numFailedEntries;
			}
		});
		loadAutoEntries();
		if(numFailedEntries==0)
		{
			contentTree.sortAll();
			contentsByName.clear();
			contentTree.leafStream().forEach(e -> this.contentsByName.put(e.getLocation(), e));
			indexRecipes();
			initialized = true;
		}
	}

	private void cleanupOldAutoloadedEntries()
	{
		for(Pair<List<ResourceLocation>, ManualEntry> toRemove : autoloadedEntries)
		{
			getOrCreatePath(toRemove.getLeft(), p -> {
			}, 0).removeLeaf(toRemove.getRight());
		}
		for(List<ResourceLocation> toRemove : autoloadedSections)
		{
			List<ResourceLocation> parent = toRemove.subList(0, toRemove.size()-1);
			getOrCreatePath(parent, p -> {
				throw new RuntimeException(
						p.toString()+" does not exist, but "+toRemove.get(parent.size())+" should exist?"
				);
			}, 0).removeSubnode(toRemove.get(parent.size()));
		}
		autoloadedEntries.clear();
		autoloadedSections.clear();
	}

	private void loadAutoEntries()
	{
		ResourceLocation autoLoc = ManualUtils.getLocationForManual("manual/autoload.json", this);
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		List<Resource> autoload = new ArrayList<>();
		resourceManager.listPacks().forEach(packResources -> getActuallyAllResources(autoLoc, packResources, autoload));
		NavigableSet<Pair<Double, JsonObject>> autoloadSources = new TreeSet<>(Comparator.comparingDouble(Pair::getLeft));
		for(Resource r : autoload)
		{
			JsonObject autoloadJson = GsonHelper.parse(new InputStreamReader(r.getInputStream()));
			double priority = 0;
			JsonElement priorityElement = autoloadJson.remove("autoload_priority");
			if(priorityElement!=null)
				priority = priorityElement.getAsDouble();
			autoloadSources.add(Pair.of(priority, autoloadJson));
		}
		for(Pair<Double, JsonObject> p : autoloadSources.descendingSet())
			autoloadEntriesFromJson(p.getRight(), new ArrayList<>());
	}

	private static final Lazy<Field> CLIENT_RESOURCES = Lazy.of(() -> {
		try
		{
			Field clientResources = DelegatingResourcePack.class.getDeclaredField("namespacesAssets");
			clientResources.setAccessible(true);
			return clientResources;
		} catch(Exception x)
		{
			throw new RuntimeException(x);
		}
	});

	/**
	 * ResourceManager#getResources fails to get all resources when multiple mods contain the same file since (at least
	 * in dev?) all mods are bunched up into a single DelegatingResourcePack, which can only return one resource. This
	 * "breaks open" DelegatingResourcePacks and actually gets *all* resources.
	 */
	private void getActuallyAllResources(ResourceLocation path, PackResources resources, List<Resource> out)
	{
		final PackType type = PackType.CLIENT_RESOURCES;
		try
		{
			if(resources instanceof DelegatingResourcePack)
			{
				Object rawValue = CLIENT_RESOURCES.get().get(resources);
				Map<String, List<PackResources>> subResources = (Map<String, List<PackResources>>)rawValue;
				for(PackResources subResource : subResources.getOrDefault(path.getNamespace(), ImmutableList.of()))
					getActuallyAllResources(path, subResource, out);
			}
			else if(resources.hasResource(type, path))
				out.add(new SimpleResource(resources.getName(), path, resources.getResource(type, path), null));
		} catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private void autoloadEntriesFromJson(JsonObject obj, List<ResourceLocation> backtrace)
	{
		final String entryListKey = "entry_list";
		final String weightKey = "category_weight";
		double catWeight;
		if(obj.has(weightKey))
			catWeight = obj.remove(weightKey).getAsDouble();
		else
			catWeight = 0;
		InnerNode<ResourceLocation, ManualEntry> node = getOrCreatePath(backtrace, path -> {
			boolean parentIsAutoloaded = false;
			for(int i = 1; i <= path.size(); ++i)
				if(autoloadedSections.contains(path.subList(0, i)))
				{
					parentIsAutoloaded = true;
					break;
				}
			if(!parentIsAutoloaded)
				autoloadedSections.add(path);
		}, catWeight);
		if(obj.has(entryListKey))
			loadEntriesInArray(obj.remove(entryListKey).getAsJsonArray(), backtrace, node);
		for(Entry<String, JsonElement> otherEntry : obj.entrySet())
		{
			Preconditions.checkState(otherEntry.getValue().isJsonObject(), "At backtrace %s, key %s", backtrace, otherEntry.getKey());
			backtrace.add(ManualUtils.getLocationForManual(otherEntry.getKey(), this));
			autoloadEntriesFromJson(otherEntry.getValue().getAsJsonObject(), new ArrayList<>(backtrace));
			backtrace.remove(backtrace.size()-1);
		}
	}

	private void loadEntriesInArray(JsonArray entriesOnLevel, List<ResourceLocation> backtrace, InnerNode<ResourceLocation, ManualEntry> mainNode)
	{
		for(JsonElement e : entriesOnLevel)
		{
			String source;
			double weight;
			if(e.isJsonObject())
			{
				source = e.getAsJsonObject().get("source").getAsString();
				weight = e.getAsJsonObject().get("weight").getAsDouble();
			}
			else
			{
				source = e.getAsString();
				weight = mainNode.getChildren().size();
			}
			try
			{
				ManualEntryBuilder builder = new ManualEntryBuilder(this);
				builder.readFromFile(ManualUtils.getLocationForManual(source, this));
				ManualEntry entry = builder.create();
				mainNode.addNewLeaf(entry, () -> weight);
				autoloadedEntries.add(Pair.of(backtrace, entry));
				entry.refreshPages();
			} catch(Exception x)
			{
				x.printStackTrace();
				++numFailedEntries;
			}
		}
	}

	private Tree.InnerNode<ResourceLocation, ManualEntry> getOrCreatePath(
			List<ResourceLocation> path, Consumer<List<ResourceLocation>> onCreated, double newCatWeight
	)
	{
		InnerNode<ResourceLocation, ManualEntry> currentNode = getRoot();
		List<ResourceLocation> currentPath = new ArrayList<>();
		for(ResourceLocation inner : path)
		{
			currentPath.add(inner);
			final InnerNode<ResourceLocation, ManualEntry> lastNode = currentNode;
			currentNode = currentNode.getSubnode(inner).orElseGet(() -> {
				onCreated.accept(new ArrayList<>(currentPath));
				return lastNode.getOrCreateSubnode(inner, () -> newCatWeight);
			});
			Preconditions.checkNotNull(currentNode);
		}
		return currentNode;
	}

	public Leaf<ResourceLocation, ManualEntry> findEntry(ResourceLocation name, InnerNode<ResourceLocation, ManualEntry> parent)
	{
		for(AbstractNode<ResourceLocation, ManualEntry> child : parent.getChildren())
		{
			ResourceLocation loc = child.getLeafData().getLocation();
			if(child.isLeaf()&&loc.equals(name))
				return (Leaf<ResourceLocation, ManualEntry>)child;
		}
		throw new NoSuchElementException("Did not find a child with name "+name);
	}

	public abstract Font fontRenderer();

	public static class ManualLink
	{
		@Nonnull
		private final ManualEntry key;
		private final String anchor;
		private final int offset;

		public ManualLink(@Nonnull ManualEntry key, String anchor, int offset)
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

		public String getAnchor()
		{
			return anchor;
		}

		public int getOffset()
		{
			return offset;
		}

		public void changePage(ManualScreen manualScreen, boolean addCurrentToStack)
		{
			if(addCurrentToStack)// && guiManual.getCurrentPage()!=key)
				manualScreen.previousSelectedEntry.push(new ManualLink(manualScreen.getCurrentPage(), TextSplitter.START, manualScreen.page));
			manualScreen.setCurrentNode(this.key.getTreeNode());
			manualScreen.page = getPage();
			manualScreen.fullInit();
		}

		public int getPage()
		{
			return getKey().getPageForAnchor(getAnchor())+getOffset();
		}
	}
}