/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.utils.TagUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
	private List<Tag<Item>> tags;
	private IntList itemIds = null;
	private ItemStack[] array = null;

	public IngredientMultiTag(ResourceLocation... tags)
	{
		super(Stream.empty());
		this.tags = new ArrayList<>();
		for(ResourceLocation tag : tags)
			if(TagUtils.isNonemptyItemTag(tag))
				this.tags.add(ItemTags.getAllTags().getTag(tag));
	}

	private int totalSize()
	{
		int i = 0;
		for(Tag<Item> list : tags)
			i += list.getValues().size();
		return i;
	}

	@Override
	@Nonnull
	public ItemStack[] getItems()
	{
		if(array==null||this.array.length!=totalSize())
		{
			NonNullList<ItemStack> lst = NonNullList.create();
			for(Tag<Item> list : tags)
				for(Item stack : list.getValues())
					stack.fillItemCategory(CreativeModeTab.TAB_SEARCH, lst);
			this.array = lst.toArray(new ItemStack[0]);
		}
		return this.array;
	}

	@Override
	@Nonnull
	@OnlyIn(Dist.CLIENT)
	public IntList getStackingIds()
	{
		if(this.itemIds==null||this.itemIds.size()!=totalSize())
		{
			this.itemIds = new IntArrayList(totalSize());

			for(Tag<Item> list : tags)
				for(Item item : list.getValues())
					this.itemIds.add(StackedContents.getStackingIndex(new ItemStack(item)));
			this.itemIds.sort(IntComparators.NATURAL_COMPARATOR);
		}

		return this.itemIds;
	}


	@Override
	public boolean test(@Nullable ItemStack input)
	{
		if(input==null)
			return false;

		for(Tag<Item> list : tags)
			if(list.contains(input.getItem()))
				return true;

		return false;
	}

	@Override
	protected void invalidate()
	{
		this.itemIds = null;
	}
}
