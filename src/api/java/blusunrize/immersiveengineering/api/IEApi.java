/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.utils.TagUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static blusunrize.immersiveengineering.api.Lib.MODID;

/**
 * @author BluSunrize - 13.08.2015
 * <p>
 * An API class, for features that should be accessible in compatibility
 */
public class IEApi
{
	/**
	 * A list of mod-ids, representing the mods an ore should be used from in order of priority
	 */
	public static List<? extends String> modPreference;

	/**
	 * This map caches the preferred ores for the given OreDict name
	 */
	private static final Map<TagKey<Item>, ItemStack> oreOutputPreference = new HashMap<>();

	/**
	 * This map stores a list of OreDict prefixes (ingot, plate, gear, nugget) and their ingot relation (ingot:component) <br>
	 * Examples:<br>"plate"-{1,1},<br>"nugget"-{1,9},<br>"block"-{9,1},<br>"gear"-{4,1}
	 */
	public static Map<String, Integer[]> prefixToIngotMap = new HashMap<String, Integer[]>();

	/**
	 * Each element of this list will be called once when the command "/cie resetrender" is run.
	 * These elements could be something like modelCache::clear.
	 */
	public static List<Runnable> renderCacheClearers = new ArrayList<>();

	public static ItemStack getPreferredTagStack(RegistryAccess tags, TagKey<Item> tag)
	{
		// TODO caching should not be global, tags can change!
		return oreOutputPreference.computeIfAbsent(
				tag, rl -> getPreferredElementbyMod(
						TagUtils.elementStream(tags, rl), tags.registryOrThrow(Registries.ITEM)
				).orElse(Items.AIR).getDefaultInstance()
		).copy();
	}

	public static <T> Optional<T> getPreferredElementbyMod(Stream<T> list, Registry<T> registry)
	{
		return getPreferredElementbyMod(list, registry::getKey);
	}

	public static <T> Optional<T> getPreferredElementbyMod(Stream<T> list, Function<T, ResourceLocation> getName)
	{
		return list.min(
			Comparator.<T>comparingInt(t -> {
				ResourceLocation name = getName.apply(t);
				String modId = name.getNamespace();
				int idx = modPreference.indexOf(modId);
				if(idx < 0)
					return modPreference.size();
				else
					return idx;
			}).thenComparing(getName)
		);
	}

	public static ItemStack getPreferredStackbyMod(ItemStack[] array)
	{
		return getPreferredElementbyMod(Arrays.stream(array), stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()))
				.orElseThrow(() -> new RuntimeException("Empty array?"));
	}

	public static boolean isAllowedInCrate(ItemStack stack)
	{
		if(!stack.getItem().canFitInsideContainerItems()||stack.is(IETags.forbiddenInCrates))
			return false;
		return true;
	}

	public static String getCurrentVersion()
	{
		return ModList.get().getModFileById(MODID).versionString();
	}

	public static ResourceLocation ieLoc(String path)
	{
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}
}
