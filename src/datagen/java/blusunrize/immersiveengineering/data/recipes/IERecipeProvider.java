/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.TagValue;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static blusunrize.immersiveengineering.api.utils.TagUtils.createItemWrapper;

public abstract class IERecipeProvider extends RecipeProvider
{
	protected static final int standardSmeltingTime = 200;
	protected static final int blastDivider = 2;

	private final HashMap<String, Integer> pathCount = new HashMap<>();

	public IERecipeProvider(PackOutput p_248933_, CompletableFuture<Provider> provider)
	{
		super(p_248933_, provider);
	}

	protected ResourceLocation toRL(String s)
	{
		if(!s.contains("/"))
			s = "crafting/"+s;
		if(pathCount.containsKey(s))
		{
			int count = pathCount.get(s)+1;
			pathCount.put(s, count);
			return IEApi.ieLoc(s+count);
		}
		pathCount.put(s, 1);
		return IEApi.ieLoc(s);
	}

	protected void add3x3Conversion(ItemLike bigItem, ItemLike smallItem, TagKey<Item> smallTag, RecipeOutput out)
	{
		shapedMisc(bigItem)
				.define('s', smallTag)
				.define('i', smallItem)
				.pattern("sss")
				.pattern("sis")
				.pattern("sss")
				.unlockedBy("has_"+toPath(smallItem), has(smallItem))
				.save(out, toRL(toPath(smallItem)+"_to_")+toPath(bigItem));
		shapelessMisc(smallItem, 9)
				.requires(bigItem)
				.unlockedBy("has_"+toPath(bigItem), has(smallItem))
				.save(out, toRL(toPath(bigItem)+"_to_"+toPath(smallItem)));
	}

	protected ShapedRecipeBuilder shapedMisc(ItemLike output)
	{
		return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output);
	}

	protected ShapedRecipeBuilder shapedMisc(ItemLike output, int count)
	{
		return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output, count);
	}

	protected ShapelessRecipeBuilder shapelessMisc(ItemLike output)
	{
		return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, output);
	}

	protected ShapelessRecipeBuilder shapelessMisc(ItemLike output, int count)
	{
		return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, output, count);
	}

	protected String toPath(ItemLike src)
	{
		return BuiltInRegistries.ITEM.getKey(src.asItem()).getPath();
	}

	/**
	 * For smelting recipes that also have a blasting recipe, like ores
	 * keep the smelting postfix in mind when using this for non-ores or weird cases where the primary recipe for the ingot is not occupied by the smelting recipe
	 * has an overloaded method for regular use
	 *
	 * @param input        the recipe's input
	 * @param output       the recipe's output
	 * @param xp           experience awarded per smelted item
	 * @param smeltingTime smelting time in ticks
	 * @param extraPostfix adds an additional postfix before the smelting/blasting postfix when needed (for example used by dusts)
	 */
	protected void addStandardSmeltingBlastingRecipe(ItemLike input, ItemLike output, float xp, int smeltingTime, RecipeOutput out, String extraPostfix)
	{
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, output, xp, smeltingTime)
				.unlockedBy("has_"+toPath(input), has(input))
				.save(out, toRL("smelting/"+toPath(output)+extraPostfix));
		SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, output, xp, smeltingTime/blastDivider)
				.unlockedBy("has_"+toPath(input), has(input))
				.save(out, toRL("smelting/"+toPath(output)+extraPostfix+"_from_blasting"));
	}

	/**
	 * For Recipes like Coke or Blast Bricks, which use the same item in all corners, one for the sides and one for the middle
	 * Also work for shapes like TNT
	 *
	 * @param output the recipe's output
	 * @param corner the item in the corners
	 * @param side   the item on the sides
	 * @param middle the item in the middle
	 */
	@ParametersAreNonnullByDefault
	protected void addCornerStraightMiddle(ItemLike output, int count, Ingredient corner, Ingredient side, Ingredient middle,
										   Criterion<?> condition, RecipeOutput out)
	{
		shapedMisc(output, count)
				.define('c', corner)
				.define('s', side)
				.define('m', middle)
				.pattern("csc")
				.pattern("sms")
				.pattern("csc")
				.unlockedBy("has_item", condition)
				.save(out, toRL(toPath(output)));
	}

	/**
	 * For Recipes consisting of layers
	 *
	 * @param output the recipe's output
	 * @param top    the item on the top
	 * @param middle the item in the middle
	 * @param bottom the item on the bottom
	 */
	@ParametersAreNonnullByDefault
	protected void addSandwich(ItemLike output, int count, Ingredient top, Ingredient middle, Ingredient bottom,
							   Criterion<?> condition, RecipeOutput out)
	{
		shapedMisc(output, count)
				.define('t', top)
				.define('m', middle)
				.define('b', bottom)
				.pattern("ttt")
				.pattern("mmm")
				.pattern("bbb")
				.unlockedBy("has_item", condition)
				.save(out, toRL(toPath(output)));
	}

	protected void addWall(ItemLike block, RecipeOutput out)
	{
		ItemLike wall = IEBlocks.TO_WALL.get(BuiltInRegistries.ITEM.getKey(block.asItem()));
		shapedMisc(wall, 6)
				.define('s', block)
				.pattern("sss")
				.pattern("sss")
				.unlockedBy("has_"+toPath(block), has(block))
				.save(out, toRL(toPath(wall)));
	}

	protected void addStonecuttingRecipe(ItemLike input, ItemLike output, RecipeOutput out)
	{
		addStonecuttingRecipe(input, output, 1, out);
	}

	protected void addStonecuttingRecipe(ItemLike input, ItemLike output, int amount, RecipeOutput out)
	{
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(input), RecipeCategory.MISC, output, amount)
				.unlockedBy("has_"+toPath(input), has(input))
				.save(out, toRL("stonecutting/"+toPath(output)));
	}

	protected void addStandardSmeltingBlastingRecipe(ItemLike input, ItemLike output, float xp, RecipeOutput out)
	{
		addStandardSmeltingBlastingRecipe(input, output, xp, out, "");
	}

	protected void addStandardSmeltingBlastingRecipe(ItemLike input, ItemLike output, float xp, RecipeOutput out, String extraPostfix)
	{
		addStandardSmeltingBlastingRecipe(input, output, xp, standardSmeltingTime, out, extraPostfix);
	}

	public static ICondition getTagCondition(TagKey<?> tag)
	{
		return new NotCondition(new TagEmptyCondition(tag.location()));
	}

	public static ICondition getTagCondition(ResourceLocation tag)
	{
		return getTagCondition(createItemWrapper(tag));
	}

	@Nonnull
	protected Ingredient makeIngredient(ItemLike in)
	{
		return Ingredient.of(in);
	}

	@Nonnull
	protected Ingredient makeIngredient(TagKey<Item> in)
	{
		return Ingredient.of(in);
	}

	@SafeVarargs
	@Nonnull
	protected final Ingredient makeIngredient(TagKey<Item>... tags)
	{
		return Ingredient.fromValues(Arrays.stream(tags).map(TagValue::new));
	}
}
