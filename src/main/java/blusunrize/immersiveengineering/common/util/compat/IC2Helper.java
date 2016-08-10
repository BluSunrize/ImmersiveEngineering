package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Conveyor;
import net.minecraft.item.ItemStack;

public class IC2Helper extends IECompatModule
{
	@Override
	public void preInit()
	{
		IERecipes.addOredictRecipe(new ItemStack(IEContent.blockConveyor, 8, BlockTypes_Conveyor.CONVEYOR.getMeta()), "LLL", "IRI", 'I', "ingotIron", 'R', "dustRedstone", 'L', "itemRubber");
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