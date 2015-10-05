package blusunrize.immersiveengineering.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockCauldron;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.client.fx.EntityFXBlockParts;
import blusunrize.immersiveengineering.client.fx.EntityFXItemParts;
import blusunrize.immersiveengineering.client.fx.EntityFXSparks;
import blusunrize.immersiveengineering.client.gui.GuiArcFurnace;
import blusunrize.immersiveengineering.client.gui.GuiBlastFurnace;
import blusunrize.immersiveengineering.client.gui.GuiCokeOven;
import blusunrize.immersiveengineering.client.gui.GuiCrate;
import blusunrize.immersiveengineering.client.gui.GuiFermenter;
import blusunrize.immersiveengineering.client.gui.GuiModWorkbench;
import blusunrize.immersiveengineering.client.gui.GuiRefinery;
import blusunrize.immersiveengineering.client.gui.GuiRevolver;
import blusunrize.immersiveengineering.client.gui.GuiSorter;
import blusunrize.immersiveengineering.client.gui.GuiSqueezer;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalDecoration;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalDevices;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalDevices2;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalMultiblocks;
import blusunrize.immersiveengineering.client.render.BlockRenderStoneDevices;
import blusunrize.immersiveengineering.client.render.BlockRenderWoodenDecoration;
import blusunrize.immersiveengineering.client.render.BlockRenderWoodenDevices;
import blusunrize.immersiveengineering.client.render.EntityRenderNone;
import blusunrize.immersiveengineering.client.render.EntityRenderRevolvershot;
import blusunrize.immersiveengineering.client.render.EntityRenderSkycrate;
import blusunrize.immersiveengineering.client.render.ItemRenderDrill;
import blusunrize.immersiveengineering.client.render.ItemRenderRevolver;
import blusunrize.immersiveengineering.client.render.TileRenderArcFurnace;
import blusunrize.immersiveengineering.client.render.TileRenderBreakerSwitch;
import blusunrize.immersiveengineering.client.render.TileRenderBucketWheel;
import blusunrize.immersiveengineering.client.render.TileRenderConnectorHV;
import blusunrize.immersiveengineering.client.render.TileRenderConnectorLV;
import blusunrize.immersiveengineering.client.render.TileRenderConnectorMV;
import blusunrize.immersiveengineering.client.render.TileRenderConnectorStructural;
import blusunrize.immersiveengineering.client.render.TileRenderCrusher;
import blusunrize.immersiveengineering.client.render.TileRenderDieselGenerator;
import blusunrize.immersiveengineering.client.render.TileRenderElectricLantern;
import blusunrize.immersiveengineering.client.render.TileRenderEnergyMeter;
import blusunrize.immersiveengineering.client.render.TileRenderExcavator;
import blusunrize.immersiveengineering.client.render.TileRenderFloodLight;
import blusunrize.immersiveengineering.client.render.TileRenderFluidPipe;
import blusunrize.immersiveengineering.client.render.TileRenderFluidPipe_old;
import blusunrize.immersiveengineering.client.render.TileRenderFluidPump;
import blusunrize.immersiveengineering.client.render.TileRenderLantern;
import blusunrize.immersiveengineering.client.render.TileRenderPost;
import blusunrize.immersiveengineering.client.render.TileRenderRefinery;
import blusunrize.immersiveengineering.client.render.TileRenderRelayHV;
import blusunrize.immersiveengineering.client.render.TileRenderSampleDrill;
import blusunrize.immersiveengineering.client.render.TileRenderSheetmetalTank;
import blusunrize.immersiveengineering.client.render.TileRenderSilo;
import blusunrize.immersiveengineering.client.render.TileRenderTransformer;
import blusunrize.immersiveengineering.client.render.TileRenderWallmount;
import blusunrize.immersiveengineering.client.render.TileRenderWatermill;
import blusunrize.immersiveengineering.client.render.TileRenderWindmill;
import blusunrize.immersiveengineering.client.render.TileRenderWindmillAdvanced;
import blusunrize.immersiveengineering.client.render.TileRenderWorkbench;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBreakerSwitch;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorMV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorStructural;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorSorter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityElectricLantern;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFloodLight;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe_old;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLantern;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRelayHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySheetmetalTank;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySilo;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformerHV;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockArcFurnace;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBucketWheel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockCokeOven;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockCrusher;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavator;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavatorDemo;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockFermenter;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockLightningRod;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockRefinery;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSheetmetalTank;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSilo;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSqueezer;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWallmount;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWatermill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmillAdvanced;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.entities.EntitySkycrate;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.IESound;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualPages;
import blusunrize.lib.manual.ManualPages.PositionedItemStack;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.VillagerRegistry;

public class ClientProxy extends CommonProxy
{
	public static TextureMap revolverTextureMap;
	public static final ResourceLocation revolverTextureResource = new ResourceLocation("textures/atlas/immersiveengineering/revolvers.png");

