/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.core.Registry;
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

public class TagUtils
{
	public static Tag<Item> getItemTag(TagContainer tags, ResourceLocation key)
	{
		return tags.getOrEmpty(Registry.ITEM_REGISTRY).getTag(key);
	}

	public static Tag<Block> getBlockTag(TagContainer tags, ResourceLocation key)
	{
		return tags.getOrEmpty(Registry.BLOCK_REGISTRY).getTag(key);
	}

	public static Collection<ResourceLocation> getTagsForItem(TagContainer tags, Item item)
	{
		return tags.getOrEmpty(Registry.ITEM_REGISTRY).getMatchingTags(item);
	}

	public static Collection<ResourceLocation> getTagsForBlock(TagContainer tags, Block block)
	{
		return tags.getOrEmpty(Registry.BLOCK_REGISTRY).getMatchingTags(block);
	}

	public static boolean isInBlockOrItemTag(TagContainer tags, ItemStack stack, ResourceLocation oreName)
	{
		if(!isNonemptyBlockOrItemTag(tags, oreName))
			return false;
		Tag<Item> itemTag = getItemTag(tags, oreName);
		if(itemTag!=null&&itemTag.getValues().contains(stack.getItem()))
			return true;
		Tag<Block> blockTag = getBlockTag(tags, oreName);
		return blockTag!=null&&blockTag.getValues()
				.stream()
				.map(ItemLike::asItem)
				.anyMatch(i -> stack.getItem()==i);
	}

	public static boolean isNonemptyItemTag(TagContainer tags, ResourceLocation name)
	{
		Tag<Item> t = getItemTag(tags, name);
		return t!=null&&!t.getValues().isEmpty();
	}

	public static boolean isNonemptyBlockTag(TagContainer tags, ResourceLocation name)
	{
		Tag<Block> t = getBlockTag(tags, name);
		return t!=null&&!t.getValues().isEmpty();
	}

	public static boolean isNonemptyBlockOrItemTag(TagContainer tags, ResourceLocation name)
	{
		return isNonemptyBlockTag(tags, name)||isNonemptyItemTag(tags, name);
	}

	public static String getMatchingPrefix(TagContainer tags, ItemStack stack, String... componentTypes)
	{
		for(ResourceLocation name : getMatchingTagNames(tags, stack))
			for(String componentType : componentTypes)
				if(name.getPath().startsWith(componentType))
					return componentType;
		return null;
	}

	public static Collection<ResourceLocation> getMatchingTagNames(TagContainer tags, ItemStack stack)
	{
		Collection<ResourceLocation> ret = new HashSet<>(getTagsForItem(tags, stack.getItem()));
		Block b = Block.byItem(stack.getItem());
		if(b!=Blocks.AIR)
			ret.addAll(getTagsForBlock(tags, b));
		return ret;
	}

	public static String[] getMatchingPrefixAndRemaining(TagContainer tags, ItemStack stack, String... componentTypes)
	{
		for(ResourceLocation name : getMatchingTagNames(tags, stack))
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

	public static boolean isIngot(TagContainer tags, ItemStack stack)
	{
		return tags.getOrEmpty(Registry.ITEM_REGISTRY).getTag(Items.INGOTS.getName()).contains(stack.getItem());
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
