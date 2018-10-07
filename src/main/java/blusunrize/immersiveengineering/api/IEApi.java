/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
	public static List<String> modPreference;

	/**
	 * This map caches the preferred ores for the given OreDict name
	 */
	public static HashMap<String, ItemStack> oreOutputPreference = new HashMap<String, ItemStack>();

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
	 * 2: conductive, increases flux damage done to the target (CoFH/RedstoneArsenal compat)<br>
	 */
	public static Potion[] potions;

	/**
	 * Each element of this list will be called once when the command "/cie resetrender" is run.
	 * These elements could be something like modelCache::clear.
	 */
	public static List<Runnable> renderCacheClearers = new ArrayList<>();

	/**
	 * If one of the predicates in this list returns true for a given stack, it can't be placed in a crate or in the Engineer's toolbox
	 */
	public static List<Predicate<ItemStack>> forbiddenInCrates = new ArrayList<>();

	public static ItemStack getPreferredOreStack(String oreName)
	{
		if(!oreOutputPreference.containsKey(oreName))
		{
			ItemStack preferredStack = ApiUtils.isExistingOreName(oreName)?
					getPreferredStackbyMod(OreDictionary.getOres(oreName)): ItemStack.EMPTY;
			if(preferredStack.getMetadata()==OreDictionary.WILDCARD_VALUE)
				preferredStack.setItemDamage(0);
			oreOutputPreference.put(oreName, preferredStack);
			return preferredStack;
		}
		ItemStack s = oreOutputPreference.get(oreName);
		return s==null?ItemStack.EMPTY: s.copy();
	}

	public static ItemStack getPreferredStackbyMod(Collection<ItemStack> list)
	{
		ItemStack preferredStack = ItemStack.EMPTY;
		int lastPref = -1;
		for(ItemStack stack : list)
			if(!stack.isEmpty())
			{
				ResourceLocation rl = Item.REGISTRY.getNameForObject(stack.getItem());
				if(rl!=null)
				{
					String modId = rl.getNamespace();
					int idx = modId==null||modId.isEmpty()?-1: modPreference.indexOf(modId);
					if(preferredStack.isEmpty()||(idx >= 0&&(lastPref < 0||idx < lastPref)))
					{
						preferredStack = stack;
						lastPref = idx;
					}
				}
			}
		return preferredStack.copy();
	}

	public static ItemStack getPreferredStackbyMod(ItemStack[] array)
	{
		return getPreferredStackbyMod(Lists.newArrayList(array));
	}

	public static boolean isAllowedInCrate(ItemStack stack)
	{
		for(Predicate<ItemStack> check : forbiddenInCrates)
			if(check.test(stack))
				return false;
		return true;
	}
}