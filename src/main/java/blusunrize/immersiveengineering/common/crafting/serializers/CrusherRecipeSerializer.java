/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe.SecondaryOutput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class CrusherRecipeSerializer extends IERecipeSerializer<CrusherRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.crusher);
	}

	@Override
	public CrusherRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		ItemStack output = readOutput(json.get("result"));
		Ingredient input = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input"));
		JsonArray array = json.getAsJsonArray("secondaries");
		SecondaryOutput[] secondaries = new SecondaryOutput[array.size()];
		for(int i = 0; i < array.size(); i++)
		{
			JsonObject element = array.get(i).getAsJsonObject();
			float chance = JSONUtils.getFloat(element, "chance");
			ItemStack stack = readOutput(element.get("output"));
			secondaries[i] = new SecondaryOutput(stack, chance);
		}
		int energy = JSONUtils.getInt(json, "energy");
		return new CrusherRecipe(recipeId, output, input, energy).addToSecondaryOutput(secondaries);
	}

	@Nullable
	@Override
	public CrusherRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		ItemStack output = buffer.readItemStack();
		Ingredient input = Ingredient.read(buffer);
		int secondaryCount = buffer.readInt();
		SecondaryOutput[] secondaries = new SecondaryOutput[secondaryCount];
		for(int i = 0; i < secondaryCount; i++)
			secondaries[i] = new SecondaryOutput(buffer.readItemStack(), buffer.readFloat());
		int energy = buffer.readInt();
		return new CrusherRecipe(recipeId, output, input, energy).addToSecondaryOutput(secondaries);
	}

	@Override
	public void write(PacketBuffer buffer, CrusherRecipe recipe)
	{
		buffer.writeItemStack(recipe.output);
		recipe.input.write(buffer);
		buffer.writeInt(recipe.secondaryOutputs.size());
		for(SecondaryOutput secondaryOutput : recipe.secondaryOutputs)
		{
			buffer.writeItemStack(secondaryOutput.stack);
			buffer.writeFloat(secondaryOutput.chance);
		}
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
