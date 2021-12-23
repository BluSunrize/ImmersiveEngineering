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
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.fx.FluidSplashParticle.Data;
import blusunrize.immersiveengineering.client.fx.FractalParticle;
import blusunrize.immersiveengineering.client.fx.IEParticles;
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
import blusunrize.immersiveengineering.client.models.multilayer.MultiLayerLoader;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.client.models.split.SplitModelLoader;
import blusunrize.immersiveengineering.client.render.IEBipedLayerRenderer;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.client.render.entity.*;
import blusunrize.immersiveengineering.client.render.tile.*;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel.ModelType;
import blusunrize.immersiveengineering.client.utils.VertexBufferHolder;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IColouredBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.RecipeReloadListener;
import blusunrize.immersiveengineering.common.entities.*;
import blusunrize.immersiveengineering.common.gui.GuiHandler;
import blusunrize.immersiveengineering.common.items.DrillheadItem.DrillHeadPerm;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.RockcutterItem;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.util.sound.IETickableSound;
import blusunrize.immersiveengineering.common.util.sound.IETileSound;
import blusunrize.immersiveengineering.common.util.sound.SkyhookSound;
import blusunrize.lib.manual.gui.ManualScreen;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.client.ClientUtils.mc;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Bus.MOD)
public class ClientProxy extends CommonProxy
{
	public static KeyMapping keybind_magnetEquip = new KeyMapping("key.immersiveengineering.magnetEquip", GLFW.GLFW_KEY_S, "key.categories.immersiveengineering");
	public static KeyMapping keybind_chemthrowerSwitch = new KeyMapping("key.immersiveengineering.chemthrowerSwitch", -1, "key.categories.immersiveengineering");
	public static KeyMapping keybind_railgunZoom = new KeyMapping("key.immersiveengineering.railgunZoom", InputConstants.Type.MOUSE, 2, "key.categories.immersiveengineering");

	@Override
	public void modConstruction()
	{
		super.modConstruction();
		// Apparently this runs in data generation runs... but registering model loaders causes NPEs there
		if(Minecraft.getInstance()!=null)
		{
			populateAPI();
			requestModelsAndTextures();
		}
	}

	@SubscribeEvent
	public static void registerModelLoaders(ModelRegistryEvent ev) {
		ModelLoaderRegistry.registerLoader(IEOBJLoader.LOADER_NAME, IEOBJLoader.instance);
		ModelLoaderRegistry.registerLoader(ConnectionLoader.LOADER_NAME, new ConnectionLoader());
		ModelLoaderRegistry.registerLoader(ModelConfigurableSides.Loader.NAME, new ModelConfigurableSides.Loader());
		ModelLoaderRegistry.registerLoader(ConveyorLoader.LOCATION, new ConveyorLoader());
		ModelLoaderRegistry.registerLoader(CoresampleLoader.LOCATION, new CoresampleLoader());
		ModelLoaderRegistry.registerLoader(MultiLayerLoader.LOCATION, new MultiLayerLoader());
		ModelLoaderRegistry.registerLoader(FeedthroughLoader.LOCATION, new FeedthroughLoader());
		ModelLoaderRegistry.registerLoader(SplitModelLoader.LOCATION, new SplitModelLoader());
		ModelLoaderRegistry.registerLoader(Loader.LOADER_NAME, new PotionBucketModel.Loader());
	}

	public static boolean stencilEnabled = false;

