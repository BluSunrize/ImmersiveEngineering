/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.PositionedItemStack;
import blusunrize.lib.manual.utils.ManualRecipeRef;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collection;
import java.util.List;

public class ManualElementBottling extends ManualElementIECrafting
{
	public ManualElementBottling(ManualInstance manual, ManualRecipeRef... stacks)
	{
		super(manual, stacks);
		recalculateCraftingRecipes();
	}

	@Override
	public void recalculateCraftingRecipes()
	{
		this.recipes.clear();
		this.providedItems.clear();

		final Collection<BottlingMachineRecipe> allRecipes = BottlingMachineRecipe.RECIPES.getRecipes(Minecraft.getInstance().level);
		for(final ManualRecipeRef ref : stacks)
			ref.forEachMatchingRecipe(IERecipeTypes.BOTTLING_MACHINE.get(), allRecipes, recipe -> {
				int h = (int)Math.ceil(recipe.output.get().size()/2f);
				int middle = (int)(h/2f*18);

				FluidStack fs = recipe.fluidInput.getRandomizedExampleStack(0);
				ItemStack bucket = fs.getFluid().getBucket().getDefaultInstance();
				if(fs.hasTag())
					bucket.setTag(fs.getTag());
				String bucketFraction = FluidUtils.getBucketFraction(recipe.fluidInput.getAmount());

				int inputSize = recipe.inputs.size();
				int outputSize = recipe.output.get().size();

				PositionedItemStack[] pIngredients = new PositionedItemStack[inputSize+outputSize+1];
				int idx = 0;
				for(int i = 0; i < inputSize; i++)
					pIngredients[idx++] = new PositionedItemStack(recipe.inputs.get(i).getMatchingStacks(), 20-i%2*18, 8+i/2*18);
				pIngredients[idx++] = new PositionedItemStack(bucket, 46, middle-8, bucketFraction);

				List<ItemStack> outputs = recipe.output.get();
				for(int i = 0; i < outputs.size(); i++)
				{
					int j = i+2;
					pIngredients[idx++] = new PositionedItemStack(outputs.get(i), 70+j%2*18, -10+j/2*18);
				}

				this.recipes.add(pIngredients);
				this.arrowPositions.add(new ArrowPosition(46, h*18+1));

				if(h*18 > yOff)
					yOff = h*18+14;
				for(ItemStack output : outputs)
					this.addProvidedItem(output);
			});
	}
}