/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ArcRecyclingChecker
{
	private final Object2BooleanMap<Item> knownItemValidity = new Object2BooleanOpenHashMap<>();

	public ArcRecyclingChecker(RegistryAccess tags)
	{
		RECYCLING_ALLOWED_ENUMERATED.stream()
				.flatMap(f -> f.apply(tags))
				// filter out blacklisted
				.filter((Item item) -> !item.builtInRegistryHolder().is(IETags.recyclingBlacklist))
				.forEach(i -> knownItemValidity.put(i, true));
	}

	public boolean isAllowed(RegistryAccess tags, ItemStack stack)
	{
		if(stack.is(IETags.recyclingBlacklist))
			return false;
		if(knownItemValidity.computeIfAbsent(
				stack.getItem(), (Item i) -> RECYCLING_ALLOWED.stream().anyMatch(p -> p.test(tags, i))
		))
			return true;
		for(BiPredicate<RegistryAccess, ItemStack> stackSensitive : RECYCLING_ALLOWED_STACK_SENSITIVE)
			if(stackSensitive.test(tags, stack))
				return true;
		return false;
	}

	private static final Set<RecipeType<?>> RECYCLING_RECIPE_TYPES = new HashSet<>();
	private static final List<BiPredicate<RegistryAccess, ItemStack>> RECYCLING_ALLOWED_STACK_SENSITIVE = new ArrayList<>();
	private static final List<BiPredicate<RegistryAccess, Item>> RECYCLING_ALLOWED = new ArrayList<>();
	private static final List<Function<RegistryAccess, Stream<Item>>> RECYCLING_ALLOWED_ENUMERATED = new ArrayList<>();
	private static final List<BiPredicate<RegistryAccess, ItemStack>> INVALID_RECYCLING_OUTPUTS = new ArrayList<>();

	/**
	 * Mark the items in the generated stream as valid for recycling.
	 * This should be preferred over the predicate versions if the stream is reasonably small and quick to generate.
	 */
	public static void allowEnumeratedItemsForRecycling(Function<RegistryAccess, Stream<Item>> getAllowedItems)
	{
		RECYCLING_ALLOWED_ENUMERATED.add(getAllowedItems);
	}

	public static void allowEnumeratedItemsForRecycling(Supplier<Stream<? extends ItemLike>> getAllowedItems)
	{
		allowEnumeratedItemsForRecycling($ -> getAllowedItems.get().map(ItemLike::asItem));
	}

	public static void allowPrefixedTagForRecycling(String prefix)
	{
		allowEnumeratedItemsForRecycling(
				tags -> tags.registryOrThrow(Registries.ITEM).getTags()
						.filter(e -> e.getFirst().location().getPath().startsWith(prefix))
						.map(Pair::getSecond)
						.flatMap(HolderSet::stream)
						.map(Holder::value)
		);
	}

	public static void allowItemTagForRecycling(TagKey<Item> tagKey)
	{
		allowEnumeratedItemsForRecycling(tags -> TagUtils.elementStream(tags.registryOrThrow(Registries.ITEM), tagKey));
	}

	/**
	 * Add a predicate to the list of predicates determining whether an item may be recycled.
	 * This should be preferred over the stack-sensitive version.
	 */
	public static void allowSimpleItemForRecycling(BiPredicate<RegistryAccess, Item> predicate)
	{
		RECYCLING_ALLOWED.add(predicate);
	}

	public static void allowSimpleItemForRecycling(Predicate<Item> predicate)
	{
		RECYCLING_ALLOWED.add(($, item) -> predicate.test(item));
	}

	/**
	 * Add a predicate to the list of predicates determining whether an item may be recycled
	 */
	public static void allowRecipeTypeForRecycling(RecipeType<?> recipeType)
	{
		RECYCLING_RECIPE_TYPES.add(recipeType);
	}

	/**
	 * Add a predicate to the list of predicates determining whether an item may be recycled
	 */
	public static void allowItemForRecycling(BiPredicate<RegistryAccess, ItemStack> predicate)
	{
		RECYCLING_ALLOWED_STACK_SENSITIVE.add(predicate);
	}

	/**
	 * Add a predicate to determine an invalid output for the recycling process.
	 * Used for magical ingots that should not be reclaimable or similar
	 */
	public static void makeItemInvalidRecyclingOutput(BiPredicate<RegistryAccess, ItemStack> predicate)
	{
		INVALID_RECYCLING_OUTPUTS.add(predicate);
	}

	public static void makeItemInvalidRecyclingOutput(Predicate<ItemStack> predicate)
	{
		INVALID_RECYCLING_OUTPUTS.add(($, stack) -> predicate.test(stack));
	}

	/**
	 * @param tags
	 * @return a predicate for IRecipes which is used to filter the list of crafting recipes for recycling
	 */
	public static Pair<Predicate<Recipe<?>>, ArcRecyclingChecker> assembleRecyclingFilter(RegistryAccess tags)
	{
		ArcRecyclingChecker checker = new ArcRecyclingChecker(tags);
		return Pair.of(iRecipe -> {
			if(!RECYCLING_RECIPE_TYPES.contains(iRecipe.getType()))
				return false;
			return checker.isAllowed(tags, iRecipe.getResultItem(tags));
		}, checker);
	}

	/**
	 * @return true if the given ItemStack should not be returned from recycling
	 */
	public static boolean isValidRecyclingOutput(RegistryAccess tags, ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		for(BiPredicate<RegistryAccess, ItemStack> predicate : INVALID_RECYCLING_OUTPUTS)
			if(predicate.test(tags, stack))
				return false;
		return true;
	}
}
