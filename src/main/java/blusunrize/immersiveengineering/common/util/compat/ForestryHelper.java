package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.common.IEContent;
import cpw.mods.fml.common.registry.GameRegistry;
import forestry.api.fuels.EngineBronzeFuel;
import forestry.api.fuels.FuelManager;
import forestry.core.config.GameMode;

public class ForestryHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

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

		FuelManager.bronzeEngineFuel.put(IEContent.fluidBiodiesel, new EngineBronzeFuel(IEContent.fluidBiodiesel, 50, (int)(2500*GameMode.getGameMode().getFloatSetting("fuel.biomass.biogas")), 1));
	}
}