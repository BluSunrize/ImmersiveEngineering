/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class IngredientWithSize implements Predicate<ItemStack>
{
	private static final Codec<IngredientWithSize> COUNT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Ingredient.CODEC.fieldOf("basePredicate").forGetter(i -> i.basePredicate),
			Codec.INT.fieldOf("count").forGetter(i -> i.count)
	).apply(inst, IngredientWithSize::new));
	private static final Codec<IngredientWithSize> SINGLE_CODEC = Ingredient.CODEC.xmap(
			IngredientWithSize::new, IngredientWithSize::getBaseIngredient
	);
	public static final Codec<IngredientWithSize> CODEC = Codec.either(COUNT_CODEC, SINGLE_CODEC)
			.xmap(
					e -> e.map(Function.identity(), Function.identity()),
					iws -> iws.count==1?Either.right(iws): Either.left(iws)
			);

	public static final SetRestrictedField<IIngredientWithSizeSerializer> SERIALIZER = SetRestrictedField.common();
	protected final Ingredient basePredicate;
	protected final int count;

	public IngredientWithSize(Ingredient basePredicate, int count)
	{
		this.basePredicate = basePredicate;
		this.count = count;
	}

	public IngredientWithSize(Ingredient basePredicate)
	{
		this(basePredicate, 1);
	}

	public IngredientWithSize(TagKey<Item> basePredicate, int count)
	{
		this(Ingredient.of(basePredicate), count);
	}

	public IngredientWithSize(TagKey<Item> basePredicate)
	{
		this(basePredicate, 1);
	}

	public static IngredientWithSize read(FriendlyByteBuf input)
	{
		return SERIALIZER.get().parse(input);
	}

	@Override
	public boolean test(@Nullable ItemStack itemStack)
	{
		if(itemStack==null)
			return false;
		return basePredicate.test(itemStack)&&itemStack.getCount() >= this.count;
	}

	@Nonnull
	public ItemStack[] getMatchingStacks()
	{
		ItemStack[] baseStacks = basePredicate.getItems();
		ItemStack[] ret = new ItemStack[baseStacks.length];
		for(int i = 0; i < baseStacks.length; ++i)
			ret[i] = ItemHandlerHelper.copyStackWithSize(baseStacks[i], this.count);
		return ret;
	}

	@Nonnull
	public List<ItemStack> getMatchingStackList()
	{
		return Arrays.asList(getMatchingStacks());
	}

	@Nonnull
	public JsonElement serialize()
	{
		return CODEC.encodeStart(JsonOps.INSTANCE, this).result().orElseThrow();
	}

	public boolean hasNoMatchingItems()
	{
		return basePredicate.isEmpty();
	}

	public int getCount()
	{
		return count;
	}

	public Ingredient getBaseIngredient()
	{
		return basePredicate;
	}

	public IngredientWithSize withSize(int size)
	{
		return new IngredientWithSize(this.basePredicate, size);
	}

	public static IngredientWithSize of(ItemStack stack)
	{
		return new IngredientWithSize(Ingredient.of(stack), stack.getCount());
	}

	public ItemStack getRandomizedExampleStack(int rand)
	{
		ItemStack[] all = getMatchingStacks();
		if(all.length==0)
			return ItemStack.EMPTY;
		else
			return all[(rand/20)%all.length];
	}

	public boolean testIgnoringSize(ItemStack itemstack)
	{
		return basePredicate.test(itemstack);
	}

	public void write(FriendlyByteBuf out)
	{
		SERIALIZER.get().write(out, this);
	}
}
