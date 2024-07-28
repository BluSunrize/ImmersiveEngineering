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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.NamedRenderTypeManager;
import net.neoforged.neoforge.client.model.geometry.GeometryLoaderManager;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

		initClient(NeoForgeRegistries.FLUID_TYPES, FluidType::initializeClient, FluidType.class);
		initClient(BuiltInRegistries.ITEM, Item::initializeClient, Item.class);
		initClient(BuiltInRegistries.BLOCK, Block::initializeClient, Block.class);
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

	private static <R, P> void initClient(Registry<R> reg, BiConsumer<R, Consumer<P>> initialize, Class<R> type)
	{
		Field field = ObfuscationReflectionHelper.findField(type, "renderProperties");
		reg.stream().forEach(obj -> initialize.accept(obj, setter(obj, field)));
	}

	private static <T> Consumer<T> setter(Object owner, Field toSet)
	{
		return t -> {
			try
			{
				toSet.set(owner, t);
			} catch(IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
		};
	}
}
