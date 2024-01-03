/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.energy.GeneratorFuel;
import blusunrize.immersiveengineering.api.energy.ThermoelectricSource;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.client.manual.*;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.lib.manual.*;
import blusunrize.lib.manual.ManualEntry.ManualEntryBuilder;
import blusunrize.lib.manual.ManualEntry.SpecialElementData;
import blusunrize.lib.manual.Tree.InnerNode;
import blusunrize.lib.manual.utils.ManualRecipeRef;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

public class IEManual
{
	public static void initManual()
	{
		IEManualInstance ieMan = new IEManualInstance();
		ManualHelper.IE_MANUAL_INSTANCE.setValue(ieMan);
		ManualHelper.ADD_CONFIG_GETTER.setValue(e -> {
			synchronized(ieMan.configGetters)
			{
				ieMan.configGetters.add(e);
			}
		});

		ManualHelper.DYNAMIC_TABLES.put("squeezer", () -> formatToTable_ItemIntMap(
				SqueezerRecipe.getFluidValuesSorted(
						Minecraft.getInstance().level,
						IEFluids.PLANTOIL.getStill(),
						true
				), "mB"
		));
		ManualHelper.DYNAMIC_TABLES.put("fermenter", () -> formatToTable_ItemIntMap(
				FermenterRecipe.getFluidValuesSorted(
						Minecraft.getInstance().level,
						IEFluids.ETHANOL.getStill(),
						true
				), "mB"
		));
		ManualHelper.DYNAMIC_TABLES.put("thermoelectric", () -> formatToTable_ItemIntMap(
				ThermoelectricSource.getThermalValuesSorted(Minecraft.getInstance().level, true),
				"K"
		));
		ManualHelper.DYNAMIC_TABLES.put("generatorfuel", () -> formatToTable_ItemIntMap(
				GeneratorFuel.getManualFuelList(Minecraft.getInstance().level),
				"ticks"
		));

		ieMan.registerSpecialElement(new ResourceLocation(MODID, "blueprint"),
				s -> new ManualElementBlueprint(ieMan, collectRecipeStacksFromJSON(s)));
		ieMan.registerSpecialElement(new ResourceLocation(MODID, "bottling"),
				s -> new ManualElementBottling(ieMan, collectRecipeStacksFromJSON(s)));
		ieMan.registerSpecialElement(new ResourceLocation(MODID, "mixer"),
				s -> new ManualElementMixer(ieMan, collectRecipeFluidsFromJSON(s)));
		ieMan.registerSpecialElement(new ResourceLocation(MODID, "multiblock"),
				s -> {
					ResourceLocation name = ManualUtils.getLocationForManual(
							GsonHelper.getAsString(s, "name"),
							ieMan
					);
					IMultiblock mb = MultiblockHandler.getByUniqueName(name);
					if(mb==null)
						throw new NullPointerException("Multiblock "+name+" does not exist");
					return new ManualElementMultiblock(ieMan, mb);
				});
		ieMan.registerSpecialElement(new ResourceLocation(MODID, "dynamic_table"),
				s -> new ManualElementTable(
						ManualHelper.getManual(),
						ManualHelper.DYNAMIC_TABLES.get(GsonHelper.getAsString(s, "table")).get(),
						false
				));
	}

