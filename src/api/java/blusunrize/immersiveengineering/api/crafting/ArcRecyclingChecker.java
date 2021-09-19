package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.utils.TagUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Named;
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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ArcRecyclingChecker
{
	private final Object2BooleanMap<Item> knownItemValidity = new Object2BooleanOpenHashMap<>();

	public ArcRecyclingChecker()
	{
		RECYCLING_ALLOWED_ENUMERATED.stream()
				.flatMap(Supplier::get)
				.forEach(i -> knownItemValidity.put(i, true));
	}

	public boolean isAllowed(ItemStack stack)
	{
		if(knownItemValidity.computeBooleanIfAbsent(
				stack.getItem(), i -> RECYCLING_ALLOWED.stream().anyMatch(p -> p.test(i))
		))
			return true;
		for(Predicate<ItemStack> stackSensitive : RECYCLING_ALLOWED_STACK_SENSITIVE)
			if(stackSensitive.test(stack))
				return true;
		return false;
	}

	private static final Set<RecipeType<?>> RECYCLING_RECIPE_TYPES = new HashSet<>();
	private static final List<Predicate<ItemStack>> RECYCLING_ALLOWED_STACK_SENSITIVE = new ArrayList<>();
	private static final List<Predicate<Item>> RECYCLING_ALLOWED = new ArrayList<>();
	private static final List<Supplier<Stream<Item>>> RECYCLING_ALLOWED_ENUMERATED = new ArrayList<>();
	private static final List<Predicate<ItemStack>> INVALID_RECYCLING_OUTPUTS = new ArrayList<>();

	/**
	 * Mark the items in the generated stream as valid for recycling.
	 * This should be preferred over the predicate versions if the stream is reasonably small and quick to generate.
	 */
	public static void allowEnumeratedItemsForRecycling(Supplier<Stream<Item>> getAllowedItems)
	{
		RECYCLING_ALLOWED_ENUMERATED.add(getAllowedItems);
	}

	public static void allowPrefixedTagForRecycling(String prefix)
	{
		allowEnumeratedItemsForRecycling(
				() -> TagUtils.GET_ITEM_TAG_COLLECTION.get().getAllTags().entrySet().stream()
						.filter(e -> e.getKey().getPath().startsWith(prefix))
						.map(Entry::getValue)
						.flatMap(t -> t.getValues().stream())
		);
	}

	public static void allowItemTagForRecycling(Named<Item> tag)
	{
		allowEnumeratedItemsForRecycling(() -> {
			Tag<Item> realTag = TagUtils.getItemTag(tag.getName());
			return realTag.getValues().stream();
		});
	}

	/**
	 * Add a predicate to the list of predicates determining whether an item may be recycled.
	 * This should be preferred over the stack-sensitive version.
	 */
	public static void allowSimpleItemForRecycling(Predicate<Item> predicate)
	{
		RECYCLING_ALLOWED.add(predicate);
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
	public static void allowItemForRecycling(Predicate<ItemStack> predicate)
	{
		RECYCLING_ALLOWED_STACK_SENSITIVE.add(predicate);
	}

	/**
	 * Add a predicate to determine an invalid output for the recycling process.
	 * Used for magical ingots that should not be reclaimable or similar
	 */
	public static void makeItemInvalidRecyclingOutput(Predicate<ItemStack> predicate)
	{
		INVALID_RECYCLING_OUTPUTS.add(predicate);
	}

	/**
	 * @return a predicate for IRecipes which is used to filter the list of crafting recipes for recycling
	 */
	public static Pair<Predicate<Recipe<?>>, ArcRecyclingChecker> assembleRecyclingFilter()
	{
		ArcRecyclingChecker checker = new ArcRecyclingChecker();
		return Pair.of(iRecipe -> {
			if(!RECYCLING_RECIPE_TYPES.contains(iRecipe.getType()))
				return false;
			return checker.isAllowed(iRecipe.getResultItem());
		}, checker);
	}

	/**
	 * @return true if the given ItemStack should not be returned from recycling
	 */
	public static boolean isValidRecyclingOutput(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		for(Predicate<ItemStack> predicate : INVALID_RECYCLING_OUTPUTS)
			if(predicate.test(stack))
				return false;
		return true;
	}
}
