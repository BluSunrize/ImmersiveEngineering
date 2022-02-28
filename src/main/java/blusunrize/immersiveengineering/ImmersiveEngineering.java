/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ArcRecyclingChecker;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.*;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IECommonConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.IngredientSerializers;
import blusunrize.immersiveengineering.common.crafting.RecipeCachingReloadListener;
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_SERVER;

@Mod(ImmersiveEngineering.MODID)
public class ImmersiveEngineering
{
	public static final String MODID = Lib.MODID;
	public static final String MODNAME = "Immersive Engineering";
	public static final String VERSION = IEApi.getCurrentVersion();
	public static CommonProxy proxy = DistExecutor.safeRunForDist(
			bootstrapErrorToXCPInDev(() -> ClientProxy::new), bootstrapErrorToXCPInDev(() -> CommonProxy::new)
	);

	public static final SimpleChannel packetHandler = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(MODID, "main"))
			.networkProtocolVersion(() -> VERSION)
			.serverAcceptedVersions(VERSION::equals)
			.clientAcceptedVersions(VERSION::equals)
			.simpleChannel();

	// Complete hack: DistExecutor::safeRunForDist intentionally tries to access the "wrong" supplier in dev, which
	// throws an error (rather than an exception) on J16 due to trying to load a client-only class. So we need to
	// replace the error with an exception in dev.
	public static <T>
	Supplier<T> bootstrapErrorToXCPInDev(Supplier<T> in)
	{
		if(FMLLoader.isProduction())
			return in;
		return () -> {
			try
			{
				return in.get();
			} catch(BootstrapMethodError e)
			{
				throw new RuntimeException(e);
			}
		};
	}

	public ImmersiveEngineering()
	{
		IELogger.logger = LogManager.getLogger(MODID);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMCs);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(this::addReloadListeners);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::addReloadListenersLowest);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
		RecipeSerializers.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
		Villages.Registers.POINTS_OF_INTEREST.register(FMLJavaModLoadingContext.get().getModEventBus());
		Villages.Registers.PROFESSIONS.register(FMLJavaModLoadingContext.get().getModEventBus());
		ModLoadingContext.get().registerConfig(Type.COMMON, IECommonConfig.CONFIG_SPEC.getBaseSpec());
		ModLoadingContext.get().registerConfig(Type.CLIENT, IEClientConfig.CONFIG_SPEC.getBaseSpec());
		ModLoadingContext.get().registerConfig(Type.SERVER, IEServerConfig.CONFIG_SPEC.getBaseSpec());
		IEContent.modConstruction();
		proxy.modConstruction();
		IngredientSerializers.init();

		IEWorldGen ieWorldGen = new IEWorldGen();
		MinecraftForge.EVENT_BUS.register(ieWorldGen);
		IEWorldGen.init();
		DeferredWorkQueue.runLater(IERecipes::registerRecipeTypes);
	}

	public void setup(FMLCommonSetupEvent event)
	{
		proxy.preInit(event);

		IEAdvancements.preInit();

		IEApi.prefixToIngotMap.put("ingots", new Integer[]{1, 1});
		IEApi.prefixToIngotMap.put("nuggets", new Integer[]{1, 9});
		IEApi.prefixToIngotMap.put("storage_blocks", new Integer[]{9, 1});
		IEApi.prefixToIngotMap.put("plates", new Integer[]{1, 1});
		IEApi.prefixToIngotMap.put("wires", new Integer[]{1, 2});
		IEApi.prefixToIngotMap.put("gears", new Integer[]{4, 1});
		IEApi.prefixToIngotMap.put("rods", new Integer[]{1, 2});
		IEApi.prefixToIngotMap.put("fences", new Integer[]{5, 3});
		IECompatModule.doModulesPreInit();

		ArcRecyclingChecker.allowRecipeTypeForRecycling(RecipeType.CRAFTING);
		ArcRecyclingChecker.allowRecipeTypeForRecycling(MetalPressRecipe.TYPE);
		// Vanilla Tools, Swords & Armor
		ArcRecyclingChecker.allowSimpleItemForRecycling(stack -> stack instanceof DiggerItem
				||stack instanceof ShearsItem||stack instanceof SwordItem||
				stack instanceof ArmorItem||stack instanceof HorseArmorItem||
				stack instanceof BucketItem);
		// IE Tools
		ArcRecyclingChecker.allowSimpleItemForRecycling(stack -> stack instanceof HammerItem
				||stack instanceof WirecutterItem||stack instanceof ScrewdriverItem
				||stack instanceof DrillheadItem);
		// Molds
		ArcRecyclingChecker.allowEnumeratedItemsForRecycling(() -> Stream.of(
				Molds.moldPlate, Molds.moldGear, Molds.moldRod, Molds.moldBulletCasing, Molds.moldWire,
				Molds.moldPacking4, Molds.moldPacking9, Molds.moldUnpacking
		));
		// Blocks, Plates, Rods, Wires, Gears, Scaffoldings, Fences
		ArcRecyclingChecker.allowItemTagForRecycling(IETags.plates);
		ArcRecyclingChecker.allowPrefixedTagForRecycling("rods/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("wires/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("gears/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("scaffoldings/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("fences/");
		// Prevent tools used during crafting to be recycled as components
		ArcRecyclingChecker.makeItemInvalidRecyclingOutput(stack -> stack.getItem() instanceof HammerItem
				||stack.getItem() instanceof WirecutterItem||stack.getItem() instanceof ScrewdriverItem);
		// Ignore bricks
		ArcRecyclingChecker.makeItemInvalidRecyclingOutput(stack -> TagUtils.isIngot(stack)
				&&Objects.requireNonNull(TagUtils.getMatchingPrefixAndRemaining(stack, "ingots"))[1].contains("brick"));


		new ThreadContributorSpecialsDownloader();

		proxy.preInitEnd();
		IEContent.init(event);

		MinecraftForge.EVENT_BUS.register(new EventHandler());
		proxy.init();

		IECompatModule.doModulesInit();
		proxy.initEnd();
		registerMessage(MessageTileSync.class, MessageTileSync::new);
		registerMessage(MessageContainerUpdate.class, MessageContainerUpdate::new, PLAY_TO_SERVER);
		registerMessage(MessageSpeedloaderSync.class, MessageSpeedloaderSync::new, PLAY_TO_CLIENT);
		registerMessage(MessageSkyhookSync.class, MessageSkyhookSync::new, PLAY_TO_CLIENT);
		registerMessage(MessageMinecartShaderSync.class, MessageMinecartShaderSync::new);
		registerMessage(MessageRequestBlockUpdate.class, MessageRequestBlockUpdate::new, PLAY_TO_SERVER);
		registerMessage(MessageNoSpamChatComponents.class, MessageNoSpamChatComponents::new, PLAY_TO_CLIENT);
		registerMessage(MessageShaderManual.class, MessageShaderManual::new);
		registerMessage(MessageBirthdayParty.class, MessageBirthdayParty::new, PLAY_TO_CLIENT);
		registerMessage(MessageMagnetEquip.class, MessageMagnetEquip::new, PLAY_TO_SERVER);
		registerMessage(MessageScrollwheelItem.class, MessageScrollwheelItem::new, PLAY_TO_SERVER);
		registerMessage(MessageObstructedConnection.class, MessageObstructedConnection::new, PLAY_TO_CLIENT);
		registerMessage(MessageSetGhostSlots.class, MessageSetGhostSlots::new, PLAY_TO_SERVER);
		registerMessage(MessageWireSync.class, MessageWireSync::new, PLAY_TO_CLIENT);
		registerMessage(MessageMaintenanceKit.class, MessageMaintenanceKit::new, PLAY_TO_SERVER);
		registerMessage(MessageRevolverRotate.class, MessageRevolverRotate::new, PLAY_TO_SERVER);
		registerMessage(MessageMultiblockSync.class, MessageMultiblockSync::new, PLAY_TO_CLIENT);
		registerMessage(MessageClientCommand.class, MessageClientCommand::new, PLAY_TO_CLIENT);
		registerMessage(MessageContainerData.class, MessageContainerData::new, PLAY_TO_CLIENT);

		IEIMCHandler.init();
		IEIMCHandler.handleIMCMessages(InterModComms.getMessages(MODID));

		proxy.postInit();
		IECompatModule.doModulesPostInit();
		proxy.postInitEnd();
		CommandMineral.registerArguments();
	}

	private int messageId = 0;

	private <T extends IMessage> void registerMessage(Class<T> packetType, Function<FriendlyByteBuf, T> decoder)
	{
		registerMessage(packetType, decoder, Optional.empty());
	}

	private <T extends IMessage> void registerMessage(
			Class<T> packetType, Function<FriendlyByteBuf, T> decoder, NetworkDirection direction
	)
	{
		registerMessage(packetType, decoder, Optional.of(direction));
	}

	private final Set<Class<?>> knownPacketTypes = new HashSet<>();

	private <T extends IMessage> void registerMessage(
			Class<T> packetType, Function<FriendlyByteBuf, T> decoder, Optional<NetworkDirection> direction
	)
	{
		if(!knownPacketTypes.add(packetType))
			throw new IllegalStateException("Duplicate packet type: "+packetType.getName());
		packetHandler.registerMessage(messageId++, packetType, IMessage::toBytes, decoder, (t, ctx) -> {
			t.process(ctx);
			ctx.get().setPacketHandled(true);
		}, direction);
	}

	public void enqueueIMCs(InterModEnqueueEvent event)
	{
		IECompatModule.doModulesIMCs();
	}

	public void loadComplete(FMLLoadCompleteEvent event)
	{
		IECompatModule.doModulesLoadComplete();
	}

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
		ServerResources dataPackRegistries = event.getDataPackRegistries();
		event.addListener(new RecipeReloadListener(dataPackRegistries));
	}

	public void addReloadListenersLowest(AddReloadListenerEvent event)
	{
		event.addListener(new RecipeCachingReloadListener(event.getDataPackRegistries()));
	}

	public void serverStarted(FMLServerStartedEvent event)
	{
		//TODO isn't this always true? if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			//TODO hardcoding DimensionType.OVERWORLD seems hacky/broken
			ServerLevel world = event.getServer().getLevel(Level.OVERWORLD);
			if(!world.isClientSide)
			{
				IESaveData worldData = world.getDataStorage().computeIfAbsent(IESaveData::new, IESaveData.dataName);
				IESaveData.setInstance(worldData);
			}
		}
	}

	public static ResourceLocation rl(String path)
	{
		return new ResourceLocation(MODID, path);
	}

	public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(MODID)
	{
		@Override
		@Nonnull
		public ItemStack makeIcon()
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
