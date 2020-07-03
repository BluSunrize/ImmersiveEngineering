/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
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
public class IngredientMultiTag extends Ingredient
{
	private List<ITag<Item>> tags;
	private IntList itemIds = null;
	private ItemStack[] array = null;

	public IngredientMultiTag(ResourceLocation... tags)
	{
		super(Stream.empty());
		this.tags = new ArrayList<>();
		for(int i = 0; i < tags.length; i++)
			this.tags.add(ItemTags.getCollection().getOrCreate(tags[i]));
	}

	private int totalSize()
	{
		int i = 0;
		for(ITag<Item> list : tags)
			i += list.func_230236_b_().size();
		return i;
	}

	@Override
	@Nonnull
	public ItemStack[] getMatchingStacks()
	{
		if(array==null||this.array.length!=totalSize())
		{
			NonNullList<ItemStack> lst = NonNullList.create();
			for(ITag<Item> list : tags)
				for(Item stack : list.func_230236_b_())
					stack.getItem().fillItemGroup(ItemGroup.SEARCH, lst);
			this.array = lst.toArray(new ItemStack[0]);
		}
		return this.array;
	}

	@Override
	@Nonnull
	@OnlyIn(Dist.CLIENT)
	public IntList getValidItemStacksPacked()
	{
		if(this.itemIds==null||this.itemIds.size()!=totalSize())
		{
			this.itemIds = new IntArrayList(totalSize());

			for(ITag<Item> list : tags)
				for(Item item : list.func_230236_b_())
					this.itemIds.add(RecipeItemHelper.pack(new ItemStack(item)));
			this.itemIds.sort(IntComparators.NATURAL_COMPARATOR);
		}

		return this.itemIds;
	}


	@Override
	public boolean test(@Nullable ItemStack input)
	{
		if(input==null)
			return false;

		for(ITag<Item> list : tags)
			if(list.func_230235_a_(input.getItem()))
				return true;

		return false;
	}

	@Override
	protected void invalidate()
	{
		this.itemIds = null;
	}
}
