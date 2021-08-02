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
		Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
		JsonArray array = json.getAsJsonArray("secondaries");
		int energy = GsonHelper.getAsInt(json, "energy");
		CrusherRecipe recipe = IEServerConfig.MACHINES.crusherConfig.apply(new CrusherRecipe(recipeId, output, input, energy));
		for(int i = 0; i < array.size(); i++)
		{
			JsonObject element = array.get(i).getAsJsonObject();
			if(CraftingHelper.processConditions(element, "conditions"))
			{
				float chance = GsonHelper.getAsFloat(element, "chance");
				ItemStack stack = readOutput(element.get("output"));
				recipe.addToSecondaryOutput(new StackWithChance(stack, chance));
			}
		}
		return recipe;
	}

	@Nullable
	@Override
	public CrusherRecipe fromNetwork(@Nonnull ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		ItemStack output = buffer.readItem();
		Ingredient input = Ingredient.fromNetwork(buffer);
		int energy = buffer.readInt();
		int secondaryCount = buffer.readInt();
		CrusherRecipe recipe = new CrusherRecipe(recipeId, output, input, energy);
		for(int i = 0; i < secondaryCount; i++)
			recipe.addToSecondaryOutput(StackWithChance.read(buffer));
		return recipe;
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, CrusherRecipe recipe)
	{
		buffer.writeItem(recipe.output);
		recipe.input.toNetwork(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeInt(recipe.secondaryOutputs.size());
		for(StackWithChance secondaryOutput : recipe.secondaryOutputs)
			secondaryOutput.write(buffer);
	}
}
