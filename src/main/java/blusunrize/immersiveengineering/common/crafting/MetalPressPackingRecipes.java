/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.common.register.IEItems.Molds;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MetalPressPackingRecipes
{
	public static final CachedRecipeList<CraftingRecipe> CRAFTING_RECIPE_MAP = new CachedRecipeList<>(() -> RecipeType.CRAFTING);
	public static final ResourceLocation UNPACK_ID = ImmersiveEngineering.rl("unpacking");
	public static final ResourceLocation PACK4_ID = ImmersiveEngineering.rl("packing4");
	public static final ResourceLocation PACK9_ID = ImmersiveEngineering.rl("packing9");
	// TODO clear at recipe reload!
	private static final HashMap<ComparableItemStack, RecipeHolder<MetalPressRecipe>> UNPACKING_CACHE = new HashMap<>();

	public static void init()
	{
		MetalPressRecipe.addSpecialRecipe(
				IEApi.ieLoc("metalpress/packing2x2"),
				new MetalPressPackingRecipe(Molds.MOLD_PACKING_4.asItem(), 2)
		);
		MetalPressRecipe.addSpecialRecipe(
				IEApi.ieLoc("metalpress/packing3x3"),
				new MetalPressPackingRecipe(Molds.MOLD_PACKING_9.asItem(), 3)
		);
		MetalPressRecipe.addSpecialRecipe(
				IEApi.ieLoc("metalpress/unpacking"),
				new MetalPressContainerRecipe(Molds.MOLD_UNPACKING.asItem())
				{
					@Override
					protected RecipeHolder<MetalPressRecipe> getRecipeFunction(ItemStack input, Level world)
					{
						return getUnpackingCached(input, world);
					}
				}
		);
	}

	public static abstract class MetalPressContainerRecipe extends MetalPressRecipe
	{
		public MetalPressContainerRecipe(Item mold)
		{
			super(TagOutput.EMPTY, new IngredientWithSize(Ingredient.EMPTY), mold, 3200);
		}

		@Override
		public boolean listInJEI()
		{
			return false;
		}

		@Override
		public boolean matches(ItemStack mold, ItemStack input, Level world)
		{
			return getRecipeFunction(input, world)!=null;
		}

		@Override
		public RecipeHolder<MetalPressRecipe> getActualRecipe(ResourceLocation ownId, ItemStack mold, ItemStack input, Level world)
		{
			return getRecipeFunction(input, world);
		}

		protected abstract RecipeHolder<MetalPressRecipe> getRecipeFunction(ItemStack input, Level world);
	}

	public static class MetalPressPackingRecipe extends MetalPressContainerRecipe
	{
		private final int size;
		private final Map<ComparableItemStack, RecipeHolder<MetalPressRecipe>> PACKING_CACHE = new HashMap<>();

		public MetalPressPackingRecipe(Item mold, int size)
		{
			super(mold);
			this.size = size;
		}

		@Override
		public boolean matches(ItemStack mold, ItemStack input, Level world)
		{
			return input.getCount() >= size*size&&super.matches(mold, input, world);
		}

		@Override
		protected RecipeHolder<MetalPressRecipe> getRecipeFunction(ItemStack input, Level world)
		{
			ComparableItemStack comp = new ComparableItemStack(input, false);
			if(PACKING_CACHE.containsKey(comp))
				return PACKING_CACHE.get(comp);

			int totalSize = size*size;
			comp.copy();
			Pair<RecipeHolder<CraftingRecipe>, ItemStack> out = getPackedOutput(size, input, world);
			if(out==null)
				return null;
			ItemStack outStack = out.getSecond();
			if(outStack.isEmpty())
			{
				PACKING_CACHE.put(comp, null);
				return null;
			}

			RecipeHolder<MetalPressRecipe> delegate = RecipeDelegate.getPacking(out, input.copyWithCount(totalSize), size==3);
			PACKING_CACHE.put(comp, delegate);
			return delegate;
		}
	}

	public static class RecipeDelegate extends MetalPressRecipe
	{
		public final RecipeHolder<CraftingRecipe> baseRecipe;

		private RecipeDelegate(ItemStack output, ItemStack input, Item mold, RecipeHolder<CraftingRecipe> baseRecipe)
		{
			super(new TagOutput(output), IngredientWithSize.of(input), mold, 3200);
			this.baseRecipe = baseRecipe;
		}

		public static RecipeHolder<MetalPressRecipe> getPacking(
				Pair<RecipeHolder<CraftingRecipe>, ItemStack> originalRecipe, ItemStack input, boolean big
		)
		{
			ItemStack output = originalRecipe.getSecond();
			input = input.copyWithCount(big?9: 4);
			return new RecipeHolder<>(
					big?PACK9_ID: PACK4_ID,
					new RecipeDelegate(
							output, input, (big?Molds.MOLD_PACKING_9: Molds.MOLD_PACKING_4).get(),
							originalRecipe.getFirst()
					)
			);
		}

		public static RecipeHolder<MetalPressRecipe> getUnpacking(
				Pair<RecipeHolder<CraftingRecipe>, ItemStack> originalRecipe, ItemStack input
		)
		{
			ItemStack output = originalRecipe.getSecond();
			return new RecipeHolder<>(
					UNPACK_ID,
					new RecipeDelegate(output, input, Molds.MOLD_UNPACKING.get(), originalRecipe.getFirst())
			);
		}

		@Override
		public boolean listInJEI()
		{
			return false;
		}
	}

	@Nullable
	public static RecipeHolder<MetalPressRecipe> getRecipeDelegate(
			RecipeHolder<CraftingRecipe> recipeHolder, ResourceLocation id, RegistryAccess access
	)
	{
		final CraftingRecipe recipe = recipeHolder.value();
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		if(ingredients.isEmpty()||ingredients.get(0).isEmpty())
			return null;
		ItemStack input = ingredients.get(0).getItems()[0];
		if(PACK4_ID.equals(id))
		{
			if(ingredients.size()==4)
				return RecipeDelegate.getPacking(Pair.of(recipeHolder, recipe.getResultItem(access)), input, false);
		}
		else if(PACK9_ID.equals(id))
		{
			if(ingredients.size()==9)
				return RecipeDelegate.getPacking(Pair.of(recipeHolder, recipe.getResultItem(access)), input, true);
		}
		else if(UNPACK_ID.equals(id))
		{
			if(ingredients.size()==1)
				return RecipeDelegate.getUnpacking(Pair.of(recipeHolder, recipe.getResultItem(access)), input);
		}
		return null;
	}

	public static Pair<RecipeHolder<CraftingRecipe>, ItemStack> getPackedOutput(int gridSize, ItemStack stack, Level world)
	{
		CraftingContainer invC = Utils.InventoryCraftingFalse.createFilledCraftingInventory(
				gridSize, gridSize, NonNullList.withSize(gridSize*gridSize, stack.copy())
		);
		return world.getRecipeManager()
				.getRecipeFor(RecipeType.CRAFTING, invC, world)
				.map(recipe -> Pair.of(recipe, recipe.value().assemble(invC, world.registryAccess())))
				.orElse(null);
	}

	private static RecipeHolder<MetalPressRecipe> getUnpackingCached(ItemStack input, Level world)
	{
		ComparableItemStack comp = new ComparableItemStack(input, false);
		if(UNPACKING_CACHE.containsKey(comp))
			return UNPACKING_CACHE.get(comp);

		comp.copy();
		Pair<RecipeHolder<CraftingRecipe>, ItemStack> out = getPackedOutput(1, input, world);
		if(out==null)
			return null;
		ItemStack outStack = out.getSecond();

		int count = outStack.getCount();
		if(count!=4&&count!=9)
		{
			UNPACKING_CACHE.put(comp, null);
			return null;
		}

		Pair<RecipeHolder<CraftingRecipe>, ItemStack> rePacked = getPackedOutput(count==4?2: 3, outStack, world);
		ItemStack singleInput = input.copyWithCount(1);
		if(rePacked==null||rePacked.getSecond().isEmpty()||!ItemStack.matches(singleInput, rePacked.getSecond()))
		{
			UNPACKING_CACHE.put(comp, null);
			return null;
		}

		RecipeHolder<MetalPressRecipe> delegate = RecipeDelegate.getUnpacking(out, singleInput);
		UNPACKING_CACHE.put(comp, delegate);
		return delegate;
	}
}
