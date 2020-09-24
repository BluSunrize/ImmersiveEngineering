/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

public class SawmillRecipeBuilder extends IEFinishedRecipe<SawmillRecipeBuilder>
{
	JsonArray secondaryArray = new JsonArray();

	private SawmillRecipeBuilder()
	{
		super(SawmillRecipe.SERIALIZER.get());
		addWriter(jsonObject -> jsonObject.add("secondaries", secondaryArray));
	}

	public static SawmillRecipeBuilder builder(Item result)
	{
		return new SawmillRecipeBuilder().addResult(result);
	}

	public static SawmillRecipeBuilder builder(ItemStack result)
	{
		return new SawmillRecipeBuilder().addResult(result);
	}

	public static SawmillRecipeBuilder builder(Tag<Item> result, int count)
	{
		return new SawmillRecipeBuilder().addResult(new IngredientWithSize(result, count));
	}

	public SawmillRecipeBuilder addStripped(IItemProvider itemProvider)
	{
		return addItem("stripped", new ItemStack(itemProvider));
	}

	public SawmillRecipeBuilder addStripped(ItemStack itemStack)
	{
		return addItem("stripped", itemStack);
	}

	public SawmillRecipeBuilder addStripped(Tag<Item> tag)
	{
		return addStripped(new IngredientWithSize(tag));
	}

	public SawmillRecipeBuilder addStripped(IngredientWithSize ingredient, ICondition... conditions)
	{
		return addWriter(jsonObject -> jsonObject.add("stripped", ingredient.serialize()));
	}

	public SawmillRecipeBuilder addSecondary(IItemProvider itemProvider, boolean stripping)
	{
		return this.addSecondary(new ItemStack(itemProvider), stripping);
	}

	public SawmillRecipeBuilder addSecondary(ItemStack itemStack, boolean stripping)
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("output", serializeItemStack(itemStack));
		jsonObject.addProperty("stripping", stripping);
		secondaryArray.add(jsonObject);
		return this;
	}

	public SawmillRecipeBuilder addSecondary(Tag<Item> tag, boolean stripping)
	{
		return addSecondary(new IngredientWithSize(tag), stripping);
	}

	public SawmillRecipeBuilder addSecondary(IngredientWithSize ingredient, boolean stripping, ICondition... conditions)
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("output", ingredient.serialize());
		jsonObject.addProperty("stripping", stripping);
		if(conditions.length > 0)
		{
			JsonArray conditionArray = new JsonArray();
			for(ICondition condition : conditions)
				conditionArray.add(CraftingHelper.serialize(condition));
			jsonObject.add("conditions", conditionArray);
		}
		secondaryArray.add(jsonObject);
		return this;
	}
}
