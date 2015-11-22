package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.IERecipes;



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
	}

	@Override
	public void postInit()
	{
	}
}