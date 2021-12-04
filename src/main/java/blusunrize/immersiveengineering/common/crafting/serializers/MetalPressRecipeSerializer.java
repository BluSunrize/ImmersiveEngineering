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
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
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
		return new ItemStack(Multiblocks.METAL_PRESS);
	}

	@Override
	public MetalPressRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		ItemStack output = readOutput(json.get("result"));
		IngredientWithSize input = IngredientWithSize.deserialize(json.get("input"));
		Item mold = ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "mold")));
		int energy = GsonHelper.getAsInt(json, "energy");
		return IEServerConfig.MACHINES.metalPressConfig.apply(
				new MetalPressRecipe(recipeId, output, input, mold, energy)
		);
	}

	@Nullable
	@Override
	public MetalPressRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		ItemStack output = buffer.readItem();
		IngredientWithSize input = IngredientWithSize.read(buffer);
		Item mold = buffer.readRegistryIdSafe(Item.class);
		int energy = buffer.readInt();
		return new MetalPressRecipe(recipeId, output, input, mold, energy);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, MetalPressRecipe recipe)
	{
		buffer.writeItem(recipe.output);
		recipe.input.write(buffer);
		buffer.writeRegistryId(recipe.mold);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
