/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class BlastFurnaceRecipeSerializer extends IERecipeSerializer<BlastFurnaceRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, BlastFurnaceRecipe> CODECS = DualCompositeMapCodecs.composite(
			TagOutput.CODECS.fieldOf("result"), r -> r.output,
			IngredientWithSize.CODECS.fieldOf("input"), r -> r.input,
			DualCodecs.INT.optionalFieldOf("time", 200), r -> r.time,
			optionalItemOutput("slag"), r -> r.slag,
			BlastFurnaceRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, BlastFurnaceRecipe> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.BLAST_FURNACE.iconStack();
	}
}
