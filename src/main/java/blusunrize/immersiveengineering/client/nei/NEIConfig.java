package blusunrize.immersiveengineering.client.nei;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIConfig implements IConfigureNEI
{
	@Override
	public void loadConfig()
	{
		API.registerRecipeHandler(new NEIHammerCrushingHandler());
		API.registerUsageHandler(new NEIHammerCrushingHandler());
		
		API.registerRecipeHandler(new NEICokeOvenHandler());
		API.registerUsageHandler(new NEICokeOvenHandler());
		
		API.registerRecipeHandler(new NEIBlastFurnaceHandler());
		API.registerUsageHandler(new NEIBlastFurnaceHandler());
	}


	@Override
	public String getName()
	{
		return "Immersive Engineering NEI";
	}
	@Override
	public String getVersion()
	{
		return ImmersiveEngineering.VERSION;
	}
}
