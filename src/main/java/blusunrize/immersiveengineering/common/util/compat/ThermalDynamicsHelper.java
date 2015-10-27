package blusunrize.immersiveengineering.common.util.compat;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.common.IERecipes;

public class ThermalDynamicsHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}
	
	@Override
	public void init()
	{
		Block blockFluxduct = GameRegistry.findBlock("ThermalDynamics", "ThermalDynamics_0");
		Block blockItemduct = GameRegistry.findBlock("ThermalDynamics", "ThermalDynamics_32");
		Fluid fluidRedstone = FluidRegistry.getFluid("redstone");
		Fluid fluidGlowstone = FluidRegistry.getFluid("glowstone");
		Fluid fluidCryotheum = FluidRegistry.getFluid("cryotheum");
		if(blockFluxduct!=null && blockItemduct!=null && fluidRedstone!=null && fluidGlowstone!=null && fluidCryotheum!=null)
		{
			BottlingMachineRecipe.addRecipe(new ItemStack(blockFluxduct,1,2), new ItemStack(blockFluxduct,1,3), new FluidStack(fluidRedstone,200));
			BottlingMachineRecipe.addRecipe(new ItemStack(blockFluxduct,1,4), new ItemStack(blockFluxduct,1,5), new FluidStack(fluidRedstone,200));
			BottlingMachineRecipe.addRecipe(new ItemStack(blockFluxduct,1,6), new ItemStack(blockFluxduct,1,7), new FluidStack(fluidCryotheum,500));
			
			BottlingMachineRecipe.addRecipe(new ItemStack(blockItemduct,1,2), new ItemStack(blockItemduct,1,0), new FluidStack(fluidGlowstone,200));
			BottlingMachineRecipe.addRecipe(new ItemStack(blockItemduct,1,3), new ItemStack(blockItemduct,1,1), new FluidStack(fluidGlowstone,200));
			BottlingMachineRecipe.addRecipe(new ItemStack(blockItemduct,1,6), new ItemStack(blockItemduct,1,0), new FluidStack(fluidRedstone,200));
			BottlingMachineRecipe.addRecipe(new ItemStack(blockItemduct,1,7), new ItemStack(blockItemduct,1,1), new FluidStack(fluidRedstone,200));
		}
	}

	@Override
	public void postInit()
	{
	}
}