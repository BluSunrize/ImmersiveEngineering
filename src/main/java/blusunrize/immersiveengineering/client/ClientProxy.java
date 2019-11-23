/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.multiblocks.ManualElementMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.fx.FluidSplashParticle;
import blusunrize.immersiveengineering.client.fx.FractalParticle;
import blusunrize.immersiveengineering.client.fx.IEBubbleParticle;
import blusunrize.immersiveengineering.client.gui.*;
import blusunrize.immersiveengineering.client.manual.IEManualInstance;
import blusunrize.immersiveengineering.client.models.*;
import blusunrize.immersiveengineering.client.models.connection.*;
import blusunrize.immersiveengineering.client.models.multilayer.MultiLayerLoader;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.client.render.IEBipedLayerRenderer;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.client.render.entity.*;
import blusunrize.immersiveengineering.client.render.tile.*;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IColouredBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.DropConveyor;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.SplitConveyor;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.VerticalConveyor;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.WatermillTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.WindmillTileEntity;
import blusunrize.immersiveengineering.common.entities.*;
import blusunrize.immersiveengineering.common.gui.GuiHandler;
import blusunrize.immersiveengineering.common.items.DrillheadItem.DrillHeadPerm;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.util.sound.IETileSound;
import blusunrize.immersiveengineering.common.util.sound.SkyhookSound;
import blusunrize.lib.manual.ManualElementTable;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualEntry.ManualEntryBuilder;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree.InnerNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.particle.BreakingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.client.ClientUtils.mc;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Bus.MOD)
public class ClientProxy extends CommonProxy
{
	public static AtlasTexture revolverTextureMap;
	public static final ResourceLocation revolverTextureResource = new ResourceLocation("textures/atlas/immersiveengineering/revolvers.png");
	public static FontRenderer nixieFontOptional;
	public static FontRenderer nixieFont;
	public static FontRenderer itemFont;
	public static boolean stencilBufferEnabled = false;
	public static KeyBinding keybind_magnetEquip = new KeyBinding("key.immersiveengineering.magnetEquip", GLFW.GLFW_KEY_S, "key.categories.gameplay");
	public static KeyBinding keybind_chemthrowerSwitch = new KeyBinding("key.immersiveengineering.chemthrowerSwitch", 0, "key.categories.gameplay");

