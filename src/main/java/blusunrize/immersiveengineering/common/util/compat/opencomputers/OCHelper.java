package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.common.util.compat.GeneralComputerHelper;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import li.cil.oc.api.API;

public class OCHelper extends IECompatModule
{

	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{
	}

	@Override
	public void init()
	{
		API.driver.add(new DieselGenDriver());
		API.driver.add(new CrusherDriver());
		API.driver.add(new ArcFurnaceDriver());
		API.driver.add(new AssemblerDriver());
		API.driver.add(new BottlingMachineDriver());
		API.driver.add(new RefineryDriver());
		API.driver.add(new SqueezerDriver());
		API.driver.add(new SampleDrillDriver());
		API.driver.add(new FermenterDriver());
		API.driver.add(new FloodlightDriver());
		API.driver.add(new ExcavatorDriver());
		API.driver.add(new CapacitorDriver());
		API.driver.add(new EnergyMeterDriver());
		API.driver.add(new TeslaCoilDriver());
		API.driver.add(new MixerDriver());
	}

	@Override
	public void postInit()
	{
		GeneralComputerHelper.addComputerManualContent();
	}

}
