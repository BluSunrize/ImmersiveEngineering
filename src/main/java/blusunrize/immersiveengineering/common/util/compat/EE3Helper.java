package blusunrize.immersiveengineering.common.util.compat;

import com.pahimar.ee3.api.exchange.EnergyValueRegistryProxy;

import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.common.IEContent;

public class EE3Helper extends IECompatModule
{
	public EE3Helper()
	{
		super("EE3");
	}
	
	@Override
	public void init()
	{
	}
	
	@Override
	public void postInit()
	{
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(new ItemStack(IEContent.itemSeeds,1,3), 24);
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(IEContent.itemSeeds, 24);
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(new ItemStack(IEContent.blockWoodenDecoration,1,0), 24);

		EnergyValueRegistryProxy.addPreAssignedEnergyValue(IEContent.fluidCreosote, 128);
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(IEContent.fluidEthanol, 400);
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(IEContent.fluidPlantoil, 200);
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(IEContent.fluidBiodiesel, 600);
	}
}