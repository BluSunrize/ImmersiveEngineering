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
