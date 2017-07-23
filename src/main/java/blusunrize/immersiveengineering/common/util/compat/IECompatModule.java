package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CraftTweakerHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

public abstract class IECompatModule
{
	public static HashMap<String, Class<? extends IECompatModule>> moduleClasses = new HashMap<String, Class<? extends IECompatModule>>();
	public static Set<IECompatModule> modules = new HashSet<IECompatModule>();

	public static Consumer<Object> jeiAddFunc = o -> {};
	public static Consumer<Object> jeiRemoveFunc = o -> {};

	static
	{
		moduleClasses.put("actuallyadditions", ActuallyAdditionsHelper.class);
		moduleClasses.put("attaineddrops", AttainedDropsHelper.class);
		moduleClasses.put("baubles", BaublesHelper.class);
		moduleClasses.put("betterwithmods", BetterWithModsHelper.class);
		moduleClasses.put("bloodmagic", BloodMagicHelper.class);
		moduleClasses.put("botania", BotaniaHelper.class);
		moduleClasses.put("chisel", ChiselHelper.class);
		moduleClasses.put("chiselsandbits", ChiselsAndBitsHelper.class);
		moduleClasses.put("cofhcore", CoFHHelper.class);
		moduleClasses.put("crafttweaker", CraftTweakerHelper.class);
		moduleClasses.put("denseores", DenseOresHelper.class);
		moduleClasses.put("enderio", EnderIOHelper.class);
		moduleClasses.put("extrautils2", ExtraUtilsHelper.class);
		moduleClasses.put("forestry", ForestryHelper.class);
		moduleClasses.put("foundry", FoundryHelper.class);
		moduleClasses.put("harvestcraft", HarvestcraftHelper.class);
		moduleClasses.put("ic2", IC2Helper.class);
		moduleClasses.put("mysticalagriculture", MysticalAgricultureHelper.class);
//		moduleClasses.put("opencomputers", OCHelper.class); ToDo: OpenComputers
		moduleClasses.put("theoneprobe", OneProbeHelper.class);
		moduleClasses.put("tconstruct", TConstructHelper.class);
		moduleClasses.put("thermalfoundation", ThermalFoundationHelper.class);
		moduleClasses.put("thaumcraft", ThaumcraftHelper.class);
		moduleClasses.put("railcraft", RailcraftHelper.class);
//		moduleClasses.put("waila", WailaHelper.class); ToDo: WAILA
//		moduleClasses.put("MineFactoryReloaded", MFRHelper.class);
//		moduleClasses.put("EE3", EE3Helper.class);
//		moduleClasses.put("ForgeMicroblock", FMPHelper.class);
//		moduleClasses.put("Forestry", ForestryHelper.class);
//		moduleClasses.put("BackTools", BacktoolsHelper.class);
//		moduleClasses.put("gregtech", GregTechHelper.class);
//		moduleClasses.put("HydCraft", HydCraftHelper.class);
//		moduleClasses.put("ThermalExpansion", ThermalExpansionHelper.class);
//		moduleClasses.put("ThermalDynamics", ThermalDynamicsHelper.class);
//		moduleClasses.put("harvestcraft", HarvestCraftHelper.class);
//		moduleClasses.put("CarpentersBlocks", CarpentersHelper.class);
//		moduleClasses.put("etfuturum", EtFuturumHelper.class);
//		moduleClasses.put("steamcraft2", SteamCraftHelper.class);
//		moduleClasses.put("ExtraUtilities", ExtraUtilsHelper.class);

//		moduleClasses.put("Mekanism", MekanismHelper.class);
//		moduleClasses.put("cuttingedge", CuttingEdgeHelper.class);
//		moduleClasses.put("Avaritia", AvaritiaHelper.class);
	}

	public static void doModulesPreInit()
	{
		for(Entry<String, Class<? extends IECompatModule>> e : moduleClasses.entrySet())
			if(Loader.isModLoaded(e.getKey()))
				try
				{
					//IC2 Classic is not supported.
					if("IC2".equals(e.getKey())&&Loader.isModLoaded("IC2-Classic-Spmod"))
						continue;

					Boolean enabled = Config.IEConfig.compat.get(e.getKey());
					if(enabled==null||!enabled.booleanValue())
						continue;
					IECompatModule m = e.getValue().newInstance();
					modules.add(m);
					m.preInit();
				} catch(Exception exception)
				{
					IELogger.error("Compat module for "+e.getKey()+" could not be preInitialized. Report this!");
				}
	}
	public static void doModulesInit()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try{
				compat.init();
			}catch (Exception exception){
				IELogger.error("Compat module for "+compat+" could not be initialized");
			}
	}
	public static void doModulesPostInit()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try{
				compat.postInit();
			}catch (Exception exception){
				IELogger.error("Compat module for "+compat+" could not be postInitialized");
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
				try{
					compat.loadComplete();
				}catch (Exception exception){
					IELogger.error("Compat module for "+compat+" could not be initialized");
					exception.printStackTrace();
				}
		}
	}

	public abstract void preInit();
	public abstract void init();
	public abstract void postInit();
	public void loadComplete(){}
	@SideOnly(Side.CLIENT)
	public void clientPreInit(){}
	@SideOnly(Side.CLIENT)
	public void clientInit(){}
	@SideOnly(Side.CLIENT)
	public void clientPostInit(){}
}
