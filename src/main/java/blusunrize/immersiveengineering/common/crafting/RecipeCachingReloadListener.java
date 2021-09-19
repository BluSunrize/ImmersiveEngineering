/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import javax.annotation.Nonnull;
import net.minecraft.server.ServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class RecipeCachingReloadListener implements ResourceManagerReloadListener
{
	private final ServerResources dataPackRegistries;

	public RecipeCachingReloadListener(ServerResources dataPackRegistries)
	{
		this.dataPackRegistries = dataPackRegistries;
	}

	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager resourceManager)
	{
		RecipeReloadListener.buildRecipeLists(dataPackRegistries.getRecipeManager());
	}
}
