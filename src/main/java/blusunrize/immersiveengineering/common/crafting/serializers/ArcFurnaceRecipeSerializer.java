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
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingRecipe;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

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

		int time = GsonHelper.getAsInt(json, "time");
		int energy = GsonHelper.getAsInt(json, "energy");
		return IEServerConfig.MACHINES.arcFurnaceConfig.apply(
				new ArcFurnaceRecipe(recipeId, outputs, input, slag, time, energy, ingredients)
		);
	}

	@Nullable
	@Override
	public ArcFurnaceRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		int outputCount = buffer.readInt();
		NonNullList<ItemStack> outputs = NonNullList.withSize(outputCount, ItemStack.EMPTY);
		for(int i = 0; i < outputCount; i++)
			outputs.set(i, buffer.readItem());
		IngredientWithSize input = IngredientWithSize.read(buffer);
		int additiveCount = buffer.readInt();
		IngredientWithSize[] additives = new IngredientWithSize[additiveCount];
		for(int i = 0; i < additiveCount; i++)
			additives[i] = IngredientWithSize.read(buffer);
		ItemStack slag = buffer.readItem();
		int time = buffer.readInt();
		int energy = buffer.readInt();
		if(!buffer.readBoolean())
			return new ArcFurnaceRecipe(recipeId, outputs, input, slag, time, energy, additives);
		else
		{
			final int numOutputs = buffer.readVarInt();
			Map<ItemStack, Double> recyclingOutputs = new HashMap<>(numOutputs);
			for(int i = 0; i < numOutputs; ++i)
				recyclingOutputs.put(buffer.readItem(), buffer.readDouble());
			return new ArcRecyclingRecipe(
					recipeId, () -> Minecraft.getInstance().getConnection().getTags(), recyclingOutputs, input, time, energy
			);
		}
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, ArcFurnaceRecipe recipe)
	{
		buffer.writeInt(recipe.output.size());
		for(ItemStack stack : recipe.output)
			buffer.writeItem(stack);
		recipe.input.write(buffer);
		buffer.writeInt(recipe.additives.length);
		for(IngredientWithSize ingr : recipe.additives)
			ingr.write(buffer);
		buffer.writeItem(recipe.slag);
		buffer.writeInt(recipe.getTotalProcessTime());
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeBoolean(recipe instanceof ArcRecyclingRecipe);
		if(recipe instanceof ArcRecyclingRecipe)
		{
			Map<ItemStack, Double> outputs = ((ArcRecyclingRecipe)recipe).getOutputs();
			buffer.writeVarInt(outputs.size());
			for(Entry<ItemStack, Double> e : outputs.entrySet())
			{
				buffer.writeItem(e.getKey());
				buffer.writeDouble(e.getValue());
			}
		}
	}
}
