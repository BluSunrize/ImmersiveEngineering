/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeSerializers;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nonnull;

//TODO is this stll needed?
public class RecipeSerializerIEItemRepair implements IRecipeSerializer<RecipeIEItemRepair>
{
	public static final RecipeSerializerIEItemRepair INSTANCE = RecipeSerializers.register(new RecipeSerializerIEItemRepair());

	@Nonnull
	@Override
	public RecipeIEItemRepair read(@Nonnull ResourceLocation recipeId, JsonObject json)
	{
		Ingredient ingred = CraftingHelper.getIngredient(json.get("tool"));
		return new RecipeIEItemRepair(recipeId, ingred);
	}

	@Nonnull
	@Override
	public RecipeIEItemRepair read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
		Ingredient ingred = Ingredient.fromBuffer(buffer);
		return new RecipeIEItemRepair(recipeId, ingred);
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull RecipeIEItemRepair recipe)
	{
		CraftingHelper.write(buffer, recipe.getToolIngredient());
	}

	@Nonnull
	@Override
	public ResourceLocation getName()
	{
		return new ResourceLocation(ImmersiveEngineering.MODID, "repair");
	}
}
