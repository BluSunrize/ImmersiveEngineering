package blusunrize.immersiveengineering.client.nei;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.client.gui.GuiAssembler;
import blusunrize.immersiveengineering.common.IEContent;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IUsageHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class NEIConfig implements IConfigureNEI
{
	@Override
	public void loadConfig()
	{
		//registerDualHandler(new NEIHammerCrushingHandler());
		registerDualHandler(new NEIShaderBagHandler());

		registerDualHandler(new NEIBlueprintHandler());
		
		registerDualHandler(new NEICokeOvenHandler());
		registerDualHandler(new NEIBlastFurnaceHandler());

		registerDualHandler(new NEISqueezerHandler());
		registerDualHandler(new NEIFermenterHandler());
		registerDualHandler(new NEIRefineryHandler());
		
		registerDualHandler(new NEIBottlingMachineHandler());
		
		registerDualHandler(new NEIMetalPressHandler());
		
		registerDualHandler(new NEICrusherHandler());
		
		for(String s : ArcFurnaceRecipe.specialRecipeTypes)
		{
			NEIArcFurnaceHandler handler = NEIArcFurnaceHandler.createSubHandler(s);
			registerDualHandler(handler);
		}
		registerDualHandler(new NEIArcFurnaceHandler());


		API.hideItem(new ItemStack(IEContent.blockFakeLight,1,OreDictionary.WILDCARD_VALUE));
		API.hideItem(new ItemStack(IEContent.itemFakeIcons,1,OreDictionary.WILDCARD_VALUE));

		API.registerGuiOverlay(GuiAssembler.class, "crafting", new AssemblerNEIHelper.StackPositioner());
		API.registerGuiOverlayHandler(GuiAssembler.class, new AssemblerNEIHelper.OverlayHandler(), "crafting");
	}
	
	void registerDualHandler(Object handler)
	{
		API.registerRecipeHandler((ICraftingHandler) handler);
		API.registerUsageHandler((IUsageHandler) handler);
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
