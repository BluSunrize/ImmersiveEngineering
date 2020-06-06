/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.common.crafting.TurnAndCopyRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

public class TurnAndCopyRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<TurnAndCopyRecipe>
{
	@Nonnull
	@Override
	public TurnAndCopyRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		ShapedRecipe basic = IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, json);
		TurnAndCopyRecipe recipe = new TurnAndCopyRecipe(recipeId, basic.getGroup(), basic.getWidth(), basic.getHeight(),
				basic.getIngredients(), basic.getRecipeOutput());
		if(JSONUtils.getBoolean(json, "quarter_turn", false))
			recipe.allowQuarterTurn();
		if(JSONUtils.getBoolean(json, "eighth_turn", false))
			recipe.allowEighthTurn();
		if(JSONUtils.hasField(json, "copy_nbt"))
		{
			if(JSONUtils.isJsonArray(json, "copy_nbt"))
			{
				JsonArray jArray = JSONUtils.getJsonArray(json, "copy_nbt");
				int[] array = new int[jArray.size()];
				for(int i = 0; i < array.length; i++)
					array[i] = jArray.get(i).getAsInt();
				recipe.setNBTCopyTargetRecipe(array);
			}
			else
				recipe.setNBTCopyTargetRecipe(JSONUtils.getInt(json, "copy_nbt"));
			if(JSONUtils.hasField(json, "copy_nbt_predicate"))
				recipe.setNBTCopyPredicate(JSONUtils.getString(json, "copy_nbt_predicate"));
		}
		return recipe;
	}

	@Nonnull
	@Override
	public TurnAndCopyRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
		ShapedRecipe basic = IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, buffer);
		TurnAndCopyRecipe recipe = new TurnAndCopyRecipe(recipeId, basic.getGroup(), basic.getWidth(), basic.getHeight(),
				basic.getIngredients(), basic.getRecipeOutput());
		if(buffer.readBoolean())
			recipe.allowQuarterTurn();
		if(buffer.readBoolean())
			recipe.allowEighthTurn();
		int[] array = buffer.readVarIntArray();
		if(array.length > 0)
		{
			recipe.setNBTCopyTargetRecipe(array);
			if(buffer.readBoolean())
				recipe.setNBTCopyPredicate(buffer.readString(512));

		}
		return recipe;
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull TurnAndCopyRecipe recipe)
	{
		IRecipeSerializer.CRAFTING_SHAPED.write(buffer, recipe);
		buffer.writeBoolean(recipe.isQuarterTurn());
		buffer.writeBoolean(recipe.isEightTurn());
		int[] copying = recipe.getCopyTargets();
		if(copying==null)
			copying = new int[0];
		buffer.writeVarIntArray(copying);
		if(copying.length > 0)
		{
			if(recipe.hasCopyPredicate())
			{
				buffer.writeBoolean(true);
				buffer.writeString(recipe.getBufferPredicate());
			}
			else
				buffer.writeBoolean(false);
		}
	}
}