/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.RecipeSerializers;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class RecipeSerializerTurnAndCopy implements IRecipeSerializer<RecipeTurnAndCopy>
{
	public static final IRecipeSerializer<RecipeTurnAndCopy> INSTANCE = RecipeSerializers.register(new RecipeSerializerTurnAndCopy());

	@Nonnull
	@Override
	public RecipeTurnAndCopy read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		ShapedRecipe basic = RecipeSerializers.CRAFTING_SHAPED.read(recipeId, json);
		RecipeTurnAndCopy recipe = new RecipeTurnAndCopy(recipeId, basic.getGroup(), basic.getWidth(), basic.getHeight(),
				basic.getIngredients(), basic.getRecipeOutput());
		if(JsonUtils.getBoolean(json, "quarter_turn", false))
			recipe.allowQuarterTurn();
		if(JsonUtils.getBoolean(json, "eighth_turn", false))
			recipe.allowEighthTurn();
		if(JsonUtils.hasField(json, "copy_nbt"))
		{
			if(JsonUtils.isJsonArray(json, "copy_nbt"))
			{
				JsonArray jArray = JsonUtils.getJsonArray(json, "copy_nbt");
				int[] array = new int[jArray.size()];
				for(int i = 0; i < array.length; i++)
					array[i] = jArray.get(i).getAsInt();
				recipe.setNBTCopyTargetRecipe(array);
			}
			else
				recipe.setNBTCopyTargetRecipe(JsonUtils.getInt(json, "copy_nbt"));
			if(JsonUtils.hasField(json, "copy_nbt_predicate"))
				recipe.setNBTCopyPredicate(JsonUtils.getString(json, "copy_nbt_predicate"));
		}
		return recipe;
	}

	@Nonnull
	@Override
	public RecipeTurnAndCopy read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
		ShapedRecipe basic = RecipeSerializers.CRAFTING_SHAPED.read(recipeId, buffer);
		RecipeTurnAndCopy recipe = new RecipeTurnAndCopy(recipeId, basic.getGroup(), basic.getWidth(), basic.getHeight(),
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
	public void write(@Nonnull PacketBuffer buffer, @Nonnull RecipeTurnAndCopy recipe)
	{
		RecipeSerializers.CRAFTING_SHAPED.write(buffer, recipe);
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

	@Override
	public ResourceLocation getName()
	{
		return new ResourceLocation(ImmersiveEngineering.MODID, "turn_and_copy");
	}
}