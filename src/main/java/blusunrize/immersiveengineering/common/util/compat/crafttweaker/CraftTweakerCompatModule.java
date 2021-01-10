package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import net.minecraftforge.common.MinecraftForge;

public class CraftTweakerCompatModule extends IECompatModule
{

	@Override
	public void preInit()
	{
		MinecraftForge.EVENT_BUS.register(CrafttweakerEventHandlers.class);
	}

	@Override
	public void registerRecipes()
	{

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
