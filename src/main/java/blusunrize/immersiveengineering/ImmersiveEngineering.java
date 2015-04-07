package blusunrize.immersiveengineering;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IEWorldGen;
import blusunrize.immersiveengineering.common.util.Lib;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

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
	}
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}
	@Mod.EventHandler
	public void loadComplete(FMLLoadCompleteEvent event)
	{
		IEContent.loadComplete();
	}
	@Mod.EventHandler
	public void serverStart(FMLServerAboutToStartEvent event)
	{
		proxy.serverStart();
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