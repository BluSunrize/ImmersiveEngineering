package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.potion.Potion;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Damage;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.util.IEDamageSources;



public class MekanismHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
		IERecipes.arcBlacklist.add("RefinedObsidian");
	}

	@Override
	public void init()
	{
		ChemthrowerHandler.registerFlammable("hydrogen");
		ChemthrowerHandler.registerFlammable("ethene");
		ChemthrowerHandler.registerEffect("chlorine", new ChemthrowerEffect_Potion(null,0, Potion.poison,160,0));
	   
		ChemthrowerHandler.registerEffect("sulfurdioxidegas", new ChemthrowerEffect_Damage(IEDamageSources.causeAcidDamage(),2));
		ChemthrowerHandler.registerEffect("sulfurtrioxidegas", new ChemthrowerEffect_Damage(IEDamageSources.causeAcidDamage(),2));
		ChemthrowerHandler.registerEffect("sulfuricacid", new ChemthrowerEffect_Damage(IEDamageSources.causeAcidDamage(),2));
		ChemthrowerHandler.registerEffect("hydrogenchloride", new ChemthrowerEffect_Damage(IEDamageSources.causeAcidDamage(),2));
	}

	@Override
	public void postInit()
	{
	}
}