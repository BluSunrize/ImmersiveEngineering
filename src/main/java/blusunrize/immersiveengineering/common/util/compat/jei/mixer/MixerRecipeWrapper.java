/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.mixer;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.crafting.MixerRecipePotion;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class MixerRecipeWrapper extends MultiblockRecipeWrapper
{
	public boolean potionWrapper = false;
	public MixerRecipeWrapper(MixerRecipe recipe)
	{
		super(recipe);
		if(recipe instanceof MixerRecipePotion)
		{
			potionWrapper = true;
			recipeInputs = new List[]{new ArrayList()};
			fluidOutputs.clear();
			for(PotionHelper.MixPredicate<PotionType> mixPredicate : PotionHelper.POTION_TYPE_CONVERSIONS)
				if(mixPredicate.input==((MixerRecipePotion)recipe).inputPotionType)
					for(ItemStack potionIngred : JEIHelper.modRegistry.getIngredientRegistry().getPotionIngredients())
						if(mixPredicate.reagent.apply(potionIngred))
						{
							recipeInputs[0].add(potionIngred);
							fluidOutputs.add(MixerRecipePotion.getFluidStackForType(mixPredicate.output, recipe.fluidAmount));
						}

		}
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		for(int i=0; i<recipeInputs.length; i++)
		{
			int x = 0+(i%2)*18;
			int y = 0+i/2*18;
			JEIHelper.slotDrawable.draw(minecraft, x, y);
		}
	}
}