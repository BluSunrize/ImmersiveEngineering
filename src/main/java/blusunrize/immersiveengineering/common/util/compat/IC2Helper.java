package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.util.Utils;

public class IC2Helper extends IECompatModule
{
	@Override
	public void preInit()
	{
		IERecipes.addOredictRecipe(Utils.copyStackWithAmount(ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID + ":conveyor"), 8), "LLL", "IRI", 'I', "ingotIron", 'R', "dustRedstone", 'L', "itemRubber");
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