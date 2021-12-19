/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.utils.TagUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

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
	public static HashMap<ResourceLocation, ItemStack> oreOutputPreference = new HashMap<>();

	/**
	 * The TextureSheet id for the revolver's icons
	 */
	public static int revolverTextureSheetID;

	/**
	 * This map stores a list of OreDict prefixes (ingot, plate, gear, nugget) and their ingot relation (ingot:component) <br>
	 * Examples:<br>"plate"-{1,1},<br>"nugget"-{1,9},<br>"block"-{9,1},<br>"gear"-{4,1}
	 */
	public static HashMap<String, Integer[]> prefixToIngotMap = new HashMap<String, Integer[]>();

	/**
	 * An array of all potions added by IE. indices are as follows:<br>
	 * 0: flammable, increases all fire damage done<br>
	 * 1: slippery, makes the target slide around and randomly drop their held item<br>
	 * 2: conductive, increases flux damage done to the target (CoFH/RedstoneArsenal compat112)<br>
	 */
	public static MobEffect[] potions;

	/**
	 * Each element of this list will be called once when the command "/cie resetrender" is run.
	 * These elements could be something like modelCache::clear.
	 */
	public static List<Runnable> renderCacheClearers = new ArrayList<>();

	/**
	 * If one of the predicates in this list returns true for a given stack, it can't be placed in a crate or in the Engineer's toolbox
	 */
	public static List<Predicate<ItemStack>> forbiddenInCrates = new ArrayList<>();

	public static ItemStack getPreferredTagStack(ResourceLocation name)
	{
		return oreOutputPreference.computeIfAbsent(name, rl ->
		{
			if(TagUtils.isNonemptyItemTag(name))
				return new ItemStack(getPreferredElementbyMod(TagUtils.getItemTag(name).getValues()));
			else if(TagUtils.isNonemptyBlockTag(name))
				return new ItemStack(getPreferredElementbyMod(TagUtils.getItemTag(name).getValues()));
			else
				return ItemStack.EMPTY;
		}).copy();
	}

	public static <T extends IForgeRegistryEntry<T>> T getPreferredElementbyMod(Collection<T> list)
	{
		return getPreferredElementbyMod(list, T::getRegistryName);
	}

	public static ItemStack getPreferredStackbyMod(Collection<ItemStack> list)
	{
		return getPreferredElementbyMod(list, e -> e.getItem().getRegistryName());
	}

	public static <T> T getPreferredElementbyMod(Collection<T> list, Function<T, ResourceLocation> getName)
	{
		T preferredStack = null;
		int currBest = modPreference.size();
		for(T stack : list)
		{
			ResourceLocation rl = getName.apply(stack);
			if(rl==null)
				continue;
			if(preferredStack==null)
				preferredStack = stack;
			String modId = rl.getNamespace();
			int idx = modPreference.indexOf(modId);
			if(idx < 0)
				idx = modPreference.size();
			if(idx < currBest)
			{
				preferredStack = stack;
				currBest = idx;
			}
			else if(idx==currBest&&rl.compareTo(getName.apply(preferredStack)) < 0)
				preferredStack = stack;
		}
		Preconditions.checkNotNull(preferredStack, "No entry found in %s", list);
		return preferredStack;
	}

	public static ItemStack getPreferredStackbyMod(ItemStack[] array)
	{
		return getPreferredElementbyMod(Lists.newArrayList(array), stack -> stack.getItem().getRegistryName());
	}

	public static boolean isAllowedInCrate(ItemStack stack)
	{
		for(Predicate<ItemStack> check : forbiddenInCrates)
			if(check.test(stack))
				return false;
		return true;
	}

	public static String getCurrentVersion()
	{
		return ModList.get().getModFileById(Lib.MODID).getMods().stream()
				.filter(i -> Lib.MODID.equals(i.getModId()))
				.findAny()
				.orElseThrow(RuntimeException::new)
				.getVersion()
				.toString();
	}
}
