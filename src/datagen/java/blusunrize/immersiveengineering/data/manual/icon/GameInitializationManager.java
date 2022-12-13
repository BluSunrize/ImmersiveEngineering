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
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.NamedRenderTypeManager;
import net.minecraftforge.client.model.geometry.GeometryLoaderManager;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

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

		initClient(ForgeRegistries.FLUID_TYPES.get(), FluidType::initializeClient, FluidType.class);
		initClient(ForgeRegistries.ITEMS, Item::initializeClient, Item.class);
		initClient(ForgeRegistries.BLOCKS, Block::initializeClient, Block.class);
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

	private static <R, P> void initClient(IForgeRegistry<R> reg, BiConsumer<R, Consumer<P>> initialize, Class<R> type)
	{
		Field field = ObfuscationReflectionHelper.findField(type, "renderProperties");
		for(R obj : reg.getValues())
			initialize.accept(obj, setter(obj, field));
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
