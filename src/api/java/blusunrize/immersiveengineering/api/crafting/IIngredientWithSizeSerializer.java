package blusunrize.immersiveengineering.api.crafting;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;

public interface IIngredientWithSizeSerializer
{
	IngredientWithSize parse(@Nonnull FriendlyByteBuf buffer);
	void write(@Nonnull FriendlyByteBuf buffer, @Nonnull IngredientWithSize ingredient);
	IngredientWithSize parse(@Nonnull JsonElement json);
	JsonElement write(@Nonnull IngredientWithSize ingredient);
}
