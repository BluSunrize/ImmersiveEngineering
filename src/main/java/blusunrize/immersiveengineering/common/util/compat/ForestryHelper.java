package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import cpw.mods.fml.common.registry.GameRegistry;

public class ForestryHelper extends IECompatModule
{
	@Override
	public void init()
	{
	}
	
	@Override
	public void postInit()
	{
		ItemStack propolis = GameRegistry.findItemStack("Forestry", "propolis", 1);
		Fluid fluidHoney = FluidRegistry.getFluid("for.honey");
		if(fluidHoney!=null)
		{
			DieselHandler.addSqueezerRecipe("dropHoney", 80, new FluidStack(fluidHoney,100), propolis);
			DieselHandler.addSqueezerRecipe("dropHoneydew", 80, new FluidStack(fluidHoney,100), null);
		}
	}
}