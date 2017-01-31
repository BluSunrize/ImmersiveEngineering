package blusunrize.immersiveengineering;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.IEVillagerTradeHandler;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingThreadHandler;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.util.network.MessageDrill;
import blusunrize.immersiveengineering.common.util.network.MessageMinecartShaderSync;
import blusunrize.immersiveengineering.common.util.network.MessageMineralListSync;
import blusunrize.immersiveengineering.common.util.network.MessageRequestBlockUpdate;
import blusunrize.immersiveengineering.common.util.network.MessageSkyhookSync;
import blusunrize.immersiveengineering.common.util.network.MessageSpeedloaderSync;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
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
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid=ImmersiveEngineering.MODID,name=ImmersiveEngineering.MODNAME,version = ImmersiveEngineering.VERSION, dependencies="after:Railcraft;before:TConstruct;after:ThermalFoundation;after:Avaritia")
public class ImmersiveEngineering
{
	public static final String MODID = "ImmersiveEngineering";
	public static final String MODNAME = "Immersive Engineering";
	public static final String VERSION = "${version}";
	public static final double VERSION_D = .71;

	@Mod.Instance(MODID)
	public static ImmersiveEngineering instance = new ImmersiveEngineering();
	@SidedProxy(clientSide="blusunrize.immersiveengineering.client.ClientProxy", serverSide="blusunrize.immersiveengineering.common.CommonProxy")
	public static CommonProxy proxy;

	public static final SimpleNetworkWrapper packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

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
		IEApi.prefixToIngotMap.put("ingot", new Integer[]{1,1});
		IEApi.prefixToIngotMap.put("nugget", new Integer[]{1,9});
		IEApi.prefixToIngotMap.put("block", new Integer[]{9,1});
		IEApi.prefixToIngotMap.put("plate", new Integer[]{1,1});
		IEApi.prefixToIngotMap.put("gear", new Integer[]{4,1});
		IEApi.prefixToIngotMap.put("rod", new Integer[]{2,1});
		IEApi.prefixToIngotMap.put("fence", new Integer[]{3,2});
		IECompatModule.doModulesPreInit();
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
		Config.setBoolean("ic2Manual", Lib.IC2);
		Config.setBoolean("gregManual", Lib.GREG);
		IECompatModule.doModulesInit();
		int messageId = 0;
		packetHandler.registerMessage(MessageMineralListSync.Handler.class, MessageMineralListSync.class, messageId++, Side.CLIENT);
		packetHandler.registerMessage(MessageTileSync.Handler.class, MessageTileSync.class, messageId++, Side.SERVER);
		packetHandler.registerMessage(MessageSpeedloaderSync.Handler.class, MessageSpeedloaderSync.class, messageId++, Side.CLIENT);
		packetHandler.registerMessage(MessageSkyhookSync.Handler.class, MessageSkyhookSync.class, messageId++, Side.CLIENT);
		packetHandler.registerMessage(MessageMinecartShaderSync.HandlerServer.class, MessageMinecartShaderSync.class, messageId++, Side.SERVER);
		packetHandler.registerMessage(MessageMinecartShaderSync.HandlerClient.class, MessageMinecartShaderSync.class, messageId++, Side.CLIENT);
		packetHandler.registerMessage(MessageRequestBlockUpdate.Handler.class, MessageRequestBlockUpdate.class, messageId++, Side.SERVER);
		packetHandler.registerMessage(MessageDrill.Handler.class, MessageDrill.class, messageId++, Side.CLIENT);
	}
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		IEContent.postInit();
		ExcavatorHandler.recalculateChances(true);
		proxy.postInit();
		new ThreadContributorSpecialsDownloader();
		IECompatModule.doModulesPostInit();
	}
	@Mod.EventHandler
	public void loadComplete(FMLLoadCompleteEvent event)
	{
		IECompatModule.doModulesLoadComplete();
		ShaderRegistry.compileWeight();
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		proxy.serverStarting();
		event.registerServerCommand(new CommandHandler());
		IEVillagerTradeHandler.instance.addShaderTrades();
	}
	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			World world = MinecraftServer.getServer().getEntityWorld();
			if(!world.isRemote)
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
			}
		}
		if(Config.getBoolean("arcfurnace_recycle"))
			ArcRecyclingThreadHandler.doRecipeProfiling();
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
		public static ThreadContributorSpecialsDownloader activeThread;

		public ThreadContributorSpecialsDownloader()
		{
			setName("Immersive Engineering Contributors Thread");
			setDaemon(true);
			start();
			activeThread = this;
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
