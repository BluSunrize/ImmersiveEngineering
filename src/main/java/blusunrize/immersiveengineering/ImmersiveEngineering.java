package blusunrize.immersiveengineering;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.common.*;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingThreadHandler;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.util.network.*;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

@Mod(modid = ImmersiveEngineering.MODID, name = ImmersiveEngineering.MODNAME, version = ImmersiveEngineering.VERSION,
		dependencies = "required-after:forge@[14.22.0.2447,);after:jei@[4.7,);after:railcraft;after:tconstruct@[1.12-2.7.1,);after:theoneprobe@[1.4.4,)",
		certificateFingerprint = "Blu's Key")
public class ImmersiveEngineering
{
	public static final String MODID = "immersiveengineering";
	public static final String MODNAME = "Immersive Engineering";
	public static final String VERSION = "${version}";

	@Mod.Instance(MODID)
	public static ImmersiveEngineering instance = new ImmersiveEngineering();
	@SidedProxy(clientSide="blusunrize.immersiveengineering.client.ClientProxy", serverSide="blusunrize.immersiveengineering.common.CommonProxy")
	public static CommonProxy proxy;

	public static final SimpleNetworkWrapper packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

	static
	{
		FluidRegistry.enableUniversalBucket();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		IELogger.logger = event.getModLog();
		Config.preInit(event);
		IEContent.preInit();
		proxy.preInit();

		IEAdvancements.preInit();

		WireType.wireLossRatio = IEConfig.wireLossRatio;
		WireType.wireTransferRate = IEConfig.wireTransferRate;
		WireType.wireColouration = IEConfig.wireColouration;
		WireType.wireLength = IEConfig.wireLength;

		for(int b : IEConfig.Ores.oreDimBlacklist)
			IEWorldGen.oreDimBlacklist.add(b);
		IEApi.modPreference = Arrays.asList(IEConfig.preferredOres);
		IEApi.prefixToIngotMap.put("ingot", new Integer[]{1,1});
		IEApi.prefixToIngotMap.put("nugget", new Integer[]{1,9});
		IEApi.prefixToIngotMap.put("block", new Integer[]{9,1});
		IEApi.prefixToIngotMap.put("plate", new Integer[]{1,1});
		IEApi.prefixToIngotMap.put("wire", new Integer[]{1,1});
		IEApi.prefixToIngotMap.put("gear", new Integer[]{4,1});
		IEApi.prefixToIngotMap.put("rod", new Integer[]{2,1});
		IEApi.prefixToIngotMap.put("fence", new Integer[]{5,3});
		IECompatModule.doModulesPreInit();

		new ThreadContributorSpecialsDownloader();

		IEContent.preInitEnd();
	}
	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.preInitEnd();
		IEContent.init();
		IEWorldGen ieWorldGen = new IEWorldGen();
		GameRegistry.registerWorldGenerator(ieWorldGen, 0);
		MinecraftForge.EVENT_BUS.register(ieWorldGen);

		MinecraftForge.EVENT_BUS.register(new EventHandler());
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
		proxy.init();

		IESounds.init();

//		Lib.IC2 = Loader.isModLoaded("IC2") && Config.getBoolean("ic2compat");
//		Lib.GREG = Loader.isModLoaded("gregtech") && Config.getBoolean("gregtechcompat");
//		Config.setBoolean("ic2Manual", Lib.IC2);
//		Config.setBoolean("gregManual", Lib.GREG);
		IECompatModule.doModulesInit();
		proxy.initEnd();
		int messageId = 0;
		packetHandler.registerMessage(MessageMineralListSync.Handler.class, MessageMineralListSync.class, messageId++, Side.CLIENT);
		packetHandler.registerMessage(MessageTileSync.HandlerServer.class, MessageTileSync.class, messageId++, Side.SERVER);
		packetHandler.registerMessage(MessageTileSync.HandlerClient.class, MessageTileSync.class, messageId++, Side.CLIENT);
		packetHandler.registerMessage(MessageSpeedloaderSync.Handler.class, MessageSpeedloaderSync.class, messageId++, Side.CLIENT);
		packetHandler.registerMessage(MessageSkyhookSync.Handler.class, MessageSkyhookSync.class, messageId++, Side.CLIENT);
		packetHandler.registerMessage(MessageMinecartShaderSync.HandlerServer.class, MessageMinecartShaderSync.class, messageId++, Side.SERVER);
		packetHandler.registerMessage(MessageMinecartShaderSync.HandlerClient.class, MessageMinecartShaderSync.class, messageId++, Side.CLIENT);
		packetHandler.registerMessage(MessageRequestBlockUpdate.Handler.class, MessageRequestBlockUpdate.class, messageId++, Side.SERVER);
		packetHandler.registerMessage(MessageNoSpamChatComponents.Handler.class, MessageNoSpamChatComponents.class, messageId++, Side.CLIENT);
		packetHandler.registerMessage(MessageShaderManual.HandlerServer.class, MessageShaderManual.class, messageId++, Side.SERVER);
		packetHandler.registerMessage(MessageShaderManual.HandlerClient.class, MessageShaderManual.class, messageId++, Side.CLIENT);
		packetHandler.registerMessage(MessageBirthdayParty.HandlerClient.class, MessageBirthdayParty.class, messageId++, Side.CLIENT);
		packetHandler.registerMessage(MessageMagnetEquip.Handler.class, MessageMagnetEquip.class, messageId++, Side.SERVER);
		packetHandler.registerMessage(MessageChemthrowerSwitch.Handler.class, MessageChemthrowerSwitch.class, messageId++, Side.SERVER);

