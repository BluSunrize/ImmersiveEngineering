/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class AlloyRecipeSerializer extends IERecipeSerializer<AlloyRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.alloySmelter);
	}

	@Override
	public AlloyRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		ItemStack output = readOutput(JSONUtils.getJsonObject(json, "result"));
		Ingredient input0 = Ingredient.deserialize(json.getAsJsonObject("input0"));
		Ingredient input1 = Ingredient.deserialize(json.getAsJsonObject("input1"));
		int time = JSONUtils.getInt(json, "time", 200);
		return new AlloyRecipe(recipeId, output, input0, input1, time);
	}

	@Nullable
	@Override
	public AlloyRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		ItemStack output = buffer.readItemStack();
		Ingredient input0 = Ingredient.read(buffer);
		Ingredient input1 = Ingredient.read(buffer);
		int time = buffer.readInt();
		return new AlloyRecipe(recipeId, output, input0, input1, time);
	}

	@Override
	public void write(PacketBuffer buffer, AlloyRecipe recipe)
	{
		buffer.writeItemStack(recipe.output);
		recipe.input0.write(buffer);
		recipe.input1.write(buffer);
		buffer.writeInt(recipe.time);
	}
}
