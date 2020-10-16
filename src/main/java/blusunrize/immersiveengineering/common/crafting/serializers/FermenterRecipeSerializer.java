/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class FermenterRecipeSerializer extends IERecipeSerializer<FermenterRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.fermenter);
	}

	@Override
	public FermenterRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		FluidStack fluidOutput = FluidStack.EMPTY;
		if(json.has("fluid"))
			fluidOutput = ApiUtils.jsonDeserializeFluidStack(JSONUtils.getJsonObject(json, "fluid"));
		ItemStack itemOutput = ItemStack.EMPTY;
		if(json.has("result"))
			itemOutput = readOutput(json.get("result"));
		IngredientWithSize input = IngredientWithSize.deserialize(json.get("input"));
		int energy = JSONUtils.getInt(json, "energy");
		return IEConfig.MACHINES.fermenterConfig.apply(
				new FermenterRecipe(recipeId, fluidOutput, itemOutput, input, energy)
		);
	}

	@Nullable
	@Override
	public FermenterRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		FluidStack fluidOutput = buffer.readFluidStack();
		ItemStack itemOutput = buffer.readItemStack();
		IngredientWithSize input = IngredientWithSize.read(buffer);
		int energy = buffer.readVarInt();
		return new FermenterRecipe(recipeId, fluidOutput, itemOutput, input, energy);
	}

	@Override
	public void write(PacketBuffer buffer, FermenterRecipe recipe)
	{
		buffer.writeFluidStack(recipe.fluidOutput);
		buffer.writeItemStack(recipe.itemOutput);
		recipe.input.write(buffer);
		buffer.writeVarInt(recipe.getTotalProcessEnergy());
	}
}
