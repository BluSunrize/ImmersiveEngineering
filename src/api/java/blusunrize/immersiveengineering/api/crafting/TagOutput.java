/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.utils.codec.IECodecs;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import malte0811.dualcodecs.DualCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.function.Function;

public class TagOutput
{
	public static final TagOutput EMPTY = new TagOutput(new IngredientWithSize(Ingredient.EMPTY));
	public static final DualCodec<RegistryFriendlyByteBuf, TagOutput> CODECS = new DualCodec<>(
			Codec.either(IngredientWithSize.CODEC, IECodecs.ITEM_STACK_COUNT_OPTIONAL).xmap(TagOutput::new, out -> out.rawData),
			ItemStack.OPTIONAL_STREAM_CODEC.map(TagOutput::new, TagOutput::get)
	);

	private final Either<IngredientWithSize, ItemStack> rawData;
	private ItemStack cachedStack;

	public TagOutput(Either<IngredientWithSize, ItemStack> type)
	{
		this.rawData = type;
	}

	public TagOutput(ItemLike type)
	{
		this(type, 1);
	}

	public TagOutput(ItemLike type, int count)
	{
		this(new ItemStack(type, count));
	}

	public TagOutput(IngredientWithSize type)
	{
		this(Either.left(type));
	}

	public TagOutput(ItemStack stack)
	{
		this(Either.right(stack));
	}

	public TagOutput(TagKey<Item> type, int count)
	{
		this(new IngredientWithSize(type, count));
	}

	public TagOutput(TagKey<Item> type)
	{
		this(type, 1);
	}

	public ItemStack get()
	{
		if(cachedStack==null)
			cachedStack = rawData.map(
					iws -> {
						if(iws.basePredicate.isEmpty())
							return ItemStack.EMPTY;
						else
							return IEApi.getPreferredStackbyMod(iws.getMatchingStacks());
					},
					Function.identity()
			);
		return cachedStack;
	}
}