	public static void addIEManualEntries()
	{
		IEManualInstance ieMan = (IEManualInstance)ManualHelper.getManual();
		InnerNode<ResourceLocation, ManualEntry> generalCat = ieMan.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_GENERAL), 0);

		{
			ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
			builder.readFromFile(new ResourceLocation(MODID, "minerals"));
			builder.appendText(IEManual::getMineralVeinTexts);
			ieMan.addEntry(generalCat, builder.create(), ieMan.atOffsetFrom(generalCat, "graphite", -0.5));
		}
		// hide entry on blueprints
		ieMan.hideEntry(new ResourceLocation(MODID, "blueprints"));
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
						int last = content.lastIndexOf("<np>");
						content.delete(last, last+4);
						return content.toString();
					}
			);
			for(ShaderRegistryEntry shader : ShaderRegistry.shaderRegistry.values())
			{
				String key = shader.name.getPath();
				builder.addSpecialElement(new SpecialElementData(key, 0, new ShaderManualElement(ieMan, shader)));
			}
			builder.setLocation(new ResourceLocation(MODID, "shader_list"));
			ManualEntry e = builder.create();
			ieMan.addEntry(generalCat, e);
			ieMan.hideEntry(e.getLocation());
		}

		addChangelogToManual();
	}

	private static Pair<String, List<SpecialElementData>> getMineralVeinTexts()
	{
		StringBuilder text = new StringBuilder();
		List<SpecialElementData> specials = new ArrayList<>();

		List<MineralMix> mineralsToAdd = new ArrayList<>(MineralMix.RECIPES.getRecipes(Minecraft.getInstance().level));
		Function<MineralMix, String> toName = mineral -> {
			String translationKey = mineral.getTranslationKey();
			String localizedName = I18n.get(translationKey);
			if(localizedName.equals(translationKey))
				localizedName = mineral.getPlainName();
			return localizedName;
		};
		mineralsToAdd.sort((i1, i2) -> toName.apply(i1).compareToIgnoreCase(toName.apply(i2)));
		for(MineralMix mineral : mineralsToAdd)
		{
			String dimensionString;
			if(mineral.dimensions!=null&&mineral.dimensions.size() > 0)
			{
				StringBuilder validDims = new StringBuilder();
				for(ResourceKey<Level> dim : mineral.dimensions)
					validDims.append((validDims.length() > 0)?", ": "")
							.append("<dim;")
							.append(dim.location())
							.append(">");
				dimensionString = I18n.get("ie.manual.entry.mineralsDimValid", toName.apply(mineral), validDims.toString());
			}
			else
				dimensionString = I18n.get("ie.manual.entry.mineralsDimAny", toName.apply(mineral));

			List<StackWithChance> formattedOutputs = Arrays.asList(mineral.outputs);
			List<StackWithChance> formattedSpoils = Arrays.asList(mineral.spoils);
			formattedOutputs.sort(Comparator.comparingDouble(i -> -i.chance()));
			formattedSpoils.sort(Comparator.comparingDouble(i -> -i.chance()));

			StringBuilder outputString = new StringBuilder();
			NonNullList<ItemStack> sortedOres = NonNullList.create();
			for(StackWithChance sorted : formattedOutputs)
			{
				outputString
						.append("\n")
						.append(
								new DecimalFormat("00.00")
										.format(sorted.chance()*100)
										.replaceAll("\\G0", "\u00A0")
						).append("% ")
						.append(sorted.stack().get().getHoverName().getString());
				sortedOres.add(sorted.stack().get());
			}

			StringBuilder spoilString = new StringBuilder();
			for(StackWithChance sorted : formattedSpoils)
			{
				spoilString
						.append("\n")
						.append(
								new DecimalFormat("00.00")
										.format(sorted.chance()*100)
										.replaceAll("\\G0", "\u00A0")
						).append("% ")
						.append(sorted.stack().get().getHoverName().getString());
				sortedOres.add(sorted.stack().get());
			}

			specials.add(new SpecialElementData(mineral.getId().toString(), 0, new ManualElementItem(ManualHelper.getManual(), sortedOres)));
			String desc = I18n.get("ie.manual.entry.minerals_desc", dimensionString, outputString.toString(), spoilString.toString());

			if(text.length() > 0)
				text.append("<np>");
			text.append("<&")
					.append(mineral.getId())
					.append(">")
					.append(desc);
		}
		return Pair.of(text.toString(), specials);
	}

	private static void addChangelogToManual()
	{
		SortedMap<ComparableVersion, ManualEntry> allChanges = new TreeMap<>(Comparator.reverseOrder());
		ComparableVersion currIEVer = new ComparableVersion(ImmersiveEngineering.VERSION);
		//Included changelog
		try(InputStream in = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(MODID,
				"changelog.json")).orElseThrow().open())
		{
			JsonElement ele = JsonParser.parseReader(new InputStreamReader(in));
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
		if(result.status()!=Status.PENDING&&result.status()!=Status.FAILED)
			for(Entry<ComparableVersion, String> e : result.changes().entrySet())
				if(!allChanges.containsKey(e.getKey()))
					allChanges.put(e.getKey(), addVersionToManual(currIEVer, e.getKey(), e.getValue(), true));

		ManualInstance ieMan = ManualHelper.getManual();
		InnerNode<ResourceLocation, ManualEntry> updateCat = ieMan.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_UPDATE), -2);
		for(ManualEntry entry : allChanges.values())
			ManualHelper.getManual().addEntry(updateCat, entry);
	}

	private static ManualEntry addVersionToManual(ComparableVersion currVer, ComparableVersion version, String changes, boolean ahead)
	{
		String text = changes.replace("\t", "  ");
		ManualEntry.ManualEntryBuilder builder = new ManualEntryBuilder(ManualHelper.getManual());
		builder.setContent(() -> {
			String title = version.toString();
			if(ahead)
				title += " - "+I18n.get("ie.manual.newerVersion");
			else if(currVer.equals(version))
				title += " - "+I18n.get("ie.manual.currentVersion");
			return title;
		}, () -> "", () -> text);
		builder.setLocation(new ResourceLocation(MODID, "changelog_"+version.toString()));
		return builder.create();
	}

	static Component[][] formatToTable_ItemIntMap(Map<Component, Integer> map, String valueType)
	{
		List<Entry<Component, Integer>> sortedMapArray = new ArrayList<>(map.entrySet());
		sortedMapArray.sort(Entry.comparingByValue());
		ArrayList<Component[]> list = new ArrayList<>();
		try
		{
			for(Entry<Component, Integer> entry : sortedMapArray)
			{
				Component item = entry.getKey();
				if(item==null)
					item = Component.nullToEmpty(entry.getKey().toString());

				int bt = entry.getValue();
				Component am = Component.nullToEmpty(bt+" "+valueType);
				list.add(new Component[]{item, am});
			}
		} catch(Exception e)
		{
		}
		return list.toArray(new Component[0][]);
	}

	static ManualRecipeRef[] collectRecipeStacksFromJSON(JsonObject json)
	{
		final ManualInstance manual = ManualHelper.getManual();
		ManualRecipeRef[] stacks;
		if(GsonHelper.isArrayNode(json, "recipes"))
		{
			JsonArray arr = json.get("recipes").getAsJsonArray();
			stacks = new ManualRecipeRef[arr.size()];
			for(int i = 0; i < stacks.length; ++i)
				stacks[i] = ManualUtils.getRecipeObjFromJson(manual, arr.get(i));
		}
		else
			stacks = new ManualRecipeRef[]{ManualUtils.getRecipeObjFromJson(manual, json.get("recipe"))};
		return stacks;
	}

	static Fluid[] collectRecipeFluidsFromJSON(JsonObject json)
	{
		Fluid[] stacks;
		if(GsonHelper.isArrayNode(json, "recipes"))
		{
			JsonArray arr = json.get("recipes").getAsJsonArray();
			stacks = new Fluid[arr.size()];
			for(int i = 0; i < stacks.length; ++i)
				stacks[i] = ForgeRegistries.FLUIDS.getValue(
						new ResourceLocation(GsonHelper.getAsString(arr.get(i).getAsJsonObject(), "fluid"))
				);
		}
		else
		{
			JsonElement recipe = json.get("recipe");
			Preconditions.checkArgument(recipe.isJsonObject());
			stacks = new Fluid[]{ForgeRegistries.FLUIDS.getValue(
					new ResourceLocation(GsonHelper.getAsString(recipe.getAsJsonObject(), "fluid"))
			)};
		}
		return stacks;
	}
}
