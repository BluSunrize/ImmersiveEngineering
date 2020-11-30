package blusunrize.immersiveengineering.api.crafting.builders;

import blusunrize.immersiveengineering.common.crafting.BlueprintCopyRecipe;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlueprintCopyRecipeWrapper implements IFinishedRecipe
{
	private final IFinishedRecipe original;

	public BlueprintCopyRecipeWrapper(IFinishedRecipe original)
	{
		this.original = original;
	}

	@Override
	public void serialize(@Nonnull JsonObject json)
	{
		original.serialize(json);
		// Remove recipe result, this is always a blueprint.
		json.remove("result");
	}

	@Nonnull
	@Override
	public ResourceLocation getID()
	{
		return original.getID();
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return BlueprintCopyRecipe.SERIALIZER.get();
	}

	@Nullable
	@Override
	public JsonObject getAdvancementJson()
	{
		return original.getAdvancementJson();
	}

	@Nullable
	@Override
	public ResourceLocation getAdvancementID()
	{
		return original.getAdvancementID();
	}
}
