/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.config.IECommonConfig;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.fml.ModList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public abstract class IECompatModule
{
	public static HashMap<String, Class<? extends IECompatModule>> moduleClasses = new HashMap<>();
	public static Set<IECompatModule> modules = new HashSet<>();

	static
	{
		moduleClasses.put("computercraft", ComputerCraftCompatModule.class);
		moduleClasses.put("theoneprobe", OneProbeCompatModule.class);
	}

	public static void doModulesPreInit()
	{
		for(Entry<String, Class<? extends IECompatModule>> e : moduleClasses.entrySet())
			if(ModList.get().isLoaded(e.getKey()))
				try
				{
					//IC2 Classic is not supported.
					if("ic2".equals(e.getKey())&&ModList.get().isLoaded("ic2-classic-spmod"))
						continue;

					BooleanValue enabled = IECommonConfig.compat.get(e.getKey());
					if(enabled==null||!enabled.get())
						continue;
					IECompatModule m = e.getValue().newInstance();
					modules.add(m);
					m.preInit();
				} catch(Exception exception)
				{
					IELogger.logger.error("Compat module for "+e.getKey()+" could not be preInitialized. Report this and include the error message below!", exception);
				}
	}

	public static void doModulesRecipes()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try
			{
				compat.registerRecipes();
			} catch(Exception exception)
			{
				IELogger.logger.error("Compat module for "+compat+" could not register recipes. Report this and include the error message below!", exception);
			}
	}

	public static void doModulesInit()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try
			{
				compat.init();
			} catch(Exception exception)
			{
				IELogger.logger.error("Compat module for "+compat+" could not be initialized. Report this and include the error message below!", exception);
			}
	}

	public static void doModulesPostInit()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try
			{
				compat.postInit();
			} catch(Exception exception)
			{
				IELogger.logger.error("Compat module for "+compat+" could not be postInitialized. Report this and include the error message below!", exception);
			}
	}

	//We don't want this to happen multiple times after all >_>
	public static boolean serverStartingDone = false;

	public static void doModulesLoadComplete()
	{
		if(!serverStartingDone)
		{
			serverStartingDone = true;
			for(IECompatModule compat : IECompatModule.modules)
				try
				{
					compat.loadComplete();
				} catch(Exception exception)
				{
					IELogger.logger.error("Compat module for "+compat+" could not be initialized. Report this and include the error message below!", exception);
				}
		}
	}

	public abstract void preInit();

	public abstract void registerRecipes();

	public abstract void init();

	public abstract void postInit();

	public void loadComplete()
	{
	}

	@OnlyIn(Dist.CLIENT)
	public void clientPreInit()
	{
	}

	@OnlyIn(Dist.CLIENT)
	public void clientInit()
	{
	}

	@OnlyIn(Dist.CLIENT)
	public void clientPostInit()
	{
	}
}
