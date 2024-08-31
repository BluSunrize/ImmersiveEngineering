/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import blusunrize.immersiveengineering.common.crafting.DamageToolRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class DamageToolRecipeSerializer implements RecipeSerializer<DamageToolRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, DamageToolRecipe> CODECS = DualCompositeMapCodecs.composite(
			DualCodecs.STRING.fieldOf("group"), ShapelessRecipe::getGroup,
			DualCodecs.ITEM_STACK.fieldOf("result"), r -> r.getResultItem(null),
			DualCodecs.INGREDIENT.fieldOf("tool"), DamageToolRecipe::getTool,
			IEDualCodecs.NONNULL_INGREDIENTS.fieldOf("ingredients"), ShapelessRecipe::getIngredients,
			DamageToolRecipe::new
	);

	@Override
	public MapCodec<DamageToolRecipe> codec()
	{
		return CODECS.mapCodec();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, DamageToolRecipe> streamCodec()
	{
		return CODECS.streamCodec();
	}
}