/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.common.crafting.DamageToolRecipe;
import blusunrize.immersiveengineering.common.util.IECodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import javax.annotation.Nonnull;

public class DamageToolRecipeSerializer implements RecipeSerializer<DamageToolRecipe>
{
	public static final Codec<DamageToolRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.STRING.fieldOf("group").forGetter(ShapelessRecipe::getGroup),
			ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
			Ingredient.CODEC.fieldOf("tool").forGetter(DamageToolRecipe::getTool),
			IECodecs.NONNULL_INGREDIENTS.fieldOf("ingredients").forGetter(ShapelessRecipe::getIngredients)
	).apply(inst, DamageToolRecipe::new));

	@Override
	public Codec<DamageToolRecipe> codec()
	{
		return CODEC;
	}

	@Nonnull
	@Override
	public DamageToolRecipe fromNetwork(@Nonnull FriendlyByteBuf buffer)
	{
		int stdCount = buffer.readInt();
		NonNullList<Ingredient> stdIngr = NonNullList.create();
		for(int i = 0; i < stdCount; ++i)
			stdIngr.add(Ingredient.fromNetwork(buffer));
		Ingredient tool = Ingredient.fromNetwork(buffer);
		String group = buffer.readUtf(512);
		ItemStack output = buffer.readItem();
		return new DamageToolRecipe(group, output, tool, stdIngr);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull DamageToolRecipe recipe)
	{
		int standardCount = recipe.getIngredients().size()-1;
		buffer.writeInt(standardCount);
		for(int i = 0; i < standardCount; ++i)
			recipe.getIngredients().get(i).toNetwork(buffer);
		recipe.getTool().toNetwork(buffer);
		buffer.writeUtf(recipe.getGroup());
		buffer.writeItem(recipe.getResultItem(null));
	}
}