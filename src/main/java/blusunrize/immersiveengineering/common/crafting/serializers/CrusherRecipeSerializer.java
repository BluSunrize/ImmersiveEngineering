/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.utils.IEPacketBuffer;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nonnull;
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
		int energy = JSONUtils.getInt(json, "energy");
		CrusherRecipe recipe = IEConfig.MACHINES.crusherConfig.apply(new CrusherRecipe(recipeId, output, input, energy));
		for(int i = 0; i < array.size(); i++)
		{
			JsonObject element = array.get(i).getAsJsonObject();
			if(CraftingHelper.processConditions(element, "conditions"))
			{
				float chance = JSONUtils.getFloat(element, "chance");
				ItemStack stack = readOutput(element.get("output"));
				recipe.addToSecondaryOutput(new StackWithChance(stack, chance));
			}
		}
		return recipe;
	}

	@Nullable
	@Override
	public CrusherRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull IEPacketBuffer buffer)
	{
		ItemStack output = buffer.readItemStack();
		Ingredient input = Ingredient.read(buffer);
		int energy = buffer.readVarInt();
		int secondaryCount = buffer.readVarInt();
		CrusherRecipe recipe = new CrusherRecipe(recipeId, output, input, energy);
		for(int i = 0; i < secondaryCount; i++)
			recipe.addToSecondaryOutput(StackWithChance.read(buffer));
		return recipe;
	}

	@Override
	public void write(@Nonnull IEPacketBuffer buffer, CrusherRecipe recipe)
	{
		buffer.writeItemStack(recipe.output);
		recipe.input.write(buffer);
		buffer.writeVarInt(recipe.getTotalProcessEnergy());
		buffer.writeVarInt(recipe.secondaryOutputs.size());
		for(StackWithChance secondaryOutput : recipe.secondaryOutputs)
			secondaryOutput.write(buffer);
	}
}
