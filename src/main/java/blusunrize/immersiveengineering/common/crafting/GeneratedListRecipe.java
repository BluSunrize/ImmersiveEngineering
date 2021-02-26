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
import com.google.common.base.Preconditions;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class GeneratedListRecipe extends IESerializableRecipe
{
	public static Map<ResourceLocation, RecipeListGenerator<?>> LIST_GENERATORS = new HashMap<>();
	public static RegistryObject<IERecipeSerializer<GeneratedListRecipe>> SERIALIZER;

	static
	{
		LIST_GENERATORS.put(rl("mixer_potion_list"), new RecipeListGenerator<>(
				PotionRecipeGenerators::initPotionRecipes, MixerRecipe.SERIALIZER.getId(),
				MixerRecipe.TYPE
		));
		LIST_GENERATORS.put(rl("potion_bottling_list"), new RecipeListGenerator<>(
				PotionRecipeGenerators::getPotionBottlingRecipes, BottlingMachineRecipe.SERIALIZER.getId(),
				BottlingMachineRecipe.TYPE
		));
		LIST_GENERATORS.put(rl("arc_recycling_list"), new RecipeListGenerator<>(
				ArcRecyclingCalculator::getRecipesFromRunningThreads, ArcFurnaceRecipe.SERIALIZER.getId(),
				ArcFurnaceRecipe.TYPE
		));
	}

	@Nullable
	private List<? extends IESerializableRecipe> cachedRecipes;
	private final RecipeListGenerator<?> generator;

	public GeneratedListRecipe(ResourceLocation id)
	{
		super(ItemStack.EMPTY, Preconditions.checkNotNull(LIST_GENERATORS.get(id), id).recipeType, id);
		generator = LIST_GENERATORS.get(id);
	}

	public GeneratedListRecipe(ResourceLocation id, @Nullable List<IESerializableRecipe> subRecipes)
	{
		this(id);
		this.cachedRecipes = subRecipes;
	}

	@Override
	protected IERecipeSerializer<GeneratedListRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput()
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isDynamic()
	{
		return true;
	}

	public List<? extends IESerializableRecipe> getSubRecipes()
	{
		if(cachedRecipes==null)
			cachedRecipes = generator.generator.get();
		return cachedRecipes;
	}

	public ResourceLocation getSubSerializer()
	{
		return generator.serialized;
	}

	public static class RecipeListGenerator<T extends IESerializableRecipe>
	{
		private final Supplier<List<T>> generator;
		private final ResourceLocation serialized;
		private final IRecipeType<T> recipeType;

		public RecipeListGenerator(Supplier<List<T>> generator, ResourceLocation serializer, IRecipeType<T> recipeType)
		{
			this.generator = generator;
			this.serialized = serializer;
			this.recipeType = recipeType;
		}
	}
}
