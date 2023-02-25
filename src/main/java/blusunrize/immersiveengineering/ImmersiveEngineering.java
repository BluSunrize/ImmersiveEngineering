/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ArcRecyclingChecker;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IECommonConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.IngredientSerializers;
import blusunrize.immersiveengineering.common.crafting.MetalPressPackingRecipes;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.network.*;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEItems.Molds;
import blusunrize.immersiveengineering.common.util.IEIMCHandler;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.MissingMappingsHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler;
import blusunrize.immersiveengineering.common.util.compat.IECompatModules;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import blusunrize.immersiveengineering.common.world.Villages;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

@Mod(ImmersiveEngineering.MODID)
public class ImmersiveEngineering
{
	public static final String MODID = Lib.MODID;
	public static final String MODNAME = "Immersive Engineering";
	public static final String VERSION = IEApi.getCurrentVersion();
	public static final CommonProxy proxy = DistExecutor.safeRunForDist(bootstrapErrorToXCPInDev(() -> ClientProxy::new), bootstrapErrorToXCPInDev(() -> CommonProxy::new));

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
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
		MinecraftForge.EVENT_BUS.addListener(MissingMappingsHelper::handleRemapping);
		RecipeSerializers.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
		Villages.Registers.POINTS_OF_INTEREST.register(FMLJavaModLoadingContext.get().getModEventBus());
		Villages.Registers.PROFESSIONS.register(FMLJavaModLoadingContext.get().getModEventBus());
		ModLoadingContext.get().registerConfig(Type.COMMON, IECommonConfig.CONFIG_SPEC);
		ModLoadingContext.get().registerConfig(Type.CLIENT, IEClientConfig.CONFIG_SPEC);
		ModLoadingContext.get().registerConfig(Type.SERVER, IEServerConfig.CONFIG_SPEC);
		IEContent.modConstruction();
		DistExecutor.safeRunWhenOn(Dist.CLIENT, bootstrapErrorToXCPInDev(() -> ClientProxy::modConstruction));
		IngredientSerializers.init();

