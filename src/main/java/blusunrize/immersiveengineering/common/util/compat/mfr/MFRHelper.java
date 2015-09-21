package blusunrize.immersiveengineering.common.util.compat.mfr;

import powercrystals.minefactoryreloaded.api.FactoryRegistry;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;

public class MFRHelper extends IECompatModule
{
	@Override
	public void postInit()
	{
	}

	@Override
	public void init()
	{
		 FactoryRegistry.sendMessage("registerPlantable",new IEPlantable(IEContent.itemSeeds, IEContent.blockCrop));
		 FactoryRegistry.sendMessage("registerHarvestable", new IEHarvestable());
	}
}
