/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;

public class BottlingMachineRecipeSerializer extends IERecipeSerializer<BottlingMachineRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.bottlingMachine);
	}

	@Override
	public BottlingMachineRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		ItemStack output = readOutput(json.get("result"));
		Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
		FluidTagInput fluidInput = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "fluid"));
		return IEServerConfig.MACHINES.bottlingMachineConfig.apply(
				new BottlingMachineRecipe(recipeId, output, input, fluidInput)
		);
	}

	@Nullable
	@Override
	public BottlingMachineRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		ItemStack output = buffer.readItem();
		Ingredient input = Ingredient.fromNetwork(buffer);
		FluidTagInput fluidInput = FluidTagInput.read(buffer);
		return new BottlingMachineRecipe(recipeId, output, input, fluidInput);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, BottlingMachineRecipe recipe)
	{
		buffer.writeItem(recipe.output);
		recipe.input.toNetwork(buffer);
		recipe.fluidInput.write(buffer);
	}
}
