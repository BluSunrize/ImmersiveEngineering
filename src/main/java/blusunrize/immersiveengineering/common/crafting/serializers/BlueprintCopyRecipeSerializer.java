package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.crafting.BlueprintCopyRecipe;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;

public class BlueprintCopyRecipeSerializer extends IERecipeSerializer<BlueprintCopyRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return null;
	}

	@Override
	public BlueprintCopyRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		String group = JSONUtils.getString(json, "group", "");
		Map<String, Ingredient> keys = ShapedRecipe.deserializeKey(JSONUtils.getJsonObject(json, "key"));
		String[] pattern = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(JSONUtils.getJsonArray(json, "pattern")));
		int width = pattern[0].length();
		int height = pattern.length;
		NonNullList<Ingredient> inputs = ShapedRecipe.deserializeIngredients(pattern, keys, width, height);

		// Ensure only 1 blueprint item is present.
		if (inputs.stream().filter(ing -> Arrays
				.stream(ing.getMatchingStacks())
				.anyMatch(stack -> stack.getItem() == Misc.blueprint))
		.count() != 1) {
			throw new JsonSyntaxException("Recipe must have exactly 1 blueprint as an input.");
		}
		return new BlueprintCopyRecipe(recipeId, group, width, height, inputs);
	}

	@Override
	public BlueprintCopyRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
         int width = buffer.readVarInt();
         int height = buffer.readVarInt();
         String group = buffer.readString(32767);
         NonNullList<Ingredient> inputs = NonNullList.withSize(width * height, Ingredient.EMPTY);
         for(int i = 0; i < inputs.size(); ++i) {
            inputs.set(i, Ingredient.read(buffer));
         }
		return new BlueprintCopyRecipe(recipeId, group, width, height, inputs);
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull BlueprintCopyRecipe recipe)
	{
         buffer.writeVarInt(recipe.getWidth());
         buffer.writeVarInt(recipe.getRecipeHeight());
         buffer.writeString(recipe.getGroup());

         for(Ingredient input : recipe.getIngredients()) {
            input.write(buffer);
         }
	}
}
