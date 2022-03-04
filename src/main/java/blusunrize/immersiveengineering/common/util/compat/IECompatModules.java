/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.config.CachedConfig.BooleanValue;
import blusunrize.immersiveengineering.common.config.IECommonConfig;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.compat.computers.cctweaked.ComputerCraftCompatModule;
import blusunrize.immersiveengineering.common.util.compat.computers.oc2.OC2CompatModule;
import com.google.common.collect.Sets;
import li.cil.oc2.api.API;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;

public final class IECompatModules
{
	private static final Map<String, Class<? extends StandardIECompatModule>> STANDARD_MODULE_CLASSES = new HashMap<>();
	private static final Map<String, Class<? extends EarlyIECompatModule>> EARLY_MODULE_CLASSES = new HashMap<>();
	private static final Set<IECompatModule> LOADED_MODULES = new HashSet<>();

	static
	{
		STANDARD_MODULE_CLASSES.put("computercraft", ComputerCraftCompatModule.class);
		STANDARD_MODULE_CLASSES.put("curios", CuriosCompatModule.class);
		STANDARD_MODULE_CLASSES.put("theoneprobe", OneProbeCompatModule.class);
		//TODO double-check that this is inlined!
		EARLY_MODULE_CLASSES.put(API.MOD_ID, OC2CompatModule.class);
	}

	public static void onModConstruction()
	{
		constructModules(EARLY_MODULE_CLASSES, $ -> true);
	}

	private static <T extends IECompatModule>
	void constructModules(Map<String, Class<? extends T>> modules, Predicate<BooleanValue> shouldLoad)
	{
		for(Entry<String, Class<? extends T>> e : modules.entrySet())
			if(ModList.get().isLoaded(e.getKey()))
				try
				{
					BooleanValue enabled = Objects.requireNonNull(IECommonConfig.compat.get(e.getKey()));
					if(!shouldLoad.test(enabled))
						continue;
					IECompatModule module;
					Constructor<? extends T> configConstructor = getConstructorIfExists(e.getValue(), BooleanValue.class);
					if(configConstructor!=null)
						module = configConstructor.newInstance(enabled);
					else
						module = e.getValue().getConstructor().newInstance();
					LOADED_MODULES.add(module);
				} catch(Exception exception)
				{
					IELogger.logger.error("Compat module for "+e.getKey()+" could not be preInitialized. Report this and include the error message below!", exception);
				}
	}

	public static void onCommonSetup()
	{
		constructModules(STANDARD_MODULE_CLASSES, BooleanValue::get);
		for(IECompatModule compat : LOADED_MODULES)
			try
			{
				compat.init();
			} catch(Exception exception)
			{
				IELogger.logger.error("Compat module for "+compat+" could not be initialized. Report this and include the error message below!", exception);
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
				IELogger.logger.error("Compat module for "+compat+" could not send IMCs. Report this and include the error message below!", exception);
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

	// Created during mod construction even if the relevant config option is disabled (since the config is not loaded
	// at that point)
	public static abstract non-sealed class EarlyIECompatModule extends IECompatModule
	{
	}

	// Created during common setup, only if the relevant config option is enabled
	public static abstract non-sealed class StandardIECompatModule extends IECompatModule
	{
	}
}
