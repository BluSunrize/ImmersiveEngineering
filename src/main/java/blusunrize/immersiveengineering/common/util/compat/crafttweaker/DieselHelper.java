package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.energy.DieselHandler;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersiveengineering.DieselHandler")
public class DieselHelper
{
    @ZenMethod
    public static void addFuel(ILiquidStack fuel, int time, boolean drill)
    {
    	
        CraftTweakerAPI.apply(new Add(fuel, time, drill));
    }
    
    @ZenMethod
    public static void addFuel(ILiquidStack fuel, int time)
    {
        addFuel(fuel, time, false);
    }

    private static class Add implements IAction
    {
        private final ILiquidStack fuel;
        private final int time;
        private final boolean drill;

        public Add(ILiquidStack fuel, int time, boolean drill)
        {
            this.fuel = fuel;
            this.time = time;
            this.drill = drill;
        }

        @Override
        public void apply()
        {
        	Fluid fuelFluid = FluidRegistry.getFluid(fuel.getName());
        	DieselHandler.registerFuel(fuelFluid, time);
            if(drill){
            	DieselHandler.registerDrillFuel(fuelFluid);
            }
        }

        @Override
        public String describe()
        {
            return "Registering Fuel " + fuel.getDisplayName();
        }
    }

}
