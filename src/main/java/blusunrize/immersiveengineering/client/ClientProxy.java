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
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.gui.*;
import blusunrize.immersiveengineering.client.manual.ManualElementBlueprint;
import blusunrize.immersiveengineering.client.manual.ManualElementMultiblock;
import blusunrize.immersiveengineering.client.models.*;
import blusunrize.immersiveengineering.client.models.ModelConveyor.ConveyorLoader;
import blusunrize.immersiveengineering.client.models.ModelCoresample.CoresampleLoader;
import blusunrize.immersiveengineering.client.models.PotionBucketModel.Loader;
import blusunrize.immersiveengineering.client.models.connection.BakedConnectionModel;
import blusunrize.immersiveengineering.client.models.connection.ConnectionLoader;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughLoader;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughModel;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.client.models.split.SplitModelLoader;
import blusunrize.immersiveengineering.client.render.IEBipedLayerRenderer;
import blusunrize.immersiveengineering.client.render.entity.*;
import blusunrize.immersiveengineering.client.render.tile.*;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel.ModelType;
import blusunrize.immersiveengineering.client.utils.VertexBufferHolder;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorProbeTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorRedstoneTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.crafting.RecipeReloadListener;
import blusunrize.immersiveengineering.common.entities.IEEntityTypes;
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import blusunrize.immersiveengineering.common.gui.IEBaseContainer;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.items.DrillheadItem.DrillHeadPerm;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.RockcutterItem;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.sound.IETileSound;
import blusunrize.immersiveengineering.common.util.sound.SkyhookSound;
import blusunrize.lib.manual.gui.ManualScreen;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmlclient.registry.RenderingRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.client.ClientUtils.mc;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Bus.MOD)
public class ClientProxy extends CommonProxy
{
	public static void modConstruction()
	{
		// Apparently this runs in data generation runs... but registering model loaders causes NPEs there
		if(Minecraft.getInstance()!=null)
		{
			populateAPI();
			requestModelsAndTextures();
		}
	}

	@SubscribeEvent
	public static void registerModelLoaders(ModelRegistryEvent ev) {ModelLoaderRegistry.registerLoader(IEOBJLoader.LOADER_NAME, IEOBJLoader.instance);
			ModelLoaderRegistry.registerLoader(ConnectionLoader.LOADER_NAME, new ConnectionLoader());
			ModelLoaderRegistry.registerLoader(ModelConfigurableSides.Loader.NAME, new ModelConfigurableSides.Loader());
			ModelLoaderRegistry.registerLoader(ConveyorLoader.LOCATION, new ConveyorLoader());
			ModelLoaderRegistry.registerLoader(CoresampleLoader.LOCATION, new CoresampleLoader());
			ModelLoaderRegistry.registerLoader(FeedthroughLoader.LOCATION, new FeedthroughLoader());
			ModelLoaderRegistry.registerLoader(SplitModelLoader.LOCATION, new SplitModelLoader());
			ModelLoaderRegistry.registerLoader(Loader.LOADER_NAME, new PotionBucketModel.Loader());

	}

