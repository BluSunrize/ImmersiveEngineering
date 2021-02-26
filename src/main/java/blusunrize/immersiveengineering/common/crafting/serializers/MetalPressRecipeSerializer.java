/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class MetalPressRecipeSerializer extends IERecipeSerializer<MetalPressRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.metalPress);
	}

	@Override
	public MetalPressRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		ItemStack output = readOutput(json.get("result"));
		IngredientWithSize input = IngredientWithSize.deserialize(json.get("input"));
		ItemStack mold = readOutput(json.get("mold"));
		int energy = JSONUtils.getInt(json, "energy");
		return IEServerConfig.MACHINES.metalPressConfig.apply(
				new MetalPressRecipe(recipeId, output, input, new ComparableItemStack(mold), energy)
		);
	}

	@Nullable
	@Override
	public MetalPressRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		ItemStack output = buffer.readItemStack();
		IngredientWithSize input = IngredientWithSize.read(buffer);
		ItemStack mold = buffer.readItemStack();
		int energy = buffer.readInt();
		return new MetalPressRecipe(recipeId, output, input, new ComparableItemStack(mold), energy);
	}

	@Override
	public void write(PacketBuffer buffer, MetalPressRecipe recipe)
	{
		buffer.writeItemStack(recipe.output);
		recipe.input.write(buffer);
		buffer.writeItemStack(recipe.mold.stack);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
