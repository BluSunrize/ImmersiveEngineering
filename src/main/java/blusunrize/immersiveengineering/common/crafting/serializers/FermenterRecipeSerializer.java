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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
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
			fluidOutput = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "fluid"));
		ItemStack itemOutput = ItemStack.EMPTY;
		if(json.has("result"))
			itemOutput = readOutput(json.get("result"));
		IngredientWithSize input = IngredientWithSize.deserialize(json.get("input"));
		int energy = GsonHelper.getAsInt(json, "energy");
		return IEServerConfig.MACHINES.fermenterConfig.apply(
				new FermenterRecipe(recipeId, fluidOutput, itemOutput, input, energy)
		);
	}

	@Nullable
	@Override
	public FermenterRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		FluidStack fluidOutput = buffer.readFluidStack();
		ItemStack itemOutput = buffer.readItem();
		IngredientWithSize input = IngredientWithSize.read(buffer);
		int energy = buffer.readInt();
		return new FermenterRecipe(recipeId, fluidOutput, itemOutput, input, energy);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, FermenterRecipe recipe)
	{
		buffer.writeFluidStack(recipe.fluidOutput);
		buffer.writeItem(recipe.itemOutput);
		recipe.input.write(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
