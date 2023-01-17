/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class MetalPressRecipeSerializer extends IERecipeSerializer<MetalPressRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.METAL_PRESS.iconStack();
	}

	@Override
	public MetalPressRecipe readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		Lazy<ItemStack> output = readOutput(json.get("result"));
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
		Lazy<ItemStack> output = readLazyStack(buffer);
		IngredientWithSize input = IngredientWithSize.read(buffer);
		Item mold = buffer.readRegistryIdSafe(Item.class);
		int energy = buffer.readInt();
		return new MetalPressRecipe(recipeId, output, input, mold, energy);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, MetalPressRecipe recipe)
	{
		writeLazyStack(buffer, recipe.output);
		recipe.input.write(buffer);
		buffer.writeRegistryId(ForgeRegistries.ITEMS, recipe.mold);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
