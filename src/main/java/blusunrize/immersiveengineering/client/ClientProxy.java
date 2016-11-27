package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.client.fx.EntityFXSparks;
import blusunrize.immersiveengineering.client.gui.*;
import blusunrize.immersiveengineering.client.manual.IEManualInstance;
import blusunrize.immersiveengineering.client.manual.ManualPageShader;
import blusunrize.immersiveengineering.client.models.*;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.client.models.smart.ConnLoader;
import blusunrize.immersiveengineering.client.models.smart.ConnModelReal;
import blusunrize.immersiveengineering.client.models.smart.ConnModelReal.ExtBlockstateAdapter;
import blusunrize.immersiveengineering.client.render.*;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.blocks.BlockIEFluid;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IColouredBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import blusunrize.immersiveengineering.common.blocks.cloth.BlockTypes_ClothDevice;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorBasic;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorDrop;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorSplit;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorVertical;
import blusunrize.immersiveengineering.common.blocks.multiblocks.*;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDecoration;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.entities.*;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IGuiItem;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.ItemDrillhead.DrillHeadPerm;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.commands.CommandHandler;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.util.sound.IETileSound;
import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualInstance.ManualEntry;
import blusunrize.lib.manual.ManualPages;
import blusunrize.lib.manual.ManualPages.PositionedItemStack;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelMinecart;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRedstone;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("deprecation")
public class ClientProxy extends CommonProxy
{
	public static TextureMap revolverTextureMap;
	public static final ResourceLocation revolverTextureResource = new ResourceLocation("textures/atlas/immersiveengineering/revolvers.png");
	public static FontRenderer nixieFontOptional;
	public static IENixieFontRender nixieFont;
	public static IEItemFontRender itemFont;

