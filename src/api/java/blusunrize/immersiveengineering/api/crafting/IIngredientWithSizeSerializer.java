package blusunrize.immersiveengineering.api.crafting;

import com.google.gson.JsonElement;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;

public interface IIngredientWithSizeSerializer
{
	IngredientWithSize parse(@Nonnull PacketBuffer buffer);
	void write(@Nonnull PacketBuffer buffer, @Nonnull IngredientWithSize ingredient);
	IngredientWithSize parse(@Nonnull JsonElement json);
	JsonElement write(@Nonnull IngredientWithSize ingredient);
}
