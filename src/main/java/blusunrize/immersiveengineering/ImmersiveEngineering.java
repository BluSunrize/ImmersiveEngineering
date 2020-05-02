/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.*;
import blusunrize.immersiveengineering.common.crafting.IngredientSerializers;
import blusunrize.immersiveengineering.common.crafting.RecipeReloadListener;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.network.*;
import blusunrize.immersiveengineering.common.util.IEIMCHandler;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import blusunrize.immersiveengineering.common.world.Villages;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Function;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

@Mod(ImmersiveEngineering.MODID)
public class ImmersiveEngineering
{
	public static final String MODID = "immersiveengineering";
	public static final String MODNAME = "Immersive Engineering";
	public static final String VERSION = "${version}";
	public static final String NETWORK_VERSION = "1";
	@SuppressWarnings("Convert2MethodRef")
	public static CommonProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(),
			() -> () -> new CommonProxy());

	public static final SimpleChannel packetHandler = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(MODID, "main"))
			.networkProtocolVersion(() -> NETWORK_VERSION)
			.serverAcceptedVersions(NETWORK_VERSION::equals)
			.clientAcceptedVersions(NETWORK_VERSION::equals)
			.simpleChannel();

	public ImmersiveEngineering()
	{
		IELogger.logger = LogManager.getLogger(MODID);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::wrongSignature);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
		MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
		RecipeSerializers.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
		Villages.Registers.POINTS_OF_INTEREST.register(FMLJavaModLoadingContext.get().getModEventBus());
		Villages.Registers.PROFESSIONS.register(FMLJavaModLoadingContext.get().getModEventBus());
		//TODO separate client/server config?
		ModLoadingContext.get().registerConfig(Type.COMMON, IEConfig.ALL);
		IEContent.modConstruction();
		proxy.modConstruction();
		//TODO FluidRegistry.enableUniversalBucket();
		IngredientSerializers.init();
	}

	public void setup(FMLCommonSetupEvent event)
	{
		//Previously in PREINIT

		proxy.preInit();

		IEAdvancements.preInit();


		for(String b : IEConfig.ORES.oreDimBlacklist.get())
			IEWorldGen.oreDimBlacklist.add(new ResourceLocation(b));
		IEApi.modPreference = IEConfig.GENERAL.preferredOres.get();
		IEApi.prefixToIngotMap.put("ingot", new Integer[]{1, 1});
		IEApi.prefixToIngotMap.put("nugget", new Integer[]{1, 9});
		IEApi.prefixToIngotMap.put("block", new Integer[]{9, 1});
		IEApi.prefixToIngotMap.put("plate", new Integer[]{1, 1});
		IEApi.prefixToIngotMap.put("wire", new Integer[]{1, 1});
		IEApi.prefixToIngotMap.put("gear", new Integer[]{4, 1});
		IEApi.prefixToIngotMap.put("rod", new Integer[]{2, 1});
		IEApi.prefixToIngotMap.put("fence", new Integer[]{5, 3});
		IECompatModule.doModulesPreInit();

		new ThreadContributorSpecialsDownloader();

		//Previously in INIT

		proxy.preInitEnd();
		IEContent.init();
		IEWorldGen ieWorldGen = new IEWorldGen();
		IEWorldGen.retrogenMap.put("retrogen_bauxite", IEConfig.ORES.ore_bauxite.retrogenEnabled.get());
		IEWorldGen.retrogenMap.put("retrogen_lead", IEConfig.ORES.ore_lead.retrogenEnabled.get());
		IEWorldGen.retrogenMap.put("retrogen_silver", IEConfig.ORES.ore_silver.retrogenEnabled.get());
		IEWorldGen.retrogenMap.put("retrogen_nickel", IEConfig.ORES.ore_nickel.retrogenEnabled.get());
		IEWorldGen.retrogenMap.put("retrogen_uranium", IEConfig.ORES.ore_uranium.retrogenEnabled.get());
		IEWorldGen.retrogenMap.put("retrogen_copper", IEConfig.ORES.ore_copper.retrogenEnabled.get());
		//TODO GameRegistry.registerWorldGenerator(ieWorldGen, 0);
		MinecraftForge.EVENT_BUS.register(ieWorldGen);

		MinecraftForge.EVENT_BUS.register(new EventHandler());
		//TODO NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
		proxy.init();

		IECompatModule.doModulesInit();
		proxy.initEnd();
		registerMessage(MessageTileSync.class, MessageTileSync::new);
		registerMessage(MessageTileSync.class, MessageTileSync::new);
		registerMessage(MessageSpeedloaderSync.class, MessageSpeedloaderSync::new);
		registerMessage(MessageSkyhookSync.class, MessageSkyhookSync::new);
		registerMessage(MessageMinecartShaderSync.class, MessageMinecartShaderSync::new);
		registerMessage(MessageMinecartShaderSync.class, MessageMinecartShaderSync::new);
		registerMessage(MessageRequestBlockUpdate.class, MessageRequestBlockUpdate::new);
		registerMessage(MessageNoSpamChatComponents.class, MessageNoSpamChatComponents::new);
		registerMessage(MessageShaderManual.class, MessageShaderManual::new);
		registerMessage(MessageShaderManual.class, MessageShaderManual::new);
		registerMessage(MessageBirthdayParty.class, MessageBirthdayParty::new);
		registerMessage(MessageMagnetEquip.class, MessageMagnetEquip::new);
		registerMessage(MessageScrollwheelItem.class, MessageScrollwheelItem::new);
		registerMessage(MessageObstructedConnection.class, MessageObstructedConnection::new);
		registerMessage(MessageSetGhostSlots.class, MessageSetGhostSlots::new);
		registerMessage(MessageWireSync.class, MessageWireSync::new);
		registerMessage(MessageMaintenanceKit.class, MessageMaintenanceKit::new);
		registerMessage(MessageRevolverRotate.class, MessageRevolverRotate::new);

		IEIMCHandler.init();
		//TODO IEIMCHandler.handleIMCMessages(FMLInterModComms.fetchRuntimeMessages(this));

		//Previously in POSTINIT

		IEContent.postInit();
		proxy.postInit();
		IECompatModule.doModulesPostInit();
		proxy.postInitEnd();
		ShaderRegistry.compileWeight();
	}

	private int messageId = 0;

	private <T extends IMessage> void registerMessage(Class<T> packetType, Function<PacketBuffer, T> decoder)
	{
		packetHandler.registerMessage(messageId++, packetType, IMessage::toBytes, decoder, (t, ctx) -> {
			t.process(ctx);
			ctx.get().setPacketHandled(true);
		});
	}

	public void loadComplete(FMLLoadCompleteEvent event)
	{
		IECompatModule.doModulesLoadComplete();
	}

	private static final String[] alternativeCerts = {
			"7e11c175d1e24007afec7498a1616bef0000027d",// malte0811
			"MavenKeyHere"//TODO maven
	};

	//TODO doesn't seem to be fired any more?
	public void wrongSignature(FMLFingerprintViolationEvent event)
	{
		System.out.println("[Immersive Engineering/Error] THIS IS NOT AN OFFICIAL BUILD OF IMMERSIVE ENGINEERING! Found these fingerprints: "+event.getFingerprints());
		for(String altCert : alternativeCerts)
			if(event.getFingerprints().contains(altCert))
			{
				System.out.println("[Immersive Engineering/Error] "+altCert+" is considered an alternative certificate (which may be ok to use in some cases). "+
						"If you thought this was an official build you probably shouldn't use it.");
				break;
			}
	}

	public void serverStarting(FMLServerStartingEvent event)
	{
		proxy.serverStarting();
		//TODO do client commands exist yet? I don't think so
		CommandHandler.registerServer(event.getCommandDispatcher());
	}

	public void serverAboutToStart(FMLServerAboutToStartEvent event)
	{
		event.getServer().getResourceManager().addReloadListener(new RecipeReloadListener());
	}

	public void serverStarted(FMLServerStartedEvent event)
	{
		//TODO isn't this always true? if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			//TODO hardcoding DimensionType.OVERWORLD seems hacky/broken
			ServerWorld world = event.getServer().getWorld(DimensionType.OVERWORLD);
			if(!world.isRemote)
			{
				IESaveData worldData = world.getSavedData().getOrCreate(IESaveData::new, IESaveData.dataName);
				IESaveData.setInstance(worldData);
			}
		}
		IEContent.refreshFluidReferences();
	}

	public static ItemGroup itemGroup = new ItemGroup(MODID)
	{
		@Override
		@Nonnull
		public ItemStack createIcon()
		{
			return new ItemStack(Misc.wireCoils.get(WireType.COPPER));
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
			try
			{
				IELogger.info("Attempting to download special revolvers from GitHub");
				URL url = new URL("https://raw.githubusercontent.com/BluSunrize/ImmersiveEngineering/master/contributorRevolvers.json");
				JsonStreamParser parser = new JsonStreamParser(new InputStreamReader(url.openStream()));
				while(parser.hasNext())
				{
					try
					{
						JsonElement je = parser.next();
						RevolverItem.SpecialRevolver revolver = gson.fromJson(je, RevolverItem.SpecialRevolver.class);
						if(revolver!=null)
						{
							if(revolver.uuid!=null)
								for(String uuid : revolver.uuid)
									RevolverItem.specialRevolvers.put(uuid, revolver);
							RevolverItem.specialRevolversByTag.put(!revolver.tag.isEmpty()?revolver.tag: revolver.flavour, revolver);
						}
					} catch(Exception excepParse)
					{
						IELogger.warn("Error on parsing a SpecialRevolver");
					}
				}
			} catch(Exception e)
			{
				IELogger.logger.info("Could not load contributor+special revolver list.", e);
			}
		}
	}
}
