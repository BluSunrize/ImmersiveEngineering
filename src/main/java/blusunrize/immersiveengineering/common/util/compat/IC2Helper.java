package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.IRecipeAdapter;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.RecipeQuery;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.util.Utils;
import ic2.api.recipe.IRecipeInput;
import ic2.core.recipe.AdvRecipe;

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
		AssemblerHandler.registerRecipeAdapter(AdvRecipe.class, new IRecipeAdapter<AdvRecipe>()
		{
			@Override
			public RecipeQuery[] getQueriedInputs(AdvRecipe recipe)
			{
				IRecipeInput[] in = recipe.input;
				if (in!=null)
				{
					RecipeQuery[] ret = new RecipeQuery[in.length];
					for (int i = 0;i<in.length;i++)
					{
						IRecipeInput inStack = in[i];
						ret[i] = new RecipeQuery(inStack.getInputs(), inStack.getAmount());
					}
					return ret;
				}

				return new RecipeQuery[0];
			}
		});
	}

	@Override
	public void postInit()
	{
	}
}