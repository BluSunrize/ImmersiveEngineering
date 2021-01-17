/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.common.crafting.IngredientWithSizeSerializer;
import com.google.gson.JsonElement;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public class IngredientWithSize implements Predicate<ItemStack>
{
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

	public IngredientWithSize(ITag<Item> basePredicate, int count)
	{
		this(Ingredient.fromTag(basePredicate), count);
	}

	public IngredientWithSize(ITag<Item> basePredicate)
	{
		this(basePredicate, 1);
	}

	public static IngredientWithSize deserialize(JsonElement input)
	{
		return IngredientWithSizeSerializer.INSTANCE.parse(input);
	}

	public static IngredientWithSize read(PacketBuffer input)
	{
		return IngredientWithSizeSerializer.INSTANCE.parse(input);
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
		ItemStack[] baseStacks = basePredicate.getMatchingStacks();
		ItemStack[] ret = new ItemStack[baseStacks.length];
		for(int i = 0; i < baseStacks.length; ++i)
			ret[i] = ItemHandlerHelper.copyStackWithSize(baseStacks[i], this.count);
		return ret;
	}

	@Nonnull
	public JsonElement serialize()
	{
		return IngredientWithSizeSerializer.INSTANCE.write(this);
	}

	public boolean hasNoMatchingItems()
	{
		return basePredicate.hasNoMatchingItems();
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
		return new IngredientWithSize(Ingredient.fromStacks(stack), stack.getCount());
	}

	public ItemStack getRandomizedExampleStack(int rand)
	{
		ItemStack[] all = getMatchingStacks();
		return all[(rand/20)%all.length];
	}

	public boolean testIgnoringSize(ItemStack itemstack)
	{
		return basePredicate.test(itemstack);
	}

	public void write(PacketBuffer out)
	{
		IngredientWithSizeSerializer.INSTANCE.write(out, this);
	}
}