	@Override
	public void preInit()
	{
		Framebuffer fb = mc().getFramebuffer();
		/*TODO this probably needs to be readded to Forge
		if(GLX.isUsingFBOs()&&IEConfig.stencilBufferEnabled&&!fb.isStencilEnabled())
		{
			stencilBufferEnabled = fb.enableStencil();//Enabling FBO stencils
		}
		 */
		ModelLoaderRegistry.registerLoader(IEOBJLoader.instance);
		OBJLoader.INSTANCE.addDomain("immersiveengineering");
		IEOBJLoader.instance.addDomain("immersiveengineering");
		MinecraftForge.EVENT_BUS.register(ImmersiveModelRegistry.instance);

		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(Misc.shader), new ImmersiveModelRegistry.ItemModelReplacement()
		{
			@Override
			public IBakedModel createBakedModel(IBakedModel existingModel)
			{
				return new ModelItemDynamicOverride(existingModel, null);
			}
		});

		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(Tools.voltmeter), new ImmersiveModelRegistry.ItemModelReplacement_OBJ("immersiveengineering:models/item/tool/voltmeter.obj", false)
				.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().translate(-.25, .375, .3125).rotate(-Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().translate(-.25, .375, -.625).rotate(-Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(-0.25, .125, .25).scale(.625f, .625f, .625f).rotate(-Math.PI*.375, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(-0.5, .125, -.3125).scale(.625f, .625f, .625f).rotate(-Math.PI*.375, 0, 1, 0))
				.setTransformations(TransformType.FIXED, new Matrix4().translate(-.5, .5, -.5).scale(1, 1, 1).rotate(Math.PI, 0, 1, 0))
				.setTransformations(TransformType.GUI, new Matrix4().translate(0, .5, 0).scale(1.125, 1.125, 1.125).rotate(-Math.PI*.25, 0, 1, 0))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(.25, .25, .25).scale(.5, .5, .5)));

		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(Tools.toolbox), new ImmersiveModelRegistry.ItemModelReplacement_OBJ("immersiveengineering:models/item/toolbox.obj", false)
				.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().scale(.375, .375, .375).translate(-.75, 1.25, .3125).rotate(-Math.PI*.75, 0, 1, 0))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().scale(.375, .375, .375).translate(-.125, 1.25, .9375).rotate(Math.PI*.25, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(-.25, .1875, .3125).scale(.625, .625, .625).rotate(Math.PI, 0, 1, 0).rotate(-Math.PI*.5, 1, 0, 0))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(-.25, -.4375, .3125).scale(.625, .625, .625).rotate(Math.PI*.5, 1, 0, 0))
				.setTransformations(TransformType.FIXED, new Matrix4().translate(.5, .875, -.5).scale(1, 1, 1).rotate(Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.GUI, new Matrix4().translate(-.625, .75, 0).scale(.875, .875, .875).rotate(-Math.PI*.6875, 0, 1, 0))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(.25, .5, .25).scale(.5, .5, .5)));

		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(Misc.fluorescentTube), new ImmersiveModelRegistry.ItemModelReplacement_OBJ("immersiveengineering:models/item/fluorescent_tube.obj", true)
				.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().translate(.2, .1, 0).rotate(-Math.PI/3, 1, 0, 0))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().translate(.2, .1, 0).rotate(-Math.PI/3, 1, 0, 0))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(0, .5, .1))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(0, .5, .1))
				.setTransformations(TransformType.FIXED, new Matrix4())
				.setTransformations(TransformType.GUI, new Matrix4().rotate(-Math.PI/4, 0, 0, 1).rotate(Math.PI/8, 0, 1, 0))
				.setTransformations(TransformType.GROUND, new Matrix4().scale(.5, .5, .5).translate(0, .5, 0)));

		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(Misc.shield), new ImmersiveModelRegistry.ItemModelReplacement_OBJ("immersiveengineering:models/item/shield.obj", true)
				.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().rotate(Math.toRadians(90), 0, 1, 0).rotate(.1, 1, 0, 0).translate(.5, .125, .5))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().rotate(Math.toRadians(-90), 0, 1, 0).rotate(-.1, 1, 0, 0).translate(-.5, .125, .5))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(.59375, .375, .75))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().rotate(3.14159, 0, 1, 0).translate(-.59375, .375, .25))
				.setTransformations(TransformType.FIXED, new Matrix4().rotate(1.57079, 0, 1, 0).scale(.75f, .75f, .75f).translate(.375, .5, .5))
				.setTransformations(TransformType.GUI, new Matrix4().translate(-.375, .375, 0).scale(.75, .625, .75).rotate(-2.35619, 0, 1, 0).rotate(0.13089, 0, 0, 1))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(.125, .125, .125).scale(.25, .25, .25)));

		RenderingRegistry.registerEntityRenderingHandler(RevolvershotEntity.class, RevolvershotRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(SkylineHookEntity.class, NoneRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(ChemthrowerShotEntity.class, ChemthrowerShotRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(RailgunShotEntity.class, RailgunShotRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(IEExplosiveEntity.class, IEExplosiveRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(FluorescentTubeEntity.class, FluorescentTubeRenderer::new);
		ModelLoaderRegistry.registerLoader(new ConnectionLoader());
		ModelLoaderRegistry.registerLoader(new FeedthroughLoader());
		ModelLoaderRegistry.registerLoader(new ModelConfigurableSides.Loader());
		ModelLoaderRegistry.registerLoader(new MultiLayerLoader());

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

		//TODO
		nixieFontOptional = ClientUtils.font();
		nixieFont = ClientUtils.font();
		itemFont = ClientUtils.font();
		TeslaCoilTileEntity.effectMap = ArrayListMultimap.create();

		ClientRegistry.bindTileEntitySpecialRenderer(ChargingStationTileEntity.class, new ChargingStationRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(SampleDrillTileEntity.class, new SampleDrillRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TeslaCoilTileEntity.class, new TeslaCoilRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TurretTileEntity.class, new TurretRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(BelljarTileEntity.class, new BelljarRenderer());
		// MULTIBLOCKS
		ClientRegistry.bindTileEntitySpecialRenderer(MetalPressTileEntity.class, new MetalPressRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(CrusherTileEntity.class, new CrusherRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(SheetmetalTankTileEntity.class, new SheetmetalTankRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(SiloTileEntity.class, new SiloRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(SqueezerTileEntity.class, new SqueezerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(DieselGeneratorTileEntity.class, new DieselGeneratorRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(BucketWheelTileEntity.class, new BucketWheelRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(ArcFurnaceTileEntity.class, new ArcFurnaceRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(AutoWorkbenchTileEntity.class, new AutoWorkbenchRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(BottlingMachineTileEntity.class, new BottlingMachineRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(MixerTileEntity.class, new MixerRenderer());
		//WOOD
		ClientRegistry.bindTileEntitySpecialRenderer(WatermillTileEntity.class, new WatermillRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(WindmillTileEntity.class, new WindmillRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(ModWorkbenchTileEntity.class, new ModWorkbenchRenderer());
		//STONE
		ClientRegistry.bindTileEntitySpecialRenderer(CoresampleTileEntity.class, new CoresampleRenderer());
		//CLOTH
		ClientRegistry.bindTileEntitySpecialRenderer(ShaderBannerTileEntity.class, new ShaderBannerRenderer());


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
		ManualHelper.ieManualInstance = new IEManualInstance();
		/*
		ManualHelper.addEntry("introduction", ManualHelper.CAT_GENERAL,
				new ManualPages.Text(ManualHelper.getManual(), "introduction0"),
				new ManualPages.Text(ManualHelper.getManual(), "introduction1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "introductionHammer", new ItemStack(IEContent.itemTool, 1, 0)));
		tempRecipeList = new ArrayList<>();
		if(!IERecipes.hammerCrushingList.isEmpty())
		{
			for(String ore : IERecipes.hammerCrushingList)
				tempRecipeList.add(new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("ore"+ore), 24, 0), new PositionedItemStack(new ItemStack(IEContent.itemTool, 1, 0), 42, 0), new PositionedItemStack(IEApi.getPreferredTagStack("dust"+ore), 78, 0)});
			if(!tempRecipeList.isEmpty())
				ManualHelper.addEntry("oreProcessing", ManualHelper.CAT_GENERAL, new ManualPages.CraftingMulti(ManualHelper.getManual(), "oreProcessing0", (Object[])tempRecipeList.toArray(new PositionedItemStack[tempRecipeList.size()][3])));
		}
		ManualHelper.addEntry("alloys", ManualHelper.CAT_GENERAL, new ManualPages.CraftingMulti(ManualHelper.getManual(), "alloys0", (Object[])new PositionedItemStack[][]{
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("dustCopper"), 24, 0), new PositionedItemStack(OreDictionary.getOres("dustNickel"), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal, 2, 15), 78, 0)},
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("dustGold"), 24, 0), new PositionedItemStack(OreDictionary.getOres("dustSilver"), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal, 2, 16), 78, 0)}}));
		ManualHelper.addEntry("plates", ManualHelper.CAT_GENERAL, new ManualPages.CraftingMulti(ManualHelper.getManual(), "plates0", (Object[])new PositionedItemStack[][]{
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("ingotIron"), 24, 0), new PositionedItemStack(new ItemStack(IEContent.itemTool, 1, 0), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal, 1, 39), 78, 0)},
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("ingotAluminum"), 24, 0), new PositionedItemStack(new ItemStack(IEContent.itemTool, 1, 0), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal, 1, 31), 78, 0)},
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("ingotLead"), 24, 0), new PositionedItemStack(new ItemStack(IEContent.itemTool, 1, 0), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal, 1, 32), 78, 0)},
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("ingotConstantan"), 24, 0), new PositionedItemStack(new ItemStack(IEContent.itemTool, 1, 0), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal, 1, 36), 78, 0)},
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("ingotSteel"), 24, 0), new PositionedItemStack(new ItemStack(IEContent.itemTool, 1, 0), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal, 1, 38), 78, 0)}}));
		ManualHelper.addEntry("hemp", ManualHelper.CAT_GENERAL,
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "hemp0", new ItemStack(IEContent.blockCrop, 1, 5), new ItemStack(IEContent.itemSeeds)),
				new ManualPages.Crafting(ManualHelper.getManual(), "hemp1", new ItemStack(IEContent.itemMaterial, 1, 5)),
				new ManualPages.Crafting(ManualHelper.getManual(), "hemp2", new ItemStack(IEContent.blockClothDevice, 1, BlockTypes_ClothDevice.CUSHION.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "hemp3", new ItemStack(IEContent.blockClothDevice, 1, BlockTypes_ClothDevice.STRIPCURTAIN.getMeta())));
		ManualHelper.addEntry("cokeoven", ManualHelper.CAT_GENERAL,
				new ManualPages.Text(ManualHelper.getManual(), "cokeoven0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "cokeovenBlock", new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.COKEBRICK.getMeta())),
				new ManualPageMultiblock(ManualHelper.getManual(), "", MultiblockCokeOven.instance));
		ManualHelper.addEntry("alloysmelter", ManualHelper.CAT_GENERAL,
				new ManualPages.Crafting(ManualHelper.getManual(), "alloysmelter0", new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.ALLOYBRICK.getMeta())),
				new ManualPageMultiblock(ManualHelper.getManual(), "alloysmelter1", MultiblockAlloySmelter.instance));
		ManualHelper.addEntry("blastfurnace", ManualHelper.CAT_GENERAL,
				new ManualPages.Text(ManualHelper.getManual(), "blastfurnace0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "blastfurnaceBlock", new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.BLASTBRICK.getMeta())),
				new ManualPageMultiblock(ManualHelper.getManual(), "blastfurnace1", MultiblockBlastFurnace.instance));
		ManualHelper.addEntry("workbench", ManualHelper.CAT_GENERAL,
				new ManualPages.Crafting(ManualHelper.getManual(), "workbench0", new ItemStack(IEContent.blockWoodenDevice0, 1, BlockTypes_WoodenDevice0.WORKBENCH.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "workbench1"),
				new ManualPages.Text(ManualHelper.getManual(), "workbench2"));
		ManualHelper.addEntry("blueprints", ManualHelper.CAT_GENERAL, new ManualPages.Crafting(ManualHelper.getManual(), "blueprints0", new ItemStack(IEContent.itemBlueprint)));
		((IEManualInstance)ManualHelper.getManual()).hideEntry("blueprints");
		handleMineralManual();
		ManualHelper.addEntry("components", ManualHelper.CAT_GENERAL,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "components0", new ItemStack(IEContent.itemMaterial, 1, 8), new ItemStack(IEContent.itemMaterial, 1, 9)),
				new ManualPages.Crafting(ManualHelper.getManual(), "components1", BlueprintCraftingRecipe.getTypedBlueprint("components")),
				new ManualPageBlueprint(ManualHelper.getManual(), "components2", new ItemStack(IEContent.itemMaterial, 1, 8), new ItemStack(IEContent.itemMaterial, 1, 9), new ItemStack(IEContent.itemMaterial, 1, 26)),
				new ManualPageBlueprint(ManualHelper.getManual(), "components3", new ItemStack(IEContent.itemMaterial, 1, 27)));
		ManualHelper.addEntry("graphite", ManualHelper.CAT_GENERAL, new ManualPages.ItemDisplay(ManualHelper.getManual(), "graphite0", new ItemStack(IEContent.itemMaterial, 1, 18), new ItemStack(IEContent.itemMaterial, 1, 19)), new ManualPageBlueprint(ManualHelper.getManual(), "graphite1", new ItemStack(IEContent.itemGraphiteElectrode)));
		ManualHelper.addEntry("shader", ManualHelper.CAT_GENERAL, new ManualPages.Text(ManualHelper.getManual(), "shader0"), new ManualPages.Text(ManualHelper.getManual(), "shader1"), new ManualPages.ItemDisplay(ManualHelper.getManual(), "shader2"), new ManualPages.Text(ManualHelper.getManual(), "shader2"));
		//ShaderRegistry.manualEntry = ManualHelper.getManual().getEntry("shader");
		pages = new ArrayList<IManualPage>();
		for(ShaderRegistry.ShaderRegistryEntry entry : ShaderRegistry.shaderRegistry.values())
			pages.add(new ManualPageShader(ManualHelper.getManual(), entry));
		ManualHelper.addEntry("shaderList", ManualHelper.CAT_GENERAL, pages.toArray(new IManualPage[pages.size()]));
		((IEManualInstance)ManualHelper.getManual()).hideEntry("shaderList");

		ManualHelper.addEntry("treatedwood", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Crafting(ManualHelper.getManual(), "treatedwood0", new ItemStack(IEContent.blockTreatedWood, 1, 0)),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockTreatedWood, 1, 1), new ItemStack(IEContent.blockTreatedWood, 1, 2), new ItemStack(IEContent.blockTreatedWoodSlabs, 1, OreDictionary.WILDCARD_VALUE)),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockWoodenStair)),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.itemMaterial, 1, 0), new ItemStack(IEContent.blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.FENCE.getMeta()), new ItemStack(IEContent.blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "treatedwoodPost0", new ItemStack(IEContent.blockWoodenDevice1, 1, BlockTypes_WoodenDevice1.POST.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "treatedwoodPost1"));
		NonNullList<ItemStack> storageBlocks = NonNullList.withSize(BlockTypes_MetalsIE.values().length, ItemStack.EMPTY);
		NonNullList<ItemStack> storageSlabs = NonNullList.withSize(BlockTypes_MetalsIE.values().length, ItemStack.EMPTY);
		for(int i = 0; i < BlockTypes_MetalsIE.values().length; i++)
		{
			storageBlocks.set(i, new ItemStack(IEContent.blockStorage, 1, i));
			storageSlabs.set(i, new ItemStack(IEContent.blockStorageSlabs, 1, i));
		}
		tempItemList = NonNullList.create();
		for(int i = 0; i < EnumMetals.values().length; i++)
			if(!IEContent.blockSheetmetal.isMetaHidden(i))
				tempItemList.add(new ItemStack(IEContent.blockSheetmetal, 1, i));
		ItemStack[] sheetmetal = tempItemList.toArray(new ItemStack[tempItemList.size()]);

		ManualHelper.addEntry("crate", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Crafting(ManualHelper.getManual(), "crate0", new ItemStack(IEContent.blockWoodenDevice0, 1, BlockTypes_WoodenDevice0.CRATE.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "crate1", new ItemStack(IEContent.blockWoodenDevice0, 1, BlockTypes_WoodenDevice0.REINFORCED_CRATE.getMeta())));
		ManualHelper.addEntry("barrel", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Crafting(ManualHelper.getManual(), "barrel0", new ItemStack(IEContent.blockWoodenDevice0, 1, BlockTypes_WoodenDevice0.BARREL.getMeta())));
		ManualHelper.addEntry("concrete", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Crafting(ManualHelper.getManual(), "concrete0", new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.CONCRETE.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.CONCRETE_TILE.getMeta()), new ItemStack(IEContent.blockStoneStair_concrete0, 1, 0), new ItemStack(IEContent.blockStoneStair_concrete1, 1, 0)));
		ManualHelper.addEntry("balloon", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Crafting(ManualHelper.getManual(), "balloon0", new ItemStack(IEContent.blockClothDevice, 1, BlockTypes_ClothDevice.BALLOON.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "balloon1"));
		ManualHelper.addEntry("metalconstruction", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Text(ManualHelper.getManual(), "metalconstruction0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "", storageBlocks, storageSlabs, sheetmetal),
				new ManualPages.Text(ManualHelper.getManual(), "metalconstruction1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration2, 1, BlockTypes_MetalDecoration2.STEEL_WALLMOUNT.getMeta()), new ItemStack(IEContent.blockMetalDecoration2, 1, BlockTypes_MetalDecoration2.STEEL_POST.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration2, 1, BlockTypes_MetalDecoration2.ALUMINUM_WALLMOUNT.getMeta()), new ItemStack(IEContent.blockMetalDecoration2, 1, BlockTypes_MetalDecoration2.ALUMINUM_POST.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "metalconstruction2", new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.CONNECTOR_STRUCTURAL.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.itemWireCoil, 1, 3), new ItemStack(IEContent.itemWireCoil, 1, 4)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "metalconstruction3", new ItemStack(IEContent.blockMetalLadder, 1, 0), new ItemStack(IEContent.blockMetalLadder, 1, 1), new ItemStack(IEContent.blockMetalLadder, 1, 2)));
		ManualHelper.addEntry("multiblocks", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Text(ManualHelper.getManual(), "multiblocks0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()), new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()), new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.GENERATOR.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RADIATOR.getMeta())));
		ManualHelper.addEntry("metalbarrel", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Crafting(ManualHelper.getManual(), "metalbarrel0", new ItemStack(IEContent.blockMetalDevice0, 1, BlockTypes_MetalDevice0.BARREL.getMeta())));
		ManualHelper.addEntry("lighting", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Crafting(ManualHelper.getManual(), "lighting0", new ItemStack(IEContent.blockMetalDecoration2, 1, BlockTypes_MetalDecoration2.LANTERN.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "lighting1", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.ELECTRIC_LANTERN.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "lighting2"),
				new ManualPages.Crafting(ManualHelper.getManual(), "lighting3", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.FLOODLIGHT.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "lighting4"));
		ManualHelper.addEntry("tank", ManualHelper.CAT_CONSTRUCTION,
				new ManualPageMultiblock(ManualHelper.getManual(), "tank0", MultiblockSheetmetalTank.instance),
				new ManualPages.Text(ManualHelper.getManual(), "tank1"));
		ManualHelper.addEntry("silo", ManualHelper.CAT_CONSTRUCTION,
				new ManualPageMultiblock(ManualHelper.getManual(), "silo0", MultiblockSilo.instance),
				new ManualPages.Text(ManualHelper.getManual(), "silo1"),
				new ManualPages.Text(ManualHelper.getManual(), "silo2"));

		*/
		ManualInstance ieMan = ManualHelper.getManual();
		ieMan.registerSpecialElement(new ResourceLocation(MODID, "multiblock"),
				s -> new ManualElementMultiblock(ieMan,
						MultiblockHandler.getByUniqueName(new ResourceLocation(JSONUtils.getString(s, "name")))));
		InnerNode<ResourceLocation, ManualEntry> energyCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_ENERGY), 1);
		InnerNode<ResourceLocation, ManualEntry> generalCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_GENERAL), 0);
		InnerNode<ResourceLocation, ManualEntry> constructionCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_CONSTRUCTION), 1);
		InnerNode<ResourceLocation, ManualEntry> toolsCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_TOOLS), 1);
		InnerNode<ResourceLocation, ManualEntry> machinesCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_MACHINES), 1);
		InnerNode<ResourceLocation, ManualEntry> heavyMachinesCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_HEAVYMACHINES), 1);

		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "wiring"));
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "generator"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "ores"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "hemp"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "balloon"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "jerrycan"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "mining_drill"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "conveyors"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "external_heater"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "item_router"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "fluid_router"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "turntable"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "fluid_transport"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "charging_station"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "garden_cloche"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "tesla_coil"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "razor_wire"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "turrets"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "assembler"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "bottling_machine"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "automated_workbench"));
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "refinery"));
		String[][] table = formatToTable_ItemIntHashmap(ThermoelectricHandler.getThermalValuesSorted(true), "K");
		ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
		builder.addSpecialElement("values", 0, new ManualElementTable(ieMan, table, false));
		builder.readFromFile(new ResourceLocation(MODID, "thermoelectric"));
		ieMan.addEntry(energyCat, builder.create());

		addChangelogToManual();

		/*
		ManualHelper.getManual().addEntry("generator", ManualHelper.CAT_ENERGY,
				new ManualPages.Crafting(ManualHelper.getManual(), "generator0", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.DYNAMO.getMeta())),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "generator1", new ItemStack(IEContent.blockWoodenDevice1, 1, BlockTypes_WoodenDevice1.WATERMILL.getMeta()), new ItemStack(IEContent.itemMaterial, 1, 10)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "generator2", new ItemStack(IEContent.blockWoodenDevice1, 1, BlockTypes_WoodenDevice1.WINDMILL.getMeta()), new ItemStack(IEContent.itemMaterial, 1, 11)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "generator3", new ItemStack(IEContent.itemMaterial, 1, 12)));
		ManualHelper.getManual().addEntry("breaker", ManualHelper.CAT_ENERGY, new ManualPages.Crafting(ManualHelper.getManual(), "breaker0", new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.BREAKERSWITCH.getMeta())), new ManualPages.Text(ManualHelper.getManual(), "breaker1"), new ManualPages.Crafting(ManualHelper.getManual(), "breaker2", new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.REDSTONE_BREAKER.getMeta())));
		ManualHelper.getManual().addEntry("eMeter", ManualHelper.CAT_ENERGY, new ManualPages.Crafting(ManualHelper.getManual(), "eMeter0", new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.ENERGY_METER.getMeta())));
		ManualHelper.getManual().addEntry("redstoneWires", ManualHelper.CAT_ENERGY,
				new ManualPages.Crafting(ManualHelper.getManual(), "redstoneWires0", new ItemStack(IEContent.itemWireCoil, 1, 5)),
				new ManualPages.Crafting(ManualHelper.getManual(), "redstoneWires1", new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.CONNECTOR_REDSTONE.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "redstoneWires2", new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.CONNECTOR_PROBE.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "redstoneWires3"),
				new ManualPages.Text(ManualHelper.getManual(), "redstoneWires4"),
				new ManualPages.Text(ManualHelper.getManual(), "redstoneWires5"));
		//		ManualHelper.addEntry("highvoltage", ManualHelper.CAT_ENERGY,
		//				new ManualPages.Text(ManualHelper.getManual(), "highvoltage0"),
		//				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDevice,1,8),new ItemStack(IEContent.blockMetalDevice,1,4)),
		//				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDevice,1,5),new ItemStack(IEContent.blockMetalDevice,1,7)));
		//		sortedMap = DieselHandler.getFuelValuesSorted(true);
		//		Map.Entry<String,Integer>[] dieselFuels = sortedMap.entrySet().toArray(new Map.Entry[0]);
		//		table = new String[dieselFuels.length][2];
		//		for(int i=0; i<table.length; i++)
		//		{
		//			Fluid f = FluidRegistry.getFluid(dieselFuels[i].getKey());
		//			String sf = f!=null?new FluidStack(f,1000).getTranslationKey():"";
		//			int bt = dieselFuels[i].getValue();
		//			String am = Utils.formatDouble(bt/20f, "0.###")+" ("+bt+")";
		//			table[i] = new String[]{sf,am};
		//		}
		ManualHelper.addEntry("dieselgen", ManualHelper.CAT_ENERGY,
				new ManualPages.Text(ManualHelper.getManual(), "dieselgen0"),
				new ManualPageMultiblock(ManualHelper.getManual(), "dieselgen1", MultiblockDieselGenerator.instance),
				new ManualPages.Text(ManualHelper.getManual(), "dieselgen2")
				//,
				//				new ManualPages.Table(ManualHelper.getManual(), "dieselgen3", table, false)
		);
		ManualHelper.addEntry("lightningrod", ManualHelper.CAT_ENERGY,
				new ManualPageMultiblock(ManualHelper.getManual(), "lightningrod0", MultiblockLightningrod.instance),
				new ManualPages.Text(ManualHelper.getManual(), "lightningrod1"));

		ManualHelper.addEntry("jerrycan", ManualHelper.CAT_TOOLS, new ManualPages.Crafting(ManualHelper.getManual(), "jerrycan0", new ItemStack(IEContent.itemJerrycan)));
		tempItemList = NonNullList.create();
		for(int i = 0; i < 16; i++)
			tempItemList.add(ItemNBTHelper.stackWithData(new ItemStack(IEContent.itemEarmuffs), "IE:EarmuffColour", EnumDyeColor.byDyeDamage(i).getColorValue()));
		ManualHelper.addEntry("earmuffs", ManualHelper.CAT_TOOLS,
				new ManualPages.Crafting(ManualHelper.getManual(), "earmuffs0", new ItemStack(IEContent.itemEarmuffs)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "earmuffs1", (Object[])new PositionedItemStack[][]{
						new PositionedItemStack[]{new PositionedItemStack(new ItemStack(IEContent.itemEarmuffs), 24, 0), new PositionedItemStack(new ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE), 42, 0), new PositionedItemStack(tempItemList, 78, 0)},
						new PositionedItemStack[]{new PositionedItemStack(new ItemStack(IEContent.itemEarmuffs), 24, 0), new PositionedItemStack(Lists.newArrayList(new ItemStack(Items.LEATHER_HELMET), new ItemStack(Items.IRON_HELMET)), 42, 0), new PositionedItemStack(Lists.newArrayList(ItemNBTHelper.stackWithData(new ItemStack(Items.LEATHER_HELMET), "IE:Earmuffs", true), ItemNBTHelper.stackWithData(new ItemStack(Items.IRON_HELMET), "IE:Earmuffs", true)), 78, 0)}}));
		ManualHelper.addEntry("toolbox", ManualHelper.CAT_TOOLS, new ManualPages.Crafting(ManualHelper.getManual(), "toolbox0", new ItemStack(IEContent.itemToolbox)), new ManualPages.Text(ManualHelper.getManual(), "toolbox1"));
		ManualHelper.addEntry("shield", ManualHelper.CAT_TOOLS, new ManualPages.Crafting(ManualHelper.getManual(), "shield0", new ItemStack(IEContent.itemShield)),
				new ManualPages.Crafting(ManualHelper.getManual(), "shield1", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.SHIELD_FLASH.ordinal())),
				new ManualPages.Crafting(ManualHelper.getManual(), "shield2", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.SHIELD_SHOCK.ordinal())),
				new ManualPages.Crafting(ManualHelper.getManual(), "shield3", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.SHIELD_MAGNET.ordinal())));
		ManualHelper.addEntry("drill", ManualHelper.CAT_TOOLS,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "drill0", new ItemStack(IEContent.drill, 1, 0), new ItemStack(IEContent.itemMaterial, 1, 13)),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill1", new ItemStack(IEContent.itemDrillhead, 1, 0)),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill2", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.DRILL_WATERPROOF.ordinal())),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill3", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.DRILL_LUBE.ordinal())),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill4", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.DRILL_DAMAGE.ordinal())),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill5", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.DRILL_CAPACITY.ordinal())));
		ManualHelper.addEntry("maintenanceKit", ManualHelper.CAT_TOOLS,
				new ManualPages.Crafting(ManualHelper.getManual(), "maintenanceKit0", new ItemStack(IEContent.itemMaintenanceKit, 1, 0)),
				new ManualPages.Text(ManualHelper.getManual(), "maintenanceKit1"));
		ManualHelper.addEntry("revolver", ManualHelper.CAT_TOOLS,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "revolver0", new ItemStack(IEContent.itemRevolver, 1, 0), new ItemStack(IEContent.itemMaterial, 1, 13), new ItemStack(IEContent.itemMaterial, 1, 14), new ItemStack(IEContent.itemMaterial, 1, 15), new ItemStack(IEContent.itemMaterial, 1, 16)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "revolver1", new ItemStack(IEContent.itemRevolver, 1, 1)),
				new ManualPages.Crafting(ManualHelper.getManual(), "revolver2", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.REVOLVER_BAYONET.ordinal())),
				new ManualPages.Crafting(ManualHelper.getManual(), "revolver3", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.REVOLVER_MAGAZINE.ordinal())),
				new ManualPages.Crafting(ManualHelper.getManual(), "revolver4", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.REVOLVER_ELECTRO.ordinal())));
		pages = new ArrayList<IManualPage>();
		pages.add(new ManualPages.Crafting(ManualHelper.getManual(), "bullets0", BlueprintCraftingRecipe.getTypedBlueprint("bullet")));
		pages.add(new ManualPages.CraftingMulti(ManualHelper.getManual(), "bullets1", new ItemStack(IEContent.itemBullet, 1, 0), new ItemStack(IEContent.itemBullet, 1, 1), new ItemStack(IEContent.itemMold, 1, 3)));
		for(String key : BulletHandler.registry.keySet())
			if(BulletHandler.registry.get(key).isProperCartridge())
				pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), "bullets_"+key, BulletHandler.getBulletStack(key)));
		ManualHelper.addEntry("bullets", ManualHelper.CAT_TOOLS, pages.toArray(new IManualPage[pages.size()]));
		ManualHelper.addEntry("skyhook", ManualHelper.CAT_TOOLS,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "skyhook0", new ItemStack(IEContent.itemSkyhook), new ItemStack(IEContent.itemMaterial, 1, 13)),
				new ManualPages.Text(ManualHelper.getManual(), "skyhook1"));
		ManualHelper.addEntry("chemthrower", ManualHelper.CAT_TOOLS,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "chemthrower0", new ItemStack(IEContent.itemChemthrower, 1, 0), new ItemStack(IEContent.itemMaterial, 1, 13), new ItemStack(IEContent.itemToolUpgrades, 1, 0)),
				new ManualPages.Crafting(ManualHelper.getManual(), "chemthrower1", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.DRILL_CAPACITY.ordinal())),
				new ManualPages.Crafting(ManualHelper.getManual(), "chemthrower2", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.CHEMTHROWER_FOCUS.ordinal())),
				new ManualPages.Crafting(ManualHelper.getManual(), "chemthrower3", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.CHEMTHROWER_MULTITANK.ordinal())));
		ManualHelper.addEntry("powerpack", ManualHelper.CAT_TOOLS,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "powerpack0", new ItemStack(IEContent.itemPowerpack)),
				new ManualPages.Text(ManualHelper.getManual(), "powerpack1"));
		ManualHelper.addEntry("railgun", ManualHelper.CAT_TOOLS,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "railgun0", new ItemStack(IEContent.itemRailgun, 1, 0), new ItemStack(IEContent.itemMaterial, 1, 13)),
				new ManualPages.Text(ManualHelper.getManual(), "railgun1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "railgun2", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.RAILGUN_CAPACITORS.ordinal())),
				new ManualPages.Crafting(ManualHelper.getManual(), "railgun3", new ItemStack(IEContent.itemToolUpgrades, 1, ToolUpgrades.RAILGUN_SCOPE.ordinal())));

		ManualHelper.addEntry("conveyor", ManualHelper.CAT_MACHINES,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "conveyor0", new ResourceLocation("immersiveengineering", "conveyors/conveyor_basic"), new ResourceLocation("immersiveengineering", "conveyors/conveyor_uncontrolled")),
				new ManualPages.Text(ManualHelper.getManual(), "conveyor1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "conveyor2", ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":dropper")),
				new ManualPages.Crafting(ManualHelper.getManual(), "conveyor3", ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":extract")),
				new ManualPages.Crafting(ManualHelper.getManual(), "conveyor4", ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":vertical")),
				new ManualPages.Crafting(ManualHelper.getManual(), "conveyor5", ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":splitter")),
				new ManualPages.Crafting(ManualHelper.getManual(), "conveyor6", ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":covered")),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "conveyor7", ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":droppercovered"), ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":extractcovered"), ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID+":verticalcovered")));
		ManualHelper.addEntry("furnaceHeater", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "furnaceHeater0", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.FURNACE_HEATER.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "furnaceHeater1"),
				new ManualPages.Text(ManualHelper.getManual(), "furnaceHeater2"));
		ManualHelper.addEntry("sorter", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "sorter0", new ItemStack(IEContent.blockWoodenDevice0, 1, BlockTypes_WoodenDevice0.SORTER.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "sorter1"));
		ManualHelper.addEntry("fluidSorter", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "fluidSorter0", new ItemStack(IEContent.blockWoodenDevice0, 1, BlockTypes_WoodenDevice0.FLUID_SORTER.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "fluidSorter1"));
		ManualHelper.addEntry("turntable", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "turntable0", new ItemStack(IEContent.blockWoodenDevice0, 1, BlockTypes_WoodenDevice0.TURNTABLE.getMeta())));
		pages = new ArrayList<IManualPage>();
		pages.add(new ManualPages.Crafting(ManualHelper.getManual(), "fluidPipes0", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta())));
		pages.add(new ManualPages.Text(ManualHelper.getManual(), "fluidPipes1"));
		pages.add(new ManualPages.Crafting(ManualHelper.getManual(), "fluidPipes2", new ItemStack(IEContent.blockMetalDevice0, 1, BlockTypes_MetalDevice0.FLUID_PUMP.getMeta())));
		pages.add(new ManualPages.Text(ManualHelper.getManual(), "fluidPipes3"));
		if(IEConfig.MACHINES.pump_infiniteWater||IEConfig.MACHINES.pump_placeCobble)
			pages.add(new ManualPages.Text(ManualHelper.getManual(), "fluidPipes4"));
		pages.add(new ManualPages.Crafting(ManualHelper.getManual(), "fluidPipes5", new ItemStack(IEContent.blockMetalDevice0, 1, BlockTypes_MetalDevice0.FLUID_PLACER.getMeta())));
		ManualHelper.addEntry("fluidPipes", ManualHelper.CAT_MACHINES, pages.toArray(new IManualPage[pages.size()]));
		ManualHelper.addEntry("chargingStation", ManualHelper.CAT_MACHINES, new ManualPages.Crafting(ManualHelper.getManual(), "chargingStation0", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.CHARGING_STATION.getMeta())), new ManualPages.Text(ManualHelper.getManual(), "chargingStation1"));
		ManualHelper.addEntry("belljar", ManualHelper.CAT_MACHINES, new ManualPages.Crafting(ManualHelper.getManual(), "belljar0", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.BELLJAR.getMeta())), new ManualPages.Text(ManualHelper.getManual(), "belljar1"));
		ManualHelper.addEntry("teslaCoil", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "teslaCoil0", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.TESLA_COIL.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "teslaCoil1"),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "teslaCoil2", new ItemStack(IEContent.itemsFaradaySuit[0]), new ItemStack(IEContent.itemsFaradaySuit[1]), new ItemStack(IEContent.itemsFaradaySuit[2]), new ItemStack(IEContent.itemsFaradaySuit[3]), new ItemStack(IEContent.itemFluorescentTube)),
				new ManualPages.Text(ManualHelper.getManual(), "teslaCoil3"),
				new ManualPages.Text(ManualHelper.getManual(), "teslaCoil4"));
		ManualHelper.addEntry("razorwire", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "razorwire0", new ItemStack(IEContent.blockMetalDecoration2, 1, BlockTypes_MetalDecoration2.RAZOR_WIRE.getMeta())));
		ManualHelper.addEntry("turret", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "turret0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "turret1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "turret2", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.TURRET_CHEM.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "turret3"),
				new ManualPages.Crafting(ManualHelper.getManual(), "turret4", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.TURRET_GUN.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "turret5"));

		ManualHelper.addEntry("improvedBlastfurnace", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "improvedBlastfurnace0", new ItemStack(IEContent.blockStoneDecoration, 1, BlockTypes_StoneDecoration.BLASTBRICK_REINFORCED.getMeta())),
				new ManualPageMultiblock(ManualHelper.getManual(), "improvedBlastfurnace1", MultiblockBlastFurnaceAdvanced.instance),
				new ManualPages.Text(ManualHelper.getManual(), "improvedBlastfurnace2"),
				new ManualPages.Crafting(ManualHelper.getManual(), "improvedBlastfurnace3", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.BLAST_FURNACE_PREHEATER.getMeta())));
		tempItemList = NonNullList.create();
		IEContent.itemMold.fillItemGroup(ImmersiveEngineering.itemGroup, tempItemList);
		ManualHelper.addEntry("metalPress", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "metalPress0", MultiblockMetalPress.instance),
				new ManualPages.Text(ManualHelper.getManual(), "metalPress1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "metalPress2", BlueprintCraftingRecipe.getTypedBlueprint("molds")),
				new ManualPageBlueprint(ManualHelper.getManual(), "metalPress3", tempItemList.toArray(new ItemStack[tempItemList.size()])),
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "metalPress4", new ItemStack(IEContent.itemMold, 1, 5), new ItemStack(IEContent.itemMold, 1, 6), new ItemStack(IEContent.itemMold, 1, 7)));
		ManualHelper.addEntry("assembler", ManualHelper.CAT_MACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "assembler0", MultiblockAssembler.instance),
				new ManualPages.Text(ManualHelper.getManual(), "assembler1"),
				new ManualPages.Text(ManualHelper.getManual(), "assembler2"));
		ManualHelper.addEntry("bottlingMachine", ManualHelper.CAT_MACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "bottlingMachine0", MultiblockBottlingMachine.instance),
				new ManualPages.Text(ManualHelper.getManual(), "bottlingMachine1"));
		ManualHelper.addEntry("autoworkbench", ManualHelper.CAT_MACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "autoworkbench0", MultiblockAutoWorkbench.instance),
				new ManualPages.Text(ManualHelper.getManual(), "autoworkbench1"));
		ManualHelper.addEntry("crusher", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "crusher0", MultiblockCrusher.instance),
				new ManualPages.Text(ManualHelper.getManual(), "crusher1"));
		ManualHelper.addEntry("mixer", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "mixer0", MultiblockMixer.instance),
				new ManualPages.Text(ManualHelper.getManual(), "mixer1"),
				new ManualPages.Text(ManualHelper.getManual(), "mixer2"));
		sortedMap = SqueezerRecipe.getFluidValuesSorted(IEContent.fluidPlantoil, true);
		table = formatToTable_ItemIntHashmap(sortedMap, "mB");
		ManualHelper.addEntry("squeezer", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "squeezer0", MultiblockSqueezer.instance),
				new ManualPages.Text(ManualHelper.getManual(), "squeezer1"),
				new ManualPages.Table(ManualHelper.getManual(), "squeezer2T", table, false));
		sortedMap = FermenterRecipe.getFluidValuesSorted(IEContent.fluidEthanol, true);
		table = formatToTable_ItemIntHashmap(sortedMap, "mB");
		ManualHelper.addEntry("fermenter", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "fermenter0", MultiblockFermenter.instance),
				new ManualPages.Text(ManualHelper.getManual(), "fermenter1"),
				new ManualPages.Table(ManualHelper.getManual(), "fermenter2T", table, false));
		ManualHelper.addEntry("refinery", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "refinery0", MultiblockRefinery.instance),
				new ManualPages.Text(ManualHelper.getManual(), "refinery1"));
		//		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		//		ManualHelper.addEntry("bottlingMachine", ManualHelper.CAT_MACHINES,
		//				new ManualPageMultiblock(ManualHelper.getManual(), "bottlingMachine0", MultiblockBottlingMachine.instance),
		//				new ManualPages.Text(ManualHelper.getManual(), "bottlingMachine1"));
		sortedMap = FermenterRecipe.getFluidValuesSorted(IEContent.fluidEthanol, true);
		String[][] table2 = formatToTable_ItemIntHashmap(sortedMap, "mB");
//		ManualHelper.addEntry("biodiesel", ManualHelper.CAT_HEAVYMACHINES,
//				new ManualPages.Text(ManualHelper.getManual(), "biodiesel0"),
//				new ManualPageMultiblock(ManualHelper.getManual(), "biodiesel1", MultiblockSqueezer.instance),
//				new ManualPages.Table(ManualHelper.getManual(), "biodiesel1T", table, false),
//				new ManualPageMultiblock(ManualHelper.getManual(), "biodiesel2", MultiblockFermenter.instance),
//				new ManualPages.Table(ManualHelper.getManual(), "biodiesel2T", table2, false),
//				new ManualPageMultiblock(ManualHelper.getManual(), "biodiesel3", MultiblockRefinery.instance),
//				new ManualPages.Text(ManualHelper.getManual(), "biodiesel4"));
		ManualHelper.addEntry("arcfurnace", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "arcfurnace0", MultiblockArcFurnace.instance),
				new ManualPages.Text(ManualHelper.getManual(), "arcfurnace1"),
				new ManualPages.Text(ManualHelper.getManual(), "arcfurnace2"),
				new ManualPages.Text(ManualHelper.getManual(), "arcfurnace3"),
				new ManualPages.Text(ManualHelper.getManual(), "arcfurnace4"),
				new ManualPages.Text(ManualHelper.getManual(), "arcfurnace5"));
		ManualHelper.addEntry("excavator", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "excavator0", MultiblockExcavator.instance),
				new ManualPageMultiblock(ManualHelper.getManual(), "", MultiblockBucketWheel.instance),
				new ManualPageMultiblock(ManualHelper.getManual(), "", MultiblockExcavatorDemo.instance),
				new ManualPages.Text(ManualHelper.getManual(), "excavator1"));
		*/


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

	static ManualEntry mineralEntry;

	public static void handleMineralManual()
	{
		/*if(ManualHelper.getManual()!=null)
		{
			ArrayList<IManualPage> pages = new ArrayList();
			pages.add(new ManualPages.Text(ManualHelper.getManual(), "minerals0"));
			pages.add(new ManualPages.Crafting(ManualHelper.getManual(), "minerals1", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta())));
			pages.add(new ManualPages.Text(ManualHelper.getManual(), "minerals2"));

			final ExcavatorHandler.MineralMix[] minerals = ExcavatorHandler.mineralList.keySet().toArray(new ExcavatorHandler.MineralMix[0]);

			ArrayList<Integer> mineralIndices = new ArrayList();
			for(int i = 0; i < minerals.length; i++)
				if(minerals[i].isValid())
					mineralIndices.add(i);
			Collections.sort(mineralIndices, new Comparator<Integer>()
			{
				@Override
				public int compare(Integer paramT1, Integer paramT2)
				{
					String name1 = Lib.DESC_INFO+"mineral."+minerals[paramT1].name;
					String localizedName1 = I18n.format(name1);
					if(localizedName1==name1)
						localizedName1 = minerals[paramT1].name;

					String name2 = Lib.DESC_INFO+"mineral."+minerals[paramT2].name;
					String localizedName2 = I18n.format(name2);
					if(localizedName2==name2)
						localizedName2 = minerals[paramT2].name;
					return localizedName1.compareToIgnoreCase(localizedName2);
				}
			});
			for(int i : mineralIndices)
			{
				String name = Lib.DESC_INFO+"mineral."+minerals[i].name;
				String localizedName = I18n.format(name);
				if(localizedName.equalsIgnoreCase(name))
					localizedName = minerals[i].name;

				String s0 = "";
				if(minerals[i].dimensionWhitelist!=null&&minerals[i].dimensionWhitelist.length > 0)
				{
					String validDims = "";
					for(int dim : minerals[i].dimensionWhitelist)
						validDims += (!validDims.isEmpty()?", ": "")+"<dim;"+dim+">";
					s0 = I18n.format("ie.manual.entry.mineralsDimValid", localizedName, validDims);
				}
				else if(minerals[i].dimensionBlacklist!=null&&minerals[i].dimensionBlacklist.length > 0)
				{
					String invalidDims = "";
					for(int dim : minerals[i].dimensionBlacklist)
						invalidDims += (!invalidDims.isEmpty()?", ": "")+"<dim;"+dim+">";
					s0 = I18n.format("ie.manual.entry.mineralsDimInvalid", localizedName, invalidDims);
				}
				else
					s0 = I18n.format("ie.manual.entry.mineralsDimAny", localizedName);

				ArrayList<Integer> formattedOutputs = new ArrayList<Integer>();
				for(int j = 0; j < minerals[i].oreOutput.size(); j++)
					formattedOutputs.add(j);
				final int fi = i;
				Collections.sort(formattedOutputs, new Comparator<Integer>()
				{
					@Override
					public int compare(Integer paramT1, Integer paramT2)
					{
						return -Double.compare(minerals[fi].recalculatedChances[paramT1], minerals[fi].recalculatedChances[paramT2]);
					}
				});

				String s1 = "";
				NonNullList<ItemStack> sortedOres = NonNullList.withSize(minerals[i].oreOutput.size(), ItemStack.EMPTY);
				for(int j = 0; j < formattedOutputs.size(); j++)
					if(!minerals[i].oreOutput.get(j).isEmpty())
					{
						int sorted = formattedOutputs.get(j);
						s1 += "<br>"+new DecimalFormat("00.00").format(minerals[i].recalculatedChances[sorted]*100).replaceAll("\\G0", " ")+"% "+minerals[i].oreOutput.get(sorted).getDisplayName();
						sortedOres.set(j, minerals[i].oreOutput.get(sorted));
					}
				String s2 = I18n.format("ie.manual.entry.minerals3", s0, s1);
				pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), s2, sortedOres));
			}

//			String[][][] multiTables = formatToTable_ExcavatorMinerals();
//			for(String[][] minTable : multiTables)
//				pages.add(new ManualPages.Table(ManualHelper.getManual(), "", minTable,true));
			//if(mineralEntry!=null)
			//	mineralEntry.setPages(pages.toArray(new IManualPage[pages.size()]));
			//else
			//{
			//	ManualHelper.addEntry("minerals", ManualHelper.CAT_GENERAL, pages.toArray(new IManualPage[pages.size()]));
			//	mineralEntry = ManualHelper.getManual().getEntry("minerals");
			//}
		}*/
	}

	static String[][][] formatToTable_ExcavatorMinerals()
	{
		ExcavatorHandler.MineralMix[] minerals = ExcavatorHandler.mineralList.keySet().toArray(new ExcavatorHandler.MineralMix[0]);
		String[][][] multiTables = new String[1][minerals.length][2];
		int curTable = 0;
		int totalLines = 0;
		for(int i = 0; i < minerals.length; i++)
			if(minerals[i].isValid())
			{
				String name = Lib.DESC_INFO+"mineral."+minerals[i].name;
				if(I18n.format(name)==name)
					name = minerals[i].name;
				multiTables[curTable][i][0] = name;
				multiTables[curTable][i][1] = "";
				for(int j = 0; j < minerals[i].oreOutput.size(); j++)
					if(!minerals[i].oreOutput.get(j).isEmpty())
					{
						multiTables[curTable][i][1] += minerals[i].oreOutput.get(j).getDisplayName()+" "+(new DecimalFormat("#.00").format(minerals[i].recalculatedChances[j]*100)+"%")+(j < minerals[i].oreOutput.size()-1?"\n": "");
						totalLines++;
					}
				if(i < minerals.length-1&&totalLines+minerals[i+1].oreOutput.size() >= 13)
				{
					String[][][] newMultiTables = new String[multiTables.length+1][minerals.length][2];
					System.arraycopy(multiTables, 0, newMultiTables, 0, multiTables.length);
					multiTables = newMultiTables;
					totalLines = 0;
					curTable++;
				}
			}
		return multiTables;
	}

	public void addChangelogToManual()
	{
		SortedMap<ComparableVersion, ManualEntry> allChanges = new TreeMap<>(Comparator.reverseOrder());
		ComparableVersion currIEVer = new ComparableVersion(ImmersiveEngineering.VERSION);
		//Included changelog
		try(InputStream in = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(MODID,
				"changelog.json")).getInputStream())
		{
			JsonElement ele = new JsonParser().parse(new InputStreamReader(in));
			JsonObject upToCurrent = ele.getAsJsonObject();
			for(Entry<String, JsonElement> entry : upToCurrent.entrySet())
			{
				ComparableVersion version = new ComparableVersion(entry.getKey());
				ManualEntry manualEntry = addVersionToManual(currIEVer, version,
						entry.getValue().getAsString(), false);
				if(manualEntry!=null)
					allChanges.put(version, manualEntry);
			}
		} catch(IOException x)
		{
			x.printStackTrace();
		}
		//Changelog from update JSON
		CheckResult result = VersionChecker.getResult(ModLoadingContext.get().getActiveContainer().getModInfo());
		if(result.status!=Status.PENDING&&result.status!=Status.FAILED)
			for(Entry<ComparableVersion, String> e : result.changes.entrySet())
				allChanges.put(e.getKey(), addVersionToManual(currIEVer, e.getKey(), e.getValue(), true));

		ManualInstance ieMan = ManualHelper.getManual();
		InnerNode<ResourceLocation, ManualEntry> updateCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_UPDATE), -1);
		for(ManualEntry entry : allChanges.values())
			ManualHelper.getManual().addEntry(updateCat, entry);
	}

	private ManualEntry addVersionToManual(ComparableVersion currVer, ComparableVersion version, String changes, boolean ahead)
	{
		String title = version.toString();
		if(ahead)
			title += I18n.format("ie.manual.newerVersion");
		else
		{
			int cmp = currVer.compareTo(version);
			if(cmp==0)
				title += I18n.format("ie.manual.currentVersion");
			//TODO what are these supposed to do???

			//else if(cmp < 0)
			//	return null;
		}

		String text = changes.replace("\t", "  ");
		ManualEntry.ManualEntryBuilder builder = new ManualEntryBuilder(ManualHelper.getManual());
		builder.setContent(title, "", text);
		builder.setLocation(new ResourceLocation(MODID, "changelog_"+version.toString()));
		return builder.create();
	}

	@Override
	public void serverStarting()
	{
	}

	//TODO are these here rather than in ClientEventHandler for any particular reason???
	@SubscribeEvent
	public static void textureStichPre(TextureStitchEvent.Pre event)
	{
		if(event.getMap()!=mc().getTextureMap())
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
		if(event.getMap()!=mc().getTextureMap())
			return;
		ImmersiveEngineering.proxy.clearRenderCaches();
		RevolverItem.retrieveRevolverTextures(event.getMap());
		for(DrillHeadPerm p : DrillHeadPerm.ALL_PERMS)
		{
			p.sprite = event.getMap().getSprite(p.texture);
			Preconditions.checkNotNull(p.sprite);
		}
		WireType.iconDefaultWire = event.getMap().getSprite(new ResourceLocation(MODID, "block/wire"));
		AtlasTexture texturemap = Minecraft.getInstance().getTextureMap();
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

	public static String getPropertyString(Map<IProperty, Comparable> propertyMap)
	{
		StringBuilder stringbuilder = new StringBuilder();
		for(Entry<IProperty, Comparable> entry : propertyMap.entrySet())
		{
			if(stringbuilder.length()!=0)
				stringbuilder.append(",");
			IProperty iproperty = entry.getKey();
			Comparable comparable = entry.getValue();
			stringbuilder.append(iproperty.getName());
			stringbuilder.append("=");
			stringbuilder.append(iproperty.getName(comparable));
		}
		if(stringbuilder.length()==0)
			stringbuilder.append("normal");
		return stringbuilder.toString();
	}


	HashMap<String, IETileSound> soundMap = new HashMap<String, IETileSound>();
	HashMap<BlockPos, IETileSound> tileSoundMap = new HashMap<BlockPos, IETileSound>();

	@Override
	public void handleTileSound(SoundEvent soundEvent, TileEntity tile, boolean tileActive, float volume, float pitch)
	{
		BlockPos pos = tile.getPos();
		IETileSound sound = tileSoundMap.get(pos);
		if(sound==null&&tileActive)
		{
			sound = ClientUtils.generatePositionedIESound(soundEvent, volume, pitch, true, 0, pos);
			tileSoundMap.put(pos, sound);
		}
		else if(sound!=null&&!tileActive)
		{
			sound.donePlaying = true;
			mc().getSoundHandler().stop(sound);
			tileSoundMap.remove(pos);
		}
	}

	@Override
	public void stopTileSound(String soundName, TileEntity tile)
	{
		IETileSound sound = soundMap.get(soundName);
		if(sound!=null&&new BlockPos(sound.getX(), sound.getY(), sound.getZ()).equals(tile.getPos()))
			mc().getSoundHandler().stop(sound);
	}

	@Override
	public void onWorldLoad()
	{
		/*TODO
		if(!ShaderMinecartModel.rendersReplaced)
		{
			for(Object render : mc().getRenderManager().renderers.values())
				if(MinecartRenderer.class.isAssignableFrom(render.getClass()))
				{
					Object wrapped = ObfuscationReflectionHelper.getPrivateValue(MinecartRenderer.class, (MinecartRenderer)render, "field_77013_a");//modelMinecart
					if(wrapped instanceof MinecartModel)
						ObfuscationReflectionHelper.setPrivateValue(MinecartRenderer.class, (MinecartRenderer)render,
								new ShaderMinecartModel((MinecartModel)wrapped), "field_77013_a");//modelMinecart
				}
			ShaderMinecartModel.rendersReplaced = true;
		}
		if(!IEBipedLayerRenderer.rendersAssigned)
		{
			for(Object render : mc().getRenderManager().renderers.values())
				if(BipedRenderer.class.isAssignableFrom(render.getClass()))
					((BipedRenderer)render).addLayer(new IEBipedLayerRenderer<>((BipedRenderer)render));
				else if(ArmorStandRenderer.class.isAssignableFrom(render.getClass()))
					((ArmorStandRenderer)render).addLayer(new IEBipedLayerRenderer<>((ArmorStandRenderer)render));
			IEBipedLayerRenderer.rendersAssigned = true;
		}*/
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
		FluidSplashParticle particle = new FluidSplashParticle(fs.getFluid(), world, x, y, z, mx, my, mz);
		mc().particles.addEffect(particle);
	}

	@Override
	public void spawnBubbleFX(World world, FluidStack fs, double x, double y, double z, double mx, double my, double mz)
	{
		IEBubbleParticle particle = new IEBubbleParticle(world, x, y, z, mx, my, mz);
		mc().particles.addEffect(particle);
	}

	@Override
	public void spawnFractalFX(World world, double x, double y, double z, Vec3d direction, double scale, int prefixColour, float[][] colour)
	{
		if(prefixColour >= 0)
			colour = prefixColour==1?FractalParticle.COLOUR_ORANGE: prefixColour==2?FractalParticle.COLOUR_RED: FractalParticle.COLOUR_LIGHTNING;
		FractalParticle particle = new FractalParticle(world, x, y, z, direction, scale, colour[0], colour[1]);
		mc().particles.addEffect(particle);
	}

	@Override
	public void draw3DBlockCauldron()
	{
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState state = Blocks.CAULDRON.getDefaultState();
		IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);

		renderBlockModel(blockRenderer, model, state);
	}

	@Override
	public void drawSpecificFluidPipe(String configuration)
	{
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState state = MetalDevices.fluidPipe.getDefaultState();
		IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);
		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 0, 1);
		renderBlockModel(blockRenderer, model, state);
		GlStateManager.popMatrix();
	}

	private void renderBlockModel(BlockRendererDispatcher blockRenderer, IBakedModel model, BlockState state)
	{
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, .75f, false);
	}

	static Map<String, Boolean> hasArmorModel = new HashMap<>();

	@Override
	public boolean armorHasCustomModel(ItemStack stack)
	{
		if(!stack.isEmpty()&&stack.getItem() instanceof ArmorItem)
		{
			Boolean b = hasArmorModel.get(stack.getTranslationKey());
			if(b==null)
				try
				{
					BipedModel<?> model = stack.getItem().getArmorModel(mc().player, stack, ((ArmorItem)stack.getItem()).getEquipmentSlot(), null);
					b = model!=null&&model.getClass()!=BipedModel.class; //Model isn't a base Biped
					hasArmorModel.put(stack.getTranslationKey(), b);
				} catch(Exception e)
				{
				}
			return b==null?false: b;
		}
		return false;
	}

	@Override
	public boolean drawConveyorInGui(String conveyor, Direction facing)
	{
		IConveyorBelt con = ConveyorHandler.getConveyor(new ResourceLocation(conveyor), null);
		if(con!=null)
		{
			GlStateManager.pushMatrix();
			List<BakedQuad> quads = ModelConveyor.getBaseConveyor(facing, 1, new Matrix4(facing), ConveyorDirection.HORIZONTAL,
					ClientUtils.getSprite(con.getActiveTexture()), new boolean[]{true, true}, new boolean[]{true, true}, null, DyeColor.WHITE);
//			GlStateManager.translate(0, 0, 1);
			ClientUtils.renderQuads(quads, 1, 1, 1, 1);
			GlStateManager.popMatrix();
			return true;
		}
		return false;
	}

	@Override
	public void drawFluidPumpTop()
	{
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState state = MetalDevices.fluidPump.getDefaultState();
		state = state.with(IEProperties.MULTIBLOCKSLAVE, true);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);

		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 0, 1);
		renderBlockModel(blockRenderer, model, state);
		GlStateManager.popMatrix();
	}

	static <T> String[][] formatToTable_ItemIntHashmap(Map<T, Integer> map, String valueType)
	{
		List<Entry<T, Integer>> sortedMapArray = new ArrayList<>(map.entrySet());
		sortedMapArray.sort(Comparator.comparing(Entry::getValue));
		ArrayList<String[]> list = new ArrayList<>();
		try
		{
			for(Entry<T, Integer> entry : sortedMapArray)
			{
				String item = entry.getKey().toString();
				if(entry.getKey() instanceof ResourceLocation)
				{
					ResourceLocation key = (ResourceLocation)entry.getKey();
					if(ApiUtils.isNonemptyItemTag(key))
					{
						ItemStack is = IEApi.getPreferredTagStack(key);
						if(!is.isEmpty())
							item = is.getDisplayName().getFormattedText();
					}
				}

				if(item!=null)
				{
					int bt = entry.getValue();
					String am = bt+" "+valueType;
					list.add(new String[]{item, am});
				}
			}
		} catch(Exception e)
		{
		}
		return list.toArray(new String[0][]);
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
		for(BlockRenderLayer r : BlockRenderLayer.values())
			IESmartObjModel.modelCache.remove(new RenderCacheKey(state, r));
		IESmartObjModel.modelCache.remove(new RenderCacheKey(state, null));
	}

	@Override
	public void removeStateFromConnectionModelCache(BlockState state)
	{
		//TODO
		for(BlockRenderLayer r : BlockRenderLayer.values())
			BakedConnectionModel.cache.invalidate(new RenderCacheKey(state, r));
		BakedConnectionModel.cache.invalidate(new RenderCacheKey(state, null));
	}

	@Override
	public void clearConnectionModelCache()
	{
		BakedConnectionModel.cache.invalidateAll();
	}

	@Override
	public void addFailedConnection(Connection connection, BlockPos reason, PlayerEntity player)
	{
		ClientEventHandler.FAILED_CONNECTIONS.put(connection,
				new ImmutablePair<>(reason, new AtomicInteger(200)));
	}

	@Override
	public void reloadManual()
	{
		if(ManualHelper.getManual()!=null)
			ManualHelper.getManual().reload();
	}

	static
	{
		IEApi.renderCacheClearers.add(IESmartObjModel.modelCache::clear);
		IEApi.renderCacheClearers.add(IESmartObjModel.cachedBakedItemModels::invalidateAll);
		IEApi.renderCacheClearers.add(BakedConnectionModel.cache::invalidateAll);
		IEApi.renderCacheClearers.add(ModelConveyor.modelCache::clear);
		IEApi.renderCacheClearers.add(ModelConfigurableSides.modelCache::invalidateAll);
		IEApi.renderCacheClearers.add(FluidPipeTileEntity.cachedOBJStates::clear);
		IEApi.renderCacheClearers.add(BelljarRenderer::reset);
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
	public void registerContainersAndScreens()
	{
		super.registerContainersAndScreens();
		registerScreen(Lib.GUIID_CokeOven, CokeOvenScreen::new);
		registerScreen(Lib.GUIID_AlloySmelter, AlloySmelterScreen::new);
		registerScreen(Lib.GUIID_BlastFurnace, BlastFurnaceScreen::new);
		registerScreen(Lib.GUIID_WoodenCrate, CrateScreen::new);
		registerScreen(Lib.GUIID_Workbench, ModWorkbenchScreen::new);
		registerScreen(Lib.GUIID_Assembler, AssemblerScreen::new);
		registerScreen(Lib.GUIID_Sorter, SorterScreen::new);
		registerScreen(Lib.GUIID_Squeezer, SqueezerScreen::new);
		registerScreen(Lib.GUIID_Fermenter, FermenterScreen::new);
		registerScreen(Lib.GUIID_Refinery, RefineryScreen::new);
		registerScreen(Lib.GUIID_ArcFurnace, ArcFurnaceScreen::new);
		registerScreen(Lib.GUIID_AutoWorkbench, AutoWorkbenchScreen::new);
		registerScreen(Lib.GUIID_Mixer, MixerScreen::new);
		registerScreen(Lib.GUIID_Turret, TurretScreen::new);
		registerScreen(Lib.GUIID_FluidSorter, FluidSorterScreen::new);
		registerScreen(Lib.GUIID_Belljar, BelljarScreen::new);
		registerScreen(Lib.GUIID_ToolboxBlock, ToolboxBlockScreen::new);

		registerScreen(Lib.GUIID_Toolbox, ToolboxScreen::new);
		registerScreen(Lib.GUIID_Revolver, RevolverScreen::new);
		registerScreen(Lib.GUIID_MaintenanceKit, MaintenanceKitScreen::new);
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
		return super.useIEOBJRenderer(props).setTEISR(() -> () -> IEOBJItemRenderer.INSTANCE);
	}
}
