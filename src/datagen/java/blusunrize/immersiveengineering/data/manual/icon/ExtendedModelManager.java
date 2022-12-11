/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.manual.icon;


import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ExtendedModelManager extends ModelManager
{
	ExtendedModelManager(
			final TextureManager textureManagerIn,
			final BlockColors blockColorsIn,
			final int maxMipmapLevelIn)
	{
		super(textureManagerIn, blockColorsIn, maxMipmapLevelIn);
	}

	void loadModels() throws ClassNotFoundException
	{
		final var resourceManager = (ReloadableResourceManager)Minecraft.getInstance().getResourceManager();
		final var instance = SimpleReloadInstance.of(
				resourceManager, List.of(this), Runnable::run, Runnable::run, CompletableFuture.completedFuture(Unit.INSTANCE)
		);
		instance.done().join();
	}
}
