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
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.dimension.DimensionType;
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

	public static MineralMixBuilder builder(ResourceKey<DimensionType> dimension)
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

	public MineralMixBuilder addDimension(ResourceKey<DimensionType> dimension)
	{
		return addDimension(dimension.location());
	}

	public MineralMixBuilder addDimension(ResourceLocation dimension)
	{
		this.dimensionsArray.add(new JsonPrimitive(dimension.toString()));
		return this;
	}

	public MineralMixBuilder addOre(ItemLike itemProvider, float chance)
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

	public MineralMixBuilder addOre(Tag<Item> tag, float chance)
	{
		return addOre(new IngredientWithSize(tag), chance, null);
	}

	public MineralMixBuilder addOre(Tag<Item> tag, float chance, ICondition condition)
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
