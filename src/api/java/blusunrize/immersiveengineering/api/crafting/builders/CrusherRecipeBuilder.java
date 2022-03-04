/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.ICondition;

public class CrusherRecipeBuilder extends IEFinishedRecipe<CrusherRecipeBuilder>
{
	JsonArray secondaryArray = new JsonArray();

	private CrusherRecipeBuilder()
	{
		super(CrusherRecipe.SERIALIZER.get());
		addWriter(jsonObject -> jsonObject.add("secondaries", secondaryArray));
	}

	public static CrusherRecipeBuilder builder(Item result)
	{
		return new CrusherRecipeBuilder().addResult(result);
	}

	public static CrusherRecipeBuilder builder(ItemStack result)
	{
		return new CrusherRecipeBuilder().addResult(result);
	}

	public static CrusherRecipeBuilder builder(TagKey<Item> result, int count)
	{
		return new CrusherRecipeBuilder().addResult(new IngredientWithSize(result, count));
	}

	public static CrusherRecipeBuilder builder(IngredientWithSize result)
	{
		return new CrusherRecipeBuilder().addResult(result);
	}

	public CrusherRecipeBuilder addSecondary(ItemLike itemProvider, float chance)
	{
		return this.addSecondary(new ItemStack(itemProvider), chance);
	}

	public CrusherRecipeBuilder addSecondary(ItemStack itemStack, float chance)
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("chance", chance);
		jsonObject.add("output", serializeItemStack(itemStack));
		secondaryArray.add(jsonObject);
		return this;
	}

	public CrusherRecipeBuilder addSecondary(TagKey<Item> tag, float chance)
	{
		return addSecondary(new IngredientWithSize(tag), chance);
	}

	public CrusherRecipeBuilder addSecondary(IngredientWithSize ingredient, float chance, ICondition... conditions)
	{
		secondaryArray.add(serializeStackWithChance(ingredient, chance, conditions));
		return this;
	}
}
