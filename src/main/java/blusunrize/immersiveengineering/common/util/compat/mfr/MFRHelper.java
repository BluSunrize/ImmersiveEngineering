package blusunrize.immersiveengineering.common.util.compat.mfr;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import powercrystals.minefactoryreloaded.api.FactoryRegistry;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;

public class MFRHelper extends IECompatModule
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
		FactoryRegistry.sendMessage("registerPlantable",new IEPlantable(IEContent.itemSeeds, IEContent.blockCrop));
		FactoryRegistry.sendMessage("registerHarvestable", new IEHarvestable());

		ChemthrowerHandler.registerEffect("sewage", new ChemthrowerEffect_Potion(null,0, new PotionEffect(Potion.poison.id,60,0)));
		ChemthrowerHandler.registerEffect("sludge", new ChemthrowerEffect_Potion(null,0, new PotionEffect(Potion.wither.id,20,0),new PotionEffect(Potion.confusion.id,40,0)));
	}
}
