package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.init.Items;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.IERecipes;

public class ThermalFoundationHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		CrusherRecipe r;
		
		r=IERecipes.addItemToOreDictCrusherRecipe("dustBasalz",4, "rodBasalz", 3200);
		if(r!=null)
			r.addToSecondaryOutput("dustObsidian",.5f);
		
		r=IERecipes.addItemToOreDictCrusherRecipe("dustBlitz",4, "rodBlitz", 3200);
		if(r!=null)
			r.addToSecondaryOutput("dustSaltpeter",.5f);
		
		r=IERecipes.addItemToOreDictCrusherRecipe("dustBlizz",4, "rodBlizz", 3200);
		if(r!=null)
			r.addToSecondaryOutput(Items.snowball,.5f);
	}

	@Override
	public void postInit()
	{
	}
}