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
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class RefineryRecipeSerializer extends IERecipeSerializer<RefineryRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.REFINERY.iconStack();
	}

	@Override
	public RefineryRecipe readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		FluidStack output = ApiUtils.jsonDeserializeFluidStack(GsonHelper.getAsJsonObject(json, "result"));
		FluidTagInput input0 = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input0"));
		FluidTagInput input1 = json.has("input1")?FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "input1")): null;
		Ingredient catalyst = Ingredient.EMPTY;
		if(json.has("catalyst"))
			catalyst = Ingredient.fromJson(json.get("catalyst"));
		int energy = GsonHelper.getAsInt(json, "energy");
		RefineryRecipe recipe = new RefineryRecipe(recipeId, output, input0, input1, catalyst, energy);
		recipe.modifyTimeAndEnergy(() -> 1, IEServerConfig.MACHINES.refineryConfig::get);
		return recipe;
	}

	@Nullable
	@Override
	public RefineryRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		FluidStack output = buffer.readFluidStack();
		FluidTagInput input0 = FluidTagInput.read(buffer);
		FluidTagInput input1 = buffer.readBoolean()?FluidTagInput.read(buffer): null;
		Ingredient catalyst = Ingredient.fromNetwork(buffer);
		int energy = buffer.readInt();
		return new RefineryRecipe(recipeId, output, input0, input1, catalyst, energy);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, RefineryRecipe recipe)
	{
		buffer.writeFluidStack(recipe.output);
		recipe.input0.write(buffer);
		if(recipe.input1!=null)
		{
			buffer.writeBoolean(true);
			recipe.input1.write(buffer);
		}
		else
			buffer.writeBoolean(false);
		recipe.catalyst.toNetwork(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