	@Override
	public void preInit()
	{
		ModelLoaderRegistry.registerLoader(IEOBJLoader.instance);
		OBJLoader.INSTANCE.addDomain("immersiveengineering");
		IEOBJLoader.instance.addDomain("immersiveengineering");

		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(IEContent.itemBullet, 1, 2), new ImmersiveModelRegistry.ItemModelReplacement()
		{
			@Override
			public IBakedModel createBakedModel(IBakedModel existingModel)
			{
				return new ModelItemDynamicOverride(existingModel, null);
			}
		});
		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(IEContent.itemShader), new ImmersiveModelRegistry.ItemModelReplacement()
		{
			@Override
			public IBakedModel createBakedModel(IBakedModel existingModel)
			{
				return new ModelItemDynamicOverride(existingModel, null);
			}
		});

		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(IEContent.itemTool, 1, 2), new ImmersiveModelRegistry.ItemModelReplacement_OBJ("immersiveengineering:models/item/tool/voltmeter.obj")
				.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().translate(-.25, .375, .3125).rotate(-Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().translate(-.25, .375, -.625).rotate(-Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(-0.25, .125, .25).scale(.625f,.625f,.625f).rotate(-Math.PI*.375, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(-0.5, .125, -.3125).scale(.625f,.625f,.625f).rotate(-Math.PI*.375, 0, 1, 0))
				.setTransformations(TransformType.FIXED, new Matrix4().translate(-.5,.5,-.5).scale(1,1,1).rotate(Math.PI, 0, 1, 0))
				.setTransformations(TransformType.GUI, new Matrix4().translate(0,.5,0).scale(1.125,1.125,1.125).rotate(-Math.PI*.25, 0, 1, 0))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(.25,.25,.25).scale(.5,.5,.5)));

		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(IEContent.itemToolbox), new ImmersiveModelRegistry.ItemModelReplacement_OBJ("immersiveengineering:models/item/toolbox.obj")
				.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().scale(.375,.375,.375).translate(-.75, 1.25, .3125).rotate(-Math.PI*.75, 0, 1, 0))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().scale(.375,.375,.375).translate(-.125, 1.25, .9375).rotate(Math.PI*.25, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(-.25, .1875, .3125).scale(.625,.625,.625).rotate(Math.PI, 0, 1, 0).rotate(-Math.PI*.5, 1, 0, 0))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(-.25, -.4375, .3125).scale(.625,.625,.625).rotate(Math.PI*.5, 1, 0, 0))
				.setTransformations(TransformType.FIXED, new Matrix4().translate(.5,.875,-.5).scale(1,1,1).rotate(Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.GUI, new Matrix4().translate(-.625,.75,0).scale(.875,.875,.875).rotate(-Math.PI*.6875, 0, 1, 0))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(.25,.5,.25).scale(.5,.5,.5)));

		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(IEContent.itemRevolver, 1, 0), new ImmersiveModelRegistry.ItemModelReplacement_OBJ("immersiveengineering:models/item/revolver/revolver.obj")
				.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().rotate(Math.toRadians(-90), 0,1,0).scale(.1875, .25, .25).translate(-.5, .4375, .5))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().rotate(Math.toRadians(90), 0,1,0).scale(.1875, .25, .25).translate(.45, .4375, .5))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(-.125, .125,-.125).scale(.125, .125, .125).rotate(Math.toRadians(-90), 0,1,0).rotate(Math.toRadians(-10), 0,0,1))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(.0, .0625,-.125).scale(.125, .125, .125).rotate(Math.toRadians(90), 0,1,0).rotate(Math.toRadians(0), 0,0,1))
				.setTransformations(TransformType.GUI, new Matrix4().translate(.1875, -.0781225, -.15625).scale(.2, .2, .2).rotate(Math.toRadians(-40), 0,1,0).rotate(Math.toRadians(-35), 0,0,1))
				.setTransformations(TransformType.FIXED, new Matrix4().translate(-.25, 0,-.0625).scale(.1875, .1875, .1875).rotate(Math.PI, 0, 1, 0).rotate(Math.toRadians(-40), 0, 0, 1))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(.125, 0, .0625).scale(.125, .125, .125)));

		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(IEContent.itemDrill, 1, 0), new ImmersiveModelRegistry.ItemModelReplacement_OBJ("immersiveengineering:models/item/drill/drill_diesel.obj")
				.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().scale(.375,.375,.375).translate(-.25, 1, .5).rotate(Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().scale(.375,.375,.375).translate(-1.5, 1.125, .875).rotate(-Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(.0625, .9375, .25).scale(.75,.75,.75).rotate(Math.PI*.75, 0, 1, 0).rotate(Math.PI*.375, 0, 0, 1).rotate(-Math.PI*.25, 1, 0, 0))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(.0625, .9375, .25).scale(-.75,.75,.75).rotate(-Math.PI*.75, 0, 1, 0).rotate(-Math.PI*.375, 0, 0, 1).rotate(-Math.PI*.25, 1, 0, 0))
				.setTransformations(TransformType.FIXED, new Matrix4().translate(.1875,.0625,.15625).scale(.4375,.4375,.4375).rotate(-Math.PI*.25, 0, 0, 1))
				.setTransformations(TransformType.GUI, new Matrix4().translate(-.5,.25,0).scale(.75,.75,.75).rotate(-Math.PI*.6875, 0, 1, 0).rotate(-Math.PI*.125, 0, 0, 1))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(.125,.25,.25).scale(.5,.5,.5)));


		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(IEContent.itemChemthrower, 1, 0), new ImmersiveModelRegistry.ItemModelReplacement_OBJ("immersiveengineering:models/item/chemthrower.obj")
				.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().scale(.375,.375,.375).translate(-.25, 1, .5).rotate(Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().scale(.25,.25,.25).translate(-.5, 1.25, .75).rotate(-Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(-.25, .625, -.25).scale(.5,.5,.5).rotate(Math.PI*.75, 0, 1, 0).rotate(Math.PI*.25, 0, 0, 1).rotate(-Math.PI*.25, 1, 0, 0))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(-.25, -.4375, .3125).scale(.625,.625,.625).rotate(Math.PI*.5, 1, 0, 0))
				.setTransformations(TransformType.FIXED, new Matrix4().translate(.125,.125,-.25).scale(.3125,.3125,.3125).rotate(Math.PI, 0, 1, 0).rotate(Math.PI*.25, 0, 0, 1))
				.setTransformations(TransformType.GUI, new Matrix4().translate(-.1875,.3125,0).scale(.4375,.4375,.4375).rotate(-Math.PI*.6875, 0, 1, 0).rotate(-Math.PI*.125, 0, 0, 1))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(0,.25,.125).scale(.25,.25,.25)));


		ImmersiveModelRegistry.instance.registerCustomItemModel(new ItemStack(IEContent.itemRailgun, 1, 0), new ImmersiveModelRegistry.ItemModelReplacement_OBJ("immersiveengineering:models/item/railgun.obj")
				.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().scale(.125,.125,.125).translate(-.5, 1.5, .5).rotate(Math.PI*.46875, 0, 1, 0))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().scale(.125,.125,.125).translate(-1.75, 1.625, .875).rotate(-Math.PI*.46875, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(0, .5625, -.09375).scale(.1875,.1875,.1875).rotate(Math.PI*.53125, 0, 1, 0).rotate(Math.PI*.34375, 0, 0, 1))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(-.1875, .5625, -.09375).scale(.1875,.1875,.1875).rotate(-Math.PI*.46875, 0, 1, 0).rotate(-Math.PI*.34375, 0, 0, 1))
				.setTransformations(TransformType.FIXED, new Matrix4().translate(.1875,.0625,.0625).scale(.125,.125,.125).rotate(-Math.PI*.25, 0, 0, 1))
				.setTransformations(TransformType.GUI, new Matrix4().translate(-.1875,0,0).scale(.1875,.1875,.1875).rotate(-Math.PI*.6875, 0, 1, 0).rotate(-Math.PI*.1875, 0, 0, 1))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(.125,.125,.0625).scale(.125,.125,.125)));


		RenderingRegistry.registerEntityRenderingHandler(EntityRevolvershot.class, new IRenderFactory<EntityRevolvershot>(){
			@Override
			public Render createRenderFor(RenderManager manager){
				return new EntityRenderRevolvershot(manager);
			}});
		RenderingRegistry.registerEntityRenderingHandler(EntitySkylineHook.class, new IRenderFactory<EntitySkylineHook>(){
			@Override
			public Render createRenderFor(RenderManager manager){
				return new EntityRenderNone(manager);
			}});
		RenderingRegistry.registerEntityRenderingHandler(EntityChemthrowerShot.class, new IRenderFactory<EntityChemthrowerShot>(){
			@Override
			public Render createRenderFor(RenderManager manager){
				return new EntityRenderChemthrowerShot(manager);
			}});
		RenderingRegistry.registerEntityRenderingHandler(EntityRailgunShot.class, new IRenderFactory<EntityRailgunShot>(){
			@Override
			public Render createRenderFor(RenderManager manager){
				return new EntityRenderRailgunShot(manager);
			}});
		RenderingRegistry.registerEntityRenderingHandler(EntityIEExplosive.class, new IRenderFactory<EntityIEExplosive>(){
			@Override
			public Render createRenderFor(RenderManager manager){
				return new EntityRenderIEExplosive(manager);
			}});
		RenderingRegistry.registerEntityRenderingHandler(EntityFluorescentTube.class, new IRenderFactory<EntityFluorescentTube>(){
			@Override
			public Render createRenderFor(RenderManager manager){
				return new EntityRenderFluorescentTube(manager);
			}});
		ModelLoaderRegistry.registerLoader(new ConnLoader());
		ModelLoaderRegistry.registerLoader(new ModelConfigurableSides.Loader());
	}
	@Override
	public void preInitEnd()
	{
		//Going through registered stuff at the end of preInit, because of compat modules possibly adding items
		for(Block block : IEContent.registeredIEBlocks)
		{
			Item blockItem = Item.getItemFromBlock(block);
			final ResourceLocation loc = GameData.getBlockRegistry().getNameForObject(block);
			if(block instanceof IIEMetaBlock)
			{
				IIEMetaBlock ieMetaBlock = (IIEMetaBlock) block;
				if(ieMetaBlock.useCustomStateMapper())
					ModelLoader.setCustomStateMapper(block, IECustomStateMapper.getStateMapper(ieMetaBlock));
				ModelLoader.setCustomMeshDefinition(blockItem, new ItemMeshDefinition()
				{
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack)
					{
						return new ModelResourceLocation(loc, "inventory");
					}
				});
				for(int meta = 0; meta < ieMetaBlock.getMetaEnums().length; meta++)
				{
					String location = loc.toString();
					String prop = ieMetaBlock.appendPropertiesToState() ? ("inventory," + ieMetaBlock.getMetaProperty().getName() + "=" + ieMetaBlock.getMetaEnums()[meta].toString().toLowerCase(Locale.US)) : null;
					if(ieMetaBlock.useCustomStateMapper())
					{
						String custom = ieMetaBlock.getCustomStateMapping(meta, true);
						if(custom != null)
							location += "_" + custom;
					}
					try
					{
						ModelLoader.setCustomModelResourceLocation(blockItem, meta, new ModelResourceLocation(location, prop));
					} catch(NullPointerException npe)
					{
						throw new RuntimeException("WELP! apparently " + ieMetaBlock + " lacks an item!", npe);
					}
				}
			} else if(block instanceof BlockIEFluid)
				mapFluidState(block, ((BlockIEFluid) block).getFluid());
			else
				ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation(loc, "inventory"));
		}

		for(Item item : IEContent.registeredIEItems)
		{
			if(item instanceof ItemIEBase)
			{
				ItemIEBase ieMetaItem = (ItemIEBase) item;
				if(ieMetaItem.registerSubModels && ieMetaItem.getSubNames() != null && ieMetaItem.getSubNames().length > 0)
				{
					for(int meta = 0; meta < ieMetaItem.getSubNames().length; meta++)
					{
						ResourceLocation loc = new ResourceLocation("immersiveengineering", ieMetaItem.itemName + "/" + ieMetaItem.getSubNames()[meta]);
						ModelBakery.registerItemVariants(ieMetaItem, loc);
						ModelLoader.setCustomModelResourceLocation(ieMetaItem, meta, new ModelResourceLocation(loc, "inventory"));
					}
				} else
				{
					final ResourceLocation loc = new ResourceLocation("immersiveengineering", ieMetaItem.itemName);
					ModelBakery.registerItemVariants(ieMetaItem, loc);
					ModelLoader.setCustomMeshDefinition(ieMetaItem, new ItemMeshDefinition()
					{
						@Override
						public ModelResourceLocation getModelLocation(ItemStack stack)
						{
							return new ModelResourceLocation(loc, "inventory");
						}
					});
				}
			} else
			{
				final ResourceLocation loc = GameData.getItemRegistry().getNameForObject(item);
				ModelBakery.registerItemVariants(item, loc);
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition()
				{
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack)
					{
						return new ModelResourceLocation(loc, "inventory");
					}
				});
			}
		}


		for(IECompatModule compat : IECompatModule.modules)
			try{
				compat.clientPreInit();
			}catch (Exception exception){
				IELogger.error("Compat module for "+compat+" could not be client pre-initialized");
			}
	}

	@Override
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(ImmersiveModelRegistry.instance);
		ClientEventHandler handler = new ClientEventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		((IReloadableResourceManager)ClientUtils.mc().getResourceManager()).registerReloadListener(handler);

		//		revolverTextureMap = new TextureMap("textures/revolvers",true);
		//		revolverTextureMap.setMipmapLevels(Minecraft.getMinecraft().gameSettings.mipmapLevels);
		//		Minecraft.getMinecraft().renderEngine.loadTickableTexture(revolverTextureResource, revolverTextureMap);
		//		Minecraft.getMinecraft().renderEngine.bindTexture(revolverTextureResource);
		//		revolverTextureMap.setBlurMipmapDirect(false, Minecraft.getMinecraft().gameSettings.mipmapLevels > 0);
		//		ClientUtils.mc().renderEngine.loadTextureMap(revolverTextureResource, revolverTextureMap);

		nixieFontOptional = IEConfig.nixietubeFont?new IENixieFontRender():ClientUtils.font();
		nixieFont = new IENixieFontRender();
		itemFont = new IEItemFontRender();
		TileEntityTeslaCoil.effectMap = ArrayListMultimap.create();

		//		//METAL
		//		RenderingRegistry.registerBlockHandler(new BlockRenderMetalDevices());
		//		RenderingRegistry.registerBlockHandler(new BlockRenderMetalDevices2());
		//		RenderingRegistry.registerBlockHandler(new BlockRenderMetalDecoration());
		//		RenderingRegistry.registerBlockHandler(new BlockRenderMetalMultiblocks());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityImmersiveConnectable.class, new TileRenderImmersiveConnectable());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnectorLV.class, new TileRenderConnectorLV());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnectorMV.class, new TileRenderConnectorMV());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTransformer.class, new TileRenderTransformer());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRelayHV.class, new TileRenderRelayHV());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnectorHV.class, new TileRenderConnectorHV());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTransformerHV.class, new TileRenderTransformer());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnectorStructural.class, new TileRenderConnectorStructural());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLantern.class, new TileRenderLantern());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBreakerSwitch.class, new TileRenderBreakerSwitch());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergyMeter.class, new TileRenderEnergyMeter());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityElectricLantern.class, new TileRenderElectricLantern());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFloodlight.class, new TileRenderFloodlight());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidPipe.class, new TileRenderFluidPipe());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidPump.class, new TileRenderFluidPump());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRedstoneBreaker.class, new TileRenderRedstoneBreaker());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChargingStation.class, new TileRenderChargingStation());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySampleDrill.class, new TileRenderSampleDrill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTeslaCoil.class, new TileRenderTeslaCoil());
		// MULTIBLOCKS
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMetalPress.class, new TileRenderMetalPress());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrusher.class, new TileRenderCrusher());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySheetmetalTank.class, new TileRenderSheetmetalTank());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySilo.class, new TileRenderSilo());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySqueezer.class, new TileRenderSqueezer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDieselGenerator.class, new TileRenderDieselGenerator());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBucketWheel.class, new TileRenderBucketWheel());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityArcFurnace.class, new TileRenderArcFurnace());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAssembler.class, new TileRenderAssembler());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBottlingMachine.class, new TileRenderBottlingMachine());
		//WOOD
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWatermill.class, new TileRenderWatermill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindmill.class, new TileRenderWindmill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindmillAdvanced.class, new TileRenderWindmill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityModWorkbench.class, new TileRenderWorkbench());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWallmount.class, new TileRenderWallmount());
		//
		//		RenderingRegistry.registerBlockHandler(new BlockRenderWoodenDecoration());
		//		//STONE
		//		RenderingRegistry.registerBlockHandler(new BlockRenderStoneDevices());
		//		//CLOTH
		//		RenderingRegistry.registerBlockHandler(new BlockRenderClothDevices());
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBalloon.class, new TileRenderBalloon());
		//
		//		//REVOLVER
		//		
		//		MinecraftForgeClient.registerItemRenderer(IEContent.itemRevolver, new ItemRenderRevolver());
		//		//DRILL
		//		MinecraftForgeClient.registerItemRenderer(IEContent.itemDrill, new ItemRenderDrill());
		//		//ZIPLINE
		//		RenderingRegistry.registerEntityRenderingHandler(EntitySkycrate.class, new EntityRenderSkycrate());
		//		//CHEMTHROWER
		//		MinecraftForgeClient.registerItemRenderer(IEContent.itemChemthrower, new ItemRenderChemthrower());
		//		//VOLTMETER
		//		MinecraftForgeClient.registerItemRenderer(IEContent.itemTool, new ItemRenderVoltmeter());
		//		//RAILGUN
		//		MinecraftForgeClient.registerItemRenderer(IEContent.itemRailgun, new ItemRenderRailgun());
		//		/** TODO when there is an actual model for it =P
		//		MinecraftForgeClient.registerItemRenderer(IEContent.itemSkyhook, new ItemRenderSkyhook());
		//		 */

		//		int villagerId = Config.getInt("villager_engineer");
		//		VillagerRegistry.instance().registerVillagerSkin(villagerId, new ResourceLocation("immersiveengineering:textures/models/villager_engineer.png"));


		/**Colours*/
		for(Item item : IEContent.registeredIEItems)
			if(item instanceof IColouredItem && ((IColouredItem)item).hasCustomItemColours())
				ClientUtils.mc().getItemColors().registerItemColorHandler(IEDefaultColourHandlers.INSTANCE, item);
		for(Block block : IEContent.registeredIEBlocks)
			if(block instanceof IColouredBlock && ((IColouredBlock)block).hasCustomBlockColours())
				ClientUtils.mc().getBlockColors().registerBlockColorHandler(IEDefaultColourHandlers.INSTANCE, block);

		/**Render Layers*/
		Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
		RenderPlayer render = skinMap.get("default");
		render.addLayer(new IEBipedLayerRenderer());
		render = skinMap.get("slim");
		render.addLayer(new IEBipedLayerRenderer());
	}
	@Override
	public void initEnd()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try{
				compat.clientInit();
			}catch (Exception exception){
				IELogger.error("Compat module for "+compat+" could not be client pre-initialized");
			}
	}

	@Override
	public void postInit()
	{
		ManualHelper.ieManualInstance = new IEManualInstance();
		//		int subVersion = 0;
		//		while(!(ManualHelper.ieManualInstance.formatEntryName("updateNews_"+subVersion).equals("ie.manual.entry.updateNews_"+subVersion+".name")))
		//		{
		//			ArrayList<IManualPage> pages = new ArrayList<IManualPage>();
		//			int i=0;
		//			String key;
		//			while(!ManualHelper.ieManualInstance.formatText(key = "updateNews_"+subVersion+""+i).equals(key) && i<5)
		//			{
		//				pages.add(new ManualPages.Text(ManualHelper.getManual(), key));
		//				i++;
		//			}
		//			ManualHelper.addEntry("updateNews_"+subVersion, ManualHelper.CAT_UPDATE, pages.toArray(new IManualPage[pages.size()]));
		//			subVersion++;
		//		}
		List<ItemStack> tempItemList;
		List<PositionedItemStack[]> tempRecipeList;
		List<IManualPage> pages;

		addChangelogToManual();

		ManualHelper.addEntry("introduction", ManualHelper.CAT_GENERAL,
				new ManualPages.Text(ManualHelper.getManual(), "introduction0"),
				new ManualPages.Text(ManualHelper.getManual(), "introduction1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "introductionHammer", new ItemStack(IEContent.itemTool,1,0)));
		ManualHelper.addEntry("ores", ManualHelper.CAT_GENERAL,
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "oresCopper", new ItemStack(IEContent.blockOre,1,0),new ItemStack(IEContent.itemMetal,1,0)),
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "oresBauxite", new ItemStack(IEContent.blockOre,1,1),new ItemStack(IEContent.itemMetal,1,1)),
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "oresLead", new ItemStack(IEContent.blockOre,1,2),new ItemStack(IEContent.itemMetal,1,2)),
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "oresSilver", new ItemStack(IEContent.blockOre,1,3),new ItemStack(IEContent.itemMetal,1,3)),
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "oresNickel", new ItemStack(IEContent.blockOre,1,4),new ItemStack(IEContent.itemMetal,1,4)),
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "oresUranium", new ItemStack(IEContent.blockOre,1,5),new ItemStack(IEContent.itemMetal,1,5)));
		tempRecipeList = new ArrayList();
		if(!IERecipes.hammerCrushingList.isEmpty())
		{
			for(String ore : IERecipes.hammerCrushingList)
				tempRecipeList.add(new PositionedItemStack[]{ new PositionedItemStack(OreDictionary.getOres("ore"+ore), 24, 0), new PositionedItemStack(new ItemStack(IEContent.itemTool,1,0), 42, 0), new PositionedItemStack(IEApi.getPreferredOreStack("dust"+ore), 78, 0)});
			if(!tempRecipeList.isEmpty())
				ManualHelper.addEntry("oreProcessing", ManualHelper.CAT_GENERAL, new ManualPages.CraftingMulti(ManualHelper.getManual(), "oreProcessing0", (Object[])tempRecipeList.toArray(new PositionedItemStack[tempRecipeList.size()][3])));
		}
		ManualHelper.addEntry("alloys", ManualHelper.CAT_GENERAL, new ManualPages.CraftingMulti(ManualHelper.getManual(), "alloys0", (Object[])new PositionedItemStack[][]{
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("dustCopper"),24,0), new PositionedItemStack(OreDictionary.getOres("dustNickel"),42,0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,2,15),78,0)},
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("dustGold"),24,0), new PositionedItemStack(OreDictionary.getOres("dustSilver"),42,0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,2,16),78,0)}}));
		ManualHelper.addEntry("plates", ManualHelper.CAT_GENERAL, new ManualPages.CraftingMulti(ManualHelper.getManual(), "plates0",(Object[])new PositionedItemStack[][]{
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("ingotIron"),24,0), new PositionedItemStack(new ItemStack(IEContent.itemTool,1,0), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,1,39),78,0)},
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("ingotAluminum"),24,0), new PositionedItemStack(new ItemStack(IEContent.itemTool,1,0), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,1,31),78,0)},
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("ingotLead"),24,0), new PositionedItemStack(new ItemStack(IEContent.itemTool,1,0), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,1,32),78,0)},
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("ingotConstantan"),24,0), new PositionedItemStack(new ItemStack(IEContent.itemTool,1,0), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,1,36),78,0)},
				new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("ingotSteel"),24,0), new PositionedItemStack(new ItemStack(IEContent.itemTool,1,0), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,1,38),78,0)}}));
		ManualHelper.addEntry("hemp", ManualHelper.CAT_GENERAL,
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "hemp0", new ItemStack(IEContent.blockCrop,1,5),new ItemStack(IEContent.itemSeeds)),
				new ManualPages.Crafting(ManualHelper.getManual(), "hemp1", new ItemStack(IEContent.itemMaterial,1,5)),
				new ManualPages.Crafting(ManualHelper.getManual(), "hemp2", new ItemStack(IEContent.blockClothDevice,1,BlockTypes_ClothDevice.CUSHION.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "hemp3", new ItemStack(IEContent.blockClothDevice,1,BlockTypes_ClothDevice.STRIPCURTAIN.getMeta())));
		ManualHelper.addEntry("cokeoven", ManualHelper.CAT_GENERAL,
				new ManualPages.Text(ManualHelper.getManual(), "cokeoven0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "cokeovenBlock", new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.COKEBRICK.getMeta())),
				new ManualPageMultiblock(ManualHelper.getManual(), "", MultiblockCokeOven.instance));
		ManualHelper.addEntry("blastfurnace", ManualHelper.CAT_GENERAL,
				new ManualPages.Text(ManualHelper.getManual(), "blastfurnace0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "blastfurnaceBlock", new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.BLASTBRICK.getMeta())),
				new ManualPageMultiblock(ManualHelper.getManual(), "blastfurnace1", MultiblockBlastFurnace.instance));
		handleMineralManual();
		ManualHelper.addEntry("graphite", ManualHelper.CAT_GENERAL, new ManualPages.Text(ManualHelper.getManual(), "graphite0"),new ManualPages.Crafting(ManualHelper.getManual(), "graphite1", ItemEngineersBlueprint.getTypedBlueprint("electrode")));
		ManualHelper.addEntry("shader", ManualHelper.CAT_GENERAL, new ManualPages.Text(ManualHelper.getManual(), "shader0"), new ManualPages.Text(ManualHelper.getManual(), "shader1"), new ManualPages.ItemDisplay(ManualHelper.getManual(), "shader2"), new ManualPages.CraftingMulti(ManualHelper.getManual(), "shader2"));
		ShaderRegistry.manualEntry = ManualHelper.getManual().getEntry("shader");
		pages = new ArrayList<IManualPage>();
		for(ShaderRegistry.ShaderRegistryEntry entry : ShaderRegistry.shaderRegistry.values())
			pages.add(new ManualPageShader(ManualHelper.getManual(), entry));
		ManualHelper.addEntry("shaderList", ManualHelper.CAT_GENERAL, pages.toArray(new IManualPage[pages.size()]));

		ManualHelper.addEntry("treatedwood", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Crafting(ManualHelper.getManual(), "treatedwood0",  new ItemStack(IEContent.blockTreatedWood,1,0)),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockTreatedWood,1,1), new ItemStack(IEContent.blockTreatedWood,1,2), new ItemStack(IEContent.blockTreatedWoodSlabs,1,OreDictionary.WILDCARD_VALUE)),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockWoodenStair)),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.itemMaterial,1,0),new ItemStack(IEContent.blockWoodenDecoration,1,BlockTypes_WoodenDecoration.FENCE.getMeta()),new ItemStack(IEContent.blockWoodenDecoration,1,BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "treatedwoodPost0", new ItemStack(IEContent.blockWoodenDevice1,1,BlockTypes_WoodenDevice1.POST.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "treatedwoodPost1"));
		ItemStack[] storageBlocks = new ItemStack[BlockTypes_MetalsIE.values().length];
		ItemStack[] storageSlabs = new ItemStack[BlockTypes_MetalsIE.values().length];
		for(int i=0; i<BlockTypes_MetalsIE.values().length; i++)
		{
			storageBlocks[i] = new ItemStack(IEContent.blockStorage,1,i);
			storageSlabs[i] = new ItemStack(IEContent.blockStorageSlabs,1,i);
		}
		tempItemList = new ArrayList();
		for(int i=0; i<BlockTypes_MetalsAll.values().length; i++)
			if(!IEContent.blockSheetmetal.isMetaHidden(i))
				tempItemList.add(new ItemStack(IEContent.blockSheetmetal,1,i));
		ItemStack[] sheetmetal = tempItemList.toArray(new ItemStack[tempItemList.size()]);

		ManualHelper.addEntry("crate", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Crafting(ManualHelper.getManual(), "crate0", new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.CRATE.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "crate1", new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.REINFORCED_CRATE.getMeta())));
		ManualHelper.addEntry("barrel", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Crafting(ManualHelper.getManual(), "barrel0", new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.BARREL.getMeta())));
		ManualHelper.addEntry("concrete", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Crafting(ManualHelper.getManual(), "concrete0", new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.CONCRETE.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.CONCRETE_TILE.getMeta()),new ItemStack(IEContent.blockStoneStair_concrete0,1,0),new ItemStack(IEContent.blockStoneStair_concrete1,1,0)));
		ManualHelper.addEntry("balloon", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Crafting(ManualHelper.getManual(), "balloon0", new ItemStack(IEContent.blockClothDevice,1,BlockTypes_ClothDevice.BALLOON.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "balloon1"));
		ManualHelper.addEntry("metalconstruction", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Text(ManualHelper.getManual(), "metalconstruction0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "", storageBlocks,storageSlabs,sheetmetal),
				new ManualPages.Text(ManualHelper.getManual(), "metalconstruction1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta()),new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "",new ItemStack(IEContent.blockMetalDecoration2,1,BlockTypes_MetalDecoration2.STEEL_WALLMOUNT.getMeta()), new ItemStack(IEContent.blockMetalDecoration2,1,BlockTypes_MetalDecoration2.STEEL_POST.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta()),new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "",new ItemStack(IEContent.blockMetalDecoration2,1,BlockTypes_MetalDecoration2.ALUMINUM_WALLMOUNT.getMeta()), new ItemStack(IEContent.blockMetalDecoration2,1,BlockTypes_MetalDecoration2.ALUMINUM_POST.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "metalconstruction2", new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.CONNECTOR_STRUCTURAL.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.itemWireCoil,1,3),new ItemStack(IEContent.itemWireCoil,1,4)));
		ManualHelper.addEntry("multiblocks", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Text(ManualHelper.getManual(), "multiblocks0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()), new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()), new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.GENERATOR.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RADIATOR.getMeta())));
		ManualHelper.addEntry("metalbarrel", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Crafting(ManualHelper.getManual(), "metalbarrel0", new ItemStack(IEContent.blockMetalDevice0,1,BlockTypes_MetalDevice0.BARREL.getMeta())));
		ManualHelper.addEntry("workbench", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Crafting(ManualHelper.getManual(), "workbench0", new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.WORKBENCH.getMeta())));
		ManualHelper.addEntry("blueprints", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Text(ManualHelper.getManual(), "blueprints0"),new ManualPages.Text(ManualHelper.getManual(), "blueprints1"));
		ManualHelper.addEntry("lighting", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Crafting(ManualHelper.getManual(), "lighting0", new ItemStack(IEContent.blockMetalDecoration2,1,BlockTypes_MetalDecoration2.LANTERN.getMeta())),
				new ManualPages.Crafting(ManualHelper.getManual(), "lighting1", new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.ELECTRIC_LANTERN.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "lighting2"),
				new ManualPages.Crafting(ManualHelper.getManual(), "lighting3", new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLOODLIGHT.getMeta())));
		ManualHelper.addEntry("tank", ManualHelper.CAT_CONSTRUCTION,
				new ManualPageMultiblock(ManualHelper.getManual(), "tank0", MultiblockSheetmetalTank.instance),
				new ManualPages.Text(ManualHelper.getManual(), "tank1"));
		ManualHelper.addEntry("silo", ManualHelper.CAT_CONSTRUCTION,
				new ManualPageMultiblock(ManualHelper.getManual(), "silo0", MultiblockSilo.instance),
				new ManualPages.Text(ManualHelper.getManual(), "silo1"),
				new ManualPages.Text(ManualHelper.getManual(), "silo2"));

		Object[] wires = {new ItemStack(IEContent.itemMaterial, 1, 20), new ItemStack(IEContent.itemWireCoil, 1, 0), new ItemStack(IEContent.itemMaterial, 1, 21), new ItemStack(IEContent.itemWireCoil, 1, 1), new ItemStack(IEContent.itemMaterial, 1, 22), new ItemStack(IEContent.itemMaterial, 1, 23), new ItemStack(IEContent.itemWireCoil, 1, 2)};
		ManualHelper.addEntry("wiring", ManualHelper.CAT_ENERGY,
				new ManualPages.Text(ManualHelper.getManual(), "wiring0"),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "wiring1", wires),
				new ManualPages.Image(ManualHelper.getManual(), "wiring2", "immersiveengineering:textures/misc/wiring.png;0;0;110;40", "immersiveengineering:textures/misc/wiring.png;0;40;110;30"),
				new ManualPages.Image(ManualHelper.getManual(), "wiring3", "immersiveengineering:textures/misc/wiring.png;0;70;110;60", "immersiveengineering:textures/misc/wiring.png;0;130;110;60"),
				new ManualPages.Text(ManualHelper.getManual(), "wiring4"),
				new ManualPages.Text(ManualHelper.getManual(), "wiring5"),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "wiringConnector",
						new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.CONNECTOR_LV.getMeta()),new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.RELAY_LV.getMeta()),
						new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.CONNECTOR_MV.getMeta()),new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.RELAY_HV.getMeta()),
						new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.CONNECTOR_HV.getMeta()),new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.RELAY_HV.getMeta())),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "wiringCapacitor", new ItemStack(IEContent.blockMetalDevice0,1,BlockTypes_MetalDevice0.CAPACITOR_LV.getMeta()),new ItemStack(IEContent.blockMetalDevice0,1,BlockTypes_MetalDevice0.CAPACITOR_MV.getMeta()),new ItemStack(IEContent.blockMetalDevice0,1,BlockTypes_MetalDevice0.CAPACITOR_HV.getMeta())),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "wiringTransformer0", new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.TRANSFORMER.getMeta()),new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.TRANSFORMER_HV.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "wiringTransformer1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "wiringCutters", new ItemStack(IEContent.itemTool,1,1)),
				new ManualPages.Crafting(ManualHelper.getManual(), "wiringVoltmeter", new ItemStack(IEContent.itemTool,1,2)));
		ManualHelper.getManual().addEntry("generator", ManualHelper.CAT_ENERGY,
				new ManualPages.Crafting(ManualHelper.getManual(), "generator0", new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.DYNAMO.getMeta())),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "generatorWindmill", new ItemStack(IEContent.blockWoodenDevice1,1,BlockTypes_WoodenDevice1.WINDMILL.getMeta()),new ItemStack(IEContent.itemMaterial,1,2)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "generatorWatermill", new ItemStack(IEContent.blockWoodenDevice1,1,BlockTypes_WoodenDevice1.WATERMILL.getMeta()),new ItemStack(IEContent.itemMaterial,1,1)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "generatorWindmillImproved", new ItemStack(IEContent.blockWoodenDevice1,1,BlockTypes_WoodenDevice1.WINDMILL_ADVANCED.getMeta()),new ItemStack(IEContent.itemMaterial,1,5)));
		ManualHelper.getManual().addEntry("breaker", ManualHelper.CAT_ENERGY, new ManualPages.Crafting(ManualHelper.getManual(), "breaker0", new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.BREAKERSWITCH.getMeta()))   ,new ManualPages.Text(ManualHelper.getManual(), "breaker1"), new ManualPages.Crafting(ManualHelper.getManual(), "breaker2", new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.REDSTONE_BREAKER.getMeta())));
		ManualHelper.getManual().addEntry("eMeter", ManualHelper.CAT_ENERGY, new ManualPages.Crafting(ManualHelper.getManual(), "eMeter0", new ItemStack(IEContent.blockConnectors,1,BlockTypes_Connector.ENERGY_METER.getMeta())));
		Map<String,Integer> sortedMap = ThermoelectricHandler.getThermalValuesSorted(true);
		String[][] table = formatToTable_ItemIntHashmap(sortedMap,"K");
		ManualHelper.getManual().addEntry("redstoneWires", ManualHelper.CAT_ENERGY, new ManualPages.Crafting(ManualHelper.getManual(), "redstoneWires0", new ItemStack(IEContent.itemWireCoil, 1, 5)), new ManualPages.Crafting(ManualHelper.getManual(), "redstoneWires1", new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.CONNECTOR_REDSTONE.getMeta())));
		ManualHelper.getManual().addEntry("thermoElectric", ManualHelper.CAT_ENERGY,
				new ManualPages.Crafting(ManualHelper.getManual(), "thermoElectric0", new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.THERMOELECTRIC_GEN.getMeta())),
				new ManualPages.Table(ManualHelper.getManual(), "thermoElectric1", table, false));
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
		//			String sf = f!=null?new FluidStack(f,1000).getUnlocalizedName():"";
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

		ManualHelper.addEntry("conveyor", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "conveyor0", ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID + ":conveyor")),
				new ManualPages.Text(ManualHelper.getManual(), "conveyor1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "conveyor2", ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID + ":dropper")),
				new ManualPages.Crafting(ManualHelper.getManual(), "conveyor3", ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID + ":vertical")),
				new ManualPages.Crafting(ManualHelper.getManual(), "conveyor4", ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID + ":splitter")));
		ManualHelper.addEntry("furnaceHeater", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "furnaceHeater0", new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FURNACE_HEATER.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "furnaceHeater1"),
				new ManualPages.Text(ManualHelper.getManual(), "furnaceHeater2"));
		ManualHelper.addEntry("sorter", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "sorter0", new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.SORTER.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "sorter1"));
		pages = new ArrayList<IManualPage>();
		pages.add(new ManualPages.Crafting(ManualHelper.getManual(), "fluidPipes0", new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta())));
		pages.add(new ManualPages.Text(ManualHelper.getManual(), "fluidPipes1"));
		pages.add(new ManualPages.Crafting(ManualHelper.getManual(), "fluidPipes2", new ItemStack(IEContent.blockMetalDevice0,1,BlockTypes_MetalDevice0.FLUID_PUMP.getMeta())));
		pages.add(new ManualPages.Text(ManualHelper.getManual(), "fluidPipes3"));
		if(IEConfig.Machines.pump_infiniteWater || IEConfig.Machines.pump_placeCobble)
			pages.add(new ManualPages.Text(ManualHelper.getManual(), "fluidPipes4"));
		ManualHelper.addEntry("fluidPipes", ManualHelper.CAT_MACHINES,pages.toArray(new IManualPage[pages.size()]));
		ManualHelper.addEntry("chargingStation", ManualHelper.CAT_MACHINES, new ManualPages.Crafting(ManualHelper.getManual(), "chargingStation0", new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.CHARGING_STATION.getMeta())),new ManualPages.Text(ManualHelper.getManual(), "chargingStation1"));
		ManualHelper.addEntry("teslaCoil", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "teslaCoil0", new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.TESLA_COIL.getMeta())),
				new ManualPages.Text(ManualHelper.getManual(), "teslaCoil1"),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "teslaCoil2", new ItemStack(IEContent.itemsFaradaySuit[0]), new ItemStack(IEContent.itemsFaradaySuit[1]), new ItemStack(IEContent.itemsFaradaySuit[2]), new ItemStack(IEContent.itemsFaradaySuit[3]), new ItemStack(IEContent.itemFluorescentTube)),
				new ManualPages.Text(ManualHelper.getManual(), "teslaCoil3"),
				new ManualPages.Text(ManualHelper.getManual(), "teslaCoil4"));
		ManualHelper.addEntry("jerrycan", ManualHelper.CAT_MACHINES, new ManualPages.Crafting(ManualHelper.getManual(), "jerrycan0", new ItemStack(IEContent.itemJerrycan)));
		tempItemList = new ArrayList<>();
		for(int i=0; i<16; i++)
			tempItemList.add(ItemNBTHelper.stackWithData(new ItemStack(IEContent.itemEarmuffs), "IE:EarmuffColour",EnumDyeColor.byDyeDamage(i).getMapColor().colorValue));
		ManualHelper.addEntry("earmuffs", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "earmuffs0", new ItemStack(IEContent.itemEarmuffs)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "earmuffs1",(Object[])new PositionedItemStack[][]{
						new PositionedItemStack[]{new PositionedItemStack(new ItemStack(IEContent.itemEarmuffs),24,0), new PositionedItemStack(new ItemStack(Items.DYE,1,OreDictionary.WILDCARD_VALUE), 42, 0), new PositionedItemStack(tempItemList,78,0)},
						new PositionedItemStack[]{new PositionedItemStack(new ItemStack(IEContent.itemEarmuffs),24,0), new PositionedItemStack(Lists.newArrayList(new ItemStack(Items.LEATHER_HELMET),new ItemStack(Items.IRON_HELMET)), 42, 0), new PositionedItemStack(Lists.newArrayList(ItemNBTHelper.stackWithData(new ItemStack(Items.LEATHER_HELMET),"IE:Earmuffs",true),ItemNBTHelper.stackWithData(new ItemStack(Items.IRON_HELMET),"IE:Earmuffs",true)),78,0)}}));
		ManualHelper.addEntry("toolbox", ManualHelper.CAT_MACHINES, new ManualPages.Crafting(ManualHelper.getManual(), "toolbox0", new ItemStack(IEContent.itemToolbox)));
		ManualHelper.addEntry("drill", ManualHelper.CAT_MACHINES,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "drill0", new ItemStack(IEContent.itemDrill,1,0), new ItemStack(IEContent.itemMaterial,1,9)),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill1", new ItemStack(IEContent.itemDrillhead,1,0)),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill2", new ItemStack(IEContent.itemToolUpgrades,1,0)),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill3", new ItemStack(IEContent.itemToolUpgrades,1,1)),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill4", new ItemStack(IEContent.itemToolUpgrades,1,2)),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill5", new ItemStack(IEContent.itemToolUpgrades,1,3)));
		ManualHelper.addEntry("revolver", ManualHelper.CAT_MACHINES,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "revolver0", new ItemStack(IEContent.itemRevolver,1,0), new ItemStack(IEContent.itemMaterial,1,7),new ItemStack(IEContent.itemMaterial,1,8),new ItemStack(IEContent.itemMaterial,1,9),new ItemStack(IEContent.itemMaterial,1,10)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "revolver1", new ItemStack(IEContent.itemRevolver,1,1)),
				new ManualPages.Crafting(ManualHelper.getManual(), "revolver2", new ItemStack(IEContent.itemToolUpgrades,1,4)),
				new ManualPages.Crafting(ManualHelper.getManual(), "revolver3", new ItemStack(IEContent.itemToolUpgrades,1,5)),
				new ManualPages.Crafting(ManualHelper.getManual(), "revolver4", new ItemStack(IEContent.itemToolUpgrades,1,6)));
		pages = new ArrayList<IManualPage>();
		pages.add(new ManualPages.Crafting(ManualHelper.getManual(), "bullets0", ItemEngineersBlueprint.getTypedBlueprint("bullet")));
		pages.add(new ManualPages.CraftingMulti(ManualHelper.getManual(), "bullets1", new ItemStack(IEContent.itemBullet,1,0),new ItemStack(IEContent.itemBullet,1,1), new ItemStack(IEContent.itemMold,1,3)));
		for(String key : BulletHandler.registry.keySet())
			if(BulletHandler.registry.get(key).isProperCartridge())
				pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), "bullets_" + key, BulletHandler.getBulletStack(key)));
