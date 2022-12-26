/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SawmillRecipeSerializer extends IERecipeSerializer<SawmillRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.SAWMILL.iconStack();
	}

	@Override
	public SawmillRecipe readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		Lazy<ItemStack> output = readOutput(json.get("result"));
		Ingredient input = Ingredient.fromJson(json.get("input"));
		Lazy<ItemStack> stripped = IESerializableRecipe.LAZY_EMPTY;
		if(json.has("stripped"))
			stripped = readOutput(json.get("stripped"));

		JsonArray array = json.getAsJsonArray("secondaries");
		int energy = GsonHelper.getAsInt(json, "energy");
		SawmillRecipe recipe = IEServerConfig.MACHINES.sawmillConfig.apply(new SawmillRecipe(recipeId, output, stripped, input, energy));
		for(int i = 0; i < array.size(); i++)
		{
			JsonObject element = array.get(i).getAsJsonObject();
			if(CraftingHelper.processConditions(element, "conditions", context))
			{
				boolean stripping = GsonHelper.getAsBoolean(element, "stripping");
				Lazy<ItemStack> stack = readOutput(element.get("output"));
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
		Lazy<ItemStack> output = readLazyStack(buffer);
		Lazy<ItemStack> stripped = readLazyStack(buffer);
		Ingredient input = Ingredient.fromNetwork(buffer);
		int energy = buffer.readInt();
		SawmillRecipe recipe = new SawmillRecipe(recipeId, output, stripped, input, energy);
		int secondaryCount = buffer.readInt();
		for(int i = 0; i < secondaryCount; i++)
			recipe.addToSecondaryStripping(readLazyStack(buffer));
		secondaryCount = buffer.readInt();
		for(int i = 0; i < secondaryCount; i++)
			recipe.addToSecondaryOutput(readLazyStack(buffer));
		return recipe;
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, SawmillRecipe recipe)
	{
		writeLazyStack(buffer, recipe.output);
		buffer.writeItem(recipe.stripped.get());
		recipe.input.toNetwork(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeInt(recipe.secondaryStripping.size());
		for(Lazy<ItemStack> secondaryOutput : recipe.secondaryStripping)
			buffer.writeItem(secondaryOutput.get());
		buffer.writeInt(recipe.secondaryOutputs.size());
		for(Lazy<ItemStack> secondaryOutput : recipe.secondaryOutputs)
			buffer.writeItem(secondaryOutput.get());
	}
}
