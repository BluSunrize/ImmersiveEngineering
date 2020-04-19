package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.multiblocks.ManualElementMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.OreOutput;
import blusunrize.immersiveengineering.client.manual.IEManualInstance;
import blusunrize.immersiveengineering.client.manual.ShaderManualElement;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.lib.manual.*;
import blusunrize.lib.manual.ManualEntry.ManualEntryBuilder;
import blusunrize.lib.manual.Tree.InnerNode;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

public class IEManual
{
	public static void initManual()
	{

		ManualHelper.ieManualInstance = new IEManualInstance();

		IEManualInstance ieMan = ManualHelper.getManual();
		ieMan.registerSpecialElement(new ResourceLocation(MODID, "blueprint"),
				s -> {
					ItemStack[] stacks;
					if(JSONUtils.isJsonArray(s, "recipes"))
					{
						JsonArray arr = s.get("recipes").getAsJsonArray();
						stacks = new ItemStack[arr.size()];
						for(int i = 0; i < stacks.length; ++i)
							stacks[i] = CraftingHelper.getItemStack(arr.get(i).getAsJsonObject(), true);
					}
					else
					{
						JsonElement recipe = s.get("recipe");
						Preconditions.checkArgument(recipe.isJsonObject());
						stacks = new ItemStack[]{
								CraftingHelper.getItemStack(recipe.getAsJsonObject(), true)
						};
					}
					return new ManualElementBlueprint(ieMan, stacks);
				});
		ieMan.registerSpecialElement(new ResourceLocation(MODID, "multiblock"),
				s -> {
					ResourceLocation name = ManualUtils.getLocationForManual(
							JSONUtils.getString(s, "name"),
							ieMan
					);
					IMultiblock mb = MultiblockHandler.getByUniqueName(name);
					if(mb==null)
						throw new NullPointerException("Multiblock "+name+" does not exist");
					return new ManualElementMultiblock(ieMan, mb);
				});
	}

