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
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ChunkLoaderLogic;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IECommonConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.MetalPressPackingRecipes;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.RevolverItem.SpecialRevolver;
import blusunrize.immersiveengineering.common.network.*;
import blusunrize.immersiveengineering.common.register.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.util.IEIMCHandler;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler;
import blusunrize.immersiveengineering.common.util.compat.IECompatModules;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import blusunrize.immersiveengineering.common.world.Villages;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData.Factory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

import static net.minecraft.network.protocol.PacketFlow.CLIENTBOUND;
import static net.minecraft.network.protocol.PacketFlow.SERVERBOUND;

@Mod(ImmersiveEngineering.MODID)
public class ImmersiveEngineering
{
	public static Dist DIST = Dist.DEDICATED_SERVER;
	public static final String MODID = Lib.MODID;
	public static final String MODNAME = "Immersive Engineering";
	public static final String VERSION = IEApi.getCurrentVersion();
	public static final CommonProxy proxy = Util.make(() -> {
		if(FMLLoader.getDist().isClient())
			return new ClientProxy();
		else
			return new CommonProxy();
	});

	public ImmersiveEngineering(ModContainer container, Dist dist, IEventBus modBus)
	{
		DIST = dist;
		IELogger.logger = LogManager.getLogger(MODID);
		modBus.addListener(this::setup);
		modBus.addListener(this::setupNetwork);
		modBus.addListener(this::enqueueIMCs);
		modBus.addListener(this::registerTicketController);
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
		NeoForge.EVENT_BUS.addListener(this::serverStarted);
		RecipeSerializers.RECIPE_SERIALIZERS.register(modBus);
		Villages.Registers.POINTS_OF_INTEREST.register(modBus);
		Villages.Registers.PROFESSIONS.register(modBus);
		container.registerConfig(Type.STARTUP, IECommonConfig.CONFIG_SPEC);
		container.registerConfig(Type.CLIENT, IEClientConfig.CONFIG_SPEC);
		container.registerConfig(Type.SERVER, IEServerConfig.CONFIG_SPEC);
		IEContent.modConstruction(modBus);
		if(dist.isClient())
			ClientProxy.modConstruction();

		IEWorldGen.init(modBus);
		IECompatModules.onModConstruction(modBus);
	}

	public void setup(FMLCommonSetupEvent event)
	{
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
				stack instanceof ArmorItem||stack instanceof AnimalArmorItem||
				stack instanceof BucketItem);
		// IE Tools
		ArcRecyclingChecker.allowSimpleItemForRecycling(stack -> stack instanceof HammerItem
				||stack instanceof WirecutterItem||stack instanceof ScrewdriverItem
				||stack instanceof DrillheadItem||stack instanceof JerrycanItem);
		// Revolver parts
		ArcRecyclingChecker.allowEnumeratedItemsForRecycling(() -> Stream.of(
				Ingredients.GUNPART_BARREL, Ingredients.GUNPART_DRUM, Ingredients.GUNPART_HAMMER
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
				MetalDecoration.ALU_WALLMOUNT, MetalDecoration.STEEL_WALLMOUNT, MetalDecoration.STEEL_SLOPE,
				MetalDecoration.ALU_SLOPE, MetalDecoration.ALU_POST, MetalDecoration.STEEL_POST,
				MetalDecoration.STEEL_TRAPDOOR, MetalDecoration.STEEL_DOOR,
				MetalDecoration.ALU_CATWALK, MetalDecoration.ALU_CATWALK_STAIRS,
				MetalDecoration.STEEL_CATWALK, MetalDecoration.STEEL_CATWALK_STAIRS
		));
		for(EnumMetals metal : EnumMetals.values())
			ArcRecyclingChecker.allowItemTagForRecycling(IETags.getItemTag(IETags.getTagsFor(metal).sheetmetal));
		ArcRecyclingChecker.allowItemTagForRecycling(IETags.getItemTag(IETags.sheetmetalSlabs));
		// Metal devices, Connectors, & Chutes
		ArcRecyclingChecker.allowEnumeratedItemsForRecycling(() -> Stream.of(
				MetalDevices.RAZOR_WIRE, MetalDevices.BARREL, MetalDevices.FLUID_PIPE, MetalDevices.PIPE_VALVE,
				MetalDevices.FLUID_PLACER, Connectors.CONNECTOR_STRUCTURAL
		));
		ArcRecyclingChecker.allowEnumeratedItemsForRecycling(() -> MetalDevices.CHUTES.values().stream());