		for(FMLInterModComms.IMCMessage message : FMLInterModComms.fetchRuntimeMessages(instance))
		{
			if(message.key.equals("fluidpipeCover") && message.isFunctionMessage())
			{
				Optional<Function<ItemStack, Boolean>> opFunc = message.getFunctionValue(ItemStack.class, Boolean.class);
				if(opFunc.isPresent())
					TileEntityFluidPipe.validPipeCovers.add(opFunc.get());
			}
			else if(message.key.equals("fluidpipeCoverClimb") && message.isFunctionMessage())
			{
				Optional<Function<ItemStack, Boolean>> opFunc = message.getFunctionValue(ItemStack.class, Boolean.class);
				if(opFunc.isPresent())
					TileEntityFluidPipe.climbablePipeCovers.add(opFunc.get());
			}
		}
	}
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		IEContent.postInit();
		ExcavatorHandler.recalculateChances(true);
		proxy.postInit();
		IECompatModule.doModulesPostInit();
		proxy.postInitEnd();
		ShaderRegistry.compileWeight();
	}
	@Mod.EventHandler
	public void loadComplete(FMLLoadCompleteEvent event)
	{
		IECompatModule.doModulesLoadComplete();
	}
	@Mod.EventHandler
	public void modIDMapping(FMLModIdMappingEvent event)
	{
	}

	private final static String[] alternativeCerts = {
			"7e11c175d1e24007afec7498a1616bef0000027d",// malte0811
			"MavenKeyHere"//TODO maven
	};

	@Mod.EventHandler
	public void wrongSignature(FMLFingerprintViolationEvent event)
	{
		IELogger.error("THIS IS NOT AN OFFICIAL BUILD OF IMMERSIVE ENGINEERING! Found these fingerprints: "+event.getFingerprints());
		for (String altCert:alternativeCerts)
			if (event.getFingerprints().contains(altCert))
			{
				IELogger.error(altCert+" is considered an alternative certificate (which may be ok to use in some cases). " +
						"If you thought this was an official build you probably shouldn't use it.");
				break;
			}
	}



	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		proxy.serverStarting();
		event.registerServerCommand(new CommandHandler(false));
		if(IEConfig.Machines.arcfurnace_recycle)
			ArcRecyclingThreadHandler.doRecipeProfiling();
	}
	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
			if(!world.isRemote)
			{
				IELogger.info("WorldData loading");

				IESaveData worldData = (IESaveData) world.loadData(IESaveData.class, IESaveData.dataName);
				if(worldData==null)
				{
					IELogger.info("WorldData not found");
					worldData = new IESaveData(IESaveData.dataName);
					world.setData(IESaveData.dataName, worldData);
				}
				else
					IELogger.info("WorldData retrieved");
				IESaveData.setInstance(world.provider.getDimension(), worldData);
			}
		}
		IEContent.refreshFluidReferences();
	}

//	public static Item registerItem(Item item, String name)
//	{
//		ForgeRegistries.ITEMS.register(item.setRegistryName(new ResourceLocation(MODID+":"+name)));
//		return item;
//	}
//	public static Item registerItemByFullName(Item item, String name)
//	{
//		ForgeRegistries.ITEMS.register(item.setRegistryName(new ResourceLocation(name)));
//		return item;
//	}
//	public static Block registerBlockByFullName(Block block, ItemBlock itemBlock, String name)
//	{
//		ResourceLocation rl = new ResourceLocation(name);
//		ForgeRegistries.BLOCKS.register(block.setRegistryName(rl));
//		ForgeRegistries.ITEMS.register(itemBlock.setRegistryName(rl));
//		return block;
//	}
//	public static Block registerBlockByFullName(Block block, Class<? extends ItemBlock> itemBlock, String name)
//	{
//		try{
//			return registerBlockByFullName(block, itemBlock.getConstructor(Block.class).newInstance(block), name);
//		}catch(Exception e){e.printStackTrace();}
//		return null;
//	}
//	public static Block registerBlock(Block block, Class<? extends ItemBlock> itemBlock, String name)
//	{
//		try{
//			return registerBlockByFullName(block, itemBlock.getConstructor(Block.class).newInstance(block), MODID+":"+name);
//		}catch(Exception e){e.printStackTrace();}
//		return null;
//	}


	public static CreativeTabs creativeTab = new CreativeTabs(MODID)
	{
		@Override
		public ItemStack getTabIconItem()
		{
			return ItemStack.EMPTY;
		}
		@Override
		public ItemStack getIconItemStack()
		{
			return new ItemStack(IEContent.blockMetalDecoration0,1,0);
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
