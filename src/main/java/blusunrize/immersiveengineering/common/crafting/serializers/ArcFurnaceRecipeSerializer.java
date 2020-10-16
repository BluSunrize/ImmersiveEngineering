/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ArcFurnaceRecipeSerializer extends IERecipeSerializer<ArcFurnaceRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.arcFurnace);
	}

	@Override
	public ArcFurnaceRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		JsonArray results = json.getAsJsonArray("results");
		NonNullList<ItemStack> outputs = NonNullList.withSize(results.size(), ItemStack.EMPTY);
		for(int i = 0; i < results.size(); i++)
			outputs.set(i, readOutput(results.get(i)));

		IngredientWithSize input = IngredientWithSize.deserialize(json.get("input"));

		JsonArray additives = json.getAsJsonArray("additives");
		IngredientWithSize[] ingredients = new IngredientWithSize[additives.size()];
		for(int i = 0; i < additives.size(); i++)
			ingredients[i] = IngredientWithSize.deserialize(additives.get(i));

		ItemStack slag = ItemStack.EMPTY;
		if(json.has("slag"))
			slag = readOutput(json.get("slag"));

		int time = JSONUtils.getInt(json, "time");
		int energy = JSONUtils.getInt(json, "energy");
		return IEConfig.MACHINES.arcFurnaceConfig.apply(
				new ArcFurnaceRecipe(recipeId, outputs, input, slag, time, energy, ingredients)
		);
	}

	@Nullable
	@Override
	public ArcFurnaceRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		int outputCount = buffer.readVarInt();
		NonNullList<ItemStack> outputs = NonNullList.withSize(outputCount, ItemStack.EMPTY);
		for(int i = 0; i < outputCount; i++)
			outputs.set(i, buffer.readItemStack());
		IngredientWithSize input = IngredientWithSize.read(buffer);
		int additiveCount = buffer.readVarInt();
		IngredientWithSize[] additives = new IngredientWithSize[additiveCount];
		for(int i = 0; i < additiveCount; i++)
			additives[i] = IngredientWithSize.read(buffer);
		ItemStack slag = buffer.readItemStack();
		int time = buffer.readVarInt();
		int energy = buffer.readVarInt();
		if(!buffer.readBoolean())
			return new ArcFurnaceRecipe(recipeId, outputs, input, slag, time, energy, additives);
		else
		{
			final int numOutputs = buffer.readVarInt();
			Map<ItemStack, Double> recyclingOutputs = new HashMap<>(numOutputs);
			for(int i = 0; i < numOutputs; ++i)
				recyclingOutputs.put(buffer.readItemStack(), buffer.readVarInt()/100D);
			return new ArcRecyclingRecipe(recipeId, recyclingOutputs, input, time, energy);
		}
	}

	@Override
	public void write(PacketBuffer buffer, ArcFurnaceRecipe recipe)
	{
		buffer.writeVarInt(recipe.output.size());
		for(ItemStack stack : recipe.output)
			buffer.writeItemStack(stack);
		recipe.input.write(buffer);
		buffer.writeVarInt(recipe.additives.length);
		for(IngredientWithSize ingr : recipe.additives)
			ingr.write(buffer);
		buffer.writeItemStack(recipe.slag);
		buffer.writeVarInt(recipe.getTotalProcessTime());
		buffer.writeVarInt(recipe.getTotalProcessEnergy());
		buffer.writeBoolean(recipe instanceof ArcRecyclingRecipe);
		if(recipe instanceof ArcRecyclingRecipe)
		{
			Map<ItemStack, Double> outputs = ((ArcRecyclingRecipe)recipe).getOutputs();
			buffer.writeVarInt(outputs.size());
			for(Entry<ItemStack, Double> e : outputs.entrySet())
			{
				buffer.writeItemStack(e.getKey());
				buffer.writeVarInt((int)(100*e.getValue()));
			}
		}
	}
}
