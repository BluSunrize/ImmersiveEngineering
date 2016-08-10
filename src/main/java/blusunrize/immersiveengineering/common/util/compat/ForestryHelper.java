package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class ForestryHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}
	@Override
	public void init()
	{
		FMLInterModComms.sendMessage("forestry", "add-backpack-items", String.format("forester@%s:%s", ImmersiveEngineering.instance, "seed"));
	}
	@Override
	public void postInit()
	{
		Fluid fluidHoney = FluidRegistry.getFluid("for.honey");
		if(fluidHoney!=null)
		{
			SqueezerRecipe.addRecipe(new FluidStack(fluidHoney,100), null, "dropHoney", 6400);
			SqueezerRecipe.addRecipe(new FluidStack(fluidHoney,100), null, "dropHoneydew", 6400);
		}
		ChemthrowerHandler.registerFlammable("bio.ethanol");
		ChemthrowerHandler.registerEffect("for.honey", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,60,1));
		ChemthrowerHandler.registerEffect("juice", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,40,0));
	}
}