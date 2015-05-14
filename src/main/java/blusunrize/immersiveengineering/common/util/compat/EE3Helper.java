package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.common.IEContent;

import com.pahimar.ee3.api.exchange.EnergyValueRegistryProxy;

public class EE3Helper
{
	public static void init()
	{
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(new ItemStack(IEContent.itemSeeds,1,3), 24);
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(IEContent.itemSeeds, 24);
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(new ItemStack(IEContent.blockWoodenDecoration,1,0), 24);

		EnergyValueRegistryProxy.addPreAssignedEnergyValue(IEContent.fluidCreosote, 128);
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(IEContent.fluidEthanol, 400);
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(IEContent.fluidPlantoil, 200);
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(IEContent.fluidBiodiesel, 600);
	}

	private static void addIEMetalValue(int baseMeta, boolean hasOre, float value)
	{
		if(hasOre) //Ore
			addValue(new ItemStack(IEContent.blockOres,1,baseMeta), value);
		//Ingot
		addValue(new ItemStack(IEContent.itemMetal,1,baseMeta), value);
		//Dust
		addValue(new ItemStack(IEContent.itemMetal,1,10+baseMeta), value);
		//Block
		addValue(new ItemStack(IEContent.blockStorage,1,baseMeta), value*9);
	}
	private static void addValue(Object o, float value)
	{
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(o, value);
	}
}