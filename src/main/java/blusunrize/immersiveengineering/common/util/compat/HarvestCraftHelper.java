package blusunrize.immersiveengineering.common.util.compat;

import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.common.IEContent;


public class HarvestCraftHelper extends IECompatModule
{
	@Override
	public void init()
	{
		DieselHandler.addSqueezerRecipe("listAllseed", 80, new FluidStack(IEContent.fluidPlantoil,80), null);
		DieselHandler.addFermenterRecipe("listAllfruit", 80, new FluidStack(IEContent.fluidEthanol,80), null);
		DieselHandler.addFermenterRecipe("listAllgrain", 80, new FluidStack(IEContent.fluidEthanol,80), null);
	}

	@Override
	public void postInit()
	{
	}
}