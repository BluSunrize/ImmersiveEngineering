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
import net.minecraft.data.PackOutput;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.NamedRenderTypeManager;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.geometry.GeometryLoaderManager;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.lang.reflect.InvocationTargetException;

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

	@SuppressWarnings("UnstableApiUsage")
	public void initialize(final ExistingFileHelper existingFileHelper, PackOutput output)
	{
		if(initialized)
			return;

		initialized = true;

		try
		{
			var clientExtensionEventConstructor = RegisterClientExtensionsEvent.class.getDeclaredConstructor();
			clientExtensionEventConstructor.setAccessible(true);
			ModLoader.postEvent(clientExtensionEventConstructor.newInstance());
		} catch(NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
		GLFWInitializationManager.getInstance().initialize();
		MinecraftInstanceManager.getInstance().initialize(existingFileHelper, output);
		ClientProxy.initWithMC();
		GeometryLoaderManager.init();
		NamedRenderTypeManager.init();

		try
		{
			final ExtendedModelManager extendedModelManager = (ExtendedModelManager)Minecraft.getInstance().getModelManager();
			extendedModelManager.loadModels();
		} catch(ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
}
