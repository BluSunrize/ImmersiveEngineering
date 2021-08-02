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
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
		Ingredient input = Ingredient.fromJson(json.get("input"));
		ItemStack stripped = ItemStack.EMPTY;
		if(json.has("stripped"))
			stripped = readOutput(json.get("stripped"));

		JsonArray array = json.getAsJsonArray("secondaries");
		int energy = GsonHelper.getAsInt(json, "energy");
		SawmillRecipe recipe = IEServerConfig.MACHINES.sawmillConfig.apply(new SawmillRecipe(recipeId, output, stripped, input, energy));
		for(int i = 0; i < array.size(); i++)
		{
			JsonObject element = array.get(i).getAsJsonObject();
			if(CraftingHelper.processConditions(element, "conditions"))
			{
				boolean stripping = GsonHelper.getAsBoolean(element, "stripping");
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
	public SawmillRecipe fromNetwork(@Nonnull ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		ItemStack output = buffer.readItem();
		ItemStack stripped = buffer.readItem();
		Ingredient input = Ingredient.fromNetwork(buffer);
		int energy = buffer.readInt();
		SawmillRecipe recipe = new SawmillRecipe(recipeId, output, stripped, input, energy);
		int secondaryCount = buffer.readInt();
		for(int i = 0; i < secondaryCount; i++)
			recipe.addToSecondaryStripping(buffer.readItem());
		secondaryCount = buffer.readInt();
		for(int i = 0; i < secondaryCount; i++)
			recipe.addToSecondaryOutput(buffer.readItem());
		return recipe;
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, SawmillRecipe recipe)
	{
		buffer.writeItem(recipe.output);
		buffer.writeItem(recipe.stripped);
		recipe.input.toNetwork(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeInt(recipe.secondaryStripping.size());
		for(ItemStack secondaryOutput : recipe.secondaryStripping)
			buffer.writeItem(secondaryOutput);
		buffer.writeInt(recipe.secondaryOutputs.size());
		for(ItemStack secondaryOutput : recipe.secondaryOutputs)
			buffer.writeItem(secondaryOutput);
	}
}