		// Vanilla Metals
		ArcRecyclingChecker.allowEnumeratedItemsForRecycling(() -> Stream.of(
				Items.HEAVY_WEIGHTED_PRESSURE_PLATE, Items.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.HOPPER,
				Items.IRON_TRAPDOOR, Items.IRON_DOOR, Items.IRON_BARS, Items.CAULDRON, Items.CHAIN,
				Items.MINECART, Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL, Items.LIGHTNING_ROD
		));

		// Whitelisted with tag
		ArcRecyclingChecker.allowItemTagForRecycling(IETags.recyclingWhitelist);

		// Ignore components to not propagate in recycling
		ArcRecyclingChecker.makeItemInvalidRecyclingOutput((tags, stack) -> stack.is(IETags.recyclingIgnoredComponents));

		new ThreadContributorSpecialsDownloader();

		IEContent.commonSetup(event);

		NeoForge.EVENT_BUS.register(new EventHandler());

		IECompatModules.onCommonSetup();

		IEIMCHandler.init();
		IEIMCHandler.handleIMCMessages(InterModComms.getMessages(MODID));

		MetalPressPackingRecipes.init();
		IECommonConfig.onLoad();
	}

	private void setupNetwork(RegisterPayloadHandlersEvent ev)
	{
		final PayloadRegistrar registrar = ev.registrar(MODID);
		registerMessage(registrar, MessageBlockEntitySync.ID, MessageBlockEntitySync.CODEC);
		registerMessage(registrar, MessageContainerUpdate.ID, MessageContainerUpdate.CODEC, SERVERBOUND);
		registerMessage(registrar, MessageSpeedloaderSync.ID, MessageSpeedloaderSync.CODEC, CLIENTBOUND);
		registerMessage(registrar, MessageSkyhookSync.ID, MessageSkyhookSync.CODEC, CLIENTBOUND);
		registerMessage(registrar, MessageMinecartShaderSync.ID, MessageMinecartShaderSync.CODEC);
		registerMessage(registrar, MessageRequestEnergyUpdate.ID, MessageRequestEnergyUpdate.CODEC, SERVERBOUND);
		registerMessage(registrar, MessageStoredEnergy.ID, MessageStoredEnergy.CODEC, CLIENTBOUND);
		registerMessage(registrar, MessageRequestRedstoneUpdate.ID, MessageRequestRedstoneUpdate.CODEC, SERVERBOUND);
		registerMessage(registrar, MessageRedstoneLevel.ID, MessageRedstoneLevel.CODEC, CLIENTBOUND);
		registerMessage(registrar, MessageShaderManual.ID, MessageShaderManual.CODEC);
		registerMessage(registrar, MessageBirthdayParty.ID, MessageBirthdayParty.CODEC, CLIENTBOUND);
		registerMessage(registrar, MessageMagnetEquip.ID, MessageMagnetEquip.CODEC, SERVERBOUND);
		registerMessage(registrar, MessageScrollwheelItem.ID, MessageScrollwheelItem.CODEC, SERVERBOUND);
		registerMessage(registrar, MessageObstructedConnection.ID, MessageObstructedConnection.CODEC, CLIENTBOUND);
		registerMessage(registrar, MessageSetGhostSlots.ID, MessageSetGhostSlots.CODEC, SERVERBOUND);
		registerMessage(registrar, MessageWireSync.ID, MessageWireSync.CODEC, CLIENTBOUND);
		registerMessage(registrar, MessageMaintenanceKit.ID, MessageMaintenanceKit.CODEC, SERVERBOUND);
		registerMessage(registrar, MessageRevolverRotate.ID, MessageRevolverRotate.CODEC, SERVERBOUND);
		registerMessage(registrar, MessageMultiblockSync.ID, MessageMultiblockSync.CODEC, CLIENTBOUND);
		registerMessage(registrar, MessageContainerData.ID, MessageContainerData.CODEC, CLIENTBOUND);
		registerMessage(registrar, MessageNoSpamChat.ID, MessageNoSpamChat.CODEC, CLIENTBOUND);
		registerMessage(registrar, MessageOpenManual.ID, MessageOpenManual.CODEC, CLIENTBOUND);
		registerMessage(registrar, MessagePowerpackAntenna.ID, MessagePowerpackAntenna.CODEC, CLIENTBOUND);
//		registerMessage(registrar, MessageCrateName.ID, MessageCrateName::new, SERVERBOUND);
		registerMessage(registrar, MessageNoisyToolHarvestUpdate.ID, MessageNoisyToolHarvestUpdate.CODEC, CLIENTBOUND);
		registerMessage(registrar, MessageNoisyToolAttack.ID, MessageNoisyToolAttack.CODEC, CLIENTBOUND);
	}

	private <T extends IMessage> void registerMessage(
			PayloadRegistrar registrar,
			CustomPacketPayload.Type<T> id,
			StreamCodec<? super RegistryFriendlyByteBuf, T> codec
	)
	{
		registerMessage(registrar, id, codec, Optional.empty());
	}

	private <T extends IMessage> void registerMessage(
			PayloadRegistrar registrar,
			CustomPacketPayload.Type<T> id,
			StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
			PacketFlow direction
	)
	{
		registerMessage(registrar, id, codec, Optional.of(direction));
	}

	private <T extends IMessage> void registerMessage(
			PayloadRegistrar registrar,
			CustomPacketPayload.Type<T> id,
			StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
			Optional<PacketFlow> direction
	)
	{
		if(direction.isEmpty())
			registrar.playBidirectional(id, codec, T::process);
		else if(direction.get()==CLIENTBOUND)
			registrar.playToClient(id, codec, T::process);
		else if(direction.get()==SERVERBOUND)
			registrar.playToServer(id, codec, T::process);
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

	public void registerTicketController(RegisterTicketControllersEvent e)
	{
		e.register(ChunkLoaderLogic.TICKET_CONTROLLER);
	}

	public void serverStarted(ServerStartedEvent event)
	{
		//TODO isn't this always true? if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			//TODO hardcoding DimensionType.OVERWORLD seems hacky/broken
			ServerLevel world = event.getServer().getLevel(Level.OVERWORLD);
			if(!world.isClientSide)
			{
				IESaveData worldData = world.getDataStorage().computeIfAbsent(
						new Factory<>(IESaveData::new, IESaveData::new), IESaveData.dataName
				);
				IESaveData.setInstance(worldData);
			}
		}
	}

	public static ResourceLocation rl(String path)
	{
		return IEApi.ieLoc(path);
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
						RevolverItem.SpecialRevolver revolver = SpecialRevolver.CODECS.fromJSON(je);
						if(revolver!=null)
						{
							if(revolver.uuid()!=null)
								for(String uuid : revolver.uuid())
									RevolverItem.specialRevolvers.put(uuid, revolver);
							RevolverItem.specialRevolversByTag.put(!revolver.tag().isEmpty()?revolver.tag(): revolver.flavour(), revolver);
						}
					} catch(Exception excepParse)
					{
						IELogger.logger.trace("Error on parsing a SpecialRevolver", excepParse);
					}
				}
			} catch(Exception e)
			{
				IELogger.logger.info("Could not load contributor+special revolver list.", e);
			}
		}
	}
}
