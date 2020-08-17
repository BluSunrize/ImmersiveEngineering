/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.FluidTagWithSize;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class RefineryRecipeSerializer extends IERecipeSerializer<RefineryRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.refinery);
	}

	@Override
	public RefineryRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		FluidStack output = ApiUtils.jsonDeserializeFluidStack(JSONUtils.getJsonObject(json, "result"));
		FluidTagWithSize input0 = FluidTagWithSize.deserialize(JSONUtils.getJsonObject(json, "input0"));
		FluidTagWithSize input1 = FluidTagWithSize.deserialize(JSONUtils.getJsonObject(json, "input1"));
		int energy = JSONUtils.getInt(json, "energy");
		return new RefineryRecipe(recipeId, output, input0, input1, energy);
	}

	@Nullable
	@Override
	public RefineryRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		FluidStack output = buffer.readFluidStack();
		FluidTagWithSize input0 = FluidTagWithSize.read(buffer);
		FluidTagWithSize input1 = FluidTagWithSize.read(buffer);
		int energy = buffer.readInt();
		return new RefineryRecipe(recipeId, output, input0, input1, energy);
	}

	@Override
	public void write(PacketBuffer buffer, RefineryRecipe recipe)
	{
		buffer.writeFluidStack(recipe.output);
		recipe.input0.write(buffer);
		recipe.input1.write(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
