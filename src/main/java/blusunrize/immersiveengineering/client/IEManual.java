package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.ManualElementBlueprint;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.multiblocks.ManualElementMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.api.utils.TagUtils;
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

		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "wiring"));
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "generator"));
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "breaker"));
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "current_transformer"));
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "redstone_wire"));
		{
			ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
			builder.addSpecialElement("values", 0,
					addDynamicTable(
							() -> ThermoelectricHandler.getThermalValuesSorted(true),
							"K"
					)
			);
			builder.readFromFile(new ResourceLocation(MODID, "thermoelectric"));
			ieMan.addEntry(energyCat, builder.create());
		}
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "diesel_generator"));
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "lightning_rod"));

		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "introduction"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "hemp"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "ores"));
		ieMan.addEntry(generalCat, handleMineralManual(ieMan));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "alloys"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "components"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "plates"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "craftingtable"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "workbench"));
		ResourceLocation blueprints = new ResourceLocation(MODID, "blueprints");
		ieMan.addEntry(generalCat, blueprints);
		ieMan.hideEntry(blueprints);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "alloykiln"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "cokeoven"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "crude_blast_furnace"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "improved_blast_furnace"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "graphite"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "shader"));

		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "treated_wood"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "multiblocks"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "balloon"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "crate"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "barrel"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "metalconstruction"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "metal_barrel"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "concrete"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "lighting"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "silo"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "tank"));

		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "toolbox"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "jerrycan"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "ear_defenders"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "buzzsaw"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "mining_drill"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "maintenance_kit"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "shield"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "revolver"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "bullets"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "chemthrower"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "skyhook"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "powerpack"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "railgun"));

		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "conveyors"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "item_router"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "item_batcher"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "turntable"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "fluid_transport"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "fluid_router"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "external_heater"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "charging_station"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "garden_cloche"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "tesla_coil"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "razor_wire"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "turrets"));

		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "metal_press"));
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "crusher"));
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "assembler"));
		{
			ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
			builder.addSpecialElement("list", 0,
					addDynamicTable(
							() -> FermenterRecipe.getFluidValuesSorted(IEContent.fluidEthanol, true),
							"mB"
					)
			);
			builder.readFromFile(new ResourceLocation(MODID, "fermenter"));
			ieMan.addEntry(heavyMachinesCat, builder.create());
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
			ieMan.addEntry(heavyMachinesCat, builder.create());
		}
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "refinery"));
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "mixer"));
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "bottling_machine"));
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "automated_workbench"));
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "arc_furnace"));
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "excavator"));

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
		builder.addSpecialElement("drill", 0, new ManualElementCrafting(ieManual, new ResourceLocation(MODID, "crafting/sample_drill")));

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
		List<MineralMix> mineralsToAdd = new ArrayList<>(ExcavatorHandler.mineralList.values());
		Function<MineralMix, String> toName = mineral -> {
			String translationKey = mineral.getTranslationKey();
			String localizedName = I18n.format(translationKey);
			if(localizedName.equals(translationKey))
				localizedName = mineral.getPlainName();
			return localizedName;
		};
		mineralsToAdd.sort((i1, i2) -> toName.apply(i1).compareToIgnoreCase(toName.apply(i2)));
		StringBuilder entry = new StringBuilder(I18n.format("ie.manual.entry.mineral_main"));
		for(MineralMix mineral : mineralsToAdd)
		{
			String dimensionString;
			if(mineral.dimensions!=null&&mineral.dimensions.size() > 0)
			{
				StringBuilder validDims = new StringBuilder();
				for(DimensionType dim : mineral.dimensions)
					validDims.append((validDims.length() > 0)?", ": "")
							.append("<dim;")
							.append(DimensionType.getKey(dim))
							.append(">");
				dimensionString = I18n.format("ie.manual.entry.mineralsDimValid", toName.apply(mineral), validDims.toString());
			}
			else
				dimensionString = I18n.format("ie.manual.entry.mineralsDimAny", toName.apply(mineral));

			List<StackWithChance> formattedOutputs = Arrays.asList(mineral.outputs);
			formattedOutputs.sort(Comparator.comparingDouble(i -> -i.getChance()));

			StringBuilder outputString = new StringBuilder();
			NonNullList<ItemStack> sortedOres = NonNullList.create();
			for(StackWithChance sorted : formattedOutputs)
			{
				outputString
						.append("\n")
						.append(
								new DecimalFormat("00.00")
										.format(sorted.getChance()*100)
										.replaceAll("\\G0", "\u00A0")
						).append("% ")
						.append(sorted.getStack().getDisplayName().getFormattedText());
				sortedOres.add(sorted.getStack());
			}
			splitter.addSpecialPage(mineral.getId().toString(), 0, new ManualElementItem(ManualHelper.getManual(), sortedOres));
			String desc = I18n.format("ie.manual.entry.minerals_desc", dimensionString, outputString.toString());
			if(entry.length() > 0)
				entry.append("<np>");
			entry.append("<&")
					.append(mineral.getId())
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
					if(TagUtils.isNonemptyItemTag(key))
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