	public static boolean stencilEnabled = false;

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent ev)
	{
		if(IEClientConfig.stencilBufferEnabled.get())
			ev.enqueueWork(() -> {
				Minecraft.getInstance().getMainRenderTarget().enableStencil();
				stencilEnabled = true;
			});
		registerContainersAndScreens();
		registerEntityRenderers();
		registerBERenders();
		IEKeybinds.register();
		ShaderHelper.initShaders();
		IEDefaultColourHandlers.register();

		ClientEventHandler handler = new ClientEventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		((ReloadableResourceManager)mc().getResourceManager()).registerReloadListener(handler);

		MinecraftForge.EVENT_BUS.register(new RecipeReloadListener(null));

		/*Render Layers*/
		Map<String, EntityRenderer<? extends Player>> skinMap = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
		if (skinMap.get("default") instanceof PlayerRenderer render)
			render.addLayer(new IEBipedLayerRenderer<>(render));
		if (skinMap.get("slim") instanceof PlayerRenderer render)
			render.addLayer(new IEBipedLayerRenderer<>(render));
		IEManual.addIEManualEntries();
	}

	private static <T extends Entity, T2 extends T> void registerEntityRenderingHandler(
			Supplier<EntityType<T2>> type, EntityRendererProvider<T> renderer
	)
	{
		RenderingRegistry.registerEntityRenderingHandler(type.get(), renderer);
	}

	//TODO are these here rather than in ClientEventHandler for any particular reason???
	@SubscribeEvent
	public static void textureStichPre(TextureStitchEvent.Pre event)
	{
		if(!event.getMap().location().equals(InventoryMenu.BLOCK_ATLAS))
			return;
		IELogger.info("Stitching Revolver Textures!");
		RevolverItem.addRevolverTextures(event);
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
		if(!event.getMap().location().equals(InventoryMenu.BLOCK_ATLAS))
			return;
		ImmersiveEngineering.proxy.clearRenderCaches();
		RevolverItem.retrieveRevolverTextures(event.getMap());
		for(DrillHeadPerm p : DrillHeadPerm.ALL_PERMS)
		{
			p.sprite = event.getMap().getSprite(p.texture);
			Preconditions.checkNotNull(p.sprite);
		}
		WireType.iconDefaultWire = event.getMap().getSprite(new ResourceLocation(MODID, "block/wire"));
	}

	private final Map<BlockPos, IETileSound> tileSoundMap = new HashMap<>();

	@Override
	public void handleTileSound(SoundEvent soundEvent, BlockEntity tile, boolean tileActive, float volume, float pitch)
	{
		BlockPos pos = tile.getBlockPos();
		IETileSound sound = tileSoundMap.get(pos);
		if(sound==null&&tileActive)
		{
			if(tile instanceof ISoundTile&&mc().player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > ((ISoundTile)tile).getSoundRadiusSq())
				return;
			sound = ClientUtils.generatePositionedIESound(soundEvent, volume, pitch, true, 0, pos);
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
		IETileSound sound = tileSoundMap.get(tile.getBlockPos());
		if(sound!=null)
			mc().getSoundManager().stop(sound);
	}

	@Override
	public void onWorldLoad()
	{
		if(!ShaderMinecartRenderer.rendersReplaced)
		{
			EntityRenderDispatcher rendererManager = mc().getEntityRenderDispatcher();
			for(EntityType<?> type : rendererManager.renderers.keySet())
				ShaderMinecartRenderer.overrideModelIfMinecart(type);
			ShaderMinecartRenderer.rendersReplaced = true;
		}
		if(!IEBipedLayerRenderer.rendersAssigned)
		{
			for(Object render : mc().getEntityRenderDispatcher().renderers.values())
				if(HumanoidMobRenderer.class.isAssignableFrom(render.getClass()))
					addIELayer((HumanoidMobRenderer<?, ?>)render);
				else if(ArmorStandRenderer.class.isAssignableFrom(render.getClass()))
					((ArmorStandRenderer)render).addLayer(new IEBipedLayerRenderer<>((ArmorStandRenderer)render));
			IEBipedLayerRenderer.rendersAssigned = true;
		}
	}

	private static <T extends Mob, M extends HumanoidModel<T>> void addIELayer(HumanoidMobRenderer<T, M> render)
	{
		render.addLayer(new IEBipedLayerRenderer<>(render));
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
		IEApi.renderCacheClearers.add(IESmartObjModel.modelCache::invalidateAll);
		IEApi.renderCacheClearers.add(IESmartObjModel.cachedBakedItemModels::invalidateAll);
		IEApi.renderCacheClearers.add(BakedConnectionModel.cache::invalidateAll);
		IEApi.renderCacheClearers.add(ModelConveyor.modelCache::clear);
		IEApi.renderCacheClearers.add(ModelConfigurableSides.modelCache::invalidateAll);
		IEApi.renderCacheClearers.add(FluidPipeTileEntity.cachedOBJStates::clear);
		IEApi.renderCacheClearers.add(ClocheRenderer::reset);
		IEApi.renderCacheClearers.add(WatermillRenderer::reset);
		IEApi.renderCacheClearers.add(WindmillRenderer::reset);
		IEApi.renderCacheClearers.add(BucketWheelRenderer::reset);
		IEApi.renderCacheClearers.add(ModelCoresample::clearCache);
		IEApi.renderCacheClearers.add(ModelItemDynamicOverride.modelCache::clear);
		IEApi.renderCacheClearers.add(ModelPowerpack.catenaryCacheLeft::invalidateAll);
		IEApi.renderCacheClearers.add(ModelPowerpack.catenaryCacheRight::invalidateAll);
		IEApi.renderCacheClearers.add(FeedthroughModel.CACHE::invalidateAll);
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
		if(guiId.equals(Lib.GUIID_RedstoneConnector)&&tileEntity instanceof ConnectorRedstoneTileEntity)
			Minecraft.getInstance().setScreen(new RedstoneConnectorScreen((ConnectorRedstoneTileEntity)tileEntity, tileEntity.getBlockState().getBlock().getName()));

		if(guiId.equals(Lib.GUIID_RedstoneProbe)&&tileEntity instanceof ConnectorProbeTileEntity)
			Minecraft.getInstance().setScreen(new RedstoneProbeScreen((ConnectorProbeTileEntity)tileEntity, tileEntity.getBlockState().getBlock().getName()));
	}

	private static void registerEntityRenderers()
	{
		registerEntityRenderingHandler(IEEntityTypes.REVOLVERSHOT, RevolvershotRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.FLARE_REVOLVERSHOT, RevolvershotRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.HOMING_REVOLVERSHOT, RevolvershotRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.WOLFPACK_SHOT, RevolvershotRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.SKYLINE_HOOK, NoneRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.CHEMTHROWER_SHOT, ChemthrowerShotRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.RAILGUN_SHOT, RailgunShotRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.EXPLOSIVE, IEExplosiveRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.FLUORESCENT_TUBE, FluorescentTubeRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.BARREL_MINECART, IEMinecartRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.CRATE_MINECART, IEMinecartRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.REINFORCED_CRATE_CART, IEMinecartRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.METAL_BARREL_CART, IEMinecartRenderer::new);
		registerEntityRenderingHandler(IEEntityTypes.SAWBLADE, SawbladeRenderer::new);
	}

	private static void registerContainersAndScreens()
	{
		registerTileScreen(IEContainerTypes.COKE_OVEN, CokeOvenScreen::new);
		registerTileScreen(IEContainerTypes.ALLOY_SMELTER, AlloySmelterScreen::new);
		registerTileScreen(IEContainerTypes.BLAST_FURNACE, BlastFurnaceScreen::new);
		registerTileScreen(IEContainerTypes.CRAFTING_TABLE, CraftingTableScreen::new);
		registerTileScreen(IEContainerTypes.WOODEN_CRATE, CrateScreen.StandardCrate::new);
		registerTileScreen(IEContainerTypes.MOD_WORKBENCH, ModWorkbenchScreen::new);
		registerTileScreen(IEContainerTypes.CIRCUIT_TABLE, CircuitTableScreen::new);
		registerTileScreen(IEContainerTypes.ASSEMBLER, AssemblerScreen::new);
		registerTileScreen(IEContainerTypes.SORTER, SorterScreen::new);
		registerTileScreen(IEContainerTypes.ITEM_BATCHER, ItemBatcherScreen::new);
		registerTileScreen(IEContainerTypes.LOGIC_UNIT, LogicUnitScreen::new);
		registerTileScreen(IEContainerTypes.SQUEEZER, SqueezerScreen::new);
		registerTileScreen(IEContainerTypes.FERMENTER, FermenterScreen::new);
		registerTileScreen(IEContainerTypes.REFINERY, RefineryScreen::new);
		registerTileScreen(IEContainerTypes.ARC_FURNACE, ArcFurnaceScreen::new);
		registerTileScreen(IEContainerTypes.AUTO_WORKBENCH, AutoWorkbenchScreen::new);
		registerTileScreen(IEContainerTypes.MIXER, MixerScreen::new);
		registerTileScreen(IEContainerTypes.GUN_TURRET, GunTurretScreen::new);
		registerTileScreen(IEContainerTypes.CHEM_TURRET, ChemTurretScreen::new);
		registerTileScreen(IEContainerTypes.FLUID_SORTER, FluidSorterScreen::new);
		registerTileScreen(IEContainerTypes.CLOCHE, ClocheScreen::new);
		registerTileScreen(IEContainerTypes.TOOLBOX_BLOCK, ToolboxBlockScreen::new);

		registerScreen(IEContainerTypes.TOOLBOX, ToolboxScreen::new);
		registerScreen(IEContainerTypes.REVOLVER, RevolverScreen::new);
		registerScreen(IEContainerTypes.MAINTENANCE_KIT, MaintenanceKitScreen::new);

		registerScreen(IEContainerTypes.CRATE_MINECART, CrateScreen.EntityCrate::new);
	}

	private static <T extends BlockEntity>
	void registerNoContext(BlockEntityType<? extends T> type, Supplier<BlockEntityRenderer<T>> render) {
		BlockEntityRenderers.register(type, $ -> render.get());
	}

	private static void registerBERenders()
	{
		registerNoContext(IETileTypes.CHARGING_STATION.get(), ChargingStationRenderer::new);
		registerNoContext(IETileTypes.SAMPLE_DRILL.get(), SampleDrillRenderer::new);
		registerNoContext(IETileTypes.TESLACOIL.get(), TeslaCoilRenderer::new);
		registerNoContext(IETileTypes.TURRET_CHEM.get(), TurretRenderer::new);
		registerNoContext(IETileTypes.TURRET_GUN.get(), TurretRenderer::new);
		registerNoContext(IETileTypes.CLOCHE.get(), ClocheRenderer::new);
		// MULTIBLOCKS
		registerNoContext(IETileTypes.METAL_PRESS.get(), MetalPressRenderer::new);
		registerNoContext(IETileTypes.CRUSHER.get(), CrusherRenderer::new);
		registerNoContext(IETileTypes.SAWMILL.get(), SawmillRenderer::new);
		registerNoContext(IETileTypes.SHEETMETAL_TANK.get(), SheetmetalTankRenderer::new);
		registerNoContext(IETileTypes.SILO.get(), SiloRenderer::new);
		registerNoContext(IETileTypes.SQUEEZER.get(), SqueezerRenderer::new);
		registerNoContext(IETileTypes.DIESEL_GENERATOR.get(), DieselGeneratorRenderer::new);
		registerNoContext(IETileTypes.BUCKET_WHEEL.get(), BucketWheelRenderer::new);
		registerNoContext(IETileTypes.ARC_FURNACE.get(), ArcFurnaceRenderer::new);
		registerNoContext(IETileTypes.AUTO_WORKBENCH.get(), AutoWorkbenchRenderer::new);
		registerNoContext(IETileTypes.BOTTLING_MACHINE.get(), BottlingMachineRenderer::new);
		registerNoContext(IETileTypes.MIXER.get(), MixerRenderer::new);
		//WOOD
		registerNoContext(IETileTypes.WATERMILL.get(), WatermillRenderer::new);
		registerNoContext(IETileTypes.WINDMILL.get(), WindmillRenderer::new);
		registerNoContext(IETileTypes.MOD_WORKBENCH.get(), ModWorkbenchRenderer::new);
		//STONE
		registerNoContext(IETileTypes.CORE_SAMPLE.get(), CoresampleRenderer::new);
		//CLOTH
		registerNoContext(IETileTypes.SHADER_BANNER.get(), ShaderBannerRenderer::new);
	}

	public static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>>
	void registerScreen(IEContainerTypes.EntityContainerType<?, C> type, ScreenConstructor<C, S> factory)
	{
		MenuScreens.register(type.getType(), factory);
	}

	public static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>>
	void registerScreen(IEContainerTypes.ItemContainerType<C> type, ScreenConstructor<C, S> factory)
	{
		MenuScreens.register(type.getType(), factory);
	}

	public static <C extends IEBaseContainer<?>, S extends Screen & MenuAccess<C>>
	void registerTileScreen(IEContainerTypes.TileContainer<?, C> type, ScreenConstructor<C, S> factory)
	{
		MenuScreens.register(type.getType(), factory);
	}

	@Override
	public Item.Properties useIEOBJRenderer(Item.Properties props)
	{
		return super.useIEOBJRenderer(props);//TODO .setISTER(() -> () -> IEOBJItemRenderer.INSTANCE);
	}

	private static void requestModelsAndTextures()
	{
		DynamicModelLoader.requestTexture(SawbladeRenderer.SAWBLADE);
		DynamicModelLoader.requestTexture(ArcFurnaceRenderer.HOT_METLA_FLOW);
		DynamicModelLoader.requestTexture(ArcFurnaceRenderer.HOT_METLA_STILL);
		for(DrillHeadPerm p : DrillHeadPerm.ALL_PERMS)
			DynamicModelLoader.requestTexture(p.texture);
		DynamicModelLoader.requestTexture(RockcutterItem.texture);
		DynamicModelLoader.requestTexture(new ResourceLocation(MODID, "block/wire"));
		DynamicModelLoader.requestTexture(new ResourceLocation(MODID, "block/shaders/greyscale_fire"));

		for(ResourceLocation rl : ModelConveyor.rl_casing)
			DynamicModelLoader.requestTexture(rl);
		DynamicModelLoader.requestTexture(ConveyorHandler.textureConveyorColour);
		DynamicModelLoader.requestTexture(BasicConveyor.texture_off);
		DynamicModelLoader.requestTexture(BasicConveyor.texture_on);
		DynamicModelLoader.requestTexture(DropConveyor.texture_off);
		DynamicModelLoader.requestTexture(DropConveyor.texture_on);
		DynamicModelLoader.requestTexture(VerticalConveyor.texture_off);
		DynamicModelLoader.requestTexture(VerticalConveyor.texture_on);
		DynamicModelLoader.requestTexture(SplitConveyor.texture_off);
		DynamicModelLoader.requestTexture(SplitConveyor.texture_on);
		DynamicModelLoader.requestTexture(SplitConveyor.texture_casing);
		DynamicModelLoader.requestTexture(RedstoneConveyor.texture_panel);

		DynamicModelLoader.requestTexture(new ResourceLocation(MODID, "item/shader_slot"));

		ArcFurnaceRenderer.ELECTRODES = DynamicModel.createSided(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/arc_furnace_electrodes.obj.ie"),
				"arc_furnace_electrodes", ModelType.IE_OBJ
		);
		AutoWorkbenchRenderer.DYNAMIC = DynamicModel.createSided(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/auto_workbench_animated.obj.ie"),
				"auto_workbench_animated", ModelType.IE_OBJ
		);
		BottlingMachineRenderer.DYNAMIC = DynamicModel.createSided(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/bottling_machine_animated.obj.ie"),
				"bottling_machine", ModelType.IE_OBJ
		);
		BucketWheelRenderer.WHEEL = DynamicModel.createSimple(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/bucket_wheel.obj.ie"),
				"bucket_wheel", ModelType.IE_OBJ
		);
		CrusherRenderer.BARREL = DynamicModel.createSided(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/crusher_drum.obj"),
				"crusher_barrel", ModelType.OBJ
		);
		SawmillRenderer.BLADE = DynamicModel.createSided(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/sawmill_animated.obj"),
				"blade", ModelType.OBJ
		);
		DieselGeneratorRenderer.FAN = DynamicModel.createSided(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/diesel_generator_fan.obj"),
				"diesel_gen", ModelType.OBJ
		);
		MetalPressRenderer.PISTON = DynamicModel.createSimple(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/metal_press_piston.obj"),
				"metal_press_piston", ModelType.OBJ
		);
		MixerRenderer.AGITATOR = DynamicModel.createSided(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/mixer_agitator.obj"),
				"mixer", ModelType.OBJ
		);
		SampleDrillRenderer.DRILL = DynamicModel.createSimple(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_device/core_drill_center.obj"),
				"sample_drill", ModelType.OBJ
		);
		SqueezerRenderer.PISTON = DynamicModel.createSided(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_multiblock/squeezer_piston.obj"),
				"squeezer", ModelType.OBJ
		);
		WatermillRenderer.MODEL = DynamicModel.createSimple(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/wooden_device/watermill.obj.ie"),
				"watermill", ModelType.IE_OBJ
		);
		WindmillRenderer.MODEL = DynamicModel.createSimple(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/wooden_device/windmill.obj.ie"),
				"windmill", ModelType.IE_OBJ
		);
		RedstoneConveyor.MODEL_PANEL = DynamicModel.createSided(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor_redstone.obj.ie"),
				"conveyor_redstone", ModelType.IE_OBJ
		);
		SawbladeRenderer.MODEL = DynamicModel.createSimple(
				new ResourceLocation(ImmersiveEngineering.MODID, "item/buzzsaw_diesel.obj.ie"),
				"sawblade_entity", ModelType.IE_OBJ
		);
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
