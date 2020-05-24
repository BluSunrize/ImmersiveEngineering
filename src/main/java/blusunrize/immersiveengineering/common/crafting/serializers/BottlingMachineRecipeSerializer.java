/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

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
		Ingredient input = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input"));
		FluidStack fluidStack = ApiUtils.jsonDeserializeFluidStack(JSONUtils.getJsonObject(json, "fluid"));
		return new BottlingMachineRecipe(recipeId, output, input, fluidStack);
	}

	@Nullable
	@Override
	public BottlingMachineRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		ItemStack output = buffer.readItemStack();
		Ingredient input = Ingredient.read(buffer);
		FluidStack fluidStack = buffer.readFluidStack();
		return new BottlingMachineRecipe(recipeId, output, input, fluidStack);
	}

	@Override
	public void write(PacketBuffer buffer, BottlingMachineRecipe recipe)
	{
		buffer.writeItemStack(recipe.output);
		recipe.input.write(buffer);
		buffer.writeFluidStack(recipe.fluidInput);
	}
}
