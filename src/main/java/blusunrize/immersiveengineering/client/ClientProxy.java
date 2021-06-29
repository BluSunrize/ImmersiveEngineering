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
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IColouredBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.RecipeReloadListener;
import blusunrize.immersiveengineering.common.entities.IEEntityTypes;
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import blusunrize.immersiveengineering.common.gui.IEBaseContainer;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.items.DrillheadItem.DrillHeadPerm;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.IEItems;
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
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.particle.BreakingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
	public static KeyBinding keybind_magnetEquip = new KeyBinding("key.immersiveengineering.magnetEquip", GLFW.GLFW_KEY_S, "key.categories.immersiveengineering");
	public static KeyBinding keybind_chemthrowerSwitch = new KeyBinding("key.immersiveengineering.chemthrowerSwitch", -1, "key.categories.immersiveengineering");
	public static KeyBinding keybind_railgunZoom = new KeyBinding("key.immersiveengineering.railgunZoom", InputMappings.Type.MOUSE, 2, "key.categories.immersiveengineering");

	@Override
	public void modConstruction()
	{
		super.modConstruction();

		// Apparently this runs in data generation runs... but registering model loaders causes NPEs there
		if(Minecraft.getInstance()!=null)
		{
			ModelLoaderRegistry.registerLoader(IEOBJLoader.LOADER_NAME, IEOBJLoader.instance);
			ModelLoaderRegistry.registerLoader(ConnectionLoader.LOADER_NAME, new ConnectionLoader());
			ModelLoaderRegistry.registerLoader(ModelConfigurableSides.Loader.NAME, new ModelConfigurableSides.Loader());
			ModelLoaderRegistry.registerLoader(ConveyorLoader.LOCATION, new ConveyorLoader());
			ModelLoaderRegistry.registerLoader(CoresampleLoader.LOCATION, new CoresampleLoader());
			ModelLoaderRegistry.registerLoader(MultiLayerLoader.LOCATION, new MultiLayerLoader());
			ModelLoaderRegistry.registerLoader(FeedthroughLoader.LOCATION, new FeedthroughLoader());
			ModelLoaderRegistry.registerLoader(SplitModelLoader.LOCATION, new SplitModelLoader());
			ModelLoaderRegistry.registerLoader(Loader.LOADER_NAME, new PotionBucketModel.Loader());
			populateAPI();

			requestModelsAndTextures();
		}
	}

	public static boolean stencilEnabled = false;

	@Override
	public void preInit(ParallelDispatchEvent ev)
	{
		if(IEClientConfig.stencilBufferEnabled.get())
			ev.enqueueWork(() -> {
				Minecraft.getInstance().getFramebuffer().enableStencil();
				stencilEnabled = true;
			});

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

	private <T extends Entity> void registerEntityRenderingHandler(Supplier<EntityType<T>> type, IRenderFactory<? super T> renderer)
	{
		RenderingRegistry.registerEntityRenderingHandler(type.get(), renderer);
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
		((IReloadableResourceManager)mc().getResourceManager()).addReloadListener(handler);

		MinecraftForge.EVENT_BUS.register(new RecipeReloadListener(null));

		IKeyConflictContext noKeyConflict = new IKeyConflictContext()
		{
			@Override
			public boolean isActive()
			{
				return mc().currentScreen==null;
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
		for(RegistryObject<Item> itemRO : IEItems.REGISTER.getEntries())
		{
			Item item = itemRO.get();
			if(item instanceof IColouredItem&&((IColouredItem)item).hasCustomItemColours())
				mc().getItemColors().register(IEDefaultColourHandlers.INSTANCE, item);
		}
		for(BlockEntry<?> blockEntry : BlockEntry.ALL_ENTRIES)
		{
			Block block = blockEntry.get();
			if(block instanceof IColouredBlock&&((IColouredBlock)block).hasCustomBlockColours())
				mc().getBlockColors().register(IEDefaultColourHandlers.INSTANCE, block);
		}

		/*Render Layers*/
		Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();
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
		if(!event.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE))
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
		if(!event.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE))
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
		Minecraft.getInstance().getItemRenderer().getItemModelMesher().register(item, new ModelResourceLocation(path, renderCase));
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
	public void playTickableSound(SoundEvent soundEvent, SoundCategory category, String key, float volume, float pitch, Supplier<Boolean> tickFunction)
	{
		IETickableSound sound = new IETickableSound(soundEvent, category, volume, pitch, tickFunction,
				ieTickableSound -> tickableSoundMap.remove(key));
		mc().getSoundHandler().play(sound);
		tickableSoundMap.put(key, sound);
	}

	@Override
	public void handleTileSound(SoundEvent soundEvent, TileEntity tile, boolean tileActive, float volume, float pitch)
	{
		BlockPos pos = tile.getPos();
		IETileSound sound = tileSoundMap.get(pos);
		if(sound==null&&tileActive)
		{
			if(tile instanceof ISoundTile&&mc().player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) > ((ISoundTile)tile).getSoundRadiusSq())
				return;
			sound = ClientUtils.generatePositionedIESound(soundEvent, volume, pitch, true, 0, pos);
			tileSoundMap.put(pos, sound);
		}
		else if(sound!=null&&(sound.donePlaying||!tileActive))
		{
			sound.donePlaying = true;
			mc().getSoundHandler().stop(sound);
			tileSoundMap.remove(pos);
		}
	}

	@Override
	public void stopTileSound(String soundName, TileEntity tile)
	{
		IETileSound sound = tileSoundMap.get(tile.getPos());
		if(sound!=null)
			mc().getSoundHandler().stop(sound);
	}

	@Override
	public void onWorldLoad()
	{
		if(!ShaderMinecartRenderer.rendersReplaced)
		{
			EntityRendererManager rendererManager = mc().getRenderManager();
			for(EntityType<?> type : rendererManager.renderers.keySet())
				ShaderMinecartRenderer.overrideModelIfMinecart(type);
			ShaderMinecartRenderer.rendersReplaced = true;
		}
		if(!IEBipedLayerRenderer.rendersAssigned)
		{
			for(Object render : mc().getRenderManager().renderers.values())
				if(BipedRenderer.class.isAssignableFrom(render.getClass()))
					((BipedRenderer)render).addLayer(new IEBipedLayerRenderer<>((BipedRenderer)render));
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
			double x = tile.getPos().getX()+.5;
			double y = tile.getPos().getY()+2.5;
			double z = tile.getPos().getZ()+.5;
			double fixPosOffset = .5*sign;
			double fixVelOffset = .075*sign;
			for(int i = 0; i < 16; i++)
			{
				double mX = (tile.getWorldNonnull().rand.nextDouble()-.5)*.01;
				double mY = tile.getWorld().rand.nextDouble()*-0.05D;
				double mZ = (tile.getWorldNonnull().rand.nextDouble()-.5)*.01;
				double rndPosOffset = .2*(tile.getWorldNonnull().rand.nextDouble()-.5);

				Particle particle;

				if(facing.getAxis()==Axis.X)
					particle = new BreakingParticle.Factory().makeParticle(new ItemParticleData(ParticleTypes.ITEM, stack),
							(ClientWorld) tile.getWorldNonnull(), x+fixPosOffset, y, z+rndPosOffset, mX+fixVelOffset, mY, mZ);
				else
					particle = new BreakingParticle.Factory().makeParticle(new ItemParticleData(ParticleTypes.ITEM, stack),
							(ClientWorld) tile.getWorldNonnull(), x+rndPosOffset, y, z+fixPosOffset, mX, mY, mZ+fixVelOffset);

				mc().particles.addEffect(particle);
			}
		}
	}

	//TODO move to commonProxy or even use directly
	@Override
	public void spawnRedstoneFX(World world, double x, double y, double z, double mx, double my, double mz, float size, float r, float g, float b)
	{
		world.addParticle(new RedstoneParticleData(r, g, b, size), x, y, z, mx, my, mz);
	}

	@Override
	public void spawnFluidSplashFX(World world, FluidStack fs, double x, double y, double z, double mx, double my, double mz)
	{
		world.addParticle(new Data(fs.getFluid()), x, y, z, mx, my, mz);
	}

	@Override
	public void spawnBubbleFX(World world, FluidStack fs, double x, double y, double z, double mx, double my, double mz)
	{
		world.addParticle(IEParticles.IE_BUBBLE, x, y, z, mx, my, mz);
	}

	@Override
	public void spawnFractalFX(World world, double x, double y, double z, Vector3d direction, double scale, int prefixColour, float[][] colour)
	{
		if(prefixColour >= 0)
			colour = prefixColour==1?FractalParticle.COLOUR_ORANGE: prefixColour==2?FractalParticle.COLOUR_RED: FractalParticle.COLOUR_LIGHTNING;
		FractalParticle.Data particle = new FractalParticle.Data(direction, scale, 10, 16, colour[0], colour[1]);
		world.addParticle(particle, x, y, z, 0, 0, 0);
	}

	@Override
	public World getClientWorld()
	{
		return mc().world;
	}

	@Override
	public PlayerEntity getClientPlayer()
	{
		return mc().player;
	}

	@Override
	public String getNameFromUUID(String uuid)
	{
		return mc().getSessionService().fillProfileProperties(new GameProfile(UUID.fromString(uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")), null), false).getName();
	}

	@Override
	public void reInitGui()
	{
		Screen currentScreen = mc().currentScreen;
		if(currentScreen instanceof IEContainerScreen)
			currentScreen.init(mc(), currentScreen.width, currentScreen.height);
	}

	@Override
	public void resetManual()
	{
		if(mc().currentScreen instanceof ManualScreen)
			mc().displayGuiScreen(null);
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
		Minecraft.getInstance().getSoundHandler().play(new SkyhookSound(hook,
				new ResourceLocation(MODID, "skyhook")));
	}

	@Override
	public void openManual()
	{
		Minecraft.getInstance().displayGuiScreen(ManualHelper.getManual().getGui());
	}

	@Override
	public void openTileScreen(String guiId, TileEntity tileEntity)
	{
		if(guiId.equals(Lib.GUIID_RedstoneConnector)&&tileEntity instanceof ConnectorRedstoneTileEntity)
			Minecraft.getInstance().displayGuiScreen(new RedstoneConnectorScreen((ConnectorRedstoneTileEntity)tileEntity, tileEntity.getBlockState().getBlock().getTranslatedName()));

		if(guiId.equals(Lib.GUIID_RedstoneProbe)&&tileEntity instanceof ConnectorProbeTileEntity)
			Minecraft.getInstance().displayGuiScreen(new RedstoneProbeScreen((ConnectorProbeTileEntity)tileEntity, tileEntity.getBlockState().getBlock().getTranslatedName()));
	}

	@SubscribeEvent
	public static void registerContainersAndScreens(FMLClientSetupEvent ev)
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

	public static <C extends Container, S extends Screen & IHasContainer<C>>
	void registerScreen(IEContainerTypes.EntityContainerType<?, C> type, IScreenFactory<C, S> factory)
	{
		ScreenManager.registerFactory(type.getType(), factory);
	}

	public static <C extends Container, S extends Screen & IHasContainer<C>>
	void registerScreen(IEContainerTypes.ItemContainerType<C> type, IScreenFactory<C, S> factory)
	{
		ScreenManager.registerFactory(type.getType(), factory);
	}

	public static <C extends IEBaseContainer<?>, S extends Screen & IHasContainer<C>>
	void registerTileScreen(IEContainerTypes.TileContainer<?, C> type, IScreenFactory<C, S> factory)
	{
		ScreenManager.registerFactory(type.getType(), factory);
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
