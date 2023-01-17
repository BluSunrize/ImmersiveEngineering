/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;

public class AlloyRecipeSerializer extends IERecipeSerializer<AlloyRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.ALLOY_SMELTER.iconStack();
	}

	@Override
	public AlloyRecipe readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		Lazy<ItemStack> output = readOutput(json.get("result"));
		IngredientWithSize input0 = IngredientWithSize.deserialize(json.get("input0"));
		IngredientWithSize input1 = IngredientWithSize.deserialize(json.get("input1"));
		int time = GsonHelper.getAsInt(json, "time", 200);
		return new AlloyRecipe(recipeId, output, input0, input1, time);
	}

	@Nullable
	@Override
	public AlloyRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		Lazy<ItemStack> output = readLazyStack(buffer);
		IngredientWithSize input0 = IngredientWithSize.read(buffer);
		IngredientWithSize input1 = IngredientWithSize.read(buffer);
		int time = buffer.readInt();
		return new AlloyRecipe(recipeId, output, input0, input1, time);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, AlloyRecipe recipe)
	{
		writeLazyStack(buffer, recipe.output);
		recipe.input0.write(buffer);
		recipe.input1.write(buffer);
		buffer.writeInt(recipe.time);
	}
}
