package blusunrize.immersiveengineering.common.util.compat;

import cpw.mods.fml.common.registry.GameRegistry;


public class SteamCraftHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		GameRegistry.findBlock("steamcraft2", "BlockMetalLattice");
	}

	@Override
	public void postInit()
	{
	}
}