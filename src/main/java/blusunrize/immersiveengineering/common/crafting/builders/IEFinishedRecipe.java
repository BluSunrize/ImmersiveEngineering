/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.builders;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IEFinishedRecipe<R extends IEFinishedRecipe<?>> implements IFinishedRecipe
{
	private final IERecipeSerializer<?> serializer;
	private final List<Consumer<JsonObject>> writerFunctions;
	private ResourceLocation id;

	protected int inputCount = 0;
	protected int maxInputCount = 1;

	protected JsonArray multipleResults = null;
	protected int resultCount = 0;
	protected int maxResultCount = 1;

	protected JsonArray conditions = null;

	protected IEFinishedRecipe(IERecipeSerializer<?> serializer)
	{
		this.serializer = serializer;
		this.writerFunctions = new ArrayList<>();
	}

	public void build(Consumer<IFinishedRecipe> out, ResourceLocation id)
	{
		this.id = id;
		out.accept(this);
	}

	@SuppressWarnings("unchecked cast")
	public R addWriter(Consumer<JsonObject> writer)
	{
		Preconditions.checkArgument(id==null, "This recipe has already been finalized");
		this.writerFunctions.add(writer);
		return (R)this;
	}

	@SuppressWarnings("unchecked cast")
	public R addCondition(ICondition condition)
	{
		if(this.conditions==null)
		{
			this.conditions = new JsonArray();
			addWriter(jsonObject -> jsonObject.add("conditions", conditions));
		}
		this.conditions.add(CraftingHelper.serialize(condition));
		return (R)this;
	}

	/* =============== Common Objects =============== */

	public R setTime(int time)
	{
		return addWriter(jsonObject -> jsonObject.addProperty("time", time));
	}

	/* =============== Result Handling =============== */

	public R setMultipleResults(int maxResultCount)
	{
		this.multipleResults = new JsonArray();
		this.maxResultCount = maxResultCount;
		return addWriter(jsonObject -> jsonObject.add("results", multipleResults));
	}

	@SuppressWarnings("unchecked cast")
	public R addMultiResult(JsonElement obj)
	{
		Preconditions.checkArgument(maxResultCount <= 1, "This recipe does not support multiple results");
		Preconditions.checkArgument(resultCount < maxResultCount, "Recipe can only have "+maxResultCount+" results");
		multipleResults.add(obj);
		resultCount++;
		return (R)this;
	}

	public R addResult(IItemProvider itemProvider)
	{
		return addResult(new ItemStack(itemProvider));
	}

	public R addResult(ItemStack itemStack)
	{
		if(multipleResults!=null)
			return addMultiResult(serializeItemStack(itemStack));
		else
			return addItem("result", itemStack);
	}

	public R addResult(Ingredient ingredient)
	{
		if(multipleResults!=null)
			return addMultiResult(ingredient.serialize());
		else
			return addWriter(jsonObject -> jsonObject.add("result", ingredient.serialize()));
	}

	public R addResult(IngredientWithSize ingredientWithSize)
	{
		if(multipleResults!=null)
			return addMultiResult(ingredientWithSize.serialize());
		else
			return addWriter(jsonObject -> jsonObject.add("result", ingredientWithSize.serialize()));
	}

	/* =============== Input Handling =============== */

	protected String generateSafeInputKey()
	{
		Preconditions.checkArgument(inputCount < maxInputCount, "Recipe can only have "+maxInputCount+" inputs");
		String key = maxInputCount==1?"input": "input"+inputCount;
		inputCount++;
		return key;
	}

	public R addInput(IItemProvider... itemProviders)
	{
		return addIngredient(generateSafeInputKey(), itemProviders);
	}

	public R addInput(ItemStack... itemStacks)
	{
		return addIngredient(generateSafeInputKey(), itemStacks);
	}

	public R addInput(Tag<Item> tag)
	{
		return addIngredient(generateSafeInputKey(), tag);
	}

	public R addInput(Ingredient input)
	{
		return addIngredient(generateSafeInputKey(), input);
	}

	public R addInput(IngredientWithSize input)
	{
		return addIngredient(generateSafeInputKey(), input);
	}

	/* =============== ItemStacks =============== */

	public JsonObject serializeItemStack(ItemStack stack)
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("item", stack.getItem().getRegistryName().toString());
		if(stack.getCount() > 1)
			obj.addProperty("count", stack.getCount());
		return obj;
	}

	public R addItem(String key, IItemProvider item)
	{
		return addItem(key, new ItemStack(item));
	}

	public R addItem(String key, ItemStack stack)
	{
		Preconditions.checkArgument(!stack.isEmpty(), "May not add empty ItemStack to recipe");
		return addWriter(jsonObject -> jsonObject.add(key, serializeItemStack(stack)));
	}

	/* =============== Ingredients =============== */

	public R addIngredient(String key, IItemProvider... itemProviders)
	{
		return addIngredient(key, Ingredient.fromItems(itemProviders));
	}

	public R addIngredient(String key, ItemStack... itemStacks)
	{
		return addIngredient(key, Ingredient.fromStacks(itemStacks));
	}

	public R addIngredient(String key, Tag<Item> tag)
	{
		return addIngredient(key, Ingredient.fromTag(tag));
	}

	public R addIngredient(String key, Ingredient ingredient)
	{
		return addWriter(jsonObject -> jsonObject.add(key, ingredient.serialize()));
	}

	public R addIngredient(String key, IngredientWithSize ingredient)
	{
		return addWriter(jsonObject -> jsonObject.add(key, ingredient.serialize()));
	}

	/* =============== IFinishedRecipe =============== */

	@Override
	public void serialize(JsonObject jsonObject)
	{
		for(Consumer<JsonObject> writer : this.writerFunctions)
			writer.accept(jsonObject);
	}

	@Override
	public ResourceLocation getID()
	{
		return id;
	}

	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return serializer;
	}

	@Nullable
	@Override
	public JsonObject getAdvancementJson()
	{
		return null;
	}

	@Nullable
	@Override
	public ResourceLocation getAdvancementID()
	{
		return null;
	}
}