	@Override
	public void init()
	{
		//METAL
		RenderingRegistry.registerBlockHandler(new BlockRenderMetalDevices());
		RenderingRegistry.registerBlockHandler(new BlockRenderMetalDevices2());
		RenderingRegistry.registerBlockHandler(new BlockRenderMetalDecoration());
		RenderingRegistry.registerBlockHandler(new BlockRenderMetalMultiblocks());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnectorLV.class, new TileRenderConnectorLV());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnectorMV.class, new TileRenderConnectorMV());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTransformer.class, new TileRenderTransformer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRelayHV.class, new TileRenderRelayHV());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnectorHV.class, new TileRenderConnectorHV());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTransformerHV.class, new TileRenderTransformer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySampleDrill.class, new TileRenderSampleDrill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDieselGenerator.class, new TileRenderDieselGenerator());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRefinery.class, new TileRenderRefinery());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrusher.class, new TileRenderCrusher());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnectorStructural.class, new TileRenderConnectorStructural());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBucketWheel.class, new TileRenderBucketWheel());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityExcavator.class, new TileRenderExcavator());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLantern.class, new TileRenderLantern());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBreakerSwitch.class, new TileRenderBreakerSwitch());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityArcFurnace.class, new TileRenderArcFurnace());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergyMeter.class, new TileRenderEnergyMeter());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySheetmetalTank.class, new TileRenderSheetmetalTank());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySilo.class, new TileRenderSilo());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityElectricLantern.class, new TileRenderElectricLantern());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFloodLight.class, new TileRenderFloodLight());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidPipe_old.class, new TileRenderFluidPipe_old());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidPipe.class, new TileRenderFluidPipe());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidPump.class, new TileRenderFluidPump());

		//WOOD
		RenderingRegistry.registerBlockHandler(new BlockRenderWoodenDevices());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWoodenPost.class, new TileRenderPost());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWatermill.class, new TileRenderWatermill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindmill.class, new TileRenderWindmill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindmillAdvanced.class, new TileRenderWindmillAdvanced());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityModWorkbench.class, new TileRenderWorkbench());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWallmount.class, new TileRenderWallmount());

		RenderingRegistry.registerBlockHandler(new BlockRenderWoodenDecoration());
		//STONE
		RenderingRegistry.registerBlockHandler(new BlockRenderStoneDevices());

		//REVOLVER
		revolverTextureMap = new TextureMap(Config.getInt("revolverSheetID"), "textures/revolvers");
		ClientUtils.mc().renderEngine.loadTextureMap(revolverTextureResource, revolverTextureMap);

		MinecraftForgeClient.registerItemRenderer(IEContent.itemRevolver, new ItemRenderRevolver());
		RenderingRegistry.registerEntityRenderingHandler(EntityRevolvershot.class, new EntityRenderRevolvershot());
		//DRILL
		MinecraftForgeClient.registerItemRenderer(IEContent.itemDrill, new ItemRenderDrill());
		//ZIPLINE
		RenderingRegistry.registerEntityRenderingHandler(EntitySkylineHook.class, new EntityRenderNone());
		RenderingRegistry.registerEntityRenderingHandler(EntitySkycrate.class, new EntityRenderSkycrate());
		/** TODO when there is an actual model for it =P
		MinecraftForgeClient.registerItemRenderer(IEContent.itemSkyhook, new ItemRenderSkyhook());
		 */

		int villagerId = Config.getInt("villager_engineer");
		VillagerRegistry.instance().registerVillagerSkin(villagerId, new ResourceLocation("immersiveengineering:textures/models/villager_engineer.png"));

		ClientEventHandler handler = new ClientEventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		FMLCommonHandler.instance().bus().register(handler);
	}

	@Override
	public void postInit()
	{
		ManualHelper.ieManualInstance = new IEManualInstance();
		ManualHelper.addEntry("introduction", ManualHelper.CAT_GENERAL,
				new ManualPages.Text(ManualHelper.getManual(), "introduction0"),
				new ManualPages.Text(ManualHelper.getManual(), "introduction1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "introductionHammer", new ItemStack(IEContent.itemTool,1,0)));
		ManualHelper.addEntry("ores", ManualHelper.CAT_GENERAL,
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "oresCopper", new ItemStack(IEContent.blockOres,1,0),new ItemStack(IEContent.itemMetal,1,0)),
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "oresBauxite", new ItemStack(IEContent.blockOres,1,1),new ItemStack(IEContent.itemMetal,1,1)),
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "oresLead", new ItemStack(IEContent.blockOres,1,2),new ItemStack(IEContent.itemMetal,1,2)),
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "oresSilver", new ItemStack(IEContent.blockOres,1,3),new ItemStack(IEContent.itemMetal,1,3)),
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "oresNickel", new ItemStack(IEContent.blockOres,1,4),new ItemStack(IEContent.itemMetal,1,4)));
		ArrayList<PositionedItemStack[]> recipes = new ArrayList();
		for(int i=0; i<7; i++)
		{
			ItemStack ore = i==0?new ItemStack(Blocks.iron_ore): i==1?new ItemStack(Blocks.gold_ore): new ItemStack(IEContent.blockOres,1,i-2);
			ItemStack ingot = i==0?new ItemStack(Items.iron_ingot): i==1?new ItemStack(Items.gold_ingot): new ItemStack(IEContent.itemMetal,1,i-2);
			if(Config.getBoolean("crushingOreRecipe"))
				recipes.add(new PositionedItemStack[]{ new PositionedItemStack(ore, 24, 0), new PositionedItemStack(new ItemStack(IEContent.itemTool,1,0), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,2,8+i), 78, 0)});
			recipes.add(new PositionedItemStack[]{ new PositionedItemStack(ingot, 24, 0), new PositionedItemStack(new ItemStack(IEContent.itemTool,1,0), 42, 0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,1,8+i), 78, 0)});
		}
		PositionedItemStack[][] recA = recipes.toArray(new PositionedItemStack[0][0]);
		ManualHelper.addEntry("oreProcessing", ManualHelper.CAT_GENERAL,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "oreProcessing0", (Object[])recA),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "oreProcessing1", (Object[])new PositionedItemStack[][]{
					new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("dustCopper"),24,0), new PositionedItemStack(OreDictionary.getOres("dustNickel"),42,0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,2,15),78,0)},
					new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("dustGold"),24,0), new PositionedItemStack(OreDictionary.getOres("dustSilver"),42,0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,2,16),78,0)}}));
		ManualHelper.addEntry("hemp", ManualHelper.CAT_GENERAL,
				new ManualPages.ItemDisplay(ManualHelper.getManual(), "hemp0", new ItemStack(IEContent.blockCrop,1,5),new ItemStack(IEContent.itemSeeds)));
		ManualHelper.addEntry("cokeoven", ManualHelper.CAT_GENERAL,
				new ManualPages.Text(ManualHelper.getManual(), "cokeoven0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "cokeovenBlock", new ItemStack(IEContent.blockStoneDecoration,1,1)),
				new ManualPageMultiblock(ManualHelper.getManual(), "", MultiblockCokeOven.instance));
		ManualHelper.addEntry("blastfurnace", ManualHelper.CAT_GENERAL,
				new ManualPages.Text(ManualHelper.getManual(), "blastfurnace0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "blastfurnaceBlock", new ItemStack(IEContent.blockStoneDecoration,1,2)),
				new ManualPageMultiblock(ManualHelper.getManual(), "", MultiblockBlastFurnace.instance));
		String[][][] multiTables = formatToTable_ExcavatorMinerals();
		ArrayList<IManualPage> pages = new ArrayList();
		pages.add(new ManualPages.Text(ManualHelper.getManual(), "minerals0"));
		pages.add(new ManualPages.Crafting(ManualHelper.getManual(), "minerals1", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_sampleDrill)));
		pages.add(new ManualPages.Text(ManualHelper.getManual(), "minerals2"));
		for(String[][] minTable : multiTables)
			pages.add(new ManualPages.Table(ManualHelper.getManual(), "", minTable,true));
		ManualHelper.addEntry("minerals", ManualHelper.CAT_GENERAL, pages.toArray(new IManualPage[pages.size()]));
		int blueprint = BlueprintCraftingRecipe.blueprintCategories.indexOf("electrode");
		ManualHelper.addEntry("graphite", ManualHelper.CAT_GENERAL, new ManualPages.Text(ManualHelper.getManual(), "graphite0"),new ManualPages.Crafting(ManualHelper.getManual(), "graphite1", new ItemStack(IEContent.itemBlueprint,1,blueprint)));

		ManualHelper.addEntry("treatedwood", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Text(ManualHelper.getManual(), "treatedwood0"), 
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockTreatedWood,1,0),new ItemStack(IEContent.blockWoodenDecoration,1,2),new ItemStack(IEContent.blockWoodenStair)),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.itemMaterial,1,0),new ItemStack(IEContent.blockWoodenDecoration,1,1),new ItemStack(IEContent.blockWoodenDecoration,1,6)),
				new ManualPages.Crafting(ManualHelper.getManual(), "treatedwoodPost0", new ItemStack(IEContent.blockWoodenDevice,1,0)),
				new ManualPages.Text(ManualHelper.getManual(), "treatedwoodPost1"));
		ItemStack[] storageBlocks = new ItemStack[8];
		ItemStack[] storageSlabs = new ItemStack[8];
		for(int i=0; i<8; i++)
		{
			storageBlocks[i] = new ItemStack(IEContent.blockStorage,1,i);
			storageSlabs[i] = new ItemStack(IEContent.blockStorageSlabs,1,i);
		}
		ManualHelper.addEntry("barrel", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Crafting(ManualHelper.getManual(), "barrel0", new ItemStack(IEContent.blockWoodenDevice,1,6)));
		ManualHelper.addEntry("metalconstruction", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Text(ManualHelper.getManual(), "metalconstruction0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "", storageBlocks,storageSlabs,new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_sheetMetal)),
				new ManualPages.Text(ManualHelper.getManual(), "metalconstruction1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_fence),new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_scaffolding),new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_structuralArm)),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_wallMount)),
				new ManualPages.Crafting(ManualHelper.getManual(), "metalconstruction2", new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_connectorStructural)),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.itemWireCoil,1,3),new ItemStack(IEContent.itemWireCoil,1,4)));
		ManualHelper.addEntry("multiblocks", ManualHelper.CAT_CONSTRUCTION,
				new ManualPages.Text(ManualHelper.getManual(), "multiblocks0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_lightEngineering),new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_heavyEngineering)),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_generator),new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_radiator)));
		ManualHelper.addEntry("workbench", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Crafting(ManualHelper.getManual(), "workbench0", new ItemStack(IEContent.blockWoodenDevice,1,5)));
		ManualHelper.addEntry("blueprints", ManualHelper.CAT_CONSTRUCTION, new ManualPages.Text(ManualHelper.getManual(), "blueprints0"),new ManualPages.Text(ManualHelper.getManual(), "blueprints1"));


		ManualHelper.addEntry("wiring", ManualHelper.CAT_ENERGY,
				new ManualPages.Text(ManualHelper.getManual(), "wiring0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "wiring1", new ItemStack(IEContent.itemWireCoil,1,OreDictionary.WILDCARD_VALUE)),
				new ManualPages.Image(ManualHelper.getManual(), "wiring2", "immersiveengineering:textures/misc/wiring.png;0;0;110;40", "immersiveengineering:textures/misc/wiring.png;0;40;110;30"),
				new ManualPages.Image(ManualHelper.getManual(), "wiring3", "immersiveengineering:textures/misc/wiring.png;0;70;110;60", "immersiveengineering:textures/misc/wiring.png;0;130;110;60"),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "wiringConnector", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_connectorLV),new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_connectorMV),new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_relayHV),new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_connectorHV)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "wiringCapacitor", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_capacitorLV),new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_capacitorMV),new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_capacitorHV)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "wiringTransformer0", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_transformer),new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_transformerHV)),
				new ManualPages.Text(ManualHelper.getManual(), "wiringTransformer1"),
				new ManualPages.Crafting(ManualHelper.getManual(), "wiringCutters", new ItemStack(IEContent.itemTool,1,1)),
				new ManualPages.Crafting(ManualHelper.getManual(), "wiringVoltmeter", new ItemStack(IEContent.itemTool,1,2)));
		ManualHelper.getManual().addEntry("generator", ManualHelper.CAT_ENERGY,
				new ManualPages.Crafting(ManualHelper.getManual(), "generator0", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_dynamo)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "generatorWindmill", new ItemStack(IEContent.blockWoodenDevice,1,2),new ItemStack(IEContent.itemMaterial,1,2)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "generatorWatermill", new ItemStack(IEContent.blockWoodenDevice,1,1),new ItemStack(IEContent.itemMaterial,1,1)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "generatorWindmillImproved", new ItemStack(IEContent.blockWoodenDevice,1,3),new ItemStack(IEContent.itemMaterial,1,4),new ItemStack(IEContent.itemMaterial,1,5)));
		ManualHelper.getManual().addEntry("breaker", ManualHelper.CAT_ENERGY, new ManualPages.Crafting(ManualHelper.getManual(), "breaker0", new ItemStack(IEContent.blockMetalDevice2,1,BlockMetalDevices2.META_breakerSwitch)),new ManualPages.Text(ManualHelper.getManual(), "breaker1"));
		Map<String,Integer> sortedMap = ThermoelectricHandler.getThermalValuesSorted(true);
		String[][] table = formatToTable_ItemIntHashmap(sortedMap,"K");	
		ManualHelper.getManual().addEntry("thermoElectric", ManualHelper.CAT_ENERGY,
				new ManualPages.Crafting(ManualHelper.getManual(), "thermoElectric0", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_thermoelectricGen)),
				new ManualPages.Table(ManualHelper.getManual(), "thermoElectric1", table, false));
		ManualHelper.addEntry("highvoltage", ManualHelper.CAT_ENERGY,
				new ManualPages.Text(ManualHelper.getManual(), "highvoltage0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDevice,1,8),new ItemStack(IEContent.blockMetalDevice,1,4)),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalDevice,1,5),new ItemStack(IEContent.blockMetalDevice,1,7)));
		sortedMap = DieselHandler.getFuelValuesSorted(true);
		Map.Entry<String,Integer>[] dieselFuels = sortedMap.entrySet().toArray(new Map.Entry[0]);
		table = new String[dieselFuels.length][2];
		for(int i=0; i<table.length; i++)
		{
			Fluid f = FluidRegistry.getFluid(dieselFuels[i].getKey());
			String sf = f!=null?new FluidStack(f,1000).getUnlocalizedName():"";
			int bt = dieselFuels[i].getValue();
			String am = Utils.formatDouble(bt/20f, "0.###")+" ("+bt+")";
			table[i] = new String[]{sf,am};
		}
		ManualHelper.addEntry("dieselgen", ManualHelper.CAT_ENERGY,
				new ManualPages.Text(ManualHelper.getManual(), "dieselgen0"),
				new ManualPageMultiblock(ManualHelper.getManual(), "dieselgen1", MultiblockDieselGenerator.instance),
				new ManualPages.Text(ManualHelper.getManual(), "dieselgen2"),
				new ManualPages.Table(ManualHelper.getManual(), "dieselgen3", table, false)
				);
		ManualHelper.addEntry("lightningrod", ManualHelper.CAT_ENERGY,
				new ManualPages.Crafting(ManualHelper.getManual(), "lightningrod0",  new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_lightningRod)),
				new ManualPageMultiblock(ManualHelper.getManual(), "lightningrod1", MultiblockLightningRod.instance),
				new ManualPages.Text(ManualHelper.getManual(), "lightningrod2"));

		ManualHelper.addEntry("conveyor", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "conveyor0", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_conveyorBelt)),
				new ManualPages.Text(ManualHelper.getManual(), "conveyor1"));
		ManualHelper.addEntry("furnaceHeater", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "furnaceHeater0", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_furnaceHeater)),
				new ManualPages.Text(ManualHelper.getManual(), "furnaceHeater1"),
				new ManualPages.Text(ManualHelper.getManual(), "furnaceHeater2"));
		ManualHelper.addEntry("sorter", ManualHelper.CAT_MACHINES,
				new ManualPages.Crafting(ManualHelper.getManual(), "sorter0", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_sorter)),
				new ManualPages.Text(ManualHelper.getManual(), "sorter1"));
		ManualHelper.addEntry("tanksilo", ManualHelper.CAT_MACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "tanksilo0", MultiblockSheetmetalTank.instance),
				new ManualPageMultiblock(ManualHelper.getManual(), "tanksilo1", MultiblockSilo.instance),
				new ManualPages.Text(ManualHelper.getManual(), "tanksilo2"));
		ManualHelper.addEntry("drill", ManualHelper.CAT_MACHINES,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "drill0", new ItemStack(IEContent.itemDrill,1,0), new ItemStack(IEContent.itemMaterial,1,9)),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill1", new ItemStack(IEContent.itemDrillhead,1,0)),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill2", new ItemStack(IEContent.itemToolUpgrades,1,0)),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill3", new ItemStack(IEContent.itemToolUpgrades,1,1)),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill4", new ItemStack(IEContent.itemToolUpgrades,1,2)),
				new ManualPages.Crafting(ManualHelper.getManual(), "drill5", new ItemStack(IEContent.itemToolUpgrades,1,3)));
		int blueprint_bullet = BlueprintCraftingRecipe.blueprintCategories.indexOf("bullet");
		ManualHelper.addEntry("revolver", ManualHelper.CAT_MACHINES,
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "revolver0", new ItemStack(IEContent.itemRevolver,1,0), new ItemStack(IEContent.itemMaterial,1,7),new ItemStack(IEContent.itemMaterial,1,8),new ItemStack(IEContent.itemMaterial,1,9),new ItemStack(IEContent.itemMaterial,1,10)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "revolver1", new ItemStack(IEContent.itemBullet,1,0),new ItemStack(IEContent.itemBullet,1,1),new ItemStack(IEContent.itemRevolver,1,1)),
				new ManualPages.CraftingMulti(ManualHelper.getManual(), "revolver2", new ItemStack(IEContent.itemBlueprint,1,blueprint_bullet)),
				new ManualPages.Text(ManualHelper.getManual(), "revolver3"),
				new ManualPages.Text(ManualHelper.getManual(), "revolver4"),
				new ManualPages.Crafting(ManualHelper.getManual(), "revolver5", new ItemStack(IEContent.itemToolUpgrades,1,4)),
				new ManualPages.Crafting(ManualHelper.getManual(), "revolver6", new ItemStack(IEContent.itemToolUpgrades,1,5)),
				new ManualPages.Crafting(ManualHelper.getManual(), "revolver7", new ItemStack(IEContent.itemToolUpgrades,1,6)));


		sortedMap = DieselHandler.getPlantoilValuesSorted(true);
		table = formatToTable_ItemIntHashmap(sortedMap,"mB");	
		sortedMap = DieselHandler.getEthanolValuesSorted(true);
		String[][] table2 = formatToTable_ItemIntHashmap(sortedMap,"mB");
		ManualHelper.addEntry("biodiesel", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPages.Text(ManualHelper.getManual(), "biodiesel0"),
				new ManualPages.Crafting(ManualHelper.getManual(), "", new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_squeezer),new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_fermenter)),
				new ManualPageMultiblock(ManualHelper.getManual(), "biodiesel1", MultiblockSqueezer.instance),
				new ManualPages.Table(ManualHelper.getManual(), "biodiesel1T", table, false),
				new ManualPageMultiblock(ManualHelper.getManual(), "biodiesel2", MultiblockFermenter.instance),
				new ManualPages.Table(ManualHelper.getManual(), "biodiesel2T", table2, false),
				new ManualPageMultiblock(ManualHelper.getManual(), "biodiesel3", MultiblockRefinery.instance),
				new ManualPages.Text(ManualHelper.getManual(), "biodiesel4"));
		ManualHelper.addEntry("crusher", ManualHelper.CAT_HEAVYMACHINES,
				new ManualPageMultiblock(ManualHelper.getManual(), "crusher0", MultiblockCrusher.instance),
				new ManualPages.Text(ManualHelper.getManual(), "crusher1"));
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
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(ID==Lib.GUIID_CokeOven && te instanceof TileEntityCokeOven)
			return new GuiCokeOven(player.inventory, (TileEntityCokeOven) te);
		if(ID==Lib.GUIID_BlastFurnace && te instanceof TileEntityBlastFurnace)
			return new GuiBlastFurnace(player.inventory, (TileEntityBlastFurnace) te);
		if(ID==Lib.GUIID_Revolver && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemRevolver)
			return new GuiRevolver(player.inventory, world);
		if(ID==Lib.GUIID_Manual && ManualHelper.getManual()!=null && player.getCurrentEquippedItem()!=null && OreDictionary.itemMatches(new ItemStack(IEContent.itemTool,1,3), player.getCurrentEquippedItem(), false))
			return ManualHelper.getManual().getGui();
		if(ID==Lib.GUIID_WoodenCrate && te instanceof TileEntityWoodenCrate)
			return new GuiCrate(player.inventory, (TileEntityWoodenCrate) te);
		if(ID==Lib.GUIID_Squeezer && te instanceof TileEntitySqueezer)
			return new GuiSqueezer(player.inventory, (TileEntitySqueezer) te);
		if(ID==Lib.GUIID_Fermenter && te instanceof TileEntityFermenter)
			return new GuiFermenter(player.inventory, (TileEntityFermenter) te);
		if(ID==Lib.GUIID_Sorter && te instanceof TileEntityConveyorSorter)
			return new GuiSorter(player.inventory, (TileEntityConveyorSorter) te);
		if(ID==Lib.GUIID_Refinery && te instanceof TileEntityRefinery)
			return new GuiRefinery(player.inventory, (TileEntityRefinery) te);
		//		if(ID==Lib.GUIID_Workbench && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemDrill)
		//			return new GuiDrill(player.inventory, world);
		if(ID==Lib.GUIID_Workbench && te instanceof TileEntityModWorkbench)
			return new GuiModWorkbench(player.inventory, (TileEntityModWorkbench) te);
		if(ID==Lib.GUIID_ArcFurnace && te instanceof TileEntityArcFurnace)
			return new GuiArcFurnace(player.inventory, (TileEntityArcFurnace) te);
		return null;
	}

	HashMap<String, IESound> soundMap = new HashMap<String, IESound>();
	@Override
	public void handleTileSound(String soundName, TileEntity tile, boolean tileActive, float volume, float pitch)
	{
		IESound sound = soundMap.get(soundName);
		if(sound!=null)
		{
			if(sound.getXPosF()==tile.xCoord && sound.getYPosF()==tile.yCoord && sound.getZPosF()==tile.zCoord)
			{
				if(!tileActive)
				{
					ClientUtils.mc().getSoundHandler().stopSound(sound);
					sound = null;
					soundMap.remove(soundName);
				}
			}
			else if(tileActive)
			{
				double dx = (sound.getXPosF()-ClientUtils.mc().renderViewEntity.posX)*(sound.getXPosF()-ClientUtils.mc().renderViewEntity.posX);
				double dy = (sound.getYPosF()-ClientUtils.mc().renderViewEntity.posY)*(sound.getYPosF()-ClientUtils.mc().renderViewEntity.posY);
				double dz = (sound.getZPosF()-ClientUtils.mc().renderViewEntity.posZ)*(sound.getZPosF()-ClientUtils.mc().renderViewEntity.posZ);
				double dx1 = (tile.xCoord-ClientUtils.mc().renderViewEntity.posX)*(tile.xCoord-ClientUtils.mc().renderViewEntity.posX);
				double dy1 = (tile.yCoord-ClientUtils.mc().renderViewEntity.posY)*(tile.yCoord-ClientUtils.mc().renderViewEntity.posY);
				double dz1 = (tile.zCoord-ClientUtils.mc().renderViewEntity.posZ)*(tile.zCoord-ClientUtils.mc().renderViewEntity.posZ);
				if((dx1+dy1+dz1)<(dx+dy+dz))
				{
					sound.setPos(tile.xCoord, tile.yCoord, tile.zCoord);
					soundMap.put(soundName, sound);
				}
			}
		}
		if(tileActive)
		{
			if(sound==null || !ClientUtils.mc().getSoundHandler().isSoundPlaying(sound))
			{
				sound = ClientUtils.generatePositionedIESound("immersiveengineering:"+soundName, volume,pitch, true,0, tile.xCoord,tile.yCoord,tile.zCoord);
				soundMap.put(soundName, sound);
			}
		}
	}
	@Override
	public void stopTileSound(String soundName, TileEntity tile)
	{
		IESound sound = soundMap.get(soundName);
		if(sound!=null && sound.getXPosF()==tile.xCoord && sound.getYPosF()==tile.yCoord && sound.getZPosF()==tile.zCoord)
		{
			ClientUtils.mc().getSoundHandler().stopSound(sound);
			sound = null;
		}
	}
	@Override
	public void spawnCrusherFX(TileEntityCrusher tile, ItemStack stack)
	{
		if(stack!=null)
			for(int i=0; i<3; i++)
			{
				double x = tile.xCoord+.5+.5*(tile.facing<4?tile.getWorldObj().rand.nextGaussian()-.5:0);
				double y = tile.yCoord+2 + tile.getWorldObj().rand.nextGaussian()/2;
				double z = tile.zCoord+.5+.5*(tile.facing>3?tile.getWorldObj().rand.nextGaussian()-.5:0);
				double mX = tile.getWorldObj().rand.nextGaussian() * 0.01D;
				double mY = tile.getWorldObj().rand.nextGaussian() * 0.05D;
				double mZ = tile.getWorldObj().rand.nextGaussian() * 0.01D;
				EntityFX particle = null;
				if(stack.getItem().getSpriteNumber()==0)
					particle = new EntityFXBlockParts(tile.getWorldObj(), stack, tile.getWorldObj().rand.nextInt(16), x,y,z, mX,mY,mZ);
				else
					particle = new EntityFXItemParts(tile.getWorldObj(), stack, tile.getWorldObj().rand.nextInt(16), x,y,z, mX,mY,mZ);
				Minecraft.getMinecraft().effectRenderer.addEffect(particle);
			}
	}
	@Override
	public void spawnBucketWheelFX(TileEntityBucketWheel tile, ItemStack stack)
	{
		if(stack!=null && Config.getBoolean("excavator_particles"))
			for(int i=0; i<16; i++)
			{
				double x = tile.xCoord+.5+.1*(tile.facing<4?2*(tile.getWorldObj().rand.nextGaussian()-.5):0);
				double y = tile.yCoord+2.5;// + tile.getWorldObj().rand.nextGaussian()/2;
				double z = tile.zCoord+.5+.1*(tile.facing>3?2*(tile.getWorldObj().rand.nextGaussian()-.5):0);
				double mX = ((tile.facing==4?-.075:tile.facing==5?.075:0)*(tile.mirrored?-1:1)) + ((tile.getWorldObj().rand.nextDouble()-.5)*.01);
				double mY = -.15D;//tile.getWorldObj().rand.nextGaussian() * -0.05D;
				double mZ = ((tile.facing==2?-.075:tile.facing==3?.075:0)*(tile.mirrored?-1:1)) + ((tile.getWorldObj().rand.nextDouble()-.5)*.01);

				EntityFX particle = null;
				if(stack.getItem().getSpriteNumber()==0)
					particle = new EntityFXBlockParts(tile.getWorldObj(), stack, tile.getWorldObj().rand.nextInt(16), x,y,z, mX,mY,mZ);
				else
					particle = new EntityFXItemParts(tile.getWorldObj(), stack, tile.getWorldObj().rand.nextInt(16), x,y,z, mX,mY,mZ);
				particle.noClip=true;
				particle.multipleParticleScaleBy(2);
				Minecraft.getMinecraft().effectRenderer.addEffect(particle);
			}
	}
	@Override
	public void spawnSparkFX(World world, double x, double y, double z, double mx, double my, double mz)
	{
		EntityFX particleMysterious = new EntityFXSparks(world, x,y,z, mx,my,mz);
		Minecraft.getMinecraft().effectRenderer.addEffect(particleMysterious);
	}

	@Override
	public void draw3DBlockCauldron()
	{
		RenderBlocks blockRender = RenderBlocks.getInstance();
		blockRender.setRenderBounds(0, 0, 0, 1, 1, 1);
		Tessellator.instance.startDrawingQuads();
		Tessellator.instance.addTranslation(-.5f, -.5f, -.5f);
		blockRender.renderFaceYPos(Blocks.cauldron, 0,0,0, Blocks.cauldron.getBlockTextureFromSide(1));
		IIcon icon = Blocks.cauldron.getBlockTextureFromSide(2);
		blockRender.renderFaceXNeg(Blocks.cauldron, 0,0,0, icon);
		blockRender.renderFaceXPos(Blocks.cauldron, 0,0,0, icon);
		blockRender.renderFaceZNeg(Blocks.cauldron, 0,0,0, icon);
		blockRender.renderFaceZPos(Blocks.cauldron, 0,0,0, icon);
		float f4 = 0.125F;
		blockRender.renderFaceXPos(Blocks.cauldron, ((float)0 - 1.0F + f4), (double)0, (double)0, icon);
		blockRender.renderFaceXNeg(Blocks.cauldron, (double)((float)0 + 1.0F - f4), (double)0, (double)0, icon);
		blockRender.renderFaceZPos(Blocks.cauldron, (double)0, (double)0, (double)((float)0 - 1.0F + f4), icon);
		blockRender.renderFaceZNeg(Blocks.cauldron, (double)0, (double)0, (double)((float)0 + 1.0F - f4), icon);
		IIcon iicon1 = BlockCauldron.getCauldronIcon("inner");
		blockRender.renderFaceYPos(Blocks.cauldron, (double)0, (double)((float)0 - 1.0F + 0.25F), (double)0, iicon1);
		blockRender.renderFaceYNeg(Blocks.cauldron, (double)0, (double)((float)0 + 1.0F - 0.75F), (double)0, iicon1);
		Tessellator.instance.addTranslation(.5f, .5f, .5f);
		Tessellator.instance.draw();
	}

	static String[][] formatToTable_ItemIntHashmap(Map<String, Integer> map, String valueType)
	{
		Map.Entry<String,Integer>[] sortedMapArray = map.entrySet().toArray(new Map.Entry[0]);
		ArrayList<String[]> list = new ArrayList();
		try{
			for(int i=0; i<sortedMapArray.length; i++)
			{
				String item = null;
				if(!OreDictionary.getOres(sortedMapArray[i].getKey()).isEmpty() && OreDictionary.getOres(sortedMapArray[i].getKey()).size()>0)
				{
					ItemStack is = OreDictionary.getOres(sortedMapArray[i].getKey()).get(0);
					if(is!=null)
						item = is.getDisplayName();
				}
				else if(sortedMapArray[i].getKey().contains("::"))
				{
					String[] split = sortedMapArray[i].getKey().split("::");
					Item it = GameData.getItemRegistry().getObject(split[0]);
					int meta = 0;
					try{meta = Integer.parseInt(split[1]);}catch(Exception e){}
					if(it!=null)
						item = new ItemStack(it, 1, meta).getDisplayName();
				}
				else
					item = sortedMapArray[i].getKey();

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

	static String[][][] formatToTable_ExcavatorMinerals()
	{
		ExcavatorHandler.MineralMix[] minerals = ExcavatorHandler.mineralList.keySet().toArray(new ExcavatorHandler.MineralMix[0]);
		String[][][] multiTables = new String[1][ExcavatorHandler.mineralList.size()][2];
		int curTable = 0;
		int totalLines = 0;
		for(int i=0; i<minerals.length; i++)
		{
			multiTables[curTable][i][0] = Lib.DESC_INFO+"mineral."+minerals[i].name;
			multiTables[curTable][i][1] = "";
			//			for(int j=0; j<minerals[i].recalculatedOres.length; j++)
			//				if(!OreDictionary.getOres(minerals[i].recalculatedOres[j]).isEmpty())
			//				{
			//					ItemStack stackOre = OreDictionary.getOres(minerals[i].recalculatedOres[j]).get(0);
			//					multiTables[curTable][i][1] += stackOre.getDisplayName()+" "+( Utils.formatDouble(minerals[i].recalculatedChances[j]*100,"#.00")+"%" )+(j<minerals[i].recalculatedOres.length-1?"\n":"");
			//					totalLines++;
			//				}
			for(int j=0; j<minerals[i].oreOutput.length; j++)
				if(minerals[i].oreOutput[j]!=null)
				{
					multiTables[curTable][i][1] += minerals[i].oreOutput[j].getDisplayName()+" "+( Utils.formatDouble(minerals[i].recalculatedChances[j]*100,"#.00")+"%" )+(j<minerals[i].oreOutput.length-1?"\n":"");
					totalLines++;
				}
			if(i<minerals.length-1 && totalLines+minerals[i+1].oreOutput.length>=13)
			{
				String[][][] newMultiTables = new String[multiTables.length+1][ExcavatorHandler.mineralList.size()][2];
				System.arraycopy(multiTables,0, newMultiTables,0, multiTables.length);
				multiTables = newMultiTables;
				totalLines = 0;
				curTable++;
			}
		}
		return multiTables;
	}




	@Override
	public String[] splitStringOnWidth(String s, int w)
	{
		return ((List<String>)ClientUtils.font().listFormattedStringToWidth(s, w)).toArray(new String[0]);
	}
}