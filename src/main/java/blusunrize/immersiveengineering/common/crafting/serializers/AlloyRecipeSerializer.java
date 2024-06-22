/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class AlloyRecipeSerializer extends IERecipeSerializer<AlloyRecipe>
{
	private static final DualMapCodec<RegistryFriendlyByteBuf, AlloyRecipe> CODECS = DualMapCodec.composite(
			TagOutput.CODECS.fieldOf("result"), r -> r.output,
			IngredientWithSize.CODECS.fieldOf("input0"), r -> r.input0,
			IngredientWithSize.CODECS.fieldOf("input1"), r -> r.input1,
			DualCodecs.INT.optionalFieldOf("time", 200), r -> r.time,
			AlloyRecipe::new
	);

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.ALLOY_SMELTER.iconStack();
	}

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, AlloyRecipe> codecs()
	{
		return CODECS;
	}
}
