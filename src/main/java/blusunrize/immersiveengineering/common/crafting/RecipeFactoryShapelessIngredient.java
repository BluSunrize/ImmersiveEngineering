/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class RecipeFactoryShapelessIngredient implements IRecipeFactory
{
	@Override
	public IRecipe parse(JsonContext context, JsonObject json)
	{
		String group = JsonUtils.getString(json, "group", "");

		NonNullList<Ingredient> ings = NonNullList.create();
		for(JsonElement ele : JsonUtils.getJsonArray(json, "ingredients"))
			ings.add(CraftingHelper.getIngredient(ele, context));

		if(ings.isEmpty())
			throw new JsonParseException("No ingredients for shapeless recipe");

		ItemStack result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
		RecipeShapelessIngredient recipe = new RecipeShapelessIngredient(group.isEmpty()?null: new ResourceLocation(group), result, ings);

		if(JsonUtils.hasField(json, "damage_tool"))
			recipe.setToolDamageRecipe(JsonUtils.getInt(json, "damage_tool"));
		if(JsonUtils.hasField(json, "copy_nbt"))
			recipe.setNBTCopyTargetRecipe(JsonUtils.getInt(json, "copy_nbt"));

		return recipe;
	}
}