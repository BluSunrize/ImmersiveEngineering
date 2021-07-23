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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

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

		Ingredient seed = Ingredient.fromJson(json.get("input"));
		Ingredient soil = Ingredient.fromJson(json.get("soil"));
		int time = GsonHelper.getAsInt(json, "time");

		ClocheRenderReference renderReference = ClocheRenderReference.deserialize(GsonHelper.getAsJsonObject(json, "render"));

		return new ClocheRecipe(recipeId, outputs, seed, soil, time, renderReference);
	}

	@Nullable
	@Override
	public ClocheRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		int outputCount = buffer.readInt();
		List<ItemStack> outputs = new ArrayList<>(outputCount);
		for(int i = 0; i < outputCount; i++)
			outputs.add(buffer.readItem());
		Ingredient seed = Ingredient.fromNetwork(buffer);
		Ingredient soil = Ingredient.fromNetwork(buffer);
		int time = buffer.readInt();
		ClocheRenderReference renderReference = ClocheRenderReference.read(buffer);
		return new ClocheRecipe(recipeId, outputs, seed, soil, time, renderReference);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, ClocheRecipe recipe)
	{
		buffer.writeInt(recipe.outputs.size());
		for(ItemStack stack : recipe.outputs)
			buffer.writeItem(stack);
		recipe.seed.toNetwork(buffer);
		recipe.soil.toNetwork(buffer);
		buffer.writeInt(recipe.time);
		recipe.renderReference.write(buffer);
	}
}
