/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.crafting.MixerRecipePotion;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MixerRecipeSerializer extends IERecipeSerializer<MixerRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.mixer);
	}

	@Override
	public MixerRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		FluidStack fluidOutput = ApiUtils.jsonDeserializeFluidStack(JSONUtils.getJsonObject(json, "result"));
		FluidTagInput fluidInput = FluidTagInput.deserialize(JSONUtils.getJsonObject(json, "fluid"));
		JsonArray inputs = json.getAsJsonArray("inputs");
		IngredientWithSize[] ingredients = new IngredientWithSize[inputs.size()];
		for(int i = 0; i < ingredients.length; i++)
			ingredients[i] = IngredientWithSize.deserialize(inputs.get(i));
		int energy = JSONUtils.getInt(json, "energy");
		return new MixerRecipe(recipeId, fluidOutput, fluidInput, ingredients, energy);
	}

	@Nullable
	@Override
	public MixerRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		FluidStack fluidOutput = buffer.readFluidStack();
		final boolean isPotionRecipe = buffer.readBoolean();
		int numInputs;
		if(isPotionRecipe)
			numInputs = buffer.readInt();
		else
			numInputs = 1;
		List<Pair<FluidTagInput, IngredientWithSize[]>> inputs = new ArrayList<>(numInputs);
		for(int i = 0; i < numInputs; ++i)
		{
			FluidTagInput fluidInput = FluidTagInput.read(buffer);
			int ingredientCount = buffer.readInt();
			IngredientWithSize[] itemInputs = new IngredientWithSize[ingredientCount];
			for(int j = 0; j < ingredientCount; j++)
				itemInputs[j] = IngredientWithSize.read(buffer);
			inputs.add(Pair.of(fluidInput, itemInputs));
		}
		Pair<FluidTagInput, IngredientWithSize[]> firstInput = inputs.get(0);
		if(isPotionRecipe)
		{
			MixerRecipePotion ret = new MixerRecipePotion(recipeId, fluidOutput, firstInput.getLeft(), firstInput.getRight());
			for(int i = 1; i < inputs.size(); ++i)
			{
				Pair<FluidTagInput, IngredientWithSize[]> input = inputs.get(i);
				Preconditions.checkState(input.getRight().length==1);
				ret.addAlternateInput(input.getLeft(), input.getRight()[0]);
			}
			return ret;
		}
		else
		{
			int energy = buffer.readInt();
			return new MixerRecipe(recipeId, fluidOutput, firstInput.getLeft(), firstInput.getRight(), energy);
		}
	}

	@Override
	public void write(PacketBuffer buffer, MixerRecipe recipe)
	{
		buffer.writeFluidStack(recipe.fluidOutput);
		buffer.writeBoolean(recipe instanceof MixerRecipePotion);
		Pair<FluidTagInput, IngredientWithSize[]> firstInput = Pair.of(recipe.fluidInput, recipe.itemInputs);
		List<Pair<FluidTagInput, IngredientWithSize[]>> inputs = new ArrayList<>();
		inputs.add(firstInput);
		if(recipe instanceof MixerRecipePotion)
		{
			inputs.addAll(((MixerRecipePotion)recipe).getAlternateInputs());
			buffer.writeInt(inputs.size());
		}
		for(Pair<FluidTagInput, IngredientWithSize[]> inputPair : inputs)
		{
			inputPair.getLeft().write(buffer);
			buffer.writeInt(inputPair.getRight().length);
			for(IngredientWithSize input : inputPair.getRight())
				input.write(buffer);
		}
		if(!(recipe instanceof MixerRecipePotion))
			buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