		IEWorldGen.init();
		IECompatModules.onModConstruction();
	}

	public void setup(FMLCommonSetupEvent event)
	{
		IEAdvancements.preInit();

		IEApi.prefixToIngotMap.put("ingots", new Integer[]{1, 1});
		IEApi.prefixToIngotMap.put("nuggets", new Integer[]{1, 9});
		IEApi.prefixToIngotMap.put("storage_blocks", new Integer[]{9, 1});
		IEApi.prefixToIngotMap.put("plates", new Integer[]{1, 1});
		IEApi.prefixToIngotMap.put("wires", new Integer[]{1, 2});
		IEApi.prefixToIngotMap.put("gears", new Integer[]{4, 1});
		IEApi.prefixToIngotMap.put("rods", new Integer[]{1, 2});
		IEApi.prefixToIngotMap.put("fences", new Integer[]{5, 3});
		IEApi.prefixToIngotMap.put("cut_blocks", new Integer[]{9, 4});
		IEApi.prefixToIngotMap.put("cut_stairs", new Integer[]{9, 4});
		IEApi.prefixToIngotMap.put("cut_slabs", new Integer[]{9, 8});

		/* ARC FURNACE RECYCLING */
		ArcRecyclingChecker.allowRecipeTypeForRecycling(RecipeType.CRAFTING);
		ArcRecyclingChecker.allowRecipeTypeForRecycling(IERecipeTypes.METAL_PRESS.get());
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
				Molds.MOLD_PLATE, Molds.MOLD_GEAR, Molds.MOLD_ROD, Molds.MOLD_BULLET_CASING, Molds.MOLD_WIRE,
				Molds.MOLD_PACKING_4, Molds.MOLD_PACKING_9, Molds.MOLD_UNPACKING
		));
		// Blocks, Plates, Rods, Wires, Gears, Scaffoldings, Fences
		ArcRecyclingChecker.allowItemTagForRecycling(IETags.plates);
		ArcRecyclingChecker.allowPrefixedTagForRecycling("rods/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("wires/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("gears/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("scaffoldings/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("scaffolding_stairs/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("scaffolding_slabs/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("fences/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("cut_blocks/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("cut_stairs/");
		ArcRecyclingChecker.allowPrefixedTagForRecycling("cut_slabs/");

		// Decoration blocks & Sheetmetal
		ArcRecyclingChecker.allowEnumeratedItemsForRecycling(() -> Stream.of(
				MetalDecoration.ENGINEERING_RS, MetalDecoration.ENGINEERING_LIGHT, MetalDecoration.ENGINEERING_HEAVY,
				MetalDecoration.GENERATOR, MetalDecoration.RADIATOR
		));
		ArcRecyclingChecker.allowEnumeratedItemsForRecycling(() -> Stream.of(
				MetalDecoration.ALU_WALLMOUNT, MetalDecoration.STEEL_WALLMOUNT, MetalDecoration.STEEL_SLOPE,
				MetalDecoration.ALU_SLOPE, MetalDecoration.ALU_POST, MetalDecoration.STEEL_POST
		));
		for(EnumMetals metal : EnumMetals.values())
			ArcRecyclingChecker.allowItemTagForRecycling(IETags.getItemTag(IETags.getTagsFor(metal).sheetmetal));
		ArcRecyclingChecker.allowItemTagForRecycling(IETags.getItemTag(IETags.sheetmetalSlabs));
		// Metal devices & Chutes
		ArcRecyclingChecker.allowEnumeratedItemsForRecycling(() -> Stream.of(
				MetalDevices.RAZOR_WIRE, MetalDevices.BARREL, MetalDevices.FLUID_PIPE
		));
		ArcRecyclingChecker.allowEnumeratedItemsForRecycling(() -> MetalDevices.CHUTES.values().stream());

		// Vanilla Metals
		ArcRecyclingChecker.allowEnumeratedItemsForRecycling(() -> Stream.of(
				Items.HEAVY_WEIGHTED_PRESSURE_PLATE, Items.LIGHT_WEIGHTED_PRESSURE_PLATE,
				Items.IRON_TRAPDOOR, Items.IRON_DOOR, Items.IRON_BARS, Items.CAULDRON,
				Items.MINECART, Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL, Items.LIGHTNING_ROD
		));

		// Whitelisted with tag
		ArcRecyclingChecker.allowItemTagForRecycling(IETags.recyclingWhitelist);

		// Ignore components to not propagate in recycling
		ArcRecyclingChecker.makeItemInvalidRecyclingOutput((tags, stack) -> stack.is(IETags.recyclingIgnoredComponents));

		new ThreadContributorSpecialsDownloader();

		IEContent.commonSetup(event);

		MinecraftForge.EVENT_BUS.register(new EventHandler());

		IECompatModules.onCommonSetup();
		registerMessage(MessageBlockEntitySync.class, MessageBlockEntitySync::new);
		registerMessage(MessageContainerUpdate.class, MessageContainerUpdate::new, PLAY_TO_SERVER);
		registerMessage(MessageSpeedloaderSync.class, MessageSpeedloaderSync::new, PLAY_TO_CLIENT);
		registerMessage(MessageSkyhookSync.class, MessageSkyhookSync::new, PLAY_TO_CLIENT);
		registerMessage(MessageMinecartShaderSync.class, MessageMinecartShaderSync::new);
		registerMessage(MessageRequestEnergyUpdate.class, MessageRequestEnergyUpdate::new, PLAY_TO_SERVER);
		registerMessage(MessageStoredEnergy.class, MessageStoredEnergy::new, PLAY_TO_CLIENT);
		registerMessage(MessageRequestRedstoneUpdate.class, MessageRequestRedstoneUpdate::new, PLAY_TO_SERVER);
		registerMessage(MessageRedstoneLevel.class, MessageRedstoneLevel::new, PLAY_TO_CLIENT);
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
		registerMessage(MessageContainerData.class, MessageContainerData::new, PLAY_TO_CLIENT);
		registerMessage(MessageNoSpamChat.class, MessageNoSpamChat::new, PLAY_TO_CLIENT);
		registerMessage(MessageOpenManual.class, MessageOpenManual::new, PLAY_TO_CLIENT);
		registerMessage(MessagePowerpackAntenna.class, MessagePowerpackAntenna::new, PLAY_TO_CLIENT);

		IEIMCHandler.init();
		IEIMCHandler.handleIMCMessages(InterModComms.getMessages(MODID));

		MetalPressPackingRecipes.init();
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
		IECompatModules.doModulesIMCs();
		IEContent.clearLastFuture();
	}

	public void registerCommands(RegisterCommandsEvent event)
	{
		//TODO do client commands exist yet? I don't think so
		CommandHandler.registerServer(event.getDispatcher());
	}

	public void serverStarted(ServerStartedEvent event)
	{
		//TODO isn't this always true? if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			//TODO hardcoding DimensionType.OVERWORLD seems hacky/broken
			ServerLevel world = event.getServer().getLevel(Level.OVERWORLD);
			if(!world.isClientSide)
			{
				IESaveData worldData = world.getDataStorage().computeIfAbsent(IESaveData::new, IESaveData::new, IESaveData.dataName);
				IESaveData.setInstance(worldData);
			}
		}
	}

	public static ResourceLocation rl(String path)
	{
		return new ResourceLocation(MODID, path);
	}

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
							if(revolver.uuid()!=null)
								for(String uuid : revolver.uuid())
									RevolverItem.specialRevolvers.put(uuid, revolver);
							RevolverItem.specialRevolversByTag.put(!revolver.tag().isEmpty()?revolver.tag(): revolver.flavour(), revolver);
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