//		pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), "bullets2", ItemBullet.getBulletStack("casull")));
//		pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), "bullets3", ItemBullet.getBulletStack("armorPiercing")));
//		pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), "bullets4", ItemBullet.getBulletStack("buckshot")));
//		pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), "bullets5", ItemBullet.getBulletStack("HE")));
//		pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), "bullets6", ItemBullet.getBulletStack("silver")));
//		pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), "bullets7", ItemBullet.getBulletStack("dragonsbreath")));
//		pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), "bullets8", ItemBullet.getBulletStack("potion")));
//		pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), "bullets8", ItemBullet.getBulletStack("potion")));
//		ItemBullet.getBulletStack("flare")));
//		ItemBullet.getBulletStack("homing")));
//		ItemBullet.getBulletStack("wolfpack")));
//		if(Config.getBoolean("botaniaBullets"))
//		{
//			pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), "bulletsBotania0", new ItemStack(IEContent.itemBullet,1,7)));
//			pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), "bulletsBotania1", new ItemStack(IEContent.itemBullet,1,8)));
//		}
		ManualHelper.addEntry("bullets", ManualHelper.CAT_MACHINES, pages.toArray(new IManualPage[pages.size()]));
		ManualHelper.addEntry("skyhook", ManualHelper.CAT_MACHINES,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "skyhook0", new ItemStack(IEContent.itemSkyhook), new ItemStack(IEContent.itemMaterial, 1, 9)),
				new ManualPages.Text(ManualHelper.getManual(), "skyhook1"));
		ManualHelper.addEntry("chemthrower", ManualHelper.CAT_MACHINES,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "chemthrower0", new ItemStack(IEContent.itemChemthrower,1,0), new ItemStack(IEContent.itemMaterial,1,9), new ItemStack(IEContent.itemToolUpgrades,1,0)),
				new ManualPages.Crafting(ManualHelper.getManual(), "chemthrower1", new ItemStack(IEContent.itemToolUpgrades,1,3)),
				new ManualPages.Crafting(ManualHelper.getManual(), "chemthrower2", new ItemStack(IEContent.itemToolUpgrades,1,7)));
		ManualHelper.addEntry("railgun", ManualHelper.CAT_MACHINES,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "railgun0", new ItemStack(IEContent.itemRailgun,1,0), new ItemStack(IEContent.itemMaterial,1,9)),
				new ManualPages.Text(ManualHelper.getManual(), "railgun1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "railgun2", new ItemStack(IEContent.itemToolUpgrades,1,9)),
				new ManualPages.Crafting(ManualHelper.getManual(), "railgun3", new ItemStack(IEContent.itemToolUpgrades,1,8)));

		ManualHelper.addEntry("improvedBlastfurnace", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "improvedBlastfurnace0", new ItemStack(IEContent.blockStoneDecoration,1,BlockTypes_StoneDecoration.BLASTBRICK_REINFORCED.getMeta())),
				new ManualPageMultiblock(ManualHelper.getManual(), "improvedBlastfurnace1", MultiblockBlastFurnaceAdvanced.instance),
				new ManualPages.Text(ManualHelper.getManual(), "improvedBlastfurnace2"),
				new ManualPages.Crafting(ManualHelper.getManual(), "improvedBlastfurnace3", new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.BLAST_FURNACE_PREHEATER.getMeta())));
		tempItemList = new ArrayList();
		IEContent.itemMold.getSubItems(IEContent.itemMold,ImmersiveEngineering.creativeTab, tempItemList);
		ManualHelper.addEntry("metalPress", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "metalPress0", MultiblockMetalPress.instance),
				new ManualPages.Text(ManualHelper.getManual(), "metalPress1"),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "metalPress2", (Object[])tempItemList.toArray(new ItemStack[tempItemList.size()])));
		ManualHelper.addEntry("assembler", ManualHelper.CAT_MACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "assembler0", MultiblockAssembler.instance),
				new ManualPages.Text(ManualHelper.getManual(), "assembler1"),
				new ManualPages.Text(ManualHelper.getManual(), "assembler2"));
		ManualHelper.addEntry("crusher", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "crusher0", MultiblockCrusher.instance),
				new ManualPages.Text(ManualHelper.getManual(), "crusher1"));
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
		String[][] table2 = formatToTable_ItemIntHashmap(sortedMap,"mB");
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


		ClientCommandHandler.instance.registerCommand(new CommandHandler(true));
	}
	@Override
	public void postInitEnd()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try{
				compat.clientPostInit();
			}catch (Exception exception){
				IELogger.error("Compat module for "+compat+" could not be client pre-initialized");
			}
	}
	static ManualEntry mineralEntry;
	public static void handleMineralManual()
	{
		if(ManualHelper.getManual()!=null)
		{
			ArrayList<IManualPage> pages = new ArrayList();
			pages.add(new ManualPages.Text(ManualHelper.getManual(), "minerals0"));
			pages.add(new ManualPages.Crafting(ManualHelper.getManual(), "minerals1", new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta())));
			pages.add(new ManualPages.Text(ManualHelper.getManual(), "minerals2"));

			final ExcavatorHandler.MineralMix[] minerals = ExcavatorHandler.mineralList.keySet().toArray(new ExcavatorHandler.MineralMix[0]);

			ArrayList<Integer> mineralIndices = new ArrayList();
			for(int i=0; i<minerals.length; i++)
				if(minerals[i].isValid())
					mineralIndices.add(i);
			Collections.sort(mineralIndices, new Comparator<Integer>(){
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
				if(minerals[i].dimensionWhitelist!=null && minerals[i].dimensionWhitelist.length>0)
				{
					String validDims = "";
					for(int dim : minerals[i].dimensionWhitelist)
						validDims += (!validDims.isEmpty()?", ":"")+"<dim;"+dim+">";
					s0 = I18n.format("ie.manual.entry.mineralsDimValid",localizedName,validDims);
				}
				else if(minerals[i].dimensionBlacklist!=null && minerals[i].dimensionBlacklist.length>0)
				{
					String invalidDims = "";
					for(int dim : minerals[i].dimensionBlacklist)
						invalidDims += (!invalidDims.isEmpty()?", ":"")+"<dim;"+dim+">";
					s0 = I18n.format("ie.manual.entry.mineralsDimInvalid",localizedName,invalidDims);
				}
				else
					s0 = I18n.format("ie.manual.entry.mineralsDimAny",localizedName);

				ArrayList<Integer> formattedOutputs = new ArrayList<Integer>();
				for(int j=0; j<minerals[i].oreOutput.length; j++)
					formattedOutputs.add(j);
				final int fi = i;
				Collections.sort(formattedOutputs, new Comparator<Integer>(){
					@Override
					public int compare(Integer paramT1, Integer paramT2)
					{
						return -Double.compare( minerals[fi].recalculatedChances[paramT1],  minerals[fi].recalculatedChances[paramT2]);
					}
				});

				String s1 = "";
				ItemStack[] sortedOres = new ItemStack[minerals[i].oreOutput.length];
				for(int j=0; j<formattedOutputs.size(); j++)
					if(minerals[i].oreOutput[j]!=null)
					{
						int sorted = formattedOutputs.get(j);
						s1 += "<br>" + new DecimalFormat("00.00").format(minerals[i].recalculatedChances[sorted]*100).replaceAll("\\G0"," ")+"% "+minerals[i].oreOutput[sorted].getDisplayName();
						sortedOres[j] = minerals[i].oreOutput[sorted];
					}
				String s2 = I18n.format("ie.manual.entry.minerals3", s0,s1);
				pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), s2, sortedOres));
			}

