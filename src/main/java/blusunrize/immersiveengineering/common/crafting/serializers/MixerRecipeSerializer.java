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
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class MixerRecipeSerializer extends IERecipeSerializer<MixerRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.MIXER.iconStack();
	}

	@Override
	public MixerRecipe readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		FluidStack fluidOutput = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result"));
		FluidTagInput fluidInput = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "fluid"));
		JsonArray inputs = json.getAsJsonArray("inputs");
		IngredientWithSize[] ingredients = new IngredientWithSize[inputs.size()];
		for(int i = 0; i < ingredients.length; i++)
			ingredients[i] = IngredientWithSize.deserialize(inputs.get(i));
		int energy = GsonHelper.getAsInt(json, "energy");
		return IEServerConfig.MACHINES.mixerConfig.apply(
				new MixerRecipe(recipeId, fluidOutput, fluidInput, ingredients, energy)
		);
	}

	@Nullable
	@Override
	public MixerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		FluidStack fluidOutput = buffer.readFluidStack();
		FluidTagInput fluidInput = FluidTagInput.read(buffer);
		int ingredientCount = buffer.readInt();
		IngredientWithSize[] itemInputs = new IngredientWithSize[ingredientCount];
		for(int i = 0; i < ingredientCount; i++)
			itemInputs[i] = IngredientWithSize.read(buffer);
		int energy = buffer.readInt();
		return new MixerRecipe(recipeId, fluidOutput, fluidInput, itemInputs, energy);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, MixerRecipe recipe)
	{
		buffer.writeFluidStack(recipe.fluidOutput);
		recipe.fluidInput.write(buffer);
		buffer.writeInt(recipe.itemInputs.length);
		for(IngredientWithSize input : recipe.itemInputs)
			input.write(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
