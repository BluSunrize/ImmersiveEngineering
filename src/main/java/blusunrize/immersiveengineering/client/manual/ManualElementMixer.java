/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.PositionedItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;

import java.util.Arrays;
import java.util.List;

public class ManualElementMixer extends ManualElementIECrafting
{
	protected final Fluid[] fluids;

	public ManualElementMixer(ManualInstance manual, Fluid... fluids)
	{
		super(manual);
		this.fluids = fluids;
		recalculateCraftingRecipes();
	}

	@Override
	public void recalculateCraftingRecipes()
	{
		this.recipes.clear();
		this.providedItems.clear();

		for(RecipeHolder<MixerRecipe> holder : MixerRecipe.RECIPES.getRecipes(Minecraft.getInstance().level))
		{
			MixerRecipe recipe = holder.value();
			for(int iFluid = 0; iFluid < fluids.length; iFluid++)
				if(recipe.fluidOutput.getFluid()==fluids[iFluid])
				{
					int h = (int)Math.ceil(recipe.itemInputs.size()/2f);
					int middle = (int)(h/2f*18);

					PositionedItemStack[] pIngredients = new PositionedItemStack[recipe.itemInputs.size()+2];

					// Fluid input
					List<ItemStack> inputBucket = Arrays.stream(recipe.fluidInput.getFluids())
							.map(fluidStack -> fluidStack.getFluid().getBucket().getDefaultInstance()).toList();
					String inputFraction = FluidUtils.getBucketFraction(recipe.fluidInput.amount());
					pIngredients[0] = new PositionedItemStack(inputBucket, 8, middle-9, inputFraction);

					// Item inputs
					int i = 0;
					for(; i < recipe.itemInputs.size(); i++)
						pIngredients[1+i] = new PositionedItemStack(
								recipe.itemInputs.get(i).getMatchingStacks(),
								32+i%2*18,
								i/2*18
						);

					// Fluid output
					ItemStack outputBucket = recipe.fluidOutput.getFluid().getBucket().getDefaultInstance();
					String outputFraction = FluidUtils.getBucketFraction(recipe.fluidOutput.getAmount());
					pIngredients[++i] = new PositionedItemStack(outputBucket, 86, middle-9, outputFraction);

					this.addProvidedItem(outputBucket);
					this.recipes.add(pIngredients);
					this.arrowPositions.add(new ArrowPosition(69, middle-5));
					if(h*18 > yOff)
						yOff = h*18;
				}
		}
	}
}