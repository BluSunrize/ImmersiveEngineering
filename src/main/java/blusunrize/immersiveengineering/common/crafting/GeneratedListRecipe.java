/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.IListRecipe;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Unit;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class GeneratedListRecipe<R extends IESerializableRecipe, E> extends IESerializableRecipe implements IListRecipe
{
	public static Map<ResourceLocation, RecipeListGenerator<?, ?>> LIST_GENERATORS = new HashMap<>();
	public static RegistryObject<IERecipeSerializer<GeneratedListRecipe<?, ?>>> SERIALIZER;

	static
	{
		LIST_GENERATORS.put(rl("mixer_potion_list"), RecipeListGenerator.simple(
				PotionRecipeGenerators::initPotionRecipes, MixerRecipe.SERIALIZER.getId(),
				IERecipeTypes.MIXER
		));
		LIST_GENERATORS.put(rl("potion_bottling_list"), RecipeListGenerator.simple(
				PotionRecipeGenerators::getPotionBottlingRecipes, BottlingMachineRecipe.SERIALIZER.getId(),
				IERecipeTypes.BOTTLING_MACHINE
		));
		LIST_GENERATORS.put(rl("arc_recycling_list"), new RecipeListGenerator<>(
				ArcRecyclingCalculator::makeFuture,
				recyclingList -> Objects.requireNonNull(recyclingList.getValue()),
				ArcFurnaceRecipe.SERIALIZER.getId(),
				IERecipeTypes.ARC_FURNACE
		));
	}

	@Nullable
	private List<? extends IESerializableRecipe> cachedRecipes;
	private final RecipeListGenerator<R, E> generator;
	private E earlyResult;

	public static GeneratedListRecipe<?, ?> from(ResourceLocation id)
	{
		GeneratedListRecipe<?, ?> result = fromInternal(id);
		result.initEarly();
		return result;
	}

	private static GeneratedListRecipe<?, ?> fromInternal(ResourceLocation id)
	{
		RecipeListGenerator<?, ?> gen = LIST_GENERATORS.get(id);
		Preconditions.checkNotNull(gen, id);
		return new GeneratedListRecipe<>(id, gen);
	}

	public static GeneratedListRecipe<?, ?> resolved(ResourceLocation id, List<IESerializableRecipe> recipes)
	{
		GeneratedListRecipe<?, ?> result = fromInternal(id);
		result.cachedRecipes = recipes;
		return result;
	}

	private GeneratedListRecipe(ResourceLocation id, RecipeListGenerator<R, E> generator)
	{
		super(LAZY_EMPTY, generator.recipeType, id);
		this.generator = generator;
	}

	private void initEarly()
	{
		this.earlyResult = this.generator.makeEarlyResult().get();
	}

	@Override
	protected IERecipeSerializer<GeneratedListRecipe<?, ?>> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(RegistryAccess access)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}

	public List<? extends IESerializableRecipe> getSubRecipes()
	{
		if(cachedRecipes==null)
			cachedRecipes = generator.generator().apply(earlyResult);
		return cachedRecipes;
	}

	public ResourceLocation getSubSerializer()
	{
		return generator.serialized;
	}

	public record RecipeListGenerator<T extends IESerializableRecipe, EarlyResult>(
			Supplier<EarlyResult> makeEarlyResult,
			Function<EarlyResult, List<? extends T>> generator,
			ResourceLocation serialized,
			IERecipeTypes.TypeWithClass<T> recipeType
	)
	{
		public static <R extends IESerializableRecipe>
		RecipeListGenerator<R, ?> simple(
				Supplier<List<? extends R>> generator, ResourceLocation serialized, IERecipeTypes.TypeWithClass<R> recipeType
		)
		{
			return new RecipeListGenerator<>(() -> Unit.INSTANCE, $ -> generator.get(), serialized, recipeType);
		}
	}
}
