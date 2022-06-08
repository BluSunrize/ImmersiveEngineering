/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */


package blusunrize.immersiveengineering.data.manual.icon;

import blusunrize.immersiveengineering.client.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.ModelLoaderRegistry.VanillaProxy;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GameInitializationManager
{
	private static final GameInitializationManager INSTANCE = new GameInitializationManager();

	public static GameInitializationManager getInstance()
	{
		return INSTANCE;
	}

	private boolean initialized = false;

	private GameInitializationManager()
	{
	}

	public void initialize(final ExistingFileHelper existingFileHelper, DataGenerator gen)
	{
		if(initialized)
			return;

		initialized = true;

		GLFWInitializationManager.getInstance().initialize();
		MinecraftInstanceManager.getInstance().initialize(existingFileHelper, gen);
		ClientProxy.initWithMC();
		// TODO can the Forge method be made to work in this context?
		//  ModelLoaderRegistry.init();
		ModelLoaderRegistry.registerLoader(new ResourceLocation("minecraft", "elements"), VanillaProxy.Loader.INSTANCE);
		ModelLoaderRegistry.registerLoader(new ResourceLocation("forge", "obj"), OBJLoader.INSTANCE);
		ModelLoaderRegistry.registerLoader(new ResourceLocation("forge", "bucket"), DynamicBucketModel.Loader.INSTANCE);
		ModelLoaderRegistry.registerLoader(new ResourceLocation("forge", "composite"), CompositeModel.Loader.INSTANCE);
		ModelLoaderRegistry.registerLoader(new ResourceLocation("forge", "multi-layer"), MultiLayerModel.Loader.INSTANCE);
		ModelLoaderRegistry.registerLoader(new ResourceLocation("forge", "item-layers"), ItemLayerModel.Loader.INSTANCE);
		ModelLoaderRegistry.registerLoader(new ResourceLocation("forge", "separate-perspective"), SeparatePerspectiveModel.Loader.INSTANCE);

		final ExtendedModelManager extendedModelManager = (ExtendedModelManager)Minecraft.getInstance().getModelManager();
		extendedModelManager.loadModels();
	}
}
