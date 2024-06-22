/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.crafting.LazyShapelessRecipe;
import blusunrize.immersiveengineering.common.register.IEItems;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

public class HammerCrushingRecipeSerializer extends IERecipeSerializer<LazyShapelessRecipe>
{
	private final DualMapCodec<RegistryFriendlyByteBuf, LazyShapelessRecipe> codecs = DualMapCodec.composite(
			TagOutput.CODECS.fieldOf("result"), LazyShapelessRecipe::getResult,
			DualCodecs.INGREDIENT.fieldOf("input"), r -> r.getIngredients().get(0),
			(result, input) -> new LazyShapelessRecipe(
					"", result, NonNullList.of(Ingredient.EMPTY, input, Ingredient.of(IEItems.Tools.HAMMER)), this
			)
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, LazyShapelessRecipe> codecs()
	{
		return codecs;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Blocks.CRAFTING_TABLE);
	}
}
