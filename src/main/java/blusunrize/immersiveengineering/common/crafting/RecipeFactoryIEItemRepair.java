/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class RecipeFactoryIEItemRepair implements IRecipeFactory
{

	@Override
	public IRecipe parse(JsonContext context, JsonObject json)
	{
		Ingredient ingred = CraftingHelper.getIngredient(json.get("tool"), context);
		return new RecipeIEItemRepair(ingred);
	}
}
