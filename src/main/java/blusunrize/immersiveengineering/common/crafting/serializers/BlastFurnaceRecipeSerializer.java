/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
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

public class BlastFurnaceRecipeSerializer extends IERecipeSerializer<BlastFurnaceRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.BLAST_FURNACE.iconStack();
	}

	@Override
	public BlastFurnaceRecipe readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		Lazy<ItemStack> output = readOutput(json.get("result"));
		IngredientWithSize input = IngredientWithSize.deserialize(json.get("input"));
		int time = GsonHelper.getAsInt(json, "time", 200);
		Lazy<ItemStack> slag = IESerializableRecipe.LAZY_EMPTY;
		if(json.has("slag"))
			slag = readOutput(GsonHelper.getAsJsonObject(json, "slag"));
		return new BlastFurnaceRecipe(recipeId, output, input, time, slag);
	}

	@Nullable
	@Override
	public BlastFurnaceRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		Lazy<ItemStack> output = readLazyStack(buffer);
		IngredientWithSize input = IngredientWithSize.read(buffer);
		int time = buffer.readInt();
		Lazy<ItemStack> slag = IESerializableRecipe.LAZY_EMPTY;
		if(buffer.readBoolean())
			slag = readLazyStack(buffer);
		return new BlastFurnaceRecipe(recipeId, output, input, time, slag);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, BlastFurnaceRecipe recipe)
	{
		writeLazyStack(buffer, recipe.output);
		recipe.input.write(buffer);
		buffer.writeInt(recipe.time);
		buffer.writeBoolean(!recipe.slag.get().isEmpty());
		if(!recipe.slag.get().isEmpty())
			buffer.writeItem(recipe.slag.get());
	}
}
