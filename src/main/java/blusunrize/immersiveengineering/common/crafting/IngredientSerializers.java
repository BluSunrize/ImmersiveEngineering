/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraftforge.common.crafting.CraftingHelper;

public class IngredientSerializers
{
	public static void init()
	{
		CraftingHelper.register(
				IngredientSerializerFluidStack.NAME,
				IngredientSerializerFluidStack.INSTANCE
		);
	}
}
