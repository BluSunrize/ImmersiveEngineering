/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags.Items;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

public class TagUtils
{
	// These will be overriden on the client side, because TagCollectionManager doesn't work there
	public static Supplier<TagCollection<Item>> GET_ITEM_TAG_COLLECTION;
	public static Supplier<TagCollection<Block>> GET_BLOCK_TAG_COLLECTION;

	static
	{
		setTagCollectionGetters(
				() -> SerializationTags.getInstance().getItems(),
				() -> SerializationTags.getInstance().getBlocks()
		);
	}

	public static void setTagCollectionGetters(
			Supplier<TagCollection<Item>> items, Supplier<TagCollection<Block>> blocks
	)
	{
		GET_ITEM_TAG_COLLECTION = items;
		GET_BLOCK_TAG_COLLECTION = blocks;
	}

	public static Tag<Item> getItemTag(ResourceLocation key)
	{
		return GET_ITEM_TAG_COLLECTION.get().getTag(key);
	}

	public static Tag<Block> getBlockTag(ResourceLocation key)
	{
		return GET_BLOCK_TAG_COLLECTION.get().getTag(key);
	}

	public static Collection<ResourceLocation> getTagsForItem(Item item)
	{
		return GET_ITEM_TAG_COLLECTION.get().getMatchingTags(item);
	}

	public static Collection<ResourceLocation> getTagsForBlock(Block block)
	{
		return GET_BLOCK_TAG_COLLECTION.get().getMatchingTags(block);
	}

	public static boolean isInBlockOrItemTag(ItemStack stack, ResourceLocation oreName)
	{
		if(!isNonemptyBlockOrItemTag(oreName))
			return false;
		Tag<Item> itemTag = getItemTag(oreName);
		if(itemTag!=null&&itemTag.getValues().contains(stack.getItem()))
			return true;
		Tag<Block> blockTag = getBlockTag(oreName);
		return blockTag!=null&&blockTag.getValues()
				.stream()
				.map(ItemLike::asItem)
				.anyMatch(i -> stack.getItem()==i);
	}

	public static boolean isNonemptyItemTag(ResourceLocation name)
	{
		Tag<Item> t = getItemTag(name);
		return t!=null&&!t.getValues().isEmpty();
	}

	public static boolean isNonemptyBlockTag(ResourceLocation name)
	{
		Tag<Block> t = getBlockTag(name);
		return t!=null&&!t.getValues().isEmpty();
	}

	public static boolean isNonemptyBlockOrItemTag(ResourceLocation name)
	{
		return isNonemptyBlockTag(name)||isNonemptyItemTag(name);
	}

	public static NonNullList<ItemStack> getItemsInTag(ResourceLocation name)
	{
		NonNullList<ItemStack> ret = NonNullList.create();
		addItemsInTag(ret, getItemTag(name));
		addItemsInTag(ret, getBlockTag(name));
		return ret;
	}

	private static <T extends ItemLike> void addItemsInTag(NonNullList<ItemStack> out, Tag<T> in)
	{
		if(in!=null)
			in.getValues().stream()
					.map(ItemStack::new)
					.forEach(out::add);
	}

	public static boolean isInPrefixedTag(ItemStack stack, String componentType)
	{
		return getMatchingPrefix(stack, componentType)!=null;
	}

	public static String getMatchingPrefix(ItemStack stack, String... componentTypes)
	{
		for(ResourceLocation name : getMatchingTagNames(stack))
			for(String componentType : componentTypes)
				if(name.getPath().startsWith(componentType))
					return componentType;
		return null;
	}

	public static Collection<ResourceLocation> getMatchingTagNames(ItemStack stack)
	{
		Collection<ResourceLocation> ret = new HashSet<>(getTagsForItem(stack.getItem()));
		Block b = Block.byItem(stack.getItem());
		if(b!=Blocks.AIR)
			ret.addAll(getTagsForBlock(b));
		return ret;
	}

	public static String[] getMatchingPrefixAndRemaining(ItemStack stack, String... componentTypes)
	{
		for(ResourceLocation name : getMatchingTagNames(stack))
		{
			for(String componentType : componentTypes)
				if(name.getPath().startsWith(componentType))
				{
					String material = name.getPath().substring(componentType.length());
					if(material.startsWith("/"))
						material = material.substring(1);
					if(material.length() > 0)
						return new String[]{componentType, material};
				}
		}
		return null;
	}

	public static boolean isIngot(ItemStack stack)
	{
		return getItemTag(Items.INGOTS.getName()).contains(stack.getItem());
	}

	public static boolean isPlate(ItemStack stack)
	{
		return isInPrefixedTag(stack, "plates/");
	}

	public static Named<Item> createItemWrapper(ResourceLocation name)
	{
		return ItemTags.bind(name.toString());
	}

	public static Named<Block> createBlockWrapper(ResourceLocation name)
	{
		return BlockTags.bind(name.toString());
	}

	public static Named<Fluid> createFluidWrapper(ResourceLocation name)
	{
		return FluidTags.bind(name.toString());
	}
}
