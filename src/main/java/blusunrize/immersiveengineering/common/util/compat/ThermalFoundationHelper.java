package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.init.Items;
import blusunrize.immersiveengineering.common.IERecipes;

public class ThermalFoundationHelper extends IECompatModule
{
	@Override
	public void init()
	{
		IERecipes.addItemToOreDictCrusherRecipe("dustBasalz",4, "rodBasalz", 3200).addToSecondaryOutput("dustObsidian",.5f);
		IERecipes.addItemToOreDictCrusherRecipe("dustBlitz",4, "rodBlitz", 3200).addToSecondaryOutput("dustSaltpeter",.5f);
		IERecipes.addItemToOreDictCrusherRecipe("dustBlizz",4, "rodBlizz", 3200).addToSecondaryOutput(Items.snowball,.5f);
	}

	@Override
	public void postInit()
	{
	}
}