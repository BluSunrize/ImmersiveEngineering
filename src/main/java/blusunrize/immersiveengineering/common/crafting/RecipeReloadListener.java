/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import javax.annotation.Nonnull;
import java.util.Collection;

public class RecipeReloadListener implements ResourceManagerReloadListener
{
	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager resourceManager)
	{
		/*TODO
		startArcRecyclingRecipeGen(serverResources.getRecipeManager(), serverResources.getTags());
		 */
	}

	private void startArcRecyclingRecipeGen(RecipeManager recipeManager, RegistryAccess tags)
	{
		Collection<Recipe<?>> recipes = recipeManager.getRecipes();
		new ArcRecyclingCalculator(recipes, tags).run();
	}
}
