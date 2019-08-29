/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author BluSunrize - 03.07.2017
 */
public class IngredientIngrStack extends Ingredient
{
	private IngredientStack ingredientStack;
	private IntList itemIds = null;
	private ItemStack[] array = null;

	public IngredientIngrStack(IngredientStack ingredientStack)
	{
		super(Stream.empty());
		this.ingredientStack = ingredientStack;
	}

	@Override
	@Nonnull
	public ItemStack[] getMatchingStacks()
	{
		List<ItemStack> list = null;
		if(array==null||this.array.length!=(list = this.ingredientStack.getSizedStackList()).size())
		{
			if(list==null)
				list = this.ingredientStack.getSizedStackList();

			List<ItemStack> list2 = new ArrayList<>(list.size());
			for(ItemStack stack : list)
				list2.add(stack);
			this.array = list2.toArray(new ItemStack[list2.size()]);
		}
		return this.array;
	}

	@Override
	@Nonnull
	@OnlyIn(Dist.CLIENT)
	public IntList getValidItemStacksPacked()
	{
		List<ItemStack> list = null;
		if(this.itemIds==null||this.itemIds.size()!=(list = this.ingredientStack.getSizedStackList()).size())
		{
			if(list==null)
				list = this.ingredientStack.getSizedStackList();

			this.itemIds = new IntArrayList(list.size());
			for(ItemStack stack : list)
				this.itemIds.add(RecipeItemHelper.pack(stack));
			this.itemIds.sort(IntComparators.NATURAL_COMPARATOR);
		}

		return this.itemIds;
	}

	@Override
	public boolean test(@Nullable ItemStack input)
	{
		if(input==null)
			return false;
		return this.ingredientStack.matchesItemStack(input);
	}

	@Override
	protected void invalidate()
	{
		this.itemIds = null;
	}
}
