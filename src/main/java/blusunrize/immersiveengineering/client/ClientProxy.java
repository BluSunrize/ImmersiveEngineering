/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import blusunrize.immersiveengineering.client.gui.*;
import blusunrize.immersiveengineering.client.manual.ManualElementBlueprint;
import blusunrize.immersiveengineering.client.manual.ManualElementMultiblock;
import blusunrize.immersiveengineering.client.models.*;
import blusunrize.immersiveengineering.client.models.ModelConveyor.ConveyorLoader;
import blusunrize.immersiveengineering.client.models.ModelCoresample.CoresampleLoader;
import blusunrize.immersiveengineering.client.models.PotionBucketModel.Loader;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughLoader;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughModel;
import blusunrize.immersiveengineering.client.models.mirror.MirroredModelLoader;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.client.models.obj.callback.DefaultCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.DynamicSubmodelCallbacks;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallbacks;
import blusunrize.immersiveengineering.client.models.obj.callback.block.*;
import blusunrize.immersiveengineering.client.models.obj.callback.item.*;
import blusunrize.immersiveengineering.client.models.split.SplitModelLoader;
import blusunrize.immersiveengineering.client.render.ConnectionRenderer;
import blusunrize.immersiveengineering.client.render.IEBipedLayerRenderer;
import blusunrize.immersiveengineering.client.render.conveyor.RedstoneConveyorRender;
import blusunrize.immersiveengineering.client.render.conveyor.SplitConveyorRender;
import blusunrize.immersiveengineering.client.render.entity.*;
import blusunrize.immersiveengineering.client.render.tile.*;
import blusunrize.immersiveengineering.client.render.tooltip.RevolverClientTooltip;
import blusunrize.immersiveengineering.client.render.tooltip.RevolverServerTooltip;
import blusunrize.immersiveengineering.client.utils.BasicClientProperties;
import blusunrize.immersiveengineering.client.utils.VertexBufferHolder;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundBE;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorProbeBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorRedstoneBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorBase;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.DropConveyor;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.SplitConveyor;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.VerticalConveyor;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import blusunrize.immersiveengineering.common.gui.IEBaseContainerOld;
import blusunrize.immersiveengineering.common.items.GrindingDiskItem;
import blusunrize.immersiveengineering.common.items.RockcutterItem;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.BEContainer;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.sound.IEBlockEntitySound;
import blusunrize.immersiveengineering.common.util.sound.SkyhookSound;
import blusunrize.lib.manual.gui.ManualScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.client.ClientUtils.mc;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Bus.MOD)
public class ClientProxy extends CommonProxy
{
	public static void modConstruction()
	{
		IEOBJCallbacks.register(rl("default"), DefaultCallback.INSTANCE);
		IEOBJCallbacks.register(rl("drill"), DrillCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("buzzsaw"), BuzzsawCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("fluorescent_tube"), FluorescentTubeCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("revolver"), RevolverCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("chemthrower"), ChemthrowerCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("railgun"), RailgunCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("shield"), ShieldCallbacks.INSTANCE);

		IEOBJCallbacks.register(rl("balloon"), BalloonCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("bottling_machine"), BottlingMachineCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("bucket_wheel"), BucketWheelCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("breaker"), BreakerSwitchCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("chute"), ChuteCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("cloche"), ClocheCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("floodlight"), FloodlightCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("lantern"), LanternCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("logic_unit"), LogicUnitCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("pipe"), PipeCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("probe"), ProbeConnectorCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("post"), PostCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("razor_wire"), RazorWireCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("connector_rs"), RSConnectorCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("structural_arm"), StructuralArmCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("structural_connector"), StructuralConnectorCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("turret"), TurretCallbacks.INSTANCE);
		IEOBJCallbacks.register(rl("workbench"), WorkbenchCallbacks.INSTANCE);

		IEOBJCallbacks.register(rl("submodel"), DynamicSubmodelCallbacks.INSTANCE);

		// Apparently this runs in data generation runs... but registering model loaders causes NPEs there
		if(Minecraft.getInstance()!=null)
			initWithMC();
	}

