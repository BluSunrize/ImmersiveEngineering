package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.common.IEContent;
import cpw.mods.fml.common.registry.GameRegistry;
import forestry.api.core.ForestryAPI;
import forestry.api.fuels.EngineBronzeFuel;
import forestry.api.fuels.FuelManager;

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

		int burnDuration = (int) (2500 * ForestryAPI.activeMode.getFloatSetting("fuel.biomass.biogas"));
		EngineBronzeFuel engineBronzeFuel = new EngineBronzeFuel(IEContent.fluidBiodiesel, 50, burnDuration, 1);
		FuelManager.bronzeEngineFuel.put(IEContent.fluidBiodiesel, engineBronzeFuel);
	
		ChemthrowerHandler.registerFlammable("bioEthanol");
	}
}