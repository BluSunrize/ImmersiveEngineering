package blusunrize.immersiveengineering.common.util.compat.hydcraft;

import k4unl.minecraft.Hydraulicraft.api.HCApi;
import k4unl.minecraft.Hydraulicraft.api.recipes.FluidShapedOreRecipe;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import cpw.mods.fml.common.registry.GameRegistry;

public class HydCraftHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void postInit()
	{
	}

	@Override
	public void init()
	{
		HCApi.getInstance().getTrolleyRegistrar().registerTrolley(new IETrolley());
		
		ItemStack ieTrolly = HCApi.getInstance().getTrolleyRegistrar().getTrolleyItem("ieCrop");
		ieTrolly.stackSize = 4;
		Block pressureCore = GameRegistry.findBlock("HydCraft","LPBlockCore");
	    Block pressureWall = GameRegistry.findBlock("HydCraft","hydraulicPressureWall");
	    Block hydraulicPiston = GameRegistry.findBlock("HydCraft","hydraulicPiston");
	    Fluid fLube = FluidRegistry.getFluid("lubricant");
		if(pressureCore!=null && pressureWall!=null && hydraulicPiston!=null && fLube!=null)
			HCApi.getInstance().getRecipeHandler().addAssemblerRecipe( new FluidShapedOreRecipe(ieTrolly, new Object[]{true, new Object[]{" P ","WCW","SIS", 'C',new ItemStack(pressureCore,1,1), 'W',pressureWall, 'S',"treatedStick", 'I',"ingotIron", 'P',hydraulicPiston}}).addFluidInput(new FluidStack(fLube, 40)));
	}
}
