/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IEFinishedRecipe<R extends IEFinishedRecipe<R>> implements IFinishedRecipe
{
	private final IERecipeSerializer<?> serializer;
	private final List<Consumer<JsonObject>> writerFunctions;
	private ResourceLocation id;

	protected JsonArray inputArray = null;
	protected int inputCount = 0;
	protected int maxInputCount = 1;

	protected JsonArray resultArray = null;
	protected int resultCount = 0;
	protected int maxResultCount = 1;

	protected JsonArray conditions = null;

	protected IEFinishedRecipe(IERecipeSerializer<?> serializer)
	{
		this.serializer = serializer;
		this.writerFunctions = new ArrayList<>();
	}

	protected boolean isComplete()
	{
		return true;
	}

	public void build(Consumer<IFinishedRecipe> out, ResourceLocation id)
	{
		Preconditions.checkArgument(isComplete(), "This recipe is incomplete");
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

	public R setEnergy(int energy)
	{
		return addWriter(jsonObject -> jsonObject.addProperty("energy", energy));
	}

	/* =============== Result Handling =============== */

	public R setMultipleResults(int maxResultCount)
	{
		this.resultArray = new JsonArray();
		this.maxResultCount = maxResultCount;
		return addWriter(jsonObject -> jsonObject.add("results", resultArray));
	}

	@SuppressWarnings("unchecked cast")
	public R addMultiResult(JsonElement obj)
	{
		Preconditions.checkArgument(maxResultCount > 1, "This recipe does not support multiple results");
		Preconditions.checkArgument(resultCount < maxResultCount, "Recipe can only have "+maxResultCount+" results");
		resultArray.add(obj);
		resultCount++;
		return (R)this;
	}

	public R addResult(IItemProvider itemProvider)
	{
		return addResult(new ItemStack(itemProvider));
	}

	public R addResult(ItemStack itemStack)
	{
		if(resultArray!=null)
			return addMultiResult(serializeItemStack(itemStack));
		else
			return addItem("result", itemStack);
	}

	public R addResult(Ingredient ingredient)
	{
		if(resultArray!=null)
			return addMultiResult(ingredient.serialize());
		else
			return addWriter(jsonObject -> jsonObject.add("result", ingredient.serialize()));
	}

	public R addResult(IngredientWithSize ingredientWithSize)
	{
		if(resultArray!=null)
			return addMultiResult(ingredientWithSize.serialize());
		else
			return addWriter(jsonObject -> jsonObject.add("result", ingredientWithSize.serialize()));
	}

	/* =============== Input Handling =============== */

	public R setUseInputArray(int maxInputCount, String key)
	{
		this.inputArray = new JsonArray();
		this.maxInputCount = maxInputCount;
		return addWriter(jsonObject -> jsonObject.add(key, inputArray));
	}

	public R setUseInputArray(int maxInputCount)
	{
		return setUseInputArray(maxInputCount, "inputs");
	}

	@SuppressWarnings("unchecked cast")
	public R addMultiInput(JsonElement obj)
	{
		Preconditions.checkArgument(maxInputCount > 1, "This recipe does not support multiple inputs");
		Preconditions.checkArgument(inputCount < maxInputCount, "Recipe can only have "+maxInputCount+" inputs");
		inputArray.add(obj);
		inputCount++;
		return (R)this;
	}

	public R addMultiInput(Ingredient ingredient)
	{
		return addMultiInput(ingredient.serialize());
	}

	public R addMultiInput(IngredientWithSize ingredient)
	{
		return addMultiInput(ingredient.serialize());
	}

	protected String generateSafeInputKey()
	{
		Preconditions.checkArgument(inputCount < maxInputCount, "Recipe can only have "+maxInputCount+" inputs");
		String key = maxInputCount==1?"input": "input"+inputCount;
		inputCount++;
		return key;
	}

	public R addInput(IItemProvider... itemProviders)
	{
		if(inputArray!=null)
			return addMultiInput(Ingredient.fromItems(itemProviders));
		else
			return addIngredient(generateSafeInputKey(), itemProviders);
	}

	public R addInput(ItemStack... itemStacks)
	{
		if(inputArray!=null)
			return addMultiInput(Ingredient.fromStacks(itemStacks));
		else
			return addIngredient(generateSafeInputKey(), itemStacks);
	}

	public R addInput(Tag<Item> tag)
	{
		if(inputArray!=null)
			return addMultiInput(Ingredient.fromTag(tag));
		else
			return addIngredient(generateSafeInputKey(), tag);
	}

	public R addInput(Ingredient input)
	{
		if(inputArray!=null)
			return addMultiInput(input);
		else
			return addIngredient(generateSafeInputKey(), input);
	}

	public R addInput(IngredientWithSize input)
	{
		if(inputArray!=null)
			return addMultiInput(input);
		else
			return addIngredient(generateSafeInputKey(), input);
	}

	/* =============== ItemStacks =============== */

	public JsonObject serializeItemStack(ItemStack stack)
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("item", stack.getItem().getRegistryName().toString());
		if(stack.getCount() > 1)
			obj.addProperty("count", stack.getCount());
		if(stack.hasTag())
			obj.addProperty("nbt", stack.getTag().toString());
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

	/* =============== Fluids =============== */

	public R addFluid(String key, FluidStack fluidStack)
	{
		return addWriter(jsonObject -> jsonObject.add(key, ApiUtils.jsonSerializeFluidStack(fluidStack)));
	}

	public R addFluid(FluidStack fluidStack)
	{
		return addFluid("fluid", fluidStack);
	}

	public R addFluid(Fluid fluid, int amount)
	{
		return addFluid("fluid", new FluidStack(fluid, amount));
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
