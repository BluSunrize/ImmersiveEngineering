package blusunrize.immersiveengineering.api.crafting;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Named;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ArcRecyclingChecker
{
	private final Object2BooleanMap<Item> knownItemValidity = new Object2BooleanOpenHashMap<>();

	public ArcRecyclingChecker(TagContainer tags)
	{
		RECYCLING_ALLOWED_ENUMERATED.stream()
				.flatMap(f -> f.apply(tags))
				.forEach(i -> knownItemValidity.put(i, true));
	}

	public boolean isAllowed(TagContainer tags, ItemStack stack)
	{
		if(knownItemValidity.computeBooleanIfAbsent(
				stack.getItem(), i -> RECYCLING_ALLOWED.stream().anyMatch(p -> p.test(tags, i))
		))
			return true;
		for(BiPredicate<TagContainer, ItemStack> stackSensitive : RECYCLING_ALLOWED_STACK_SENSITIVE)
			if(stackSensitive.test(tags, stack))
				return true;
		return false;
	}

	private static final Set<RecipeType<?>> RECYCLING_RECIPE_TYPES = new HashSet<>();
	private static final List<BiPredicate<TagContainer, ItemStack>> RECYCLING_ALLOWED_STACK_SENSITIVE = new ArrayList<>();
	private static final List<BiPredicate<TagContainer, Item>> RECYCLING_ALLOWED = new ArrayList<>();
	private static final List<Function<TagContainer, Stream<Item>>> RECYCLING_ALLOWED_ENUMERATED = new ArrayList<>();
	private static final List<BiPredicate<TagContainer, ItemStack>> INVALID_RECYCLING_OUTPUTS = new ArrayList<>();

	/**
	 * Mark the items in the generated stream as valid for recycling.
	 * This should be preferred over the predicate versions if the stream is reasonably small and quick to generate.
	 */
	public static void allowEnumeratedItemsForRecycling(Function<TagContainer, Stream<Item>> getAllowedItems)
	{
		RECYCLING_ALLOWED_ENUMERATED.add(getAllowedItems);
	}

	public static void allowEnumeratedItemsForRecycling(Supplier<Stream<Item>> getAllowedItems)
	{
		allowEnumeratedItemsForRecycling($ -> getAllowedItems.get());
	}

	public static void allowPrefixedTagForRecycling(String prefix)
	{
		allowEnumeratedItemsForRecycling(
				tags -> tags.getOrEmpty(Registry.ITEM_REGISTRY).getAllTags().entrySet().stream()
						.filter(e -> e.getKey().getPath().startsWith(prefix))
						.map(Entry::getValue)
						.flatMap(t -> t.getValues().stream())
		);
	}

	public static void allowItemTagForRecycling(Named<Item> tag)
	{
		allowEnumeratedItemsForRecycling(tags -> {
			Tag<Item> realTag = tags.getOrEmpty(Registry.ITEM_REGISTRY).getTag(tag.getName());
			return realTag.getValues().stream();
		});
	}

	/**
	 * Add a predicate to the list of predicates determining whether an item may be recycled.
	 * This should be preferred over the stack-sensitive version.
	 */
	public static void allowSimpleItemForRecycling(BiPredicate<TagContainer, Item> predicate)
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
	public static void allowItemForRecycling(BiPredicate<TagContainer, ItemStack> predicate)
	{
		RECYCLING_ALLOWED_STACK_SENSITIVE.add(predicate);
	}

	/**
	 * Add a predicate to determine an invalid output for the recycling process.
	 * Used for magical ingots that should not be reclaimable or similar
	 */
	public static void makeItemInvalidRecyclingOutput(BiPredicate<TagContainer, ItemStack> predicate)
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
	public static Pair<Predicate<Recipe<?>>, ArcRecyclingChecker> assembleRecyclingFilter(TagContainer tags)
	{
		ArcRecyclingChecker checker = new ArcRecyclingChecker(tags);
		return Pair.of(iRecipe -> {
			if(!RECYCLING_RECIPE_TYPES.contains(iRecipe.getType()))
				return false;
			return checker.isAllowed(tags, iRecipe.getResultItem());
		}, checker);
	}

	/**
	 * @return true if the given ItemStack should not be returned from recycling
	 */
	public static boolean isValidRecyclingOutput(TagContainer tags, ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		for(BiPredicate<TagContainer, ItemStack> predicate : INVALID_RECYCLING_OUTPUTS)
			if(predicate.test(tags, stack))
				return false;
		return true;
	}
}
