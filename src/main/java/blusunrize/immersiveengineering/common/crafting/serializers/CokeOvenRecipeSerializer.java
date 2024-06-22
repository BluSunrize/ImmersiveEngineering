/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class CokeOvenRecipeSerializer extends IERecipeSerializer<CokeOvenRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, CokeOvenRecipe> CODECS = DualMapCodec.composite(
			TagOutput.CODECS.fieldOf("result"), r -> r.output,
			IngredientWithSize.CODECS.fieldOf("input"), r -> r.input,
			DualCodecs.INT.optionalFieldOf("time", 200), r -> r.time,
			DualCodecs.INT.fieldOf("creosote"), r -> r.creosoteOutput,
			CokeOvenRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, CokeOvenRecipe> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.COKE_OVEN.iconStack();
	}
}
