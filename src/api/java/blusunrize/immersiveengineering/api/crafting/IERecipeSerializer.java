/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.IEApi;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class IERecipeSerializer<R extends Recipe<?>> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<R>
{
	public abstract ItemStack getIcon();

	@Override
	public R fromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		if(CraftingHelper.processConditions(json, "conditions", context))
			return readFromJson(recipeId, json, context);
		return null;
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public R fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject serializedRecipe)
	{
		return null;
	}

	protected static Lazy<ItemStack> readOutput(JsonElement outputObject)
	{
		if(outputObject.isJsonObject()&&outputObject.getAsJsonObject().has("item"))
			return Lazy.of(() -> ShapedRecipe.itemStackFromJson(outputObject.getAsJsonObject()));
		IngredientWithSize outgredient = IngredientWithSize.deserialize(outputObject);
		return Lazy.of(() -> IEApi.getPreferredStackbyMod(outgredient.getMatchingStacks()));
	}

	@Nullable
	protected static StackWithChance readConditionalStackWithChance(JsonElement element, IContext context)
	{
		JsonObject object = element.getAsJsonObject();
		if(CraftingHelper.processConditions(object, "conditions", context))
		{
			float chance = GsonHelper.getAsFloat(object, "chance");
			Lazy<ItemStack> stack = readOutput(object.get("output"));
			return new StackWithChance(stack, chance);
		}
		return null;
	}

	public abstract R readFromJson(ResourceLocation recipeId, JsonObject json, IContext context);

	protected static Lazy<ItemStack> readLazyStack(FriendlyByteBuf buf)
	{
		ItemStack stack = buf.readItem();
		return Lazy.of(() -> stack);
	}

	protected static void writeLazyStack(FriendlyByteBuf buf, Lazy<ItemStack> stack)
	{
		buf.writeItem(stack.get());
	}
}
