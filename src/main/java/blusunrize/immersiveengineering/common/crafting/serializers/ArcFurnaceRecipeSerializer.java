/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ArcFurnaceRecipeSerializer extends IERecipeSerializer<ArcFurnaceRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, ArcFurnaceRecipe> CODECS = DualCompositeMapCodecs.composite(
			TagOutputList.CODEC.fieldOf("results"), r -> r.output,
			TagOutput.CODECS.optionalFieldOf("slag", TagOutput.EMPTY), r -> r.slag,
			CHANCE_LIST_CODECS.optionalFieldOf("secondaries", List.of()), r -> r.secondaryOutputs,
			DualCodecs.INT.fieldOf("time"), MultiblockRecipe::getBaseTime,
			DualCodecs.INT.fieldOf("energy"), MultiblockRecipe::getBaseEnergy,
			IngredientWithSize.CODECS.fieldOf("input"), r -> r.input,
			IngredientWithSize.CODECS.listOf().fieldOf("additives"), r -> r.additives,
			DualCodecs.STRING.optionalFieldOf("specialRecipeType", ""), r -> r.specialRecipeType,
			ArcFurnaceRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, ArcFurnaceRecipe> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.ARC_FURNACE.iconStack();
	}
}
