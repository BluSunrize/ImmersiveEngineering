package blusunrize.immersiveengineering.common.util.compat.hydcraft;

import k4unl.minecraft.Hydraulicraft.api.HCApi;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;

public class HydCraftHelper extends IECompatModule
{
	public HydCraftHelper()
	{
		super("HydCraft");
	}

	@Override
	public void postInit()
	{
	}

	@Override
	public void init()
	{
		HCApi.getInstance().getTrolleyRegistrar().registerTrolley(new IETrolley());	
	}
}
