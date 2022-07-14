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
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.network.PacketUtils;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BottlingMachineRecipeSerializer extends IERecipeSerializer<BottlingMachineRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.BOTTLING_MACHINE);
	}

	@Override
	public BottlingMachineRecipe readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		JsonArray results = json.getAsJsonArray("results");
		List<Lazy<ItemStack>> outputs = new ArrayList<>();
		for(int i = 0; i < results.size(); i++)
			outputs.add(readOutput(results.get(i)));

		Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
		FluidTagInput fluidInput = FluidTagInput.deserialize(GsonHelper.getAsJsonObject(json, "fluid"));
		return IEServerConfig.MACHINES.bottlingMachineConfig.apply(
				new BottlingMachineRecipe(recipeId, outputs, input, fluidInput)
		);
	}

	@Nullable
	@Override
	public BottlingMachineRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		List<Lazy<ItemStack>> outputs = PacketUtils.readList(buffer, IERecipeSerializer::readLazyStack);
		Ingredient input = Ingredient.fromNetwork(buffer);
		FluidTagInput fluidInput = FluidTagInput.read(buffer);
		return new BottlingMachineRecipe(recipeId, outputs, input, fluidInput);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, BottlingMachineRecipe recipe)
	{
		PacketUtils.writeListReverse(buffer, recipe.output.get(), FriendlyByteBuf::writeItem);
		recipe.input.toNetwork(buffer);
		recipe.fluidInput.write(buffer);
	}
}
