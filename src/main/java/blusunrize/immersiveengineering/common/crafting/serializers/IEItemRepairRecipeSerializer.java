/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.crafting.IEItemRepairRecipe;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

//TODO is this stll needed?
public class IEItemRepairRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<IEItemRepairRecipe>
{
	public static final IEItemRepairRecipeSerializer INSTANCE = IRecipeSerializer.register(
			ImmersiveEngineering.MODID+":repair", new IEItemRepairRecipeSerializer()
	);

	@Nonnull
	@Override
	public IEItemRepairRecipe read(@Nonnull ResourceLocation recipeId, JsonObject json)
	{
		Ingredient ingred = CraftingHelper.getIngredient(json.get("tool"));
		return new IEItemRepairRecipe(recipeId, ingred);
	}

	@Nonnull
	@Override
	public IEItemRepairRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
		Ingredient ingred = Ingredient.read(buffer);
		return new IEItemRepairRecipe(recipeId, ingred);
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull IEItemRepairRecipe recipe)
	{
		CraftingHelper.write(buffer, recipe.getToolIngredient());
	}
}
