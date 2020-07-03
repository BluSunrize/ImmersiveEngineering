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
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.font.IEFontReloadListener;
import blusunrize.immersiveengineering.client.font.IEFontRender;
import blusunrize.immersiveengineering.client.fx.FluidSplashParticle.Data;
import blusunrize.immersiveengineering.client.fx.FractalParticle;
import blusunrize.immersiveengineering.client.fx.IEParticles;
import blusunrize.immersiveengineering.client.gui.*;
import blusunrize.immersiveengineering.client.models.*;
import blusunrize.immersiveengineering.client.models.ModelConveyor.ConveyorLoader;
import blusunrize.immersiveengineering.client.models.ModelCoresample.CoresampleLoader;
import blusunrize.immersiveengineering.client.models.connection.*;
import blusunrize.immersiveengineering.client.models.multilayer.MultiLayerLoader;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.client.render.IEBipedLayerRenderer;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.client.render.entity.*;
import blusunrize.immersiveengineering.client.render.tile.*;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel.ModelType;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IColouredBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.WatermillTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.WindmillTileEntity;
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
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.particle.BreakingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
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
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
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
	public static AtlasTexture revolverTextureMap;
	public static final ResourceLocation revolverTextureResource = new ResourceLocation("textures/atlas/immersiveengineering/revolvers.png");
	public static IEFontRender nixieFontOptional;
	public static IEFontRender nixieFont;
	public static IEFontRender itemFont;
	public static KeyBinding keybind_magnetEquip = new KeyBinding("key.immersiveengineering.magnetEquip", GLFW.GLFW_KEY_S, "key.categories.gameplay");
	public static KeyBinding keybind_chemthrowerSwitch = new KeyBinding("key.immersiveengineering.chemthrowerSwitch", -1, "key.categories.gameplay");

	@Override
	public void modConstruction()
	{
		super.modConstruction();

		// Apparently this runs in data generation runs... but registering model loaders causes NPEs there
		if(Minecraft.getInstance()!=null)
		{
			ModelLoaderRegistry.registerLoader(new ResourceLocation(MODID, "ie_obj"), IEOBJLoader.instance);
			ModelLoaderRegistry.registerLoader(ConnectionLoader.LOADER_NAME, new ConnectionLoader());
			ModelLoaderRegistry.registerLoader(ModelConfigurableSides.Loader.NAME, new ModelConfigurableSides.Loader());
			ModelLoaderRegistry.registerLoader(ConveyorLoader.LOCATION, new ConveyorLoader());
			ModelLoaderRegistry.registerLoader(CoresampleLoader.LOCATION, new CoresampleLoader());
			ModelLoaderRegistry.registerLoader(MultiLayerLoader.LOCATION, new MultiLayerLoader());
			ModelLoaderRegistry.registerLoader(FeedthroughLoader.LOCATION, new FeedthroughLoader());

			((IReloadableResourceManager)mc().getResourceManager()).addReloadListener(new IEFontReloadListener());
		}
	}

	@Override
	public void preInit()
	{
		//TODO auto-detect old cards that don't support this?
		if(IEConfig.GENERAL.stencilBufferEnabled.get())
			DeferredWorkQueue.runLater(() -> Minecraft.getInstance().getFramebuffer().enableStencil());

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
		((IReloadableResourceManager)mc().getResourceManager()).addReloadListener(handler);

		MinecraftForge.EVENT_BUS.register(new RecipeReloadListener());

		keybind_magnetEquip.setKeyConflictContext(new IKeyConflictContext()
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
		});
		ClientRegistry.registerKeyBinding(keybind_magnetEquip);
		ShaderHelper.initShaders();

		keybind_chemthrowerSwitch.setKeyConflictContext(KeyConflictContext.IN_GAME);
		ClientRegistry.registerKeyBinding(keybind_chemthrowerSwitch);

		TeslaCoilTileEntity.effectMap = ArrayListMultimap.create();

		ClientRegistry.bindTileEntityRenderer(ChargingStationTileEntity.TYPE, ChargingStationRenderer::new);
		ClientRegistry.bindTileEntityRenderer(SampleDrillTileEntity.TYPE, SampleDrillRenderer::new);
		ClientRegistry.bindTileEntityRenderer(TeslaCoilTileEntity.TYPE, TeslaCoilRenderer::new);
		ClientRegistry.bindTileEntityRenderer(TurretChemTileEntity.TYPE, TurretRenderer::new);
		ClientRegistry.bindTileEntityRenderer(TurretGunTileEntity.TYPE, TurretRenderer::new);
		ClientRegistry.bindTileEntityRenderer(ClocheTileEntity.TYPE, ClocheRenderer::new);
		// MULTIBLOCKS
		ClientRegistry.bindTileEntityRenderer(MetalPressTileEntity.TYPE, MetalPressRenderer::new);
		ClientRegistry.bindTileEntityRenderer(CrusherTileEntity.TYPE, CrusherRenderer::new);
		ClientRegistry.bindTileEntityRenderer(SheetmetalTankTileEntity.TYPE, SheetmetalTankRenderer::new);
		ClientRegistry.bindTileEntityRenderer(SiloTileEntity.TYPE, SiloRenderer::new);
		ClientRegistry.bindTileEntityRenderer(SqueezerTileEntity.TYPE, SqueezerRenderer::new);
		ClientRegistry.bindTileEntityRenderer(DieselGeneratorTileEntity.TYPE, DieselGeneratorRenderer::new);
		ClientRegistry.bindTileEntityRenderer(BucketWheelTileEntity.TYPE, BucketWheelRenderer::new);
		ClientRegistry.bindTileEntityRenderer(ArcFurnaceTileEntity.TYPE, ArcFurnaceRenderer::new);
		ClientRegistry.bindTileEntityRenderer(AutoWorkbenchTileEntity.TYPE, AutoWorkbenchRenderer::new);
		ClientRegistry.bindTileEntityRenderer(BottlingMachineTileEntity.TYPE, BottlingMachineRenderer::new);
		ClientRegistry.bindTileEntityRenderer(MixerTileEntity.TYPE, MixerRenderer::new);
		//WOOD
		ClientRegistry.bindTileEntityRenderer(WatermillTileEntity.TYPE, WatermillRenderer::new);
		ClientRegistry.bindTileEntityRenderer(WindmillTileEntity.TYPE, WindmillRenderer::new);
		ClientRegistry.bindTileEntityRenderer(ModWorkbenchTileEntity.TYPE, ModWorkbenchRenderer::new);
		//STONE
		ClientRegistry.bindTileEntityRenderer(CoresampleTileEntity.TYPE, CoresampleRenderer::new);
		//CLOTH
		ClientRegistry.bindTileEntityRenderer(ShaderBannerTileEntity.TYPE, ShaderBannerRenderer::new);

		/* Initialize Dynamic Models */
		RedstoneConveyor.MODEL_PANEL = DynamicModel.createSided(
				new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor_redstone.obj.ie"),
				"conveyor_redstone", ModelType.IE_OBJ);

		/*Colours*/
		for(Item item : IEContent.registeredIEItems)
			if(item instanceof IColouredItem&&((IColouredItem)item).hasCustomItemColours())
				mc().getItemColors().register(IEDefaultColourHandlers.INSTANCE, item);
		for(Block block : IEContent.registeredIEBlocks)
			if(block instanceof IColouredBlock&&((IColouredBlock)block).hasCustomBlockColours())
				mc().getBlockColors().register(IEDefaultColourHandlers.INSTANCE, block);

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
		IEManual.initManual();
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
		if(event.getMap().getTextureLocation() != PlayerContainer.LOCATION_BLOCKS_TEXTURE)
			return;
		IELogger.info("Stitching Revolver Textures!");
		RevolverItem.addRevolverTextures(event);
		for(ShaderRegistry.ShaderRegistryEntry entry : ShaderRegistry.shaderRegistry.values())
			for(ShaderCase sCase : entry.getCases())
				if(sCase.stitchIntoSheet())
					for(ShaderLayer layer : sCase.getLayers())
						if(layer.getTexture()!=null)
							event.addSprite(layer.getTexture());

		for(DrillHeadPerm p : DrillHeadPerm.ALL_PERMS)
			event.addSprite(p.texture);
		event.addSprite(RockcutterItem.texture);
		event.addSprite(new ResourceLocation(MODID, "block/wire"));
		event.addSprite(new ResourceLocation(MODID, "block/shaders/greyscale_fire"));

		//TODO this shouldn't be necessary any more
		for(BulletHandler.IBullet bullet : BulletHandler.getAllValues())
			for(ResourceLocation rl : bullet.getTextures())
				event.addSprite(rl);

		for(ResourceLocation rl : ModelConveyor.rl_casing)
			event.addSprite(rl);
		event.addSprite(ConveyorHandler.textureConveyorColour);
		event.addSprite(BasicConveyor.texture_off);
		event.addSprite(BasicConveyor.texture_on);
		event.addSprite(DropConveyor.texture_off);
		event.addSprite(DropConveyor.texture_on);
		event.addSprite(VerticalConveyor.texture_off);
		event.addSprite(VerticalConveyor.texture_on);
		event.addSprite(SplitConveyor.texture_off);
		event.addSprite(SplitConveyor.texture_on);
		event.addSprite(SplitConveyor.texture_casing);
		event.addSprite(RedstoneConveyor.texture_panel);

		event.addSprite(new ResourceLocation(MODID, "block/fluid/creosote_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/creosote_flow"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/plantoil_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/plantoil_flow"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/ethanol_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/ethanol_flow"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/biodiesel_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/biodiesel_flow"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/concrete_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/concrete_flow"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/potion_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/potion_flow"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/hot_metal_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/hot_metal_flow"));

		event.addSprite(new ResourceLocation(MODID, "item/shader_slot"));
	}

	@SubscribeEvent
	public static void textureStichPost(TextureStitchEvent.Post event)
	{
		if(event.getMap().getTextureLocation() != PlayerContainer.LOCATION_BLOCKS_TEXTURE)
			return;
		ImmersiveEngineering.proxy.clearRenderCaches();
		RevolverItem.retrieveRevolverTextures(event.getMap());
		for(DrillHeadPerm p : DrillHeadPerm.ALL_PERMS)
		{
			p.sprite = event.getMap().getSprite(p.texture);
			Preconditions.checkNotNull(p.sprite);
		}
		WireType.iconDefaultWire = event.getMap().getSprite(new ResourceLocation(MODID, "block/wire"));
		AtlasTexture texturemap = event.getMap();
		for(int i = 0; i < ClientUtils.destroyBlockIcons.length; i++)
		{
			ClientUtils.destroyBlockIcons[i] = texturemap.getSprite(new ResourceLocation("block/destroy_stage_"+i));
			Preconditions.checkNotNull(ClientUtils.destroyBlockIcons[i]);
		}
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
		if(stack!=null&&IEConfig.MACHINES.excavator_particles.get())
		{
			Direction facing = tile.getFacing();
			for(int i = 0; i < 16; i++)
			{
				double x = tile.getPos().getX()+.5;
				if(facing.getAxis()==Axis.Z)
					x += .1*(2*(tile.getWorldNonnull().rand.nextDouble()-.5));
				else
					x -= .5*facing.getAxisDirection().getOffset();
				double y = tile.getPos().getY()+2.5;
				double z = tile.getPos().getZ()+.5+.1*0;
				if(tile.getFacing().getAxis()==Axis.X)
					z += .1*(2*(tile.getWorldNonnull().rand.nextGaussian()-.5));
				else
					z -= .5*facing.getAxisDirection().getOffset();
				double mX = (tile.getWorldNonnull().rand.nextDouble()-.5)*.01;
				;
				if(facing.getAxis()==Axis.X)
				{
					int sign = (tile.getIsMirrored()^facing.getAxisDirection()==AxisDirection.NEGATIVE)?1: -1;
					mX += .075*sign;
				}
				double mY = tile.getWorld().rand.nextDouble()*-0.05D;
				double mZ = (tile.getWorldNonnull().rand.nextDouble()-.5)*.01;
				;
				if(facing.getAxis()==Axis.Z)
				{
					int sign = (tile.getIsMirrored()^facing.getAxisDirection()==AxisDirection.NEGATIVE)?1: -1;
					mZ += .075*sign;
				}

				Particle particle = new BreakingParticle.Factory().makeParticle(new ItemParticleData(ParticleTypes.ITEM, stack),
						tile.getWorldNonnull(), x, y, z, mX, mY, mZ);
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
	public String[] splitStringOnWidth(String s, int w)
	{
		return ClientUtils.font().listFormattedStringToWidth(s, w).toArray(new String[0]);
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
	public void removeStateFromSmartModelCache(BlockState state)
	{
		for(RenderType r : RenderType.getBlockRenderTypes())
			IESmartObjModel.modelCache.invalidate(new RenderCacheKey(state, r));
		IESmartObjModel.modelCache.invalidate(new RenderCacheKey(state, null));
	}

	@Override
	public void removeStateFromConnectionModelCache(BlockState state)
	{
		//TODO
		for(RenderType r : RenderType.getBlockRenderTypes())
			BakedConnectionModel.cache.invalidate(new RenderCacheKey(state, r));
		BakedConnectionModel.cache.invalidate(new RenderCacheKey(state, null));
	}

	@Override
	public void clearConnectionModelCache()
	{
		BakedConnectionModel.cache.invalidateAll();
	}

	@Override
	public void reloadManual()
	{
		if(ManualHelper.getManual()!=null)
			ManualHelper.getManual().reload();
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
	public void openTileScreen(ResourceLocation guiId, TileEntity tileEntity)
	{
		if(guiId==Lib.GUIID_RedstoneConnector&&tileEntity instanceof ConnectorRedstoneTileEntity)
			Minecraft.getInstance().displayGuiScreen(new RedstoneConnectorScreen((ConnectorRedstoneTileEntity)tileEntity, tileEntity.getBlockState().getBlock().getNameTextComponent()));

		if(guiId==Lib.GUIID_RedstoneProbe&&tileEntity instanceof ConnectorProbeTileEntity)
			Minecraft.getInstance().displayGuiScreen(new RedstoneProbeScreen((ConnectorProbeTileEntity)tileEntity, tileEntity.getBlockState().getBlock().getNameTextComponent()));
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
		registerScreen(Lib.GUIID_Assembler, AssemblerScreen::new);
		registerScreen(Lib.GUIID_Sorter, SorterScreen::new);
		registerScreen(Lib.GUIID_ItemBatcher, ItemBatcherScreen::new);
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


	public <C extends Container, S extends Screen & IHasContainer<C>>
	void registerScreen(ResourceLocation containerName, IScreenFactory<C, S> factory)
	{
		ContainerType<C> type = (ContainerType<C>)GuiHandler.getContainerType(containerName);
		ScreenManager.registerFactory(type, factory);
	}

	@Override
	public Item.Properties useIEOBJRenderer(Item.Properties props)
	{
		return super.useIEOBJRenderer(props).setISTER(() -> () -> IEOBJItemRenderer.INSTANCE);
	}
}
