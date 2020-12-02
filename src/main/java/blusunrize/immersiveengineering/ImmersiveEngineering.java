/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IECommonConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.IngredientSerializers;
import blusunrize.immersiveengineering.common.crafting.RecipeReloadListener;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Molds;
import blusunrize.immersiveengineering.common.network.*;
import blusunrize.immersiveengineering.common.util.IEIMCHandler;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler;
import blusunrize.immersiveengineering.common.util.commands.CommandMineral;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import blusunrize.immersiveengineering.common.world.Villages;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;
import java.util.function.Function;

@Mod(ImmersiveEngineering.MODID)
public class ImmersiveEngineering
{
	public static final String MODID = "immersiveengineering";
	public static final String MODNAME = "Immersive Engineering";
	public static final String VERSION = "${version}";
	@SuppressWarnings("Convert2MethodRef")
	public static CommonProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(),
			() -> () -> new CommonProxy());

	public static final SimpleChannel packetHandler = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(MODID, "main"))
			.networkProtocolVersion(() -> VERSION)
			.serverAcceptedVersions(VERSION::equals)
			.clientAcceptedVersions(VERSION::equals)
			.simpleChannel();

	public ImmersiveEngineering()
	{
		IELogger.logger = LogManager.getLogger(MODID);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(this::addReloadListeners);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
		RecipeSerializers.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
		Villages.Registers.POINTS_OF_INTEREST.register(FMLJavaModLoadingContext.get().getModEventBus());
		Villages.Registers.PROFESSIONS.register(FMLJavaModLoadingContext.get().getModEventBus());
		ModLoadingContext.get().registerConfig(Type.COMMON, IECommonConfig.CONFIG_SPEC);
		ModLoadingContext.get().registerConfig(Type.CLIENT, IEClientConfig.CONFIG_SPEC);
		ModLoadingContext.get().registerConfig(Type.SERVER, IEServerConfig.CONFIG_SPEC.getBaseSpec());
		IEContent.modConstruction();
		proxy.modConstruction();
		IngredientSerializers.init();

		IEWorldGen ieWorldGen = new IEWorldGen();
		MinecraftForge.EVENT_BUS.register(ieWorldGen);
		IEWorldGen.init();
	}

	public void setup(FMLCommonSetupEvent event)
	{
		ApiUtils.disableTicking.setValue(EventHandler.REMOVE_FROM_TICKING::add);
		proxy.preInit();

		IEAdvancements.preInit();

		IEApi.modPreference = IECommonConfig.preferredOres.get();
		IEApi.prefixToIngotMap.put("ingots", new Integer[]{1, 1});
		IEApi.prefixToIngotMap.put("nuggets", new Integer[]{1, 9});
		IEApi.prefixToIngotMap.put("storage_blocks", new Integer[]{9, 1});
		IEApi.prefixToIngotMap.put("plates", new Integer[]{1, 1});
		IEApi.prefixToIngotMap.put("wires", new Integer[]{1, 2});
		IEApi.prefixToIngotMap.put("gears", new Integer[]{4, 1});
		IEApi.prefixToIngotMap.put("rods", new Integer[]{1, 2});
		IEApi.prefixToIngotMap.put("fences", new Integer[]{5, 3});
		IECompatModule.doModulesPreInit();

		ArcFurnaceRecipe.allowRecipeTypeForRecycling(IRecipeType.CRAFTING);
		ArcFurnaceRecipe.allowRecipeTypeForRecycling(MetalPressRecipe.TYPE);
		// Vanilla Tools, Swords & Armor
		ArcFurnaceRecipe.allowItemForRecycling(stack -> stack.getItem() instanceof ToolItem
				||stack.getItem() instanceof HoeItem||stack.getItem() instanceof ShearsItem
				||stack.getItem() instanceof SwordItem||stack.getItem() instanceof ArmorItem
				||stack.getItem() instanceof HorseArmorItem||stack.getItem() instanceof BucketItem);
		// IE Tools
		ArcFurnaceRecipe.allowItemForRecycling(stack -> stack.getItem() instanceof HammerItem
				||stack.getItem() instanceof WirecutterItem||stack.getItem() instanceof ScrewdriverItem
				||stack.getItem() instanceof DrillheadItem);
		// Molds
		ArcFurnaceRecipe.allowItemForRecycling(stack -> stack.getItem()==Molds.moldPlate
				||stack.getItem()==Molds.moldGear
				||stack.getItem()==Molds.moldRod
				||stack.getItem()==Molds.moldBulletCasing
				||stack.getItem()==Molds.moldWire
				||stack.getItem()==Molds.moldPacking4
				||stack.getItem()==Molds.moldPacking9
				||stack.getItem()==Molds.moldUnpacking
		);
		// Blocks, Plates, Rods, Wires, Gears, Scaffoldings, Fences
		ArcFurnaceRecipe.allowItemForRecycling(stack -> TagUtils.isPlate(stack)
				||TagUtils.isInPrefixedTag(stack, "rods/")
				||TagUtils.isInPrefixedTag(stack, "wires/")
				||TagUtils.isInPrefixedTag(stack, "gears/")
				||TagUtils.isInPrefixedTag(stack, "scaffoldings/")
				||TagUtils.isInPrefixedTag(stack, "fences/"));
		// Prevent tools used during crafting to be recycled as components
		ArcFurnaceRecipe.makeItemInvalidRecyclingOutput(stack -> stack.getItem() instanceof HammerItem
				||stack.getItem() instanceof WirecutterItem||stack.getItem() instanceof ScrewdriverItem);
		// Ignore bricks
		ArcFurnaceRecipe.makeItemInvalidRecyclingOutput(stack -> TagUtils.isIngot(stack)
				&&Objects.requireNonNull(TagUtils.getMatchingPrefixAndRemaining(stack, "ingots"))[1].contains("brick"));


		new ThreadContributorSpecialsDownloader();

		proxy.preInitEnd();
		IEContent.init();

		MinecraftForge.EVENT_BUS.register(new EventHandler());
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
		registerMessage(MessageMultiblockSync.class, MessageMultiblockSync::new);

		IEIMCHandler.init();
		//TODO IEIMCHandler.handleIMCMessages(FMLInterModComms.fetchRuntimeMessages(this));

		IEContent.postInit();
		proxy.postInit();
		IECompatModule.doModulesPostInit();
		proxy.postInitEnd();
		ShaderRegistry.compileWeight();
		CommandMineral.registerArguments();
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

	/*
	//TODO This has been removed in some forge versions. Apparently cpw is making something better soon?
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
	*/

	public void serverStarting(FMLServerStartingEvent event)
	{
		proxy.serverStarting();
	}

	public void registerCommands(RegisterCommandsEvent event)
	{
		//TODO do client commands exist yet? I don't think so
		CommandHandler.registerServer(event.getDispatcher());
	}

	public void addReloadListeners(AddReloadListenerEvent event)
	{
		DataPackRegistries dataPackRegistries = event.getDataPackRegistries();
		event.addListener(new RecipeReloadListener(dataPackRegistries));
	}

	public void serverStarted(FMLServerStartedEvent event)
	{
		//TODO isn't this always true? if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			//TODO hardcoding DimensionType.OVERWORLD seems hacky/broken
			ServerWorld world = event.getServer().getWorld(World.OVERWORLD);
			if(!world.isRemote)
			{
				IESaveData worldData = world.getSavedData().getOrCreate(IESaveData::new, IESaveData.dataName);
				IESaveData.setInstance(worldData);
			}
		}
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
				URL url = new URL("https://raw.githubusercontent.com/BluSunrize/ImmersiveEngineering/gh-pages/contributorRevolvers.json");
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
