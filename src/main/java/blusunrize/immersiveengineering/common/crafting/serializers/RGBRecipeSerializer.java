/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.crafting.RGBColourationRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RGBRecipeSerializer implements RecipeSerializer<RGBColourationRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, RGBColourationRecipe> CODEC = DualCodecs.INGREDIENT.fieldOf("target")
			.map(RGBColourationRecipe::new, RGBColourationRecipe::target);

	@Override
	public MapCodec<RGBColourationRecipe> codec()
	{
		return CODEC.mapCodec();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, RGBColourationRecipe> streamCodec()
	{
		return CODEC.streamCodec();
	}
}
