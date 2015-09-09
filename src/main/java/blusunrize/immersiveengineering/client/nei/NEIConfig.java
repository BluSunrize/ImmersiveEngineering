package blusunrize.immersiveengineering.client.nei;

import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
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
		
		API.registerRecipeHandler(new NEICrusherHandler());
		API.registerUsageHandler(new NEICrusherHandler());

		API.registerRecipeHandler(new NEIBlueprintHandler());
		API.registerUsageHandler(new NEIBlueprintHandler());
		
		API.registerRecipeHandler(new NEISqueezerHandler());
		API.registerUsageHandler(new NEISqueezerHandler());
		API.registerRecipeHandler(new NEIFermenterHandler());
		API.registerUsageHandler(new NEIFermenterHandler());
		API.registerRecipeHandler(new NEIRefineryHandler());
		API.registerUsageHandler(new NEIRefineryHandler());
		
		API.registerRecipeHandler(new NEIArcFurnaceHandler());
		API.registerUsageHandler(new NEIArcFurnaceHandler());
		
		API.hideItem(new ItemStack(IEContent.blockFakeLight));
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