	public static void addIEManualEntries()
	{
		IEManualInstance ieMan = ManualHelper.getManual();
		InnerNode<ResourceLocation, ManualEntry> generalCat = ieMan.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_GENERAL), 0);
		InnerNode<ResourceLocation, ManualEntry> constructionCat = ieMan.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_CONSTRUCTION), 10);
		InnerNode<ResourceLocation, ManualEntry> energyCat = ieMan.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_ENERGY), 20);
		InnerNode<ResourceLocation, ManualEntry> toolsCat = ieMan.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_TOOLS), 30);
		InnerNode<ResourceLocation, ManualEntry> machinesCat = ieMan.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_MACHINES), 40);
		InnerNode<ResourceLocation, ManualEntry> heavyMachinesCat = ieMan.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_HEAVYMACHINES), 50);

		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "wiring"), 0);
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "generator"), 1);
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "breaker"), 2);
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "current_transformer"), 3);
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "redstone_wire"), 4);
		{
			ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
			builder.addSpecialElement("values", 0,
					addDynamicTable(
							() -> ThermoelectricHandler.getThermalValuesSorted(true),
							"K"
					)
			);
			builder.readFromFile(new ResourceLocation(MODID, "thermoelectric"));
			ieMan.addEntry(energyCat, builder.create(), 5);
		}
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "diesel_generator"), 6);
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "lightning_rod"), 7);

		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "introduction"), -1);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "hemp"), 0);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "ores"), 1);
		ieMan.addEntry(generalCat, handleMineralManual(ieMan), 2);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "alloys"), 3);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "components"), 4);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "plates"), 5);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "workbench"), 6);
		ResourceLocation blueprints = new ResourceLocation(MODID, "blueprints");
		ieMan.addEntry(generalCat, blueprints);
		ieMan.hideEntry(blueprints);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "alloykiln"), 7);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "cokeoven"), 8);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "crude_blast_furnace"), 9);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "improved_blast_furnace"), 10);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "graphite"), 11);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "shader"), 12);

		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "treated_wood"), 0);
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "multiblocks"), 1);
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "balloon"), 2);
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "crate"), 3);
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "barrel"), 4);
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "metalconstruction"), 5);
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "metal_barrel"), 6);
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "concrete"), 7);
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "lighting"), 8);
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "silo"), 9);
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "tank"), 10);

		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "toolbox"), 0);
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "jerrycan"), 1);
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "ear_defenders"), 2);
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "buzzsaw"), 3);
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "mining_drill"), 4);
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "maintenance_kit"), 5);
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "shield"), 6);
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "revolver"), 7);
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "bullets"), 8);
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "chemthrower"), 9);
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "skyhook"), 10);
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "powerpack"), 11);
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "railgun"), 12);

		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "conveyors"), 0);
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "item_router"), 1);
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "item_batcher"), 2);
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "turntable"), 3);
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "fluid_transport"), 4);
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "fluid_router"), 5);
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "external_heater"), 6);
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "charging_station"), 7);
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "garden_cloche"), 8);
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "tesla_coil"), 9);
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "razor_wire"), 10);
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "turrets"), 11);

		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "metal_press"), 0);
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "crusher"), 1);
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "assembler"), 2);
		{
			ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
			builder.addSpecialElement("list", 0,
					addDynamicTable(
							() -> FermenterRecipe.getFluidValuesSorted(IEContent.fluidEthanol, true),
							"mB"
					)
			);
			builder.readFromFile(new ResourceLocation(MODID, "fermenter"));
			ieMan.addEntry(heavyMachinesCat, builder.create(), 3);
		}
		{
			ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
			builder.addSpecialElement("list", 0,
					addDynamicTable(
							() -> SqueezerRecipe.getFluidValuesSorted(IEContent.fluidPlantoil, true),
							"mB"
					)
			);
			builder.readFromFile(new ResourceLocation(MODID, "squeezer"));
			ieMan.addEntry(heavyMachinesCat, builder.create(), 4);
		}
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "refinery"), 5);
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "mixer"), 6);
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "bottling_machine"), 7);
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "automated_workbench"), 8);
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "arc_furnace"), 9);
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "excavator"), 10);

		{
			ManualEntry.ManualEntryBuilder builder = new ManualEntryBuilder(ieMan);
			builder.setContent(
					//TODO translation
					() -> "Shader list",
					() -> "",
					() -> {
						StringBuilder content = new StringBuilder();
						for(ShaderRegistryEntry shader : ShaderRegistry.shaderRegistry.values())
						{
							String key = shader.name.getPath();
							content.append("<&").append(key).append(">").append("<np>");
						}
						return content.toString();
					}
			);
			for(ShaderRegistryEntry shader : ShaderRegistry.shaderRegistry.values())
			{
				String key = shader.name.getPath();
				builder.addSpecialElement(key, 0, new ShaderManualElement(ieMan, shader));
			}
			builder.setLocation(new ResourceLocation(MODID, "shader_list"));
			ManualEntry e = builder.create();
			ieMan.addEntry(generalCat, e);
			ieMan.hideEntry(e.getLocation());
		}

		addChangelogToManual();
	}


	private static ManualEntry handleMineralManual(IEManualInstance ieManual)
	{
		ManualEntryBuilder builder = new ManualEntryBuilder(ieManual);
		builder.addSpecialElement("drill", 0, new ManualElementCrafting(ieManual, new ResourceLocation(MODID, "sample_drill")));

		builder.setContent(IEManual::setupMineralEntry);
		builder.setLocation(new ResourceLocation(MODID, "minerals"));
		return builder.create();
	}

	private static Supplier<ManualElementTable> addDynamicTable(
			Supplier<SortedMap<String, Integer>> getContents,
			String valueType
	)
	{
		return () -> {
			String[][] table = formatToTable_ItemIntMap(getContents.get(), valueType);
			return new ManualElementTable(ManualHelper.getManual(), table, false);
		};
	}

	private static String[] setupMineralEntry(TextSplitter splitter)
	{
		final ExcavatorHandler.MineralMix[] minerals = ExcavatorHandler.mineralList.keySet().toArray(new ExcavatorHandler.MineralMix[0]);

		List<MineralMix> mineralsToAdd = new ArrayList<>();
		for(MineralMix mineral : minerals)
		{
			mineral.recalculateChances();
			if(mineral.isValid())
				mineralsToAdd.add(mineral);
		}
		Function<MineralMix, String> toName = mineral -> {
			String name = Lib.DESC_INFO+"mineral."+mineral.name;
			String localizedName = I18n.format(name);
			if(localizedName.equals(name))
				localizedName = mineral.name;
			return localizedName;
		};
		mineralsToAdd.sort((i1, i2) -> toName.apply(i1).compareToIgnoreCase(toName.apply(i2)));
		StringBuilder entry = new StringBuilder(I18n.format("ie.manual.entry.mineral_main"));
		for(MineralMix mineral : mineralsToAdd)
		{
			String name = Lib.DESC_INFO+"mineral."+mineral.name;
			String localizedName = I18n.format(name);
			if(localizedName.equalsIgnoreCase(name))
				localizedName = mineral.name;

			String dimensionString;
			if(mineral.dimensionWhitelist!=null&&mineral.dimensionWhitelist.size() > 0)
			{
				StringBuilder validDims = new StringBuilder();
				for(DimensionType dim : mineral.dimensionWhitelist)
					validDims.append((validDims.length() > 0)?", ": "")
							.append("<dim;")
							.append(dim)
							.append(">");
				dimensionString = I18n.format("ie.manual.entry.mineralsDimValid", localizedName, validDims.toString());
			}
			else if(mineral.dimensionBlacklist!=null&&mineral.dimensionBlacklist.size() > 0)
			{
				StringBuilder invalidDims = new StringBuilder();
				for(DimensionType dim : mineral.dimensionBlacklist)
					invalidDims.append((invalidDims.length() > 0)?", ": "")
							.append("<dim;")
							.append(dim)
							.append(">");
				dimensionString = I18n.format("ie.manual.entry.mineralsDimInvalid", localizedName, invalidDims.toString());
			}
			else
				dimensionString = I18n.format("ie.manual.entry.mineralsDimAny", localizedName);

			List<OreOutput> formattedOutputs = new ArrayList<>();
			for(OreOutput o : mineral.outputs)
				if(!o.stack.isEmpty())
					formattedOutputs.add(o);
			formattedOutputs.sort(Comparator.comparingDouble(i -> -i.recalculatedChance));

			StringBuilder outputString = new StringBuilder();
			NonNullList<ItemStack> sortedOres = NonNullList.create();
			for(OreOutput sorted : formattedOutputs)
			{
				outputString
						.append("\n")
						.append(
								new DecimalFormat("00.00")
										.format(sorted.recalculatedChance*100)
										.replaceAll("\\G0", "\u00A0")
						).append("% ")
						.append(sorted.stack.getDisplayName().getFormattedText());
				sortedOres.add(sorted.stack);
			}
			splitter.addSpecialPage(mineral.name, 0, new ManualElementItem(ManualHelper.getManual(), sortedOres));
			String desc = I18n.format("ie.manual.entry.minerals_desc", dimensionString, outputString.toString());
			if(entry.length() > 0)
				entry.append("<np>");
			entry.append("<&")
					.append(mineral.name)
					.append(">")
					.append(desc);
		}
		return new String[]{
				I18n.format("ie.manual.entry.mineral_title"),
				I18n.format("ie.manual.entry.mineral_subtitle"),
				entry.toString()
		};
	}

	private static void addChangelogToManual()
	{
		SortedMap<ComparableVersion, ManualEntry> allChanges = new TreeMap<>(Comparator.reverseOrder());
		ComparableVersion currIEVer = new ComparableVersion(ImmersiveEngineering.VERSION);
		//Included changelog
		try(InputStream in = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(MODID,
				"changelog.json")).getInputStream())
		{
			JsonElement ele = new JsonParser().parse(new InputStreamReader(in));
			JsonObject upToCurrent = ele.getAsJsonObject();
			for(Entry<String, JsonElement> entry : upToCurrent.entrySet())
			{
				ComparableVersion version = new ComparableVersion(entry.getKey());
				ManualEntry manualEntry = addVersionToManual(currIEVer, version,
						entry.getValue().getAsString(), false);
				if(manualEntry!=null)
					allChanges.put(version, manualEntry);
			}
		} catch(IOException x)
		{
			x.printStackTrace();
		}
		//Changelog from update JSON
		CheckResult result = VersionChecker.getResult(ModLoadingContext.get().getActiveContainer().getModInfo());
		if(result.status!=Status.PENDING&&result.status!=Status.FAILED)
			for(Entry<ComparableVersion, String> e : result.changes.entrySet())
				allChanges.put(e.getKey(), addVersionToManual(currIEVer, e.getKey(), e.getValue(), true));

		ManualInstance ieMan = ManualHelper.getManual();
		InnerNode<ResourceLocation, ManualEntry> updateCat = ieMan.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_UPDATE), -2);
		for(ManualEntry entry : allChanges.values())
			ManualHelper.getManual().addEntry(updateCat, entry);
	}

	private static ManualEntry addVersionToManual(ComparableVersion currVer, ComparableVersion version, String changes, boolean ahead)
	{
		String title = version.toString();
		if(ahead)
			title += I18n.format("ie.manual.newerVersion");
		else if(currVer.equals(version))
			title += I18n.format("ie.manual.currentVersion");

		String text = changes.replace("\t", "  ");
		ManualEntry.ManualEntryBuilder builder = new ManualEntryBuilder(ManualHelper.getManual());
		builder.setContent(title, "", text);
		builder.setLocation(new ResourceLocation(MODID, "changelog_"+version.toString()));
		return builder.create();
	}

	static <T> String[][] formatToTable_ItemIntMap(Map<T, Integer> map, String valueType)
	{
		List<Entry<T, Integer>> sortedMapArray = new ArrayList<>(map.entrySet());
		sortedMapArray.sort(Comparator.comparing(Entry::getValue));
		ArrayList<String[]> list = new ArrayList<>();
		try
		{
			for(Entry<T, Integer> entry : sortedMapArray)
			{
				String item = entry.getKey().toString();
				if(entry.getKey() instanceof ResourceLocation)
				{
					ResourceLocation key = (ResourceLocation)entry.getKey();
					if(ApiUtils.isNonemptyItemTag(key))
					{
						ItemStack is = IEApi.getPreferredTagStack(key);
						if(!is.isEmpty())
							item = is.getDisplayName().getFormattedText();
					}
				}

				if(item!=null)
				{
					int bt = entry.getValue();
					String am = bt+" "+valueType;
					list.add(new String[]{item, am});
				}
			}
		} catch(Exception e)
		{
		}
		return list.toArray(new String[0][]);
	}
}
