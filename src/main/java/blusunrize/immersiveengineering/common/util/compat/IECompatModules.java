/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.config.IECommonConfig;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.compat.computers.cctweaked.ComputerCraftCompatModule;
import com.google.common.collect.Sets;
import dan200.computercraft.api.ComputerCraftAPI;
import mcjty.theoneprobe.TheOneProbe;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Map.Entry;

public final class IECompatModules
{
	private static final Map<String, Class<? extends StandardIECompatModule>> STANDARD_MODULE_CLASSES = new HashMap<>();
	private static final Map<String, Class<? extends EarlyIECompatModule>> EARLY_MODULE_CLASSES = new HashMap<>();
	private static final Set<IECompatModule> LOADED_MODULES = new HashSet<>();

	static
	{
		EARLY_MODULE_CLASSES.put(ComputerCraftAPI.MOD_ID, ComputerCraftCompatModule.class);
		STANDARD_MODULE_CLASSES.put(TheOneProbe.MODID, OneProbeCompatModule.class);
		STANDARD_MODULE_CLASSES.put("create", CreateCompatModule.class);
	}

	public static void onModConstruction(IEventBus modBus)
	{
		constructModules(EARLY_MODULE_CLASSES, modBus);
	}

	private static <T extends IECompatModule>
	void constructModules(Map<String, Class<? extends T>> modules, @Nullable IEventBus modBus)
	{
		for(Entry<String, Class<? extends T>> e : modules.entrySet())
			if(ModList.get().isLoaded(e.getKey()))
				try
				{
					BooleanValue enabled = Objects.requireNonNull(IECommonConfig.compat.get(e.getKey()));
					if(!enabled.getAsBoolean())
						continue;
					IECompatModule module;
					Constructor<? extends T> configConstructor = getConstructorIfExists(e.getValue(), IEventBus.class);
					if(modBus!=null&&configConstructor!=null)
						module = configConstructor.newInstance(modBus);
					else
						module = e.getValue().getConstructor().newInstance();
					LOADED_MODULES.add(module);
				} catch(Exception exception)
				{
					IELogger.logger.error("Compat module for {} could not be preInitialized. Report this and include the error message below!", e.getKey(), exception);
				}
	}

	public static void onCommonSetup()
	{
		constructModules(STANDARD_MODULE_CLASSES, null);
		for(IECompatModule compat : LOADED_MODULES)
			try
			{
				compat.init();
			} catch(Exception exception)
			{
				IELogger.logger.error("Compat module for {} could not be initialized. Report this and include the error message below!", compat, exception);
			}
	}

	public static void doModulesIMCs()
	{
		for(IECompatModule compat : LOADED_MODULES)
			try
			{
				compat.sendIMCs();
			} catch(Exception exception)
			{
				IELogger.logger.error("Compat module for {} could not send IMCs. Report this and include the error message below!", compat, exception);
			}
	}

	public static Collection<String> getAvailableModules()
	{
		return Sets.union(STANDARD_MODULE_CLASSES.keySet(), EARLY_MODULE_CLASSES.keySet());
	}

	@Nullable
	private static <T> Constructor<T> getConstructorIfExists(Class<T> toConstruct, Class<?>... args)
	{
		try
		{
			return toConstruct.getConstructor(args);
		} catch(NoSuchMethodException e)
		{
			return null;
		}
	}

	public static abstract sealed class IECompatModule permits EarlyIECompatModule, StandardIECompatModule
	{
		public void init()
		{
		}

		public void sendIMCs()
		{
		}
	}

	// Created during mod construction, only if the relevant config option is enabled
	public static abstract non-sealed class EarlyIECompatModule extends IECompatModule
	{
	}

	// Created during common setup, only if the relevant config option is enabled
	public static abstract non-sealed class StandardIECompatModule extends IECompatModule
	{
	}
}
