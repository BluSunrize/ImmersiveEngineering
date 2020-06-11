/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.items.IEItems.Molds;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class MetalPressPackingRecipes
{
	public static Map<ResourceLocation, ICraftingRecipe> CRAFTING_RECIPE_MAP;
	private static final HashMap<ComparableItemStack, RecipeDelegate> PACKING_CACHE = new HashMap<>();
	private static final HashMap<ComparableItemStack, RecipeDelegate> UNPACKING_CACHE = new HashMap<>();

	public static MetalPressRecipe get2x2PackingContainer()
	{
		return new MetalPressContainerRecipe(new ResourceLocation(Lib.MODID, "metalpress/packing2x2"), Molds.moldPacking4)
		{
			@Override
			public boolean matches(ItemStack mold, ItemStack input, World world)
			{
				return input.getCount() >= 4&&super.matches(mold, input, world);
			}

			@Override
			protected MetalPressRecipe getRecipeFunction(ItemStack input, World world)
			{
				return getPackingCached(input, world, 2);
			}
		};
	}

	public static MetalPressRecipe get3x3PackingContainer()
	{
		return new MetalPressContainerRecipe(new ResourceLocation(Lib.MODID, "metalpress/packing3x3"), Molds.moldPacking9)
		{
			@Override
			public boolean matches(ItemStack mold, ItemStack input, World world)
			{
				return input.getCount() >= 9&&super.matches(mold, input, world);
			}

			@Override
			protected MetalPressRecipe getRecipeFunction(ItemStack input, World world)
			{
				return getPackingCached(input, world, 3);
			}
		};
	}

	public static MetalPressRecipe getUnpackingContainer()
	{
		return new MetalPressContainerRecipe(new ResourceLocation(Lib.MODID, "metalpress/unpacking"), Molds.moldUnpacking)
		{
			@Override
			protected MetalPressRecipe getRecipeFunction(ItemStack input, World world)
			{
				return getUnpackingCached(input, world);
			}
		};
	}

	public static abstract class MetalPressContainerRecipe extends MetalPressRecipe
	{
		public MetalPressContainerRecipe(ResourceLocation id, Item mold)
		{
			super(id, ItemStack.EMPTY, new IngredientWithSize(Ingredient.EMPTY), new ComparableItemStack(new ItemStack(mold)), 3200);
		}

		@Override
		public boolean listInJEI()
		{
			return false;
		}

		@Override
		public boolean matches(ItemStack mold, ItemStack input, World world)
		{
			return getRecipeFunction(input, world)!=null;
		}

		@Override
		public MetalPressRecipe getActualRecipe(ItemStack mold, ItemStack input, World world)
		{
			return getRecipeFunction(input, world);
		}

		protected abstract MetalPressRecipe getRecipeFunction(ItemStack input, World world);
	}


	public static class RecipeDelegate extends MetalPressRecipe
	{
		private RecipeDelegate(String id, ItemStack output, ItemStack input, Item mold)
		{
			super(new ResourceLocation(Lib.MODID, id), output, IngredientWithSize.of(input), new ComparableItemStack(new ItemStack(mold)), 3200);
		}

		public static RecipeDelegate getPacking(Pair<ICraftingRecipe, ItemStack> originalRecipe, ItemStack input, boolean big)
		{
			ItemStack output = originalRecipe.getRight();
			ResourceLocation originalId = originalRecipe.getLeft().getId();
			String id = "metalpress/packing_"+originalId.getNamespace()+".."+originalId.getPath();
			return new RecipeDelegate(id, output, input, big?Molds.moldPacking9: Molds.moldPacking4);
		}

		public static RecipeDelegate getUnpacking(Pair<ICraftingRecipe, ItemStack> originalRecipe, ItemStack input)
		{
			ItemStack output = originalRecipe.getRight();
			ResourceLocation originalId = originalRecipe.getLeft().getId();
			String id = "metalpress/unpacking_"+originalId.getNamespace()+".."+originalId.getPath();
			return new RecipeDelegate(id, output, input, Molds.moldUnpacking);
		}

		@Override
		public boolean listInJEI()
		{
			return false;
		}
	}

	public static RecipeDelegate getRecipeDelegate(ResourceLocation id)
	{
		// Abort early for mismatched ids
		if(!id.toString().startsWith("immersiveengineering:metalpress/packing_")
				&&!id.toString().startsWith("immersiveengineering:metalpress/unpacking_"))
			return null;

		boolean packing = id.toString().startsWith("immersiveengineering:metalpress/packing_");
		String recipeId;
		if(packing)
			recipeId = id.toString().substring("immersiveengineering:metalpress/packing_".length());
		else
			recipeId = id.toString().substring("immersiveengineering:metalpress/unpacking_".length());
		recipeId = recipeId.replaceFirst("\\.\\.", ":");
		ICraftingRecipe recipe = CRAFTING_RECIPE_MAP.get(new ResourceLocation(recipeId));
		if(recipe==null)
			return null;

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		if(packing&&(ingredients.size()!=4&&ingredients.size()!=9))
			return null;
		if(!packing&&ingredients.size()!=1)
			return null;

		ItemStack input = ingredients.get(0).getMatchingStacks()[0];
		if(packing)
			return RecipeDelegate.getPacking(Pair.of(recipe, recipe.getRecipeOutput()), input, ingredients.size()==9);
		return RecipeDelegate.getUnpacking(Pair.of(recipe, recipe.getRecipeOutput()), input);
	}

	public static Pair<ICraftingRecipe, ItemStack> getPackedOutput(int gridSize, int totalAmount, ItemStack stack, World world)
	{
		CraftingInventory invC = Utils.InventoryCraftingFalse.createFilledCraftingInventory(gridSize, gridSize, NonNullList.withSize(totalAmount, stack.copy()));
		return world.getRecipeManager()
				.getRecipe(IRecipeType.CRAFTING, invC, world)
				.map(recipe -> Pair.of(recipe, recipe.getCraftingResult(invC)))
				.orElse(null);
	}

	private static RecipeDelegate getUnpackingCached(ItemStack input, World world)
	{
		ComparableItemStack comp = new ComparableItemStack(input, false);
		if(UNPACKING_CACHE.containsKey(comp))
			return UNPACKING_CACHE.get(comp);

		comp.copy();
		Pair<ICraftingRecipe, ItemStack> out = getPackedOutput(1, 1, input, world);
		if(out==null)
			return null;
		ItemStack outStack = out.getRight();

		int count = outStack.getCount();
		if(count!=4&&count!=9)
		{
			UNPACKING_CACHE.put(comp, null);
			return null;
		}

		Pair<ICraftingRecipe, ItemStack> rePacked = getPackedOutput(count==4?2: 3, count, outStack, world);
		if(rePacked==null||rePacked.getRight().isEmpty()||!ItemStack.areItemStacksEqual(input, rePacked.getRight()))
		{
			UNPACKING_CACHE.put(comp, null);
			return null;
		}

		RecipeDelegate delegate = RecipeDelegate.getUnpacking(out, Utils.copyStackWithAmount(input, 1));
		UNPACKING_CACHE.put(comp, delegate);
		return delegate;
	}

	private static RecipeDelegate getPackingCached(ItemStack input, World world, int gridSize)
	{
		ComparableItemStack comp = new ComparableItemStack(input, false);
		if(PACKING_CACHE.containsKey(comp))
			return PACKING_CACHE.get(comp);

		int totalSize = gridSize*gridSize;
		comp.copy();
		Pair<ICraftingRecipe, ItemStack> out = getPackedOutput(gridSize, totalSize, input, world);
		if(out==null)
			return null;
		ItemStack outStack = out.getRight();
		if(outStack.isEmpty())
		{
			PACKING_CACHE.put(comp, null);
			return null;
		}

		RecipeDelegate delegate = RecipeDelegate.getPacking(out, Utils.copyStackWithAmount(input, totalSize), gridSize==3);
		PACKING_CACHE.put(comp, delegate);
		return delegate;
	}
}