	@Override
	public void preInit(ParallelDispatchEvent ev)
	{
		if(IEClientConfig.stencilBufferEnabled.get())
			ev.enqueueWork(() -> {
				Minecraft.getInstance().getMainRenderTarget().enableStencil();
				stencilEnabled = true;
			});

		RenderingRegistry.registerEntityRenderingHandler(RevolvershotEntity.TYPE, RevolvershotRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(RevolvershotFlareEntity.TYPE, RevolvershotRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(RevolvershotHomingEntity.TYPE, RevolvershotRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(WolfpackShotEntity.TYPE, RevolvershotRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(SkylineHookEntity.TYPE, NoneRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(ChemthrowerShotEntity.TYPE, ChemthrowerShotRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(RailgunShotEntity.TYPE, RailgunShotRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(IEExplosiveEntity.TYPE, IEExplosiveRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(FluorescentTubeEntity.TYPE, FluorescentTubeRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(BarrelMinecartEntity.TYPE, IEMinecartRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(CrateMinecartEntity.TYPE, IEMinecartRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(ReinforcedCrateMinecartEntity.TYPE, IEMinecartRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(MetalBarrelMinecartEntity.TYPE, IEMinecartRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(SawbladeEntity.TYPE, SawbladeRenderer::new);
	}

	@Override
	public void preInitEnd()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try
			{
				compat.clientPreInit();
			} catch(Exception exception)
			{
				IELogger.error("Compat module for "+compat+" could not be client pre-initialized");
			}
	}

	@Override
	public void init()
	{
		super.init();
		ClientEventHandler handler = new ClientEventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		((ReloadableResourceManager)mc().getResourceManager()).registerReloadListener(handler);

		MinecraftForge.EVENT_BUS.register(new RecipeReloadListener(null));

		IKeyConflictContext noKeyConflict = new IKeyConflictContext()
		{
			@Override
			public boolean isActive()
			{
				return mc().screen==null;
			}

			@Override
			public boolean conflicts(IKeyConflictContext other)
			{
				return false;
			}
		};
		keybind_magnetEquip.setKeyConflictContext(noKeyConflict);
		ClientRegistry.registerKeyBinding(keybind_magnetEquip);

		keybind_railgunZoom.setKeyConflictContext(new ItemKeybindConflictContext(
				(stack, player) -> stack.getItem() instanceof IZoomTool&&((IZoomTool)stack.getItem()).canZoom(stack, player))
		);
		ClientRegistry.registerKeyBinding(keybind_railgunZoom);

		keybind_chemthrowerSwitch.setKeyConflictContext(KeyConflictContext.IN_GAME);
		ClientRegistry.registerKeyBinding(keybind_chemthrowerSwitch);

		ShaderHelper.initShaders();

		TeslaCoilTileEntity.effectMap = ArrayListMultimap.create();

		ClientRegistry.bindTileEntityRenderer(IETileTypes.CHARGING_STATION.get(), ChargingStationRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.SAMPLE_DRILL.get(), SampleDrillRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.TESLACOIL.get(), TeslaCoilRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.TURRET_CHEM.get(), TurretRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.TURRET_GUN.get(), TurretRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.CLOCHE.get(), ClocheRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.BLASTFURNACE_PREHEATER.get(), BlastFurnacePreheaterRenderer::new);
		// MULTIBLOCKS
		ClientRegistry.bindTileEntityRenderer(IETileTypes.METAL_PRESS.get(), MetalPressRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.CRUSHER.get(), CrusherRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.SAWMILL.get(), SawmillRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.SHEETMETAL_TANK.get(), SheetmetalTankRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.SILO.get(), SiloRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.SQUEEZER.get(), SqueezerRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.DIESEL_GENERATOR.get(), DieselGeneratorRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.BUCKET_WHEEL.get(), BucketWheelRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.ARC_FURNACE.get(), ArcFurnaceRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.AUTO_WORKBENCH.get(), AutoWorkbenchRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.BOTTLING_MACHINE.get(), BottlingMachineRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.MIXER.get(), MixerRenderer::new);
		//WOOD
		ClientRegistry.bindTileEntityRenderer(IETileTypes.WATERMILL.get(), WatermillRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.WINDMILL.get(), WindmillRenderer::new);
		ClientRegistry.bindTileEntityRenderer(IETileTypes.MOD_WORKBENCH.get(), ModWorkbenchRenderer::new);
		//STONE
		ClientRegistry.bindTileEntityRenderer(IETileTypes.CORE_SAMPLE.get(), CoresampleRenderer::new);
		//CLOTH
		ClientRegistry.bindTileEntityRenderer(IETileTypes.SHADER_BANNER.get(), ShaderBannerRenderer::new);

		/*Colours*/
		for(Item item : IEContent.registeredIEItems)
			if(item instanceof IColouredItem&&((IColouredItem)item).hasCustomItemColours())
				mc().getItemColors().register(IEDefaultColourHandlers.INSTANCE, item);
		for(Block block : IEContent.registeredIEBlocks)
			if(block instanceof IColouredBlock&&((IColouredBlock)block).hasCustomBlockColours())
				mc().getBlockColors().register(IEDefaultColourHandlers.INSTANCE, block);

		/*Render Layers*/
		Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
		PlayerRenderer render = skinMap.get("default");
		render.addLayer(new IEBipedLayerRenderer<>(render));
		render = skinMap.get("slim");
		render.addLayer(new IEBipedLayerRenderer<>(render));
	}

	@Override
	public void initEnd()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try
			{
				compat.clientInit();
			} catch(Exception exception)
			{
				IELogger.error("Compat module for "+compat+" could not be client pre-initialized");
			}
	}

	@Override
	public void postInit()
	{
		IEManual.addIEManualEntries();

		//TODO ClientCommandHandler.instance.registerCommand(new CommandHandler(true));
	}

	@Override
	public void postInitEnd()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try
			{
				compat.clientPostInit();
			} catch(Exception exception)
			{
				IELogger.error("Compat module for "+compat+" could not be client pre-initialized");
			}
	}

	@Override
	public void serverStarting()
	{
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

	public void registerItemModel(Item item, String path, String renderCase)
	{
		Minecraft.getInstance().getItemRenderer().getItemModelShaper().register(item, new ModelResourceLocation(path, renderCase));
	}

	public static String getPropertyString(Map<Property, Comparable> propertyMap)
	{
		StringBuilder stringbuilder = new StringBuilder();
		for(Entry<Property, Comparable> entry : propertyMap.entrySet())
		{
			if(stringbuilder.length()!=0)
				stringbuilder.append(",");
			Property iproperty = entry.getKey();
			Comparable comparable = entry.getValue();
			stringbuilder.append(iproperty.getName());
			stringbuilder.append("=");
			stringbuilder.append(iproperty.getName(comparable));
		}
		if(stringbuilder.length()==0)
			stringbuilder.append("normal");
		return stringbuilder.toString();
	}


	HashMap<String, IETickableSound> tickableSoundMap = new HashMap<>();
	HashMap<BlockPos, IETileSound> tileSoundMap = new HashMap<>();

	@Override
	public boolean isSoundPlaying(String key)
	{
		return tickableSoundMap.containsKey(key);
	}

	@Override
	public void playTickableSound(SoundEvent soundEvent, SoundSource category, String key, float volume, float pitch, Supplier<Boolean> tickFunction)
	{
		IETickableSound sound = new IETickableSound(soundEvent, category, volume, pitch, tickFunction,
				ieTickableSound -> tickableSoundMap.remove(key));
		mc().getSoundManager().play(sound);
		tickableSoundMap.put(key, sound);
	}

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
					((HumanoidMobRenderer)render).addLayer(new IEBipedLayerRenderer<>((HumanoidMobRenderer)render));
				else if(ArmorStandRenderer.class.isAssignableFrom(render.getClass()))
					((ArmorStandRenderer)render).addLayer(new IEBipedLayerRenderer<>((ArmorStandRenderer)render));
			IEBipedLayerRenderer.rendersAssigned = true;
		}
	}

	@Override
	public void spawnBucketWheelFX(BucketWheelTileEntity tile, ItemStack stack)
	{
		if(stack!=null&&IEServerConfig.MACHINES.excavator_particles.get())
		{
			Direction facing = tile.getFacing();
			int sign = (tile.getIsMirrored()^facing.getAxisDirection()==AxisDirection.NEGATIVE)?1: -1;
			double x = tile.getBlockPos().getX()+.5;
			double y = tile.getBlockPos().getY()+2.5;
			double z = tile.getBlockPos().getZ()+.5;
			double fixPosOffset = .5*sign;
			double fixVelOffset = .075*sign;
			for(int i = 0; i < 16; i++)
			{
				double mX = (tile.getWorldNonnull().random.nextDouble()-.5)*.01;
				double mY = tile.getLevel().random.nextDouble()*-0.05D;
				double mZ = (tile.getWorldNonnull().random.nextDouble()-.5)*.01;
				double rndPosOffset = .2*(tile.getWorldNonnull().random.nextDouble()-.5);

				Particle particle;

				if(facing.getAxis()==Axis.X)
					particle = new BreakingItemParticle.Provider().createParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
							(ClientLevel) tile.getWorldNonnull(), x+fixPosOffset, y, z+rndPosOffset, mX+fixVelOffset, mY, mZ);
				else
					particle = new BreakingItemParticle.Provider().createParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
							(ClientLevel) tile.getWorldNonnull(), x+rndPosOffset, y, z+fixPosOffset, mX, mY, mZ+fixVelOffset);

				mc().particleEngine.add(particle);
			}
		}
	}

	//TODO move to commonProxy or even use directly
	@Override
	public void spawnRedstoneFX(Level world, double x, double y, double z, double mx, double my, double mz, float size, float r, float g, float b)
	{
		world.addParticle(new DustParticleOptions(r, g, b, size), x, y, z, mx, my, mz);
	}

	@Override
	public void spawnFluidSplashFX(Level world, FluidStack fs, double x, double y, double z, double mx, double my, double mz)
	{
		world.addParticle(new Data(fs.getFluid()), x, y, z, mx, my, mz);
	}

	@Override
	public void spawnBubbleFX(Level world, FluidStack fs, double x, double y, double z, double mx, double my, double mz)
	{
		world.addParticle(IEParticles.IE_BUBBLE, x, y, z, mx, my, mz);
	}

	@Override
	public void spawnFractalFX(Level world, double x, double y, double z, Vec3 direction, double scale, int prefixColour, float[][] colour)
	{
		if(prefixColour >= 0)
			colour = prefixColour==1?FractalParticle.COLOUR_ORANGE: prefixColour==2?FractalParticle.COLOUR_RED: FractalParticle.COLOUR_LIGHTNING;
		FractalParticle.Data particle = new FractalParticle.Data(direction, scale, 10, 16, colour[0], colour[1]);
		world.addParticle(particle, x, y, z, 0, 0, 0);
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
	public String getNameFromUUID(String uuid)
	{
		return mc().getMinecraftSessionService().fillProfileProperties(new GameProfile(UUID.fromString(uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")), null), false).getName();
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
	public void openTileScreen(ResourceLocation guiId, BlockEntity tileEntity)
	{
		if(guiId==Lib.GUIID_RedstoneConnector&&tileEntity instanceof ConnectorRedstoneTileEntity)
			Minecraft.getInstance().setScreen(new RedstoneConnectorScreen((ConnectorRedstoneTileEntity)tileEntity, tileEntity.getBlockState().getBlock().getName()));

		if(guiId==Lib.GUIID_RedstoneProbe&&tileEntity instanceof ConnectorProbeTileEntity)
			Minecraft.getInstance().setScreen(new RedstoneProbeScreen((ConnectorProbeTileEntity)tileEntity, tileEntity.getBlockState().getBlock().getName()));
	}

	@Override
	public void registerContainersAndScreens()
	{
		super.registerContainersAndScreens();
		registerScreen(Lib.GUIID_CokeOven, CokeOvenScreen::new);
		registerScreen(Lib.GUIID_AlloySmelter, AlloySmelterScreen::new);
		registerScreen(Lib.GUIID_BlastFurnace, BlastFurnaceScreen::new);
		registerScreen(Lib.GUIID_CraftingTable, CraftingTableScreen::new);
		registerScreen(Lib.GUIID_WoodenCrate, CrateScreen::new);
		registerScreen(Lib.GUIID_Workbench, ModWorkbenchScreen::new);
		registerScreen(Lib.GUIID_CircuitTable, CircuitTableScreen::new);
		registerScreen(Lib.GUIID_Assembler, AssemblerScreen::new);
		registerScreen(Lib.GUIID_Sorter, SorterScreen::new);
		registerScreen(Lib.GUIID_ItemBatcher, ItemBatcherScreen::new);
		registerScreen(Lib.GUIID_LogicUnit, LogicUnitScreen::new);
		registerScreen(Lib.GUIID_Squeezer, SqueezerScreen::new);
		registerScreen(Lib.GUIID_Fermenter, FermenterScreen::new);
		registerScreen(Lib.GUIID_Refinery, RefineryScreen::new);
		registerScreen(Lib.GUIID_ArcFurnace, ArcFurnaceScreen::new);
		registerScreen(Lib.GUIID_AutoWorkbench, AutoWorkbenchScreen::new);
		registerScreen(Lib.GUIID_Mixer, MixerScreen::new);
		registerScreen(Lib.GUIID_Turret_Gun, GunTurretScreen::new);
		registerScreen(Lib.GUIID_Turret_Chem, ChemTurretScreen::new);
		registerScreen(Lib.GUIID_FluidSorter, FluidSorterScreen::new);
		registerScreen(Lib.GUIID_Cloche, ClocheScreen::new);
		registerScreen(Lib.GUIID_ToolboxBlock, ToolboxBlockScreen::new);

		registerScreen(Lib.GUIID_Toolbox, ToolboxScreen::new);
		registerScreen(Lib.GUIID_Revolver, RevolverScreen::new);
		registerScreen(Lib.GUIID_MaintenanceKit, MaintenanceKitScreen::new);

		registerScreen(Lib.GUIID_CartCrate, CrateScreen::new);
		registerScreen(Lib.GUIID_CartReinforcedCrate, CrateScreen::new);
	}


	public <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>>
	void registerScreen(ResourceLocation containerName, ScreenConstructor<C, S> factory)
	{
		MenuType<C> type = (MenuType<C>)GuiHandler.getContainerType(containerName);
		MenuScreens.register(type, factory);
	}

	@Override
	public Item.Properties useIEOBJRenderer(Item.Properties props)
	{
		return super.useIEOBJRenderer(props).setISTER(() -> () -> IEOBJItemRenderer.INSTANCE);
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
		BlastFurnacePreheaterRenderer.MODEL = DynamicModel.createSimple(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_device/blastfurnace_fan.obj"),
				BlastFurnacePreheaterRenderer.NAME, ModelType.IE_OBJ
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