//			String[][][] multiTables = formatToTable_ExcavatorMinerals();
//			for(String[][] minTable : multiTables)
//				pages.add(new ManualPages.Table(ManualHelper.getManual(), "", minTable,true));
			if(mineralEntry!=null)
				mineralEntry.setPages(pages.toArray(new IManualPage[pages.size()]));
			else
			{
				ManualHelper.addEntry("minerals", ManualHelper.CAT_GENERAL, pages.toArray(new IManualPage[pages.size()]));
				mineralEntry = ManualHelper.getManual().getEntry("minerals");
			}
		}
	}
	static String[][][] formatToTable_ExcavatorMinerals()
	{
		ExcavatorHandler.MineralMix[] minerals = ExcavatorHandler.mineralList.keySet().toArray(new ExcavatorHandler.MineralMix[0]);
		String[][][] multiTables = new String[1][minerals.length][2];
		int curTable = 0;
		int totalLines = 0;
		for(int i=0; i<minerals.length; i++)
			if(minerals[i].isValid())
			{
				String name = Lib.DESC_INFO+"mineral."+minerals[i].name;
				if(I18n.format(name)==name)
					name = minerals[i].name;
				multiTables[curTable][i][0] = name;
				multiTables[curTable][i][1] = "";
				for(int j=0; j<minerals[i].oreOutput.length; j++)
					if(minerals[i].oreOutput[j]!=null)
					{
						multiTables[curTable][i][1] += minerals[i].oreOutput[j].getDisplayName()+" "+( new DecimalFormat("#.00").format(minerals[i].recalculatedChances[j]*100)+"%" )+(j<minerals[i].oreOutput.length-1?"\n":"");
						totalLines++;
					}
				if(i<minerals.length-1 && totalLines+minerals[i+1].oreOutput.length>=13)
				{
					String[][][] newMultiTables = new String[multiTables.length+1][minerals.length][2];
					System.arraycopy(multiTables,0, newMultiTables,0, multiTables.length);
					multiTables = newMultiTables;
					totalLines = 0;
					curTable++;
				}
			}
		return multiTables;
	}

	public void addChangelogToManual()
	{
		FontRenderer fr = ManualHelper.getManual().fontRenderer;
		boolean isUnicode = fr.getUnicodeFlag();
		fr.setUnicodeFlag(true);
		try
		{
			URL url = new URL("https://raw.githubusercontent.com/BluSunrize/ImmersiveEngineering/master/changelog.md");
			Scanner s = new Scanner(url.openStream());
			//sorted map to keep the chronological order
			Map<String, String> entries = new LinkedHashMap<>();
			String readVersion = null;
			String readLog = "";
			String currVersion = ImmersiveEngineering.VERSION;
			boolean readVersionBuilt = false;
			while(s.hasNextLine())
			{
				String line = s.nextLine();
				if(line.startsWith("#####"))
				{
					//add read log to map
					addToMap(readVersion, currVersion, readLog, readVersionBuilt, entries);
					//parse new version
					readVersion = "";
					readLog = "";
					for(int i = line.indexOf(' ') + 1; i < line.length() && line.charAt(i) != ' '; i++)
						readVersion += line.charAt(i);
					readVersionBuilt = line.endsWith("BUILT");
				} else
				{
					readLog += line + "\n";
				}
			}
			s.close();
			addToMap(readVersion, currVersion, readLog, readVersionBuilt, entries);
			//add to manual
			for(Entry<String, String> e : entries.entrySet())
			{
				List<String> l = ManualHelper.getManual().fontRenderer.listFormattedStringToWidth(e.getValue().replace("\t", "  "), 120);
				final int LINES_PER_PAGE = 16;
				int pageCount = l.size() / LINES_PER_PAGE + (l.size() % LINES_PER_PAGE == 0 ? 0 : 1);
				ManualPages.Text[] pages = new ManualPages.Text[pageCount];
				for(int i = 0; i < pageCount; i++)
				{
					String nextPage = "";
					for(int j = LINES_PER_PAGE * i; j < l.size() && j < (i + 1) * LINES_PER_PAGE; j++)
						nextPage += l.get(j) + "\n";
					pages[i] = new ManualPages.Text(ManualHelper.getManual(), nextPage);
				}
				ManualHelper.addEntry(e.getKey(), ManualHelper.CAT_UPDATE, pages);
			}
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		fr.setUnicodeFlag(isUnicode);
	}

	private int compareVersions(String checked, String current)
	{
		String num1 = checked == null ? "0" : checked.replaceAll("[^0-9]", "");
		String num2 = current == null ? "0" : current.replaceAll("[^0-9]", "");
		int n1 = num1.isEmpty() ? 0 : Integer.parseInt(num1);
		int n2 = num2.isEmpty() ? 0 : Integer.parseInt(num2);
		return Integer.compare(n1, n2);
	}

	private void addToMap(String readVersion, String currVersion, String readLog, boolean readVersionBuilt, Map<String, String> entries)
	{
		if(readVersion != null)
		{
			int compare = compareVersions(readVersion, currVersion);
			if(readVersionBuilt || compare >= 0)
			{
				if(!readVersionBuilt)
					readVersion += " - UNRELEASED";
				else if(compare > 0)
					readVersion += " - NEW";
				else if(compare == 0)
					readVersion += " - CURRENT";
				entries.put(readVersion, readLog);
			}
		}
	}



	@Override
	public void serverStarting()
	{
	}

	@SubscribeEvent
	public void textureStich(TextureStitchEvent.Pre event)
	{
		IELogger.info("Stitching Revolver Textures!");
		((ItemRevolver)IEContent.itemRevolver).stichRevolverTextures(event.getMap());
		for(ShaderRegistry.ShaderRegistryEntry entry : ShaderRegistry.shaderRegistry.values())
			for(ShaderCase sCase : entry.getCases())
				if(sCase.stitchIntoSheet())
					for(ShaderLayer layer : sCase.getLayers())
						if(layer.getTexture()!=null)
							ApiUtils.getRegisterSprite(event.getMap(), layer.getTexture());

		for(DrillHeadPerm p : ((ItemDrillhead)IEContent.itemDrillhead).perms)
			p.sprite = ApiUtils.getRegisterSprite(event.getMap(), p.texture);
		WireType.iconDefaultWire = ApiUtils.getRegisterSprite(event.getMap(), "immersiveengineering:blocks/wire");

		for(BulletHandler.IBullet bullet : BulletHandler.registry.values())
			for(ResourceLocation rl : bullet.getTextures())
				ApiUtils.getRegisterSprite(event.getMap(), rl);

		for(ResourceLocation rl : ModelConveyor.rl_casing)
			ApiUtils.getRegisterSprite(event.getMap(), rl);
		ApiUtils.getRegisterSprite(event.getMap(), ConveyorHandler.textureConveyorColour);
		ApiUtils.getRegisterSprite(event.getMap(), ConveyorBasic.texture_off);
		ApiUtils.getRegisterSprite(event.getMap(), ConveyorBasic.texture_on);
		ApiUtils.getRegisterSprite(event.getMap(), ConveyorDrop.texture_off);
		ApiUtils.getRegisterSprite(event.getMap(), ConveyorDrop.texture_on);
		ApiUtils.getRegisterSprite(event.getMap(), ConveyorVertical.texture_off);
		ApiUtils.getRegisterSprite(event.getMap(), ConveyorVertical.texture_on);
		ApiUtils.getRegisterSprite(event.getMap(), ConveyorSplit.texture_off);
		ApiUtils.getRegisterSprite(event.getMap(), ConveyorSplit.texture_on);
		ApiUtils.getRegisterSprite(event.getMap(), ConveyorSplit.texture_casing);

		ApiUtils.getRegisterSprite(event.getMap(), IEContent.fluidCreosote.getStill());
		ApiUtils.getRegisterSprite(event.getMap(), IEContent.fluidCreosote.getFlowing());
		ApiUtils.getRegisterSprite(event.getMap(), IEContent.fluidPlantoil.getStill());
		ApiUtils.getRegisterSprite(event.getMap(), IEContent.fluidPlantoil.getFlowing());
		ApiUtils.getRegisterSprite(event.getMap(), IEContent.fluidEthanol.getStill());
		ApiUtils.getRegisterSprite(event.getMap(), IEContent.fluidEthanol.getFlowing());
		ApiUtils.getRegisterSprite(event.getMap(), IEContent.fluidBiodiesel.getStill());
		ApiUtils.getRegisterSprite(event.getMap(), IEContent.fluidBiodiesel.getFlowing());
		ApiUtils.getRegisterSprite(event.getMap(), "immersiveengineering:items/shader_slot");
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if(ID>=Lib.GUIID_Base_Item)
		{
			EntityEquipmentSlot slot = EntityEquipmentSlot.values()[ID/100];
			ID %= 100;//Slot determined, get actual ID
			ItemStack item = player.getItemStackFromSlot(slot);
			if(item!=null && item.getItem() instanceof IGuiItem && ((IGuiItem)item.getItem()).getGuiID(item)==ID)
			{
				if(ID==Lib.GUIID_Manual && ManualHelper.getManual()!=null && OreDictionary.itemMatches(new ItemStack(IEContent.itemTool,1,3), item, false))
					return ManualHelper.getManual().getGui();
				if(ID==Lib.GUIID_Revolver && item.getItem() instanceof ItemRevolver)
					return new GuiRevolver(player.inventory, world, slot, item);
				if(ID==Lib.GUIID_Toolbox && item.getItem() instanceof ItemToolbox)
					return new GuiToolbox(player.inventory, world, slot, item);
			}
		}

		if(ID>=Lib.GUIID_Base_Item)
		{
			ItemStack item = null;
			for (EnumHand hand : EnumHand.values())
			{
				ItemStack held = player.getHeldItem(hand);
				if(held!=null && held.getItem() instanceof IGuiItem && ((IGuiItem)held.getItem()).getGuiID(held)==ID)
					item = held;
			}
			if(item!=null)
			{

			}
		}
		else
		{
			TileEntity te = world.getTileEntity(new BlockPos(x,y,z));
			if(te instanceof IGuiTile)
			{
				Object gui = null;
				if(ID==Lib.GUIID_CokeOven && te instanceof TileEntityCokeOven)
					gui = new GuiCokeOven(player.inventory, (TileEntityCokeOven) te);
				if(ID==Lib.GUIID_BlastFurnace && te instanceof TileEntityBlastFurnace)
					gui = new GuiBlastFurnace(player.inventory, (TileEntityBlastFurnace) te);
				if(ID==Lib.GUIID_WoodenCrate && te instanceof TileEntityWoodenCrate)
					gui = new GuiCrate(player.inventory, (TileEntityWoodenCrate) te);
				if(ID==Lib.GUIID_Workbench && te instanceof TileEntityModWorkbench)
					gui = new GuiModWorkbench(player.inventory, (TileEntityModWorkbench) te);
				if(ID==Lib.GUIID_Sorter && te instanceof TileEntitySorter)
					gui = new GuiSorter(player.inventory, (TileEntitySorter) te);
				if(ID==Lib.GUIID_Squeezer && te instanceof TileEntitySqueezer)
					gui = new GuiSqueezer(player.inventory, (TileEntitySqueezer) te);
				if(ID==Lib.GUIID_Fermenter && te instanceof TileEntityFermenter)
					gui = new GuiFermenter(player.inventory, (TileEntityFermenter) te);
				if(ID==Lib.GUIID_Refinery && te instanceof TileEntityRefinery)
					gui = new GuiRefinery(player.inventory, (TileEntityRefinery) te);
				if(ID==Lib.GUIID_ArcFurnace && te instanceof TileEntityArcFurnace)
					gui = new GuiArcFurnace(player.inventory, (TileEntityArcFurnace) te);
				if(ID==Lib.GUIID_Assembler && te instanceof TileEntityAssembler)
					gui = new GuiAssembler(player.inventory, (TileEntityAssembler) te);
				if(gui!=null)
					((IGuiTile)te).onGuiOpened(player, true);
				return gui;
			}
		}
		return null;
	}

	public void registerItemModel(Item item, int meta, String path, String renderCase)
	{
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, new ModelResourceLocation(path, renderCase));
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
		if(stringbuilder.length() == 0)
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
		if(sound==null && tileActive)
		{
			sound = ClientUtils.generatePositionedIESound(soundEvent, volume, pitch, true, 0, pos);
			tileSoundMap.put(pos, sound);
		}
		else if(sound!=null && !tileActive)
		{
			sound.donePlaying=true;
			ClientUtils.mc().getSoundHandler().stopSound(sound);
			tileSoundMap.remove(pos);
		}
		//		IESound sound = soundMap.get(soundName);
		//		if(tileActive)
		//		if(sound!=null)
		//		{
		//			if(new BlockPos(sound.getXPosF(), sound.getYPosF(), sound.getZPosF()).equals(tile.getPos()))
		//			{
		//				if(!tileActive)
		//				{
		////					sound.donePlaying = true;
		////					sound.volume = 0;
		////					ClientUtils.mc().getSoundHandler().stopSound(sound);
		////					soundMap.remove(soundName);
		//				}
		//			}
		//			else if(tileActive)
		//			{
		//				double dx = (sound.getXPosF()-ClientUtils.mc().getRenderViewEntity().posX)*(sound.getXPosF()-ClientUtils.mc().getRenderViewEntity().posX);
		//				double dy = (sound.getYPosF()-ClientUtils.mc().getRenderViewEntity().posY)*(sound.getYPosF()-ClientUtils.mc().getRenderViewEntity().posY);
		//				double dz = (sound.getZPosF()-ClientUtils.mc().getRenderViewEntity().posZ)*(sound.getZPosF()-ClientUtils.mc().getRenderViewEntity().posZ);
		//				double dx1 = (tile.getPos().getX()-ClientUtils.mc().getRenderViewEntity().posX)*(tile.getPos().getX()-ClientUtils.mc().getRenderViewEntity().posX);
		//				double dy1 = (tile.getPos().getY()-ClientUtils.mc().getRenderViewEntity().posY)*(tile.getPos().getY()-ClientUtils.mc().getRenderViewEntity().posY);
		//				double dz1 = (tile.getPos().getZ()-ClientUtils.mc().getRenderViewEntity().posZ)*(tile.getPos().getZ()-ClientUtils.mc().getRenderViewEntity().posZ);
		//				if((dx1+dy1+dz1)<(dx+dy+dz))
		//				{
		////					sound.setPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
		////					soundMap.put(soundName, sound);
		//				}
		//			}
		//		}
		//		else if(tileActive)
		//		{
		//			MovingSound
		//			sound = ClientUtils.generatePositionedIESound("immersiveengineering:"+soundName, volume, pitch, true, 0, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
		////			soundMap.put(soundName, sound);
		//		}
	}
	@Override
	public void stopTileSound(String soundName, TileEntity tile)
	{
		IETileSound sound = soundMap.get(soundName);
		if(sound!=null && new BlockPos(sound.getXPosF(), sound.getYPosF(), sound.getZPosF()).equals(tile.getPos()))
		{
			ClientUtils.mc().getSoundHandler().stopSound(sound);
			sound = null;
		}
	}

	@Override
	public void onWorldLoad()
	{
		if(!ModelShaderMinecart.rendersReplaced)
		{
			for(Object render : ClientUtils.mc().getRenderManager().entityRenderMap.values())
				if(RenderMinecart.class.isAssignableFrom(render.getClass()))
				{
					Object wrapped = ObfuscationReflectionHelper.getPrivateValue(RenderMinecart.class, (RenderMinecart) render, "field_77013_a", "modelMinecart");
					if(wrapped instanceof ModelMinecart)
						ObfuscationReflectionHelper.setPrivateValue(RenderMinecart.class,(RenderMinecart)render, new ModelShaderMinecart((ModelMinecart) wrapped), "field_77013_a","modelMinecart");
				}
			ModelShaderMinecart.rendersReplaced = true;
		}
		if(!IEBipedLayerRenderer.rendersAssigned)
		{
			for(Object render : ClientUtils.mc().getRenderManager().entityRenderMap.values())
				if(RenderBiped.class.isAssignableFrom(render.getClass()))
					((RenderBiped)render).addLayer(new IEBipedLayerRenderer());
				else if(RenderArmorStand.class.isAssignableFrom(render.getClass()))
					((RenderArmorStand)render).addLayer(new IEBipedLayerRenderer());
			IEBipedLayerRenderer.rendersAssigned = true;
		}
	}

	@Override
	public void spawnCrusherFX(TileEntityCrusher tile, ItemStack stack)
	{
		//		if(stack!=null)
		//			for(int i=0; i<3; i++)
		//			{
		//				double x = tile.xCoord+.5+.5*(tile.facing.getAxis()==Axis.Z?tile.getWorldObj().rand.nextGaussian()-.5:0);
		//				double y = tile.yCoord+2 + tile.getWorldObj().rand.nextGaussian()/2;
		//				double z = tile.zCoord+.5+.5*(tile.facing.getAxis()==Axis.X?tile.getWorldObj().rand.nextGaussian()-.5:0);
		//				double mX = tile.getWorldObj().rand.nextGaussian() * 0.01D;
		//				double mY = tile.getWorldObj().rand.nextGaussian() * 0.05D;
		//				double mZ = tile.getWorldObj().rand.nextGaussian() * 0.01D;
		//				EntityFX particle = null;
		//				if(stack.getItem().getSpriteNumber()==0)
		//					particle = new EntityFXBlockParts(tile.getWorldObj(), stack, tile.getWorldObj().rand.nextInt(16), x,y,z, mX,mY,mZ);
		//				else
		//					particle = new EntityFXItemParts(tile.getWorldObj(), stack, tile.getWorldObj().rand.nextInt(16), x,y,z, mX,mY,mZ);
		//				Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		//			}
	}
	@Override
	public void spawnBucketWheelFX(TileEntityBucketWheel tile, ItemStack stack)
	{
		//		if(stack!=null && Config.getBoolean("excavator_particles"))
		//			for(int i=0; i<16; i++)
		//			{
		//				double x = tile.getPos().getX()+.5+.1*(tile.facing.getAxis()==Axis.Z?2*(tile.getWorldObj().rand.nextGaussian()-.5):0);
		//				double y = tile.getPos().getY()+2.5;// + tile.getWorldObj().rand.nextGaussian()/2;
		//				double z = tile.getPos().getZ()+.5+.1*(tile.facing.getAxis()==Axis.X?2*(tile.getWorldObj().rand.nextGaussian()-.5):0);
		//				double mX = ((tile.facing==EnumFacing.WEST?-.075:tile.facing==EnumFacing.EAST?.075:0)*(tile.mirrored?-1:1)) + ((tile.getWorldObj().rand.nextDouble()-.5)*.01);
		//				double mY = -.15D;//tile.getWorldObj().rand.nextGaussian() * -0.05D;
		//				double mZ = ((tile.facing==EnumFacing.NORTH?-.075:tile.facing==EnumFacing.SOUTH?.075:0)*(tile.mirrored?-1:1)) + ((tile.getWorldObj().rand.nextDouble()-.5)*.01);
		//
		//				EntityFX particle = null;
		////				if(stack.getItem().getSpriteNumber()==0)
		//					particle = new EntityFXBlockParts(tile.getWorld(), stack, tile.getWorldObj().rand.nextInt(16), x,y,z, mX,mY,mZ);
		////				else
		////					particle = new EntityFXItemParts(tile.getWorldObj(), stack, tile.getWorldObj().rand.nextInt(16), x,y,z, mX,mY,mZ);
		//				particle.noClip=true;
		//				particle.multipleParticleScaleBy(2);
		//				Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		//			}
	}
	@Override
	public void spawnSparkFX(World world, double x, double y, double z, double mx, double my, double mz)
	{
		Particle particle = new EntityFXSparks(world, x,y,z, mx,my,mz);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}
	@Override
	public void spawnRedstoneFX(World world, double x, double y, double z, double mx, double my, double mz, float size, float r, float g, float b)
	{
		ParticleRedstone particle = (ParticleRedstone)ClientUtils.mc().effectRenderer.spawnEffectParticle(EnumParticleTypes.REDSTONE.getParticleID(), x,y,z, 0,0,0);
		particle.motionX*=mx;
		particle.motionY*=my;
		particle.motionZ*=mz;
		particle.setRBGColorF(r,g,b);
		particle.reddustParticleScale = size;
	}

	@Override
	public void draw3DBlockCauldron()
	{
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBlockState state = Blocks.CAULDRON.getDefaultState();
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);

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
	@Override
	public void drawSpecificFluidPipe(String configuration)
	{
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBlockState state = IEContent.blockMetalDevice1.getStateFromMeta(BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);
		if(state instanceof IExtendedBlockState)
			state = ((IExtendedBlockState)state).withProperty(Properties.AnimationProperty, TileEntityFluidPipe.getStateFromKey(configuration));

		GlStateManager.pushMatrix();
		GlStateManager.translate(0,0,1);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
//		if(model instanceof ISmartBlockModel)
//			model = ((ISmartBlockModel) model).handleBlockState(state);
		blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, .75f, false);
		GlStateManager.popMatrix();
	}

	@Override
	public boolean drawConveyorInGui(String conveyor, EnumFacing facing)
	{
		IConveyorBelt con = ConveyorHandler.getConveyor(new ResourceLocation(conveyor), null);
		if(con != null)
		{
			GlStateManager.pushMatrix();
			Set<BakedQuad> quads = ModelConveyor.getBaseConveyor(facing, 1, new Matrix4(facing), ConveyorDirection.HORIZONTAL, ClientUtils.getSprite(con.getActiveTexture()), new boolean[]{true, true}, new boolean[]{true, true}, null, 0);
//			GL11.glTranslatef(0, 0, 1);
			ClientUtils.renderQuads(quads, 1, 1, 1, 1);
			GlStateManager.popMatrix();
			return true;
		}
		return false;
	}

	static String[][] formatToTable_ItemIntHashmap(Map<String, Integer> map, String valueType)
	{
		Entry<String,Integer>[] sortedMapArray = map.entrySet().toArray(new Entry[0]);
		ArrayList<String[]> list = new ArrayList();
		try{
			for(int i=0; i<sortedMapArray.length; i++)
			{
				String item = sortedMapArray[i].getKey();
				if(ApiUtils.isExistingOreName(sortedMapArray[i].getKey()))
				{
					ItemStack is = OreDictionary.getOres(sortedMapArray[i].getKey()).get(0);
					if(is!=null)
						item = is.getDisplayName();
				}

				if(item!=null)
				{
					int bt = sortedMapArray[i].getValue();
					String am = bt+" "+valueType;
					list.add(new String[]{item,am});
				}
			}
		}catch(Exception e)	{}
		String[][] table = list.toArray(new String[0][]);
		return table;
	}


	@Override
	public String[] splitStringOnWidth(String s, int w)
	{
		return ClientUtils.font().listFormattedStringToWidth(s, w).toArray(new String[0]);
	}

	@Override
	public World getClientWorld()
	{
		return ClientUtils.mc().theWorld;
	}

	@Override
	public String getNameFromUUID(String uuid)
	{
		return ClientUtils.mc().getSessionService().fillProfileProperties(new GameProfile(UUID.fromString(uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")), null), false).getName();
	}

	@Override
	public void reInitGui()
	{
		if(ClientUtils.mc().currentScreen!=null)
			ClientUtils.mc().currentScreen.initGui();
	}

	@Override
	public void removeStateFromSmartModelCache(IExtendedBlockState state)
	{
		for (BlockRenderLayer r:BlockRenderLayer.values())
			IESmartObjModel.modelCache.remove(new ExtBlockstateAdapter(state, r, ImmutableSet.of()));
		IESmartObjModel.modelCache.remove(new ExtBlockstateAdapter(state, null, ImmutableSet.of()));
	}
	@Override
	public void removeStateFromConnectionModelCache(IExtendedBlockState state)
	{
		for (BlockRenderLayer r:BlockRenderLayer.values())
			ConnModelReal.cache.remove(new ExtBlockstateAdapter(state, r, ImmutableSet.of()));
		ConnModelReal.cache.remove(new ExtBlockstateAdapter(state, null, ImmutableSet.of()));
	}
	@Override
	public void clearConnectionModelCache()
	{
		ConnModelReal.cache.clear();
	}

	@Override
	public void clearRenderCaches()
	{
		IESmartObjModel.modelCache.clear();
		IESmartObjModel.cachedBakedItemModels.clear();
		ConnModelReal.cache.clear();
		ModelConveyor.modelCache.clear();
		ModelConfigurableSides.modelCache.clear();
		TileEntityFluidPipe.cachedOBJStates.clear();
	}
	private static void mapFluidState(Block block, Fluid fluid)
	{
		Item item = Item.getItemFromBlock(block);
		FluidStateMapper mapper = new FluidStateMapper(fluid);
		if(item != null)
		{
			ModelLoader.registerItemVariants(item);
			ModelLoader.setCustomMeshDefinition(item, mapper);
		}
		ModelLoader.setCustomStateMapper(block, mapper);
	}

	static class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition
	{
		public final ModelResourceLocation location;

		public FluidStateMapper(Fluid fluid)
		{
			this.location = new ModelResourceLocation(ImmersiveEngineering.MODID + ":fluid_block", fluid.getName());
		}

		@Nonnull
		@Override
		protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state)
		{
			return location;
		}

		@Nonnull
		@Override
		public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack)
		{
			return location;
		}
	}
}