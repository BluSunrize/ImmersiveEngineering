/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import malte0811.dualcodecs.DualCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TagOutputList
{
	public static final DualCodec<RegistryFriendlyByteBuf, TagOutputList> CODEC = TagOutput.CODECS.listOf().map(
			TagOutputList::new, TagOutputList::getLazyList
	);
	public static TagOutputList EMPTY = new TagOutputList(List.of());

	private final List<TagOutput> lazyList;
	private NonNullList<ItemStack> eagerList;

	public TagOutputList(List<TagOutput> lazyList)
	{
		this.lazyList = List.copyOf(lazyList);
	}

	public TagOutputList(TagOutput lazyList)
	{
		this(List.of(lazyList));
	}

	public List<TagOutput> getLazyList()
	{
		return lazyList;
	}

	public NonNullList<ItemStack> get()
	{
		if(eagerList==null)
		{
			eagerList = NonNullList.create();
			for(TagOutput output : lazyList)
				eagerList.add(output.get());
		}
		return eagerList;
	}
}
