/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class BaseHelpers
{
	public interface ItemOutput<T>
	{
		T output(TagOutput output);

		default T output(TagKey<Item> output, int count)
		{
			return output(new TagOutput(output, count));
		}

		default T output(Item output, int count)
		{
			return output(new TagOutput(output, count));
		}

		default T output(ItemStack itemStack)
		{
			return output(new TagOutput(itemStack));
		}

		default T output(TagKey<Item> output)
		{
			return output(new TagOutput(output));
		}

		default T output(ItemLike output, int count)
		{
			return output(new TagOutput(output, count));
		}

		default T output(ItemLike output)
		{
			return output(new TagOutput(output));
		}

		default T output(IngredientWithSize output)
		{
			return output(new TagOutput(output));
		}
	}

	public interface UnsizedItemInput<T>
	{
		T input(Ingredient input);

		default T input(TagKey<Item> ore)
		{
			return input(Ingredient.of(ore));
		}

		default T input(ItemLike input)
		{
			return input(Ingredient.of(input));
		}

	}

	public interface ItemInput<T> extends UnsizedItemInput<T>
	{
		T input(IngredientWithSize input);

		default T input(Ingredient input)
		{
			return input(input, 1);
		}

		default T input(Ingredient input, int count)
		{
			return input(new IngredientWithSize(input, count));
		}

		default T input(TagKey<Item> ore, int count)
		{
			return input(Ingredient.of(ore), count);
		}

		default T input(ItemLike input, int count)
		{
			return input(Ingredient.of(input), count);
		}
	}
}
