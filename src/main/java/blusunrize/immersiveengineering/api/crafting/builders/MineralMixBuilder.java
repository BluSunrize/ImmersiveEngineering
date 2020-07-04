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
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

public class MineralMixBuilder extends IEFinishedRecipe<MineralMixBuilder>
{
	JsonArray oresArray = new JsonArray();
	JsonArray dimensionsArray = new JsonArray();

	private MineralMixBuilder()
	{
		super(MineralMix.SERIALIZER.get());
		addWriter(jsonObject -> jsonObject.add("ores", oresArray));
		addWriter(jsonObject -> jsonObject.add("dimensions", dimensionsArray));
	}

	public static MineralMixBuilder builder(RegistryKey<DimensionType> dimension)
	{
		return new MineralMixBuilder().addDimension(dimension);
	}

	public static MineralMixBuilder builder(ResourceLocation dimension)
	{
		return new MineralMixBuilder().addDimension(dimension);
	}

	public MineralMixBuilder setWeight(int weight)
	{
		return addWriter(jsonObject -> jsonObject.addProperty("weight", weight));
	}

	public MineralMixBuilder setFailchance(float failChance)
	{
		return addWriter(jsonObject -> jsonObject.addProperty("fail_chance", failChance));
	}

	public MineralMixBuilder setBackground(ResourceLocation resourceLocation)
	{
		return addWriter(jsonObject -> jsonObject.addProperty("sample_background", resourceLocation.toString()));
	}

	public MineralMixBuilder addDimension(RegistryKey<DimensionType> dimension)
	{
		return addDimension(dimension.func_240901_a_());
	}

	public MineralMixBuilder addDimension(ResourceLocation dimension)
	{
		this.dimensionsArray.add(new JsonPrimitive(dimension.toString()));
		return this;
	}

	public MineralMixBuilder addOre(IItemProvider itemProvider, float chance)
	{
		return this.addOre(new ItemStack(itemProvider), chance);
	}

	public MineralMixBuilder addOre(ItemStack itemStack, float chance)
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("chance", chance);
		jsonObject.add("output", serializeItemStack(itemStack));
		oresArray.add(jsonObject);
		return this;
	}

	public MineralMixBuilder addOre(ITag<Item> tag, float chance)
	{
		return addOre(new IngredientWithSize(tag), chance, null);
	}

	public MineralMixBuilder addOre(ITag<Item> tag, float chance, ICondition condition)
	{
		return addOre(new IngredientWithSize(tag), chance, condition);
	}

	public MineralMixBuilder addOre(IngredientWithSize ingredient, float chance, ICondition condition)
	{
		JsonObject jsonObject = new JsonObject();
		if(condition!=null)
		{
			JsonArray array = new JsonArray();
			array.add(CraftingHelper.serialize(condition));
			jsonObject.add("conditions", array);
		}

		jsonObject.addProperty("chance", chance);
		jsonObject.add("output", ingredient.serialize());
		oresArray.add(jsonObject);
		return this;
	}
}
