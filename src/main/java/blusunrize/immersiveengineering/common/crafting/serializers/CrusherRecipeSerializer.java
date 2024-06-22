/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CrusherRecipeSerializer extends IERecipeSerializer<CrusherRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, CrusherRecipe> CODECS = DualMapCodec.composite(
			TagOutput.CODECS.fieldOf("result"), r -> r.output,
			DualCodecs.INGREDIENT.fieldOf("input"), r -> r.input,
			DualCodecs.INT.fieldOf("energy"), MultiblockRecipe::getBaseEnergy,
			CHANCE_LIST_CODECS.optionalFieldOf("secondaries", List.of()), r -> r.secondaryOutputs,
			CrusherRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, CrusherRecipe> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.CRUSHER.iconStack();
	}
}