	public static void initWithMC()
	{
		populateAPI();
		requestModelsAndTextures();

		ClientEventHandler handler = new ClientEventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		ReloadableResourceManager reloadableManager = (ReloadableResourceManager)mc().getResourceManager();
		reloadableManager.registerReloadListener(handler);
		reloadableManager.registerReloadListener(new ConnectionRenderer());

		IEModelLayers.registerDefinitions();
	}

	@SubscribeEvent
	public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent ev)
	{
		ev.register(RevolverServerTooltip.class, RevolverClientTooltip::new);
	}

	@SubscribeEvent
	public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders ev)
	{
		ev.register(IEOBJLoader.LOADER_NAME.getPath(), IEOBJLoader.instance);
		ev.register(ModelConfigurableSides.Loader.NAME.getPath(), new ModelConfigurableSides.Loader());
		ev.register(ConveyorLoader.LOCATION.getPath(), new ConveyorLoader());
		ev.register(CoresampleLoader.LOCATION.getPath(), new CoresampleLoader());
		ev.register(FeedthroughLoader.LOCATION.getPath(), new FeedthroughLoader());
		ev.register(SplitModelLoader.LOCATION.getPath(), new SplitModelLoader());
		ev.register(Loader.LOADER_NAME.getPath(), new PotionBucketModel.Loader());
		ev.register(MirroredModelLoader.ID.getPath(), new MirroredModelLoader());

		ArcFurnaceRenderer.ELECTRODES = new DynamicModel(ArcFurnaceRenderer.NAME);
		AutoWorkbenchRenderer.DYNAMIC = new DynamicModel(AutoWorkbenchRenderer.NAME);
		BottlingMachineRenderer.DYNAMIC = new DynamicModel(BottlingMachineRenderer.NAME);
		BucketWheelRenderer.WHEEL = new DynamicModel(BucketWheelRenderer.NAME);
		CrusherRenderer.BARREL = new DynamicModel(CrusherRenderer.NAME);
		SawmillRenderer.BLADE = new DynamicModel(SawmillRenderer.NAME);
		DieselGeneratorRenderer.FAN = new DynamicModel(DieselGeneratorRenderer.NAME);
		MetalPressRenderer.PISTON = new DynamicModel(MetalPressRenderer.NAME);
		MixerRenderer.AGITATOR = new DynamicModel(MixerRenderer.NAME);
		SampleDrillRenderer.DRILL = new DynamicModel(SampleDrillRenderer.NAME);
		SqueezerRenderer.PISTON = new DynamicModel(SqueezerRenderer.NAME);
		WatermillRenderer.MODEL = new DynamicModel(WatermillRenderer.NAME);
		WindmillRenderer.MODEL = new DynamicModel(WindmillRenderer.NAME);
		RedstoneConveyorRender.MODEL_PANEL = new DynamicModel(RedstoneConveyorRender.MODEL_NAME);
		SawbladeRenderer.MODEL = new DynamicModel(SawbladeRenderer.NAME);
		BlastFurnacePreheaterRenderer.MODEL = new DynamicModel(BlastFurnacePreheaterRenderer.NAME);
		TurretRenderer.fillModels();
		BasicClientProperties.initModels();
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent ev)
	{
		if(IEClientConfig.stencilBufferEnabled.get())
			ev.enqueueWork(() -> Minecraft.getInstance().getMainRenderTarget().enableStencil());
		registerContainersAndScreens();
		ShaderHelper.initShaders();
		IEDefaultColourHandlers.register();

		IEManual.addIEManualEntries();
		IEBannerPatterns.ALL_BANNERS.forEach(entry -> {
			ResourceKey<BannerPattern> pattern = Objects.requireNonNull(entry.pattern().getKey());
			Sheets.BANNER_MATERIALS.put(pattern, new Material(Sheets.BANNER_SHEET, BannerPattern.location(pattern, true)));
			Sheets.SHIELD_MATERIALS.put(pattern, new Material(Sheets.SHIELD_SHEET, BannerPattern.location(pattern, false)));
		});
	}

	private static <T extends Entity, T2 extends T> void registerEntityRenderingHandler(
			EntityRenderersEvent.RegisterRenderers ev, Supplier<EntityType<T2>> type, EntityRendererProvider<T> renderer
	)
	{
		ev.registerEntityRenderer(type.get(), renderer);
	}

	//TODO are these here rather than in ClientEventHandler for any particular reason???
	@SubscribeEvent
	public static void textureStichPre(TextureStitchEvent.Pre event)
	{
		ResourceLocation sheet = event.getAtlas().location();
		if(sheet.equals(Sheets.BANNER_SHEET)||sheet.equals(Sheets.SHIELD_SHEET))
			IEBannerPatterns.ALL_BANNERS.forEach(entry -> {
				ResourceKey<BannerPattern> pattern = Objects.requireNonNull(entry.pattern().getKey());
				event.addSprite(BannerPattern.location(pattern, sheet.equals(Sheets.BANNER_SHEET)));
			});

		if(!sheet.equals(InventoryMenu.BLOCK_ATLAS))
			return;
		IELogger.info("Stitching Revolver Textures!");
		RevolverCallbacks.addRevolverTextures(event);
		for(ShaderRegistry.ShaderRegistryEntry entry : ShaderRegistry.shaderRegistry.values())
			for(ShaderCase sCase : entry.getCases())
				if(sCase.stitchIntoSheet())
					for(ShaderLayer layer : sCase.getLayers())
						if(layer.getTexture()!=null)
							event.addSprite(layer.getTexture());

	}

	@SubscribeEvent
	public static void textureStichPost(TextureStitchEvent.Post event)
	{
		if(!event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS))
			return;
		ImmersiveEngineering.proxy.clearRenderCaches();
		RevolverCallbacks.retrieveRevolverTextures(event.getAtlas());
	}

	private final Map<BlockPos, IEBlockEntitySound> tileSoundMap = new HashMap<>();

	@Override
	public void handleTileSound(
			Supplier<SoundEvent> soundEvent, BlockEntity tile, boolean tileActive, float volume, float pitch
	)
	{
		BlockPos pos = tile.getBlockPos();
		IEBlockEntitySound sound = tileSoundMap.get(pos);
		if(sound==null&&tileActive)
		{
			if(tile instanceof ISoundBE&&mc().player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > ((ISoundBE)tile).getSoundRadiusSq())
				return;
			sound = ClientUtils.generatePositionedIESound(soundEvent.get(), volume, pitch, true, 0, pos);
			tileSoundMap.put(pos, sound);
		}
		else if(sound!=null&&(sound.donePlaying||!tileActive))
		{
			sound.donePlaying = true;
			mc().getSoundManager().stop(sound);
			tileSoundMap.remove(pos);
		}
	}

	@Override
	public void stopTileSound(String soundName, BlockEntity tile)
	{
		IEBlockEntitySound sound = tileSoundMap.get(tile.getBlockPos());
		if(sound!=null)
			mc().getSoundManager().stop(sound);
	}

	@SubscribeEvent
	public static void registerLayers(EntityRenderersEvent.AddLayers ev)
	{
		for(EntityRenderer<?> render : Minecraft.getInstance().getEntityRenderDispatcher().renderers.values())
		{
			if(render instanceof HumanoidMobRenderer<?, ?> hmr)
				addIELayer(hmr, ev.getEntityModels());
			else if(render instanceof ArmorStandRenderer asr)
				addIELayer(asr, ev.getEntityModels());
		}
		for(String skin : ev.getSkins())
		{
			LivingEntityRenderer<?, ?> render = ev.getSkin(skin);
			if(render!=null)
				addIELayer(render, ev.getEntityModels());
		}
		ShaderMinecartRenderer.overrideMinecartModels();
	}

	private static <T extends LivingEntity, M extends EntityModel<T>>
	void addIELayer(LivingEntityRenderer<T, M> render, EntityModelSet models)
	{
		render.addLayer(new IEBipedLayerRenderer<>(render, models));
	}

	@Override
	public Level getClientWorld()
	{
		return mc().level;
	}

	@Override
	public Player getClientPlayer()
	{
		return mc().player;
	}

	@Override
	public void reInitGui()
	{
		Screen currentScreen = mc().screen;
		if(currentScreen instanceof IEContainerScreen)
			currentScreen.init(mc(), currentScreen.width, currentScreen.height);
	}

	@Override
	public void resetManual()
	{
		if(mc().screen instanceof ManualScreen)
			mc().setScreen(null);
		if(ManualHelper.getManual()!=null)
			ManualHelper.getManual().reset();
	}

	static
	{
		IEApi.renderCacheClearers.add(ClocheRenderer::reset);
		IEApi.renderCacheClearers.add(WatermillRenderer::reset);
		IEApi.renderCacheClearers.add(WindmillRenderer::reset);
		IEApi.renderCacheClearers.add(BucketWheelRenderer::reset);
		IEApi.renderCacheClearers.add(ModelCoresample::clearCache);
		IEApi.renderCacheClearers.add(ModelPowerpack.CATENARY_DATA_CACHE::invalidateAll);
		IEApi.renderCacheClearers.add(FeedthroughModel.CACHE::invalidateAll);
		IEApi.renderCacheClearers.add(ConnectionRenderer::resetCache);
	}

	@Override
	public void clearRenderCaches()
	{
		for(Runnable r : IEApi.renderCacheClearers)
			r.run();
	}

	@Override
	public void startSkyhookSound(SkylineHookEntity hook)
	{
		Minecraft.getInstance().getSoundManager().play(new SkyhookSound(hook,
				new ResourceLocation(MODID, "skyhook")));
	}

	@Override
	public void openManual()
	{
		Minecraft.getInstance().setScreen(ManualHelper.getManual().getGui());
	}

	@Override
	public void openTileScreen(String guiId, BlockEntity tileEntity)
	{
		if(guiId.equals(Lib.GUIID_RedstoneConnector)&&tileEntity instanceof ConnectorRedstoneBlockEntity)
			Minecraft.getInstance().setScreen(new RedstoneConnectorScreen((ConnectorRedstoneBlockEntity)tileEntity, tileEntity.getBlockState().getBlock().getName()));

		if(guiId.equals(Lib.GUIID_RedstoneProbe)&&tileEntity instanceof ConnectorProbeBlockEntity)
			Minecraft.getInstance().setScreen(new RedstoneProbeScreen((ConnectorProbeBlockEntity)tileEntity, tileEntity.getBlockState().getBlock().getName()));
	}


	@SubscribeEvent
	public static void registerRenders(EntityRenderersEvent.RegisterRenderers event)
	{
		registerBERenders(event);
		registerEntityRenders(event);
	}

	private static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event)
	{
		registerEntityRenderingHandler(event, IEEntityTypes.REVOLVERSHOT, RevolvershotRenderer::new);
		registerEntityRenderingHandler(event, IEEntityTypes.FLARE_REVOLVERSHOT, RevolvershotRenderer::new);
		registerEntityRenderingHandler(event, IEEntityTypes.HOMING_REVOLVERSHOT, RevolvershotRenderer::new);
		registerEntityRenderingHandler(event, IEEntityTypes.WOLFPACK_SHOT, RevolvershotRenderer::new);
		registerEntityRenderingHandler(event, IEEntityTypes.SKYLINE_HOOK, NoneRenderer::new);
		registerEntityRenderingHandler(event, IEEntityTypes.CHEMTHROWER_SHOT, ChemthrowerShotRenderer::new);
		registerEntityRenderingHandler(event, IEEntityTypes.RAILGUN_SHOT, RailgunShotRenderer::new);
		registerEntityRenderingHandler(event, IEEntityTypes.EXPLOSIVE, IEExplosiveRenderer::new);
		registerEntityRenderingHandler(event, IEEntityTypes.FLUORESCENT_TUBE, FluorescentTubeRenderer::new);
		registerEntityRenderingHandler(event, IEEntityTypes.BARREL_MINECART, IEMinecartRenderer.provide(IEModelLayers.BARREL_MINECART));
		registerEntityRenderingHandler(event, IEEntityTypes.CRATE_MINECART, IEMinecartRenderer.provide(IEModelLayers.CRATE_MINECART));
		registerEntityRenderingHandler(event, IEEntityTypes.REINFORCED_CRATE_CART, IEMinecartRenderer.provide(IEModelLayers.REINFORCED_CRATE_CART));
		registerEntityRenderingHandler(event, IEEntityTypes.METAL_BARREL_CART, IEMinecartRenderer.provide(IEModelLayers.METAL_BARREL_CART));
		registerEntityRenderingHandler(event, IEEntityTypes.SAWBLADE, SawbladeRenderer::new);
	}

	private static void registerContainersAndScreens()
	{
		MenuScreens.register(IEMenuTypes.COKE_OVEN.getType(), CokeOvenScreen::new);
		MenuScreens.register(IEMenuTypes.ALLOY_SMELTER.getType(), AlloySmelterScreen::new);
		MenuScreens.register(IEMenuTypes.BLAST_FURNACE.getType(), BlastFurnaceScreen::new);
		MenuScreens.register(IEMenuTypes.BLAST_FURNACE_ADV.getType(), BlastFurnaceScreen.Advanced::new);
		MenuScreens.register(IEMenuTypes.CRAFTING_TABLE.getType(), CraftingTableScreen::new);
		MenuScreens.register(IEMenuTypes.WOODEN_CRATE.get(), CrateScreen.StandardCrate::new);
		registerTileScreen(IEMenuTypes.MOD_WORKBENCH, ModWorkbenchScreen::new);
		MenuScreens.register(IEMenuTypes.CIRCUIT_TABLE.getType(), CircuitTableScreen::new);
		MenuScreens.register(IEMenuTypes.ASSEMBLER.getType(), AssemblerScreen::new);
		registerTileScreen(IEMenuTypes.SORTER, SorterScreen::new);
		MenuScreens.register(IEMenuTypes.ITEM_BATCHER.getType(), ItemBatcherScreen::new);
		MenuScreens.register(IEMenuTypes.LOGIC_UNIT.getType(), LogicUnitScreen::new);
		registerTileScreen(IEMenuTypes.SQUEEZER, SqueezerScreen::new);
		MenuScreens.register(IEMenuTypes.FERMENTER.getType(), FermenterScreen::new);
		registerTileScreen(IEMenuTypes.REFINERY, RefineryScreen::new);
		MenuScreens.register(IEMenuTypes.ARC_FURNACE.getType(), ArcFurnaceScreen::new);
		MenuScreens.register(IEMenuTypes.AUTO_WORKBENCH.getType(), AutoWorkbenchScreen::new);
		registerTileScreen(IEMenuTypes.MIXER, MixerScreen::new);
		registerTileScreen(IEMenuTypes.GUN_TURRET, GunTurretScreen::new);
		registerTileScreen(IEMenuTypes.CHEM_TURRET, ChemTurretScreen::new);
		MenuScreens.register(IEMenuTypes.FLUID_SORTER.getType(), FluidSorterScreen::new);
		MenuScreens.register(IEMenuTypes.CLOCHE.getType(), ClocheScreen::new);
		MenuScreens.register(IEMenuTypes.TOOLBOX_BLOCK.getType(), ToolboxScreen::new);
		MenuScreens.register(IEMenuTypes.TOOLBOX.getType(), ToolboxScreen::new);

		registerScreen(IEMenuTypes.REVOLVER, RevolverScreen::new);
		registerScreen(IEMenuTypes.MAINTENANCE_KIT, MaintenanceKitScreen::new);

		MenuScreens.register(IEMenuTypes.CRATE_MINECART.get(), CrateScreen.EntityCrate::new);
	}

	private static <T extends BlockEntity>
	void registerBERenderNoContext(
			RegisterRenderers event, BlockEntityType<? extends T> type, Supplier<BlockEntityRenderer<T>> render
	)
	{
		event.registerBlockEntityRenderer(type, $ -> render.get());
	}

	public static void registerBERenders(RegisterRenderers event)
	{
		registerBERenderNoContext(event, IEBlockEntities.CHARGING_STATION.get(), ChargingStationRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.SAMPLE_DRILL.master(), SampleDrillRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.TESLACOIL.master(), TeslaCoilRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.TURRET_CHEM.master(), TurretRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.TURRET_GUN.master(), TurretRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.CLOCHE.master(), ClocheRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.BLASTFURNACE_PREHEATER.master(), BlastFurnacePreheaterRenderer::new);
		// MULTIBLOCKS
		registerBERenderNoContext(event, IEBlockEntities.METAL_PRESS.master(), MetalPressRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.CRUSHER.master(), CrusherRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.SAWMILL.master(), SawmillRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.SHEETMETAL_TANK.master(), SheetmetalTankRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.SILO.master(), SiloRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.SQUEEZER.master(), SqueezerRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.DIESEL_GENERATOR.master(), DieselGeneratorRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.BUCKET_WHEEL.master(), BucketWheelRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.ARC_FURNACE.master(), ArcFurnaceRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.AUTO_WORKBENCH.master(), AutoWorkbenchRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.BOTTLING_MACHINE.master(), BottlingMachineRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.MIXER.master(), MixerRenderer::new);
		//WOOD
		registerBERenderNoContext(event, IEBlockEntities.WATERMILL.master(), WatermillRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.WINDMILL.get(), WindmillRenderer::new);
		registerBERenderNoContext(event, IEBlockEntities.MOD_WORKBENCH.get(), ModWorkbenchRenderer::new);
		//STONE
		registerBERenderNoContext(event, IEBlockEntities.CORE_SAMPLE.get(), CoresampleRenderer::new);
		//CLOTH
		event.registerBlockEntityRenderer(IEBlockEntities.SHADER_BANNER.get(), ShaderBannerRenderer::new);
	}

	public static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>>
	void registerScreen(IEMenuTypes.ItemContainerType<C> type, ScreenConstructor<C, S> factory)
	{
		MenuScreens.register(type.getType(), factory);
	}

	public static <C extends IEBaseContainerOld<?>, S extends Screen & MenuAccess<C>>
	void registerTileScreen(BEContainer<?, C> type, ScreenConstructor<C, S> factory)
	{
		MenuScreens.register(type.getType(), factory);
	}

	private static void requestModelsAndTextures()
	{
		DynamicModelLoader.requestTexture(SawbladeRenderer.SAWBLADE);
		DynamicModelLoader.requestTexture(ArcFurnaceRenderer.HOT_METLA_FLOW);
		DynamicModelLoader.requestTexture(ArcFurnaceRenderer.HOT_METLA_STILL);
		DynamicModelLoader.requestTexture(RockcutterItem.TEXTURE);
		DynamicModelLoader.requestTexture(GrindingDiskItem.TEXTURE);
		DynamicModelLoader.requestTexture(new ResourceLocation(MODID, "block/wire"));
		DynamicModelLoader.requestTexture(new ResourceLocation(MODID, "block/shaders/greyscale_fire"));

		for(ResourceLocation rl : ModelConveyor.rl_casing)
			DynamicModelLoader.requestTexture(rl);
		DynamicModelLoader.requestTexture(ConveyorHandler.textureConveyorColour);
		DynamicModelLoader.requestTexture(ConveyorBase.texture_off);
		DynamicModelLoader.requestTexture(ConveyorBase.texture_on);
		DynamicModelLoader.requestTexture(DropConveyor.texture_off);
		DynamicModelLoader.requestTexture(DropConveyor.texture_on);
		DynamicModelLoader.requestTexture(VerticalConveyor.texture_off);
		DynamicModelLoader.requestTexture(VerticalConveyor.texture_on);
		DynamicModelLoader.requestTexture(SplitConveyor.texture_off);
		DynamicModelLoader.requestTexture(SplitConveyor.texture_on);
		DynamicModelLoader.requestTexture(SplitConveyorRender.texture_casing);
		DynamicModelLoader.requestTexture(RedstoneConveyorRender.texture_panel);

		DynamicModelLoader.requestTexture(new ResourceLocation(MODID, "item/shader_slot"));
	}

	public static void populateAPI()
	{
		SetRestrictedField.startInitializing(true);
		VertexBufferHolder.addToAPI();
		ManualHelper.MAKE_MULTIBLOCK_ELEMENT.setValue(mb -> new ManualElementMultiblock(ManualHelper.getManual(), mb));
		ManualHelper.MAKE_BLUEPRINT_ELEMENT.setValue(
				stacks -> new ManualElementBlueprint(ManualHelper.getManual(), stacks)
		);
		IEManual.initManual();
		SetRestrictedField.lock(true);
	}
}
