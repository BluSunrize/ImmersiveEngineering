package blusunrize.immersiveengineering;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.WireType;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.IEWorldGen;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.compat.EE3Helper;
import blusunrize.immersiveengineering.common.util.compat.mfr.MFRHelper;
import blusunrize.immersiveengineering.common.util.compat.minetweaker.MTHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid=ImmersiveEngineering.MODID,name=ImmersiveEngineering.MODNAME,version = ImmersiveEngineering.VERSION, dependencies="after:Railcraft")
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
		Config.init(event);
		IEContent.preInit();

		WireType.cableLossRatio=Config.getDoubleArray("cableLossRatio");
		WireType.cableTransferRate=Config.getIntArray("cableTransferRate");
		WireType.cableColouration=Config.getIntArray("cableColouration");
		WireType.cableLength=Config.getIntArray("cableLength");
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
		if(Loader.isModLoaded("MineFactoryReloaded"))
			MFRHelper.init();
	}
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		if(Loader.isModLoaded("MineTweaker3"))
			MTHelper.init();
		if(Loader.isModLoaded("EE3"))
			EE3Helper.init();
	}
	@Mod.EventHandler
	public void loadComplete(FMLLoadCompleteEvent event)
	{
		IEContent.loadComplete();
		proxy.loadComplete();
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
}