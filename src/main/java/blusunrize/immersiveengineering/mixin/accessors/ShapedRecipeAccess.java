/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccess
{
	@Invoker
	static Map<String, Ingredient> invokeDeserializeKey(JsonObject data)
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}

	@Invoker
	static String[] invokeShrink(String... input)
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}

	@Invoker
	static NonNullList<Ingredient> invokeDeserializeIngredients(
			String[] pattern, Map<String, Ingredient> keys, int patternWidth, int patternHeight
	)
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}

	@Invoker
	static String[] invokePatternFromJson(JsonArray jsonArr)
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}
}
