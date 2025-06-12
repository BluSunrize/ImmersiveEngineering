/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting.cache;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import com.google.common.collect.Streams;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EventBusSubscriber(modid = Lib.MODID)
public class CachedRecipeList<R extends Recipe<?>>
{
	public static final int INVALID_RELOAD_COUNT = -1;
	private static int reloadCount = 0;

	private final Supplier<RecipeType<R>> type;
	private Map<ResourceLocation, RecipeHolder<R>> recipes;
	private List<RecipeHolder<R>> recipeHolders;
	private boolean cachedDataIsClient;
	private int cachedAtReloadCount = INVALID_RELOAD_COUNT;

	public CachedRecipeList(Supplier<RecipeType<R>> type)
	{
		this.type = type;
	}

	public CachedRecipeList(IERecipeTypes.TypeWithClass<R> type)
	{
		this(type.type());
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onTagsUpdated(TagsUpdatedEvent ev)
	{
		++reloadCount;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRecipeUpdatedClient(RecipesUpdatedEvent ev)
	{
		++reloadCount;
	}

	public static int getReloadCount()
	{
		return reloadCount;
	}

	public List<RecipeHolder<R>> getRecipes(@Nonnull Level level)
	{
		updateCache(level.getRecipeManager(), level.isClientSide());
		return Objects.requireNonNull(recipeHolders);
	}

	public Collection<ResourceLocation> getRecipeNames(@Nonnull Level level)
	{
		updateCache(level.getRecipeManager(), level.isClientSide());
		return Objects.requireNonNull(recipes).keySet();
	}

	public R getById(@Nonnull Level level, ResourceLocation name)
	{
		var holder = holderById(level, name);
		return holder!=null?holder.value(): null;
	}

	public RecipeHolder<R> holderById(@Nonnull Level level, ResourceLocation name)
	{
		updateCache(level.getRecipeManager(), level.isClientSide());
		return recipes.get(name);
	}

	private void updateCache(RecipeManager manager, boolean isClient)
	{
		if(recipes!=null&&cachedAtReloadCount==reloadCount&&(!cachedDataIsClient||isClient))
			return;
		this.recipes = manager.getRecipes().stream()
				.filter(iRecipe -> iRecipe.value().getType()==type.get())
				.flatMap(r -> {
					if(r.value() instanceof IListRecipe listRecipe)
					{
						int total = listRecipe.getSubRecipes().size();
						String fmt = total >= 10000?"%05d": total >= 1000?"%04d": total >= 100?"%03d": total >= 10?"%02d": "%01d";
						return Streams.mapWithIndex(
								listRecipe.getSubRecipes().stream(),
								(subRecipe, i) -> new RecipeHolder<>(r.id().withSuffix(String.format(fmt, i)), subRecipe)
						);
					}
					else
						return Stream.of(r);
				})
				.collect(Collectors.toMap(RecipeHolder::id, rh -> (RecipeHolder<R>)rh));
		this.recipeHolders = List.copyOf(this.recipes.values());
		this.cachedDataIsClient = isClient;
		this.cachedAtReloadCount = reloadCount;
	}
}
