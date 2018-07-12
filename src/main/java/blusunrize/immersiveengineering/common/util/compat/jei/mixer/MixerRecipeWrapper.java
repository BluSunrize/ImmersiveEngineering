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
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import net.minecraft.client.Minecraft;

public class MixerRecipeWrapper extends MultiblockRecipeWrapper
{
	public boolean potionWrapper = false;

	public MixerRecipeWrapper(MixerRecipe recipe)
	{
		super(recipe);
		if(recipe instanceof MixerRecipePotion)
		{
			potionWrapper = true;
			((MixerRecipePotion)recipe).getAlternateInputs().forEach(alternate -> {
				fluidInputs.add(alternate.getLeft());
				recipeInputs[0].add(alternate.getRight()[0].getExampleStack());
			});
		}
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
	}
}