package blusunrize.immersiveengineering.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.DieselHandler;
import blusunrize.immersiveengineering.api.ExcavatorHandler;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.api.ThermoelectricHandler;
import blusunrize.immersiveengineering.client.fx.EntityFXItemParts;
import blusunrize.immersiveengineering.client.gui.GuiBlastFurnace;
import blusunrize.immersiveengineering.client.gui.GuiCokeOven;
import blusunrize.immersiveengineering.client.gui.GuiCrate;
import blusunrize.immersiveengineering.client.gui.GuiDrill;
import blusunrize.immersiveengineering.client.gui.GuiFermenter;
import blusunrize.immersiveengineering.client.gui.GuiRefinery;
import blusunrize.immersiveengineering.client.gui.GuiRevolver;
import blusunrize.immersiveengineering.client.gui.GuiSorter;
import blusunrize.immersiveengineering.client.gui.GuiSqueezer;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalDecoration;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalDevices;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalMultiblocks;
import blusunrize.immersiveengineering.client.render.BlockRenderStoneDevices;
import blusunrize.immersiveengineering.client.render.BlockRenderWoodenDecoration;
import blusunrize.immersiveengineering.client.render.BlockRenderWoodenDevices;
import blusunrize.immersiveengineering.client.render.EntityRenderRevolvershot;
import blusunrize.immersiveengineering.client.render.ItemRenderDrill;
import blusunrize.immersiveengineering.client.render.ItemRenderRevolver;
import blusunrize.immersiveengineering.client.render.TileRenderBucketWheel;
import blusunrize.immersiveengineering.client.render.TileRenderConnectorHV;
import blusunrize.immersiveengineering.client.render.TileRenderConnectorLV;
import blusunrize.immersiveengineering.client.render.TileRenderConnectorMV;
import blusunrize.immersiveengineering.client.render.TileRenderConnectorStructural;
import blusunrize.immersiveengineering.client.render.TileRenderCrusher;
import blusunrize.immersiveengineering.client.render.TileRenderDieselGenerator;
import blusunrize.immersiveengineering.client.render.TileRenderExcavator;
import blusunrize.immersiveengineering.client.render.TileRenderPost;
import blusunrize.immersiveengineering.client.render.TileRenderRefinery;
import blusunrize.immersiveengineering.client.render.TileRenderRelayHV;
import blusunrize.immersiveengineering.client.render.TileRenderSampleDrill;
import blusunrize.immersiveengineering.client.render.TileRenderTransformer;
import blusunrize.immersiveengineering.client.render.TileRenderWallmount;
import blusunrize.immersiveengineering.client.render.TileRenderWatermill;
import blusunrize.immersiveengineering.client.render.TileRenderWindmill;
import blusunrize.immersiveengineering.client.render.TileRenderWindmillAdvanced;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorMV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorStructural;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorSorter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRelayHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformerHV;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBucketWheel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockCokeOven;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockCrusher;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavator;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockFermenter;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockLightningRod;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockRefinery;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSqueezer;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWallmount;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWatermill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmillAdvanced;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.items.ItemDrill;
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

public class ClientProxy extends CommonProxy
{
	IEManualInstance manual;

	@Override
	public void init()
	{
		//METAL
		RenderingRegistry.registerBlockHandler(new BlockRenderMetalDevices());
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
		//WOOD
		RenderingRegistry.registerBlockHandler(new BlockRenderWoodenDevices());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWoodenPost.class, new TileRenderPost());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWatermill.class, new TileRenderWatermill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindmill.class, new TileRenderWindmill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindmillAdvanced.class, new TileRenderWindmillAdvanced());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWallmount.class, new TileRenderWallmount());

		RenderingRegistry.registerBlockHandler(new BlockRenderWoodenDecoration());
		//STONE
		RenderingRegistry.registerBlockHandler(new BlockRenderStoneDevices());

		//REVOLVER
		MinecraftForgeClient.registerItemRenderer(IEContent.itemRevolver, new ItemRenderRevolver());
		RenderingRegistry.registerEntityRenderingHandler(EntityRevolvershot.class, new EntityRenderRevolvershot());
		//DRILL
		MinecraftForgeClient.registerItemRenderer(IEContent.itemDrill, new ItemRenderDrill());

		ClientEventHandler handler = new ClientEventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		FMLCommonHandler.instance().bus().register(handler);
	}

	@Override
	public void loadComplete()
	{
		manual = new IEManualInstance();
		manual.addEntry("introduction", "general",
				new ManualPages.Text(manual, "introduction0"),
				new ManualPages.Text(manual, "introduction1"),
				new ManualPages.Crafting(manual, "introductionHammer", new ItemStack(IEContent.itemTool,1,0)));
		manual.addEntry("ores", "general", 
				new ManualPages.ItemDisplay(manual, "oresCopper", new ItemStack(IEContent.blockOres,1,0),new ItemStack(IEContent.itemMetal,1,0)),
				new ManualPages.ItemDisplay(manual, "oresBauxite", new ItemStack(IEContent.blockOres,1,1),new ItemStack(IEContent.itemMetal,1,1)),
				new ManualPages.ItemDisplay(manual, "oresLead", new ItemStack(IEContent.blockOres,1,2),new ItemStack(IEContent.itemMetal,1,2)),
				new ManualPages.ItemDisplay(manual, "oresSilver", new ItemStack(IEContent.blockOres,1,3),new ItemStack(IEContent.itemMetal,1,3)),
				new ManualPages.ItemDisplay(manual, "oresNickel", new ItemStack(IEContent.blockOres,1,4),new ItemStack(IEContent.itemMetal,1,4)));
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
		manual.addEntry("oreProcessing", "general", 
				new ManualPages.CraftingMulti(manual, "oreProcessing0", (Object[])recA),
				new ManualPages.CraftingMulti(manual, "oreProcessing1", (Object[])new PositionedItemStack[][]{
					new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("dustCopper"),24,0), new PositionedItemStack(OreDictionary.getOres("dustNickel"),42,0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,2,15),78,0)},
					new PositionedItemStack[]{new PositionedItemStack(OreDictionary.getOres("dustGold"),24,0), new PositionedItemStack(OreDictionary.getOres("dustSilver"),42,0), new PositionedItemStack(new ItemStack(IEContent.itemMetal,2,16),78,0)}}));

		manual.addEntry("hemp", "general", 
				new ManualPages.ItemDisplay(manual, "hemp0", new ItemStack(IEContent.blockCrop,1,5),new ItemStack(IEContent.itemSeeds)));
		manual.addEntry("cokeoven", "general",
				new ManualPages.Text(manual, "cokeoven0"),
				new ManualPages.Crafting(manual, "cokeovenBlock", new ItemStack(IEContent.blockStoneDevice,1,1)),
				new ManualPageMultiblock(manual, "", MultiblockCokeOven.instance));
		manual.addEntry("treatedwood", "general",
				new ManualPages.Text(manual, "treatedwood0"), 
				new ManualPages.Crafting(manual, "", new ItemStack(IEContent.blockWoodenDecoration,1,0),new ItemStack(IEContent.blockWoodenDecoration,1,2),new ItemStack(IEContent.blockWoodenStair)),
				new ManualPages.Crafting(manual, "", new ItemStack(IEContent.itemMaterial,1,0),new ItemStack(IEContent.blockWoodenDecoration,1,1),new ItemStack(IEContent.blockWoodenDecoration,1,6)),
				new ManualPages.Crafting(manual, "treatedwoodPost0", new ItemStack(IEContent.blockWoodenDevice,1,0)),
				new ManualPages.Text(manual, "treatedwoodPost1"));
		manual.addEntry("blastfurnace", "general",
				new ManualPages.Text(manual, "blastfurnace0"),
				new ManualPages.Crafting(manual, "blastfurnaceBlock", new ItemStack(IEContent.blockStoneDevice,1,2)),
				new ManualPageMultiblock(manual, "", MultiblockBlastFurnace.instance));
		manual.addEntry("steelconstruction", "general",
				new ManualPages.Text(manual, "steelconstruction0"),
				new ManualPages.Crafting(manual, "", new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_fence),new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_scaffolding),new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_structuralArm)),
				new ManualPages.Crafting(manual, "", new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_wallMount)),
				new ManualPages.Crafting(manual, "steelconstruction1", new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_connectorStructural)),
				new ManualPages.Crafting(manual, "", new ItemStack(IEContent.itemWireCoil,1,3),new ItemStack(IEContent.itemWireCoil,1,4)));
		manual.addEntry("multiblocks", "general",
				new ManualPages.Text(manual, "multiblocks0"),
				new ManualPages.Crafting(manual, "", new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_lightEngineering),new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_heavyEngineering)),
				new ManualPages.Crafting(manual, "", new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_generator),new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_radiator)));
		Map<String,Integer> sortedMap = DieselHandler.getPlantoilValuesSorted(true);
		String[][] table = formatToTable_ItemIntHashmap(sortedMap,"mB");	
		sortedMap = DieselHandler.getEthanolValuesSorted(true);
		String[][] table2 = formatToTable_ItemIntHashmap(sortedMap,"mB");
		manual.addEntry("biodiesel", "general",
				new ManualPages.Text(manual, "biodiesel0"),
				new ManualPages.Crafting(manual, "", new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_squeezer),new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_fermenter)),
				new ManualPageMultiblock(manual, "biodiesel1", MultiblockSqueezer.instance),
				new ManualPages.Table(manual, "biodiesel1T", table, false),
				new ManualPageMultiblock(manual, "biodiesel2", MultiblockFermenter.instance),
				new ManualPages.Table(manual, "biodiesel2T", table2, false),
				new ManualPageMultiblock(manual, "biodiesel3", MultiblockRefinery.instance),
				new ManualPages.Text(manual, "biodiesel4"));
		String[][][] multiTables = formatToTable_ExcavatorMinerals();
		ArrayList<IManualPage> pages = new ArrayList();
		pages.add(new ManualPages.Text(manual, "minerals0"));
		pages.add(new ManualPages.Crafting(manual, "minerals1", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_sampleDrill)));
		pages.add(new ManualPages.Text(manual, "minerals2"));
		for(String[][] minTable : multiTables)
			pages.add(new ManualPages.Table(manual, "", minTable,true));
		manual.addEntry("minerals", "general", pages.toArray(new IManualPage[0]));

		manual.addEntry("wiring", "energy",
				new ManualPages.Text(manual, "wiring0"), 
				new ManualPages.Crafting(manual, "wiring1", new ItemStack(IEContent.itemWireCoil,1,OreDictionary.WILDCARD_VALUE)),
				new ManualPages.Image(manual, "wiring2", "immersiveengineering:textures/misc/wiring.png;0;0;110;40", "immersiveengineering:textures/misc/wiring.png;0;40;110;30"),
				new ManualPages.Image(manual, "wiring3", "immersiveengineering:textures/misc/wiring.png;0;70;110;60", "immersiveengineering:textures/misc/wiring.png;0;130;110;60"),
				new ManualPages.CraftingMulti(manual, "wiringConnector", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_connectorLV),new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_connectorMV),new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_relayHV),new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_connectorHV)),
				new ManualPages.CraftingMulti(manual, "wiringCapacitor", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_capacitorLV),new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_capacitorMV),new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_capacitorHV)),
				new ManualPages.CraftingMulti(manual, "wiringTransformer0", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_transformer),new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_transformerHV)),
				new ManualPages.Text(manual, "wiringTransformer1"), 
				new ManualPages.Crafting(manual, "wiringCutters", new ItemStack(IEContent.itemTool,1,1)),
				new ManualPages.Crafting(manual, "wiringVoltmeter", new ItemStack(IEContent.itemTool,1,2)));
		manual.addEntry("generator", "energy", 
				new ManualPages.Crafting(manual, "generator0", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_dynamo)),
				new ManualPages.CraftingMulti(manual, "generatorWindmill", new ItemStack(IEContent.blockWoodenDevice,1,2),new ItemStack(IEContent.itemMaterial,1,2)),
				new ManualPages.CraftingMulti(manual, "generatorWatermill", new ItemStack(IEContent.blockWoodenDevice,1,1),new ItemStack(IEContent.itemMaterial,1,1)),
				new ManualPages.CraftingMulti(manual, "generatorWindmillImproved", new ItemStack(IEContent.blockWoodenDevice,1,3),new ItemStack(IEContent.itemMaterial,1,4),new ItemStack(IEContent.itemMaterial,1,5)));
		sortedMap = ThermoelectricHandler.getThermalValuesSorted(true);
		table = formatToTable_ItemIntHashmap(sortedMap,"K");	
		manual.addEntry("thermoElectric", "energy", 
				new ManualPages.Crafting(manual, "thermoElectric0", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_thermoelectricGen)),
				new ManualPages.Table(manual, "thermoElectric1", table, false));
		manual.addEntry("highvoltage", "energy",
				new ManualPages.Text(manual, "highvoltage0"),
				new ManualPages.Crafting(manual, "", new ItemStack(IEContent.blockMetalDevice,1,8),new ItemStack(IEContent.blockMetalDevice,1,4)),
				new ManualPages.Crafting(manual, "", new ItemStack(IEContent.blockMetalDevice,1,5),new ItemStack(IEContent.blockMetalDevice,1,7)));
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
		manual.addEntry("dieselgen", "energy",
				new ManualPages.Text(manual, "dieselgen0"),
				new ManualPageMultiblock(manual, "dieselgen1", MultiblockDieselGenerator.instance),
				new ManualPages.Text(manual, "dieselgen2"),
				new ManualPages.Table(manual, "dieselgen3", table, false)
				);

		manual.addEntry("lightningrod", "energy",
				new ManualPages.Crafting(manual, "lightningrod0",  new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_lightningRod)),
				new ManualPageMultiblock(manual, "lightningrod1", MultiblockLightningRod.instance));

		manual.addEntry("conveyor", "machines",
				new ManualPages.Crafting(manual, "conveyor0", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_conveyorBelt)),
				new ManualPages.Text(manual, "conveyor1"));
		manual.addEntry("furnaceHeater", "machines",
				new ManualPages.Crafting(manual, "furnaceHeater0", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_furnaceHeater)),
				new ManualPages.Text(manual, "furnaceHeater1"),
				new ManualPages.Text(manual, "furnaceHeater2"));
		manual.addEntry("sorter", "machines",
				new ManualPages.Crafting(manual, "sorter0", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_sorter)),
				new ManualPages.Text(manual, "sorter1"));
		manual.addEntry("drill", "machines",
				new ManualPages.Crafting(manual, "drill0", new ItemStack(IEContent.itemDrill,1,0)),
				new ManualPages.Crafting(manual, "drill1", new ItemStack(IEContent.itemDrillhead,1,0)),
				new ManualPages.Crafting(manual, "drill2", new ItemStack(IEContent.itemToolUpgrades,1,0)),
				new ManualPages.Crafting(manual, "drill3", new ItemStack(IEContent.itemToolUpgrades,1,1)),
				new ManualPages.Crafting(manual, "drill4", new ItemStack(IEContent.itemToolUpgrades,1,2)));
		manual.addEntry("crusher", "machines",
				new ManualPageMultiblock(manual, "crusher0", MultiblockCrusher.instance),
				new ManualPages.Text(manual, "crusher1"));
		manual.addEntry("excavator", "machines",
				new ManualPageMultiblock(manual, "excavator0", MultiblockExcavator.instance),
				new ManualPageMultiblock(manual, "", MultiblockBucketWheel.instance),
				new ManualPages.Text(manual, "excavator1"));
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if(ID==Lib.GUIID_CokeOven && world.getTileEntity(x, y, z) instanceof TileEntityCokeOven)
			return new GuiCokeOven(player.inventory, (TileEntityCokeOven) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_BlastFurnace && world.getTileEntity(x, y, z) instanceof TileEntityBlastFurnace)
			return new GuiBlastFurnace(player.inventory, (TileEntityBlastFurnace) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_Revolver && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemRevolver)
			return new GuiRevolver(player.inventory, world);
		if(ID==Lib.GUIID_Manual && manual!=null && player.getCurrentEquippedItem()!=null && OreDictionary.itemMatches(new ItemStack(IEContent.itemTool,1,3), player.getCurrentEquippedItem(), false))
			return manual.getGui();
		if(ID==Lib.GUIID_WoodenCrate && world.getTileEntity(x, y, z) instanceof TileEntityWoodenCrate)
			return new GuiCrate(player.inventory, (TileEntityWoodenCrate) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_Squeezer && world.getTileEntity(x, y, z) instanceof TileEntitySqueezer)
			return new GuiSqueezer(player.inventory, (TileEntitySqueezer) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_Fermenter && world.getTileEntity(x, y, z) instanceof TileEntityFermenter)
			return new GuiFermenter(player.inventory, (TileEntityFermenter) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_Sorter && world.getTileEntity(x, y, z) instanceof TileEntityConveyorSorter)
			return new GuiSorter(player.inventory, (TileEntityConveyorSorter) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_Refinery && world.getTileEntity(x, y, z) instanceof TileEntityRefinery)
			return new GuiRefinery(player.inventory, (TileEntityRefinery) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_Drill && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemDrill)
			return new GuiDrill(player.inventory, world);
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
			else
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
				EntityFX particleMysterious = new EntityFXItemParts(tile.getWorldObj(), stack, tile.getWorldObj().rand.nextInt(16), x,y,z, mX,mY,mZ);
				Minecraft.getMinecraft().effectRenderer.addEffect(particleMysterious);
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

				EntityFX particleMysterious = new EntityFXItemParts(tile.getWorldObj(), stack, tile.getWorldObj().rand.nextInt(16), x,y,z, mX,mY,mZ);
				particleMysterious.noClip=true;
				particleMysterious.multipleParticleScaleBy(2);
				Minecraft.getMinecraft().effectRenderer.addEffect(particleMysterious);
			}
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
				else
				{
					int lIndx = sortedMapArray[i].getKey().lastIndexOf("::");
					if(lIndx>0)
					{
						String key = sortedMapArray[i].getKey().substring(0,lIndx);
						Item keyItem = GameData.getItemRegistry().getObject(key);
						Block keyBlock = keyItem instanceof ItemBlock?Block.getBlockFromName(key):null;
						if(keyBlock!=null && !Blocks.air.equals(keyBlock))
						{
							int reqMeta = Integer.parseInt(sortedMapArray[i].getKey().substring(lIndx+2));
							item = new ItemStack(keyBlock,1,reqMeta).getDisplayName();
						}
						else if(keyItem!=null)
						{
							int reqMeta = Integer.parseInt(sortedMapArray[i].getKey().substring(lIndx+2));
							item = new ItemStack(keyItem,1,reqMeta).getDisplayName();
						}
					}
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
			for(int j=0; j<minerals[i].recalculatedOres.length; j++)
				if(!OreDictionary.getOres(minerals[i].recalculatedOres[j]).isEmpty())
				{
					ItemStack stackOre = OreDictionary.getOres(minerals[i].recalculatedOres[j]).get(0);
					multiTables[curTable][i][1] += stackOre.getDisplayName()+" "+( Utils.formatDouble(minerals[i].recalculatedChances[j]*100,"#.00")+"%" )+(j<minerals[i].recalculatedOres.length-1?"\n":""); 
					totalLines++;
				}
			if(i<minerals.length-1 && totalLines+minerals[i+1].recalculatedOres.length>=13)
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