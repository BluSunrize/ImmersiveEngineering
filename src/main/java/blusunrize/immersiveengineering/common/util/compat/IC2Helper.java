/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.IRecipeAdapter;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.RecipeQuery;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import ic2.api.recipe.IRecipeInput;
import ic2.core.recipe.AdvRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class IC2Helper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{
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
				if(in!=null)
				{
					RecipeQuery[] ret = new RecipeQuery[in.length];
					for(int i = 0; i < in.length; i++)
					{
						IRecipeInput inStack = in[i];
						ret[i] = new RecipeQuery(inStack.getInputs(), inStack.getAmount());
					}
					return ret;
				}

				return new RecipeQuery[0];
			}
		});

		Item cropRes = Item.REGISTRY.getObject(new ResourceLocation("ic2", "crop_res"));
		if(cropRes!=null)
			BelljarHandler.registerBasicItemFertilizer(new ItemStack(cropRes, 1, 2), 1.25f);
	}

	@Override
	public void postInit()
	{
	}
}