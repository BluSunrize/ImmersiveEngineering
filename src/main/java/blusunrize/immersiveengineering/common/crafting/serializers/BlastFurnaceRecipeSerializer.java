/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class BlastFurnaceRecipeSerializer extends IERecipeSerializer<BlastFurnaceRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.blastFurnace);
	}

	@Override
	public BlastFurnaceRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		ItemStack output = readOutput(json.get("result"));
		IngredientWithSize input = IngredientWithSize.deserialize(json.get("input"));
		int time = JSONUtils.getInt(json, "time", 200);
		ItemStack slag = ItemStack.EMPTY;
		if(json.has("slag"))
			slag = readOutput(JSONUtils.getJsonObject(json, "slag"));
		return new BlastFurnaceRecipe(recipeId, output, input, time, slag);
	}

	@Nullable
	@Override
	public BlastFurnaceRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		ItemStack output = buffer.readItemStack();
		IngredientWithSize input = IngredientWithSize.read(buffer);
		int time = buffer.readInt();
		ItemStack slag = ItemStack.EMPTY;
		if(buffer.readBoolean())
			slag = buffer.readItemStack();
		return new BlastFurnaceRecipe(recipeId, output, input, time, slag);
	}

	@Override
	public void write(PacketBuffer buffer, BlastFurnaceRecipe recipe)
	{
		buffer.writeItemStack(recipe.output);
		recipe.input.write(buffer);
		buffer.writeInt(recipe.time);
		buffer.writeBoolean(!recipe.slag.isEmpty());
		if(!recipe.slag.isEmpty())
			buffer.writeItemStack(recipe.slag);
	}
}
