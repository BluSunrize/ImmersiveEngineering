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
