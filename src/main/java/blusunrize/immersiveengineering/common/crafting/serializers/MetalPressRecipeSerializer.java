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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

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
		int energy = GsonHelper.getAsInt(json, "energy");
		return IEServerConfig.MACHINES.metalPressConfig.apply(
				new MetalPressRecipe(recipeId, output, input, new ComparableItemStack(mold), energy)
		);
	}

	@Nullable
	@Override
	public MetalPressRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		ItemStack output = buffer.readItem();
		IngredientWithSize input = IngredientWithSize.read(buffer);
		ItemStack mold = buffer.readItem();
		int energy = buffer.readInt();
		return new MetalPressRecipe(recipeId, output, input, new ComparableItemStack(mold), energy);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, MetalPressRecipe recipe)
	{
		buffer.writeItem(recipe.output);
		recipe.input.write(buffer);
		buffer.writeItem(recipe.mold.stack);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
