/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;

import javax.annotation.Nonnull;

public class RecipeCachingReloadListener implements IResourceManagerReloadListener
{
	private final DataPackRegistries dataPackRegistries;

	public RecipeCachingReloadListener(DataPackRegistries dataPackRegistries)
	{
		this.dataPackRegistries = dataPackRegistries;
	}

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
	{
		RecipeReloadListener.buildRecipeLists(dataPackRegistries.getRecipeManager());
	}
}
