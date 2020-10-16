/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction.ClocheRenderReference;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClocheRecipeSerializer extends IERecipeSerializer<ClocheRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(MetalDevices.cloche);
	}

	@Override
	public ClocheRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		JsonArray results = json.getAsJsonArray("results");

		List<ItemStack> outputs = new ArrayList<>(results.size());
		for(int i = 0; i < results.size(); i++)
			outputs.add(readOutput(results.get(i)));

		Ingredient seed = Ingredient.deserialize(json.get("input"));
		Ingredient soil = Ingredient.deserialize(json.get("soil"));
		int time = JSONUtils.getInt(json, "time");

		ClocheRenderReference renderReference = ClocheRenderReference.deserialize(JSONUtils.getJsonObject(json, "render"));

		return new ClocheRecipe(recipeId, outputs, seed, soil, time, renderReference);
	}

	@Nullable
	@Override
	public ClocheRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		int outputCount = buffer.readVarInt();
		List<ItemStack> outputs = new ArrayList<>(outputCount);
		for(int i = 0; i < outputCount; i++)
			outputs.add(buffer.readItemStack());
		Ingredient seed = Ingredient.read(buffer);
		Ingredient soil = Ingredient.read(buffer);
		int time = buffer.readVarInt();
		ClocheRenderReference renderReference = ClocheRenderReference.read(buffer);
		return new ClocheRecipe(recipeId, outputs, seed, soil, time, renderReference);
	}

	@Override
	public void write(PacketBuffer buffer, ClocheRecipe recipe)
	{
		buffer.writeVarInt(recipe.outputs.size());
		for(ItemStack stack : recipe.outputs)
			buffer.writeItemStack(stack);
		recipe.seed.write(buffer);
		recipe.soil.write(buffer);
		buffer.writeVarInt(recipe.time);
		recipe.renderReference.write(buffer);
	}
}
