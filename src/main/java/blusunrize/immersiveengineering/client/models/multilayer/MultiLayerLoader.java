/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.multilayer;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import javax.annotation.Nonnull;

public class MultiLayerLoader implements ICustomModelLoader
{
	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
	{
	}

	private static final ResourceLocation LOCATION = new ResourceLocation(ImmersiveEngineering.MODID,
			"models/block/multilayer");

	@Override
	public boolean accepts(@Nonnull ResourceLocation modelLocation)
	{
		return LOCATION.equals(modelLocation);
	}

	@Nonnull
	@Override
	public IModel loadModel(@Nonnull ResourceLocation modelLocation) throws Exception
	{
		return MultiLayerModel.INSTANCE;
	}
}
