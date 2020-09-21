/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SawmillRecipeSerializer extends IERecipeSerializer<SawmillRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.sawmill);
	}

	@Override
	public SawmillRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		ItemStack output = readOutput(json.get("result"));
		Ingredient input = Ingredient.deserialize(json.get("input"));
		ItemStack stripped = ItemStack.EMPTY;
		if(json.has("stripped"))
			stripped = readOutput(json.get("stripped"));

		JsonArray array = json.getAsJsonArray("secondaries");
		int energy = JSONUtils.getInt(json, "energy");
		//todo: apply machine config
		SawmillRecipe recipe = new SawmillRecipe(recipeId, output, stripped, input, energy);
		for(int i = 0; i < array.size(); i++)
		{
			JsonObject element = array.get(i).getAsJsonObject();
			if(CraftingHelper.processConditions(element, "conditions"))
			{
				boolean stripping = JSONUtils.getBoolean(element, "stripping");
				ItemStack stack = readOutput(element.get("output"));
				if(stripping)
					recipe.addToSecondaryStripping(stack);
				else
					recipe.addToSecondaryOutput(stack);
			}
		}
		return recipe;
	}

	@Nullable
	@Override
	public SawmillRecipe read(@Nonnull ResourceLocation recipeId, PacketBuffer buffer)
	{
		ItemStack output = buffer.readItemStack();
		ItemStack stripped = buffer.readItemStack();
		Ingredient input = Ingredient.read(buffer);
		int energy = buffer.readInt();
		SawmillRecipe recipe = new SawmillRecipe(recipeId, output, stripped, input, energy);
		int secondaryCount = buffer.readInt();
		for(int i = 0; i < secondaryCount; i++)
			recipe.addToSecondaryStripping(buffer.readItemStack());
		secondaryCount = buffer.readInt();
		for(int i = 0; i < secondaryCount; i++)
			recipe.addToSecondaryOutput(buffer.readItemStack());
		return recipe;
	}

	@Override
	public void write(PacketBuffer buffer, SawmillRecipe recipe)
	{
		buffer.writeItemStack(recipe.output);
		buffer.writeItemStack(recipe.stripped);
		recipe.input.write(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeInt(recipe.secondaryStripping.size());
		for(ItemStack secondaryOutput : recipe.secondaryStripping)
			buffer.writeItemStack(secondaryOutput);
		buffer.writeInt(recipe.secondaryOutputs.size());
		for(ItemStack secondaryOutput : recipe.secondaryOutputs)
			buffer.writeItemStack(secondaryOutput);
	}
}
