/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class BlueprintCraftingRecipeSerializer extends IERecipeSerializer<BlueprintCraftingRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(WoodenDevices.workbench);
	}

	@Override
	public BlueprintCraftingRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		String category = JSONUtils.getString(json, "category");
		ItemStack output = readOutput(json.get("result"));
		JsonArray inputs = json.getAsJsonArray("inputs");
		IngredientWithSize[] ingredients = new IngredientWithSize[inputs.size()];
		for(int i = 0; i < ingredients.length; i++)
			ingredients[i] = IngredientWithSize.deserialize(inputs.get(i));
		return IEConfig.MACHINES.autoWorkbenchConfig.apply(
				new BlueprintCraftingRecipe(recipeId, category, output, ingredients)
		);
	}

	@Nullable
	@Override
	public BlueprintCraftingRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		String category = buffer.readString();
		ItemStack output = buffer.readItemStack();
		int inputCount = buffer.readInt();
		IngredientWithSize[] ingredients = new IngredientWithSize[inputCount];
		for(int i = 0; i < ingredients.length; i++)
			ingredients[i] = IngredientWithSize.read(buffer);
		return new BlueprintCraftingRecipe(recipeId, category, output, ingredients);
	}

	@Override
	public void write(PacketBuffer buffer, BlueprintCraftingRecipe recipe)
	{
		buffer.writeString(recipe.blueprintCategory);
		buffer.writeItemStack(recipe.output);
		buffer.writeInt(recipe.inputs.length);
		for(IngredientWithSize ingredient : recipe.inputs)
			ingredient.write(buffer);
	}
}
