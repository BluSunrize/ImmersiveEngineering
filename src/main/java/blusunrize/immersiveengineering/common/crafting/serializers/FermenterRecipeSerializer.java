/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class FermenterRecipeSerializer extends IERecipeSerializer<FermenterRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, FermenterRecipe> CODECS = DualMapCodec.composite(
			optionalFluidOutput("fluid"), r -> r.fluidOutput,
			optionalItemOutput("result"), r -> r.itemOutput,
			IngredientWithSize.CODECS.fieldOf("input"), r -> r.input,
			DualCodecs.INT.fieldOf("energy"), MultiblockRecipe::getBaseEnergy,
			FermenterRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, FermenterRecipe> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.FERMENTER.iconStack();
	}
}
