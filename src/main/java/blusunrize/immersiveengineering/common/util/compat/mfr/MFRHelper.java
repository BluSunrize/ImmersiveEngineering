package blusunrize.immersiveengineering.common.util.compat.mfr;

import powercrystals.minefactoryreloaded.api.FactoryRegistry;
import blusunrize.immersiveengineering.common.IEContent;

public class MFRHelper
{
	public static void init()
	{
		 FactoryRegistry.sendMessage("registerPlantable",new IEPlantable(IEContent.itemSeeds, IEContent.blockCrop));
		 FactoryRegistry.sendMessage("registerHarvestable", new IEHarvestable());
	}
}
