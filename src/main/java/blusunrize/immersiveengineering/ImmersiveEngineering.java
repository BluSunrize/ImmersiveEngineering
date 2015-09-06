package blusunrize.immersiveengineering;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.world.IEWorldGen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid=ImmersiveEngineering.MODID,name=ImmersiveEngineering.MODNAME,version = ImmersiveEngineering.VERSION, dependencies="after:Railcraft;before:TConstruct")
public class ImmersiveEngineering
{
	public static final String MODID = "ImmersiveEngineering";
	public static final String MODNAME = "Immersive Engineering";
	public static final String VERSION = "${version}";

	@Mod.Instance(MODID)
	public static ImmersiveEngineering instance = new ImmersiveEngineering();
	@SidedProxy(clientSide="blusunrize.immersiveengineering.client.ClientProxy", serverSide="blusunrize.immersiveengineering.common.CommonProxy")
	public static CommonProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		IELogger.debug = VERSION.startsWith("${");
		Config.init(event);
		IEContent.preInit();

		WireType.cableLossRatio=Config.getDoubleArray("cableLossRatio");
		WireType.cableTransferRate=Config.getIntArray("cableTransferRate");
		WireType.cableColouration=Config.getIntArray("cableColouration");
		WireType.cableLength=Config.getIntArray("cableLength");

		for(int b : Config.getIntArray("oreDimBlacklist"))
			IEWorldGen.oreDimBlacklist.add(b);
		IEApi.modPreference = Arrays.asList(Config.getStringArray("preferredOres"));
	}
	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		IEContent.init();

		GameRegistry.registerWorldGenerator(new IEWorldGen(), 0);
		MinecraftForge.EVENT_BUS.register(new EventHandler());
		FMLCommonHandler.instance().bus().register(new EventHandler());
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
		proxy.init();


		Lib.IC2 = Loader.isModLoaded("IC2") && Config.getBoolean("ic2compat");
		Lib.GREG = Loader.isModLoaded("gregtech") && Config.getBoolean("gregtechcompat");
		for(IECompatModule compat : IECompatModule.modules)
			if(Loader.isModLoaded(compat.modId))
				compat.init();
	}
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		IERecipes.postInitCrusherAndArcRecipes();
		for(IECompatModule compat : IECompatModule.modules)
			if(Loader.isModLoaded(compat.modId))
				compat.postInit();
	}
	@Mod.EventHandler
	public void loadComplete(FMLLoadCompleteEvent event)
	{
		ExcavatorHandler.recalculateChances();
		IEContent.loadComplete();
		proxy.loadComplete();

		new ThreadContributorSpecialsDownloader();
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandHandler());	
	}
	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
		if(ImmersiveNetHandler.INSTANCE==null)
			ImmersiveNetHandler.INSTANCE = new ImmersiveNetHandler();
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			World world = MinecraftServer.getServer().getEntityWorld();
			if(!world.isRemote && !IESaveData.loaded)
			{
				IELogger.info("WorldData loading");
				IESaveData worldData = (IESaveData) world.loadItemData(IESaveData.class, IESaveData.dataName);
				if(worldData==null)
				{
					IELogger.info("WorldData not found");
					worldData = new IESaveData(IESaveData.dataName);
					world.setItemData(IESaveData.dataName, worldData);
				}
				else
					IELogger.info("WorldData retrieved");
				IESaveData.setInstance(world.provider.dimensionId, worldData);
				IESaveData.loaded = true;
			}
		}
	}

	public static CreativeTabs creativeTab = new CreativeTabs(MODID)
	{
		@Override
		public Item getTabIconItem()
		{
			return null;
		}
		@Override
		public ItemStack getIconItemStack()
		{
			return new ItemStack(IEContent.blockMetalDevice,1,1);
		}
	};


	public static class ThreadContributorSpecialsDownloader extends Thread
	{
		public ThreadContributorSpecialsDownloader()
		{
			setName("Immersive Engineering Contributors Thread");
			setDaemon(true);
			start();
		}

		@Override
		public void run()
		{
			Gson gson = new Gson();
			try {
				IELogger.info("Attempting to download special revolvers from GitHub");
				URL url = new URL("https://raw.githubusercontent.com/BluSunrize/ImmersiveEngineering/master/contributorRevolvers.json");
				JsonStreamParser parser = new JsonStreamParser(new InputStreamReader(url.openStream()));
				while(parser.hasNext())
				{
					try{
						JsonElement je = parser.next();
						ItemRevolver.SpecialRevolver revolver = gson.fromJson(je, ItemRevolver.SpecialRevolver.class);
						if(revolver!=null)
						{
							if(revolver.uuid!=null)
								for(String uuid : revolver.uuid)
									ItemRevolver.specialRevolvers.put(uuid, revolver);
							ItemRevolver.specialRevolversByTag.put(!revolver.tag.isEmpty()?revolver.tag:revolver.flavour, revolver);
						}
					}catch(Exception excepParse)
					{
						IELogger.warn("Error on parsing a SpecialRevolver");
					}
				}
			} catch(Exception e) {
				IELogger.info("Could not load contributor+special revolver list.");
				e.printStackTrace();
			}
		}
	}
}