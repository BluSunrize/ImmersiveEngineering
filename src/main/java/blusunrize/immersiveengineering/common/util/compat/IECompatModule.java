package blusunrize.immersiveengineering.common.util.compat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.compat.hydcraft.HydCraftHelper;
import blusunrize.immersiveengineering.common.util.compat.mfr.MFRHelper;
import blusunrize.immersiveengineering.common.util.compat.minetweaker.MTHelper;
import blusunrize.immersiveengineering.common.util.compat.waila.WailaHelper;
import cpw.mods.fml.common.Loader;

public abstract class IECompatModule
{
	public static HashMap<String, Class<? extends IECompatModule>> moduleClasses = new HashMap<String, Class<? extends IECompatModule>>();
	public static Set<IECompatModule> modules = new HashSet<IECompatModule>();

	static
	{
		moduleClasses.put("MineFactoryReloaded", MFRHelper.class);
		moduleClasses.put("MineTweaker3", MTHelper.class);
		moduleClasses.put("denseores", DenseOresHelper.class);
		moduleClasses.put("EE3", EE3Helper.class);
		moduleClasses.put("ForgeMicroblock", FMPHelper.class);
		moduleClasses.put("Forestry", ForestryHelper.class);
		moduleClasses.put("BackTools", BacktoolsHelper.class);
		moduleClasses.put("Waila", WailaHelper.class);
		moduleClasses.put("gregtech", GregTechHelper.class);
		moduleClasses.put("HydCraft", HydCraftHelper.class);
		moduleClasses.put("ThermalExpansion", TEHelper.class);
		moduleClasses.put("chisel", ChiselHelper.class);
	}

	public static void preInit()
	{
		for(Entry<String, Class<? extends IECompatModule>> e : moduleClasses.entrySet())
			if(Loader.isModLoaded(e.getKey()))
				try{
					modules.add(e.getValue().newInstance());
				}catch (Exception exception){
					IELogger.error("Compat module for "+e.getKey()+" could not be initialized. Report this!");
				}
	}

	public abstract void init();
	public abstract void postInit();
}
