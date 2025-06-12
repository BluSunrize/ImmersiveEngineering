/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.PositionedItemStack;
import blusunrize.lib.manual.utils.ManualRecipeRef;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
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

		for(final ManualRecipeRef ref : stacks)
			ref.forEachMatchingRecipe(IERecipeTypes.BOTTLING_MACHINE.get(), recipe -> {
				int h = (int)Math.ceil(recipe.output.get().size()/2f);
				int middle = (int)(h/2f*18);

				List<ItemStack> buckets = Arrays.stream(recipe.fluidInput.getFluids()).map(fs -> {
					ItemStack bucket = fs.getFluid().getBucket().getDefaultInstance();
					bucket.applyComponents(fs.getComponents());
					return bucket;
				}).toList();
				String bucketFraction = FluidUtils.getBucketFraction(recipe.fluidInput.amount());

				int inputSize = recipe.inputs.size();
				int outputSize = recipe.output.get().size();

				PositionedItemStack[] pIngredients = new PositionedItemStack[inputSize+outputSize+1];
				int idx = 0;
				for(int i = 0; i < inputSize; i++)
					pIngredients[idx++] = new PositionedItemStack(recipe.inputs.get(i).getMatchingStacks(), 26-i%2*18, 8+i/2*18);
				pIngredients[idx++] = new PositionedItemStack(buckets, 48, middle-8, bucketFraction);

				List<ItemStack> outputs = recipe.output.get();
				for(int i = 0; i < outputs.size(); i++)
				{
					int j = i+2;
					pIngredients[idx++] = new PositionedItemStack(outputs.get(i), 70+j%2*18, -10+j/2*18);
				}

				this.recipes.add(pIngredients);
				this.arrowPositions.add(new ArrowPosition(48, h*18+1));

				if(h*18 > yOff)
					yOff = h*18+14;
				for(ItemStack output : outputs)
					this.addProvidedItem(output);
			});
	}
}