package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Extinguish;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.DefaultFurnaceAdapter;
import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.common.blocks.*;
import blusunrize.immersiveengineering.common.blocks.BlockFakeLight.TileEntityFakeLight;
import blusunrize.immersiveengineering.common.blocks.cloth.BlockClothDevice;
import blusunrize.immersiveengineering.common.blocks.cloth.TileEntityBalloon;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.*;
import blusunrize.immersiveengineering.common.blocks.plant.BlockIECrop;
import blusunrize.immersiveengineering.common.blocks.plant.BlockTypes_Hemp;
import blusunrize.immersiveengineering.common.blocks.stone.*;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.crafting.IEFuelHandler;
import blusunrize.immersiveengineering.common.entities.*;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.IEVillagerTrades;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import blusunrize.immersiveengineering.common.world.VillageEngineersHouse;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityVillager.ListItemForEmeralds;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class IEContent
{
	public static ArrayList<Block> registeredIEBlocks = new ArrayList<Block>();
	public static BlockIEBase blockOre;
	public static BlockIEBase blockStorage;
	public static BlockIESlab blockStorageSlabs;
	public static BlockIEBase blockStoneDecoration;
	public static BlockIEBase blockStoneDecorationSlabs;
	public static Block blockStoneStair_hempcrete;
	public static Block blockStoneStair_concrete0;
	public static Block blockStoneStair_concrete1;
	public static Block blockStoneStair_concrete2;
	public static BlockIEBase blockStoneDevice;

	public static BlockIEBase blockTreatedWood;
	public static BlockIEBase blockTreatedWoodSlabs;
	public static Block blockWoodenStair;
	public static Block blockWoodenStair1;
	public static Block blockWoodenStair2;
	public static BlockIEBase blockWoodenDecoration;
	public static BlockIEBase blockWoodenDevice0;
	public static BlockIEBase blockWoodenDevice1;
	public static Block blockCrop;
	public static BlockIEBase blockClothDevice;
	public static Block blockFakeLight;

	public static BlockIEBase blockSheetmetal;
	public static BlockIEBase blockSheetmetalSlabs;
	public static BlockIEBase blockMetalDecoration0;
	public static BlockIEBase blockMetalDecoration1;
	public static BlockIEBase blockMetalDecoration2;
	public static BlockIEBase blockConnectors;
	public static BlockIEBase blockMetalDevice0;
	public static BlockIEBase blockMetalDevice1;
	public static BlockIEBase blockConveyor;
	public static BlockIEBase blockMetalMultiblock;

	public static ArrayList<Item> registeredIEItems = new ArrayList<Item>();
	public static ItemIEBase itemMaterial;
	public static ItemIEBase itemMetal;
	public static ItemIEBase itemTool;
	public static ItemIEBase itemToolbox;
	public static ItemIEBase itemFluidContainers;
	public static ItemIEBase itemWireCoil;
	public static ItemIEBase itemSeeds;
	public static ItemIEBase itemDrill;
	public static ItemIEBase itemDrillhead;
	public static ItemIEBase itemJerrycan;
	public static ItemIEBase itemMold;
	public static ItemIEBase itemBlueprint;
	public static ItemIEBase itemRevolver;
	public static ItemIEBase itemBullet;
	public static ItemIEBase itemChemthrower;
	public static ItemIEBase itemRailgun;
	public static ItemIEBase itemSkyhook;
	public static ItemIEBase itemToolUpgrades;
	public static ItemIEBase itemShader;
	public static ItemIEBase itemShaderBag;
	public static Item itemEarmuffs;
	public static ItemIEBase itemCoresample;
	public static ItemIEBase itemGraphiteElectrode;
	public static ItemFaradaySuit[] itemsFaradaySuit = new ItemFaradaySuit[4];
	public static ItemIEBase itemFluorescentTube;

	public static ItemIEBase itemFakeIcons;

	//	public static BlockIEBase blockClothDevice;
	public static Fluid fluidCreosote;
	public static Fluid fluidPlantoil;
	public static Fluid fluidEthanol;
	public static Fluid fluidBiodiesel;

	public static VillagerRegistry.VillagerProfession villagerProfession_engineer;

	static{
		FluidRegistry.enableUniversalBucket();
	}

	public static void preInit()
	{
		blockOre = (BlockIEBase)new BlockIEBase("ore",Material.ROCK, PropertyEnum.create("type", BlockTypes_Ore.class), ItemBlockIEBase.class).setOpaque(true).setHardness(3.0F).setResistance(5.0F);
		blockStorage = (BlockIEBase)new BlockIEBase("storage",Material.IRON, PropertyEnum.create("type", BlockTypes_MetalsIE.class), ItemBlockIEBase.class).setOpaque(true).setHardness(5.0F).setResistance(10.0F);
		blockStorageSlabs = (BlockIESlab)new BlockIESlab("storageSlab",Material.IRON, PropertyEnum.create("type", BlockTypes_MetalsIE.class)).setHardness(5.0F).setResistance(10.0F);
		int insGlassMeta = BlockTypes_StoneDecoration.INSULATING_GLASS.getMeta();
		blockStoneDecoration = (BlockIEBase)new BlockIEBase("stoneDecoration",Material.ROCK, PropertyEnum.create("type", BlockTypes_StoneDecoration.class), ItemBlockIEBase.class).setMetaBlockLayer(insGlassMeta, BlockRenderLayer.TRANSLUCENT).setMetaLightOpacity(insGlassMeta, 0).setHardness(2.0F).setResistance(10.0F);
		blockStoneDecorationSlabs = (BlockIEBase)new BlockIESlab("stoneDecorationSlab",Material.ROCK, PropertyEnum.create("type", BlockTypes_StoneDecoration.class)).setMetaHidden(3,8).setHardness(2.0F).setResistance(10.0F);
		blockStoneStair_hempcrete = new BlockIEStairs("stoneDecorationStairs_hempcrete",blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.HEMPCRETE.getMeta()));
		blockStoneStair_concrete0 = new BlockIEStairs("stoneDecorationStairs_concrete",blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE.getMeta()));
		blockStoneStair_concrete1 = new BlockIEStairs("stoneDecorationStairs_concrete_tile",blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE_TILE.getMeta()));
		blockStoneStair_concrete2 = new BlockIEStairs("stoneDecorationStairs_concrete_leaded",blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE_LEADED.getMeta()));

		blockStoneDevice = new BlockStoneDevice();

		blockTreatedWood = (BlockIEBase)new BlockIEBase("treatedWood",Material.WOOD, PropertyEnum.create("type", BlockTypes_TreatedWood.class), ItemBlockIEBase.class).setOpaque(true).setHasFlavour().setHardness(2.0F).setResistance(5.0F);
		blockTreatedWoodSlabs = (BlockIESlab)new BlockIESlab("treatedWoodSlab",Material.WOOD, PropertyEnum.create("type", BlockTypes_TreatedWood.class)).setHasFlavour().setHardness(2.0F).setResistance(5.0F);
		blockWoodenStair = new BlockIEStairs("treatedWoodStairs0",blockTreatedWood.getStateFromMeta(0)).setHasFlavour(true);
		blockWoodenStair1 = new BlockIEStairs("treatedWoodStairs1",blockTreatedWood.getStateFromMeta(1)).setHasFlavour(true);
		blockWoodenStair2 = new BlockIEStairs("treatedWoodStairs2",blockTreatedWood.getStateFromMeta(2)).setHasFlavour(true);

		blockWoodenDecoration = new BlockWoodenDecoration();
		blockWoodenDevice0 = new BlockWoodenDevice0();
		blockWoodenDevice1 = new BlockWoodenDevice1();
		blockCrop = new BlockIECrop("hemp", PropertyEnum.create("type", BlockTypes_Hemp.class));
		blockClothDevice = new BlockClothDevice();
		blockFakeLight = new BlockFakeLight();

		blockSheetmetal = (BlockIEBase)new BlockIEBase("sheetmetal",Material.IRON, PropertyEnum.create("type", BlockTypes_MetalsAll.class), ItemBlockIEBase.class).setOpaque(true).setMetaHidden(0,3,4,5,7,10).setHardness(3.0F).setResistance(10.0F);
		blockSheetmetalSlabs = (BlockIESlab)new BlockIESlab("sheetmetalSlab",Material.IRON, PropertyEnum.create("type", BlockTypes_MetalsAll.class)).setMetaHidden(0,3,4,5,7,10).setHardness(3.0F).setResistance(10.0F);
		blockMetalDecoration0 = (BlockIEBase)new BlockIEBase("metalDecoration0",Material.IRON, PropertyEnum.create("type", BlockTypes_MetalDecoration0.class), ItemBlockIEBase.class).setHardness(3.0F).setResistance(15.0F);
		blockMetalDecoration1 = new BlockMetalDecoration1();
		blockMetalDecoration2 = new BlockMetalDecoration2();
		blockConnectors = new BlockConnector();
		blockMetalDevice0 = new BlockMetalDevice0();
		blockMetalDevice1 = new BlockMetalDevice1();
		blockConveyor = new BlockConveyor();
		blockMetalMultiblock = new BlockMetalMultiblocks();

		itemMaterial = new ItemIEBase("material",64,
				"stickTreated","stickIron","stickSteel","stickAluminum",
				"hempFiber","hempFabric",
				"coalCoke","slag",
				"componentIron","componentSteel",
				"waterwheelSegment","windmillBlade","windmillBladeAdvanced",
				"woodenGrip","gunpartBarrel","gunpartDrum","gunpartHammer",
				"dustCoke","dustHOPGraphite","ingotHOPGraphite",
				"wireCopper","wireElectrum","wireAluminum","wireSteel");
		itemMetal = new ItemIEBase("metal",64,
				"ingotCopper","ingotAluminum","ingotLead","ingotSilver","ingotNickel","ingotUranium","ingotConstantan","ingotElectrum","ingotSteel",
				"dustCopper","dustAluminum","dustLead","dustSilver","dustNickel","dustUranium","dustConstantan","dustElectrum","dustSteel","dustIron","dustGold",
				"nuggetCopper","nuggetAluminum","nuggetLead","nuggetSilver","nuggetNickel","nuggetUranium","nuggetConstantan","nuggetElectrum","nuggetSteel","nuggetIron",
				"plateCopper","plateAluminum","plateLead","plateSilver","plateNickel","plateUranium","plateConstantan","plateElectrum","plateSteel","plateIron","plateGold"
		).setMetaHidden(33,34,35,40);
		itemTool = new ItemIETool();
		itemToolbox = new ItemToolbox();
		itemFluidContainers = new ItemIEBase("fluidContainers", 64, "bottleCreosote","bucketCreosote",  "bottlePlantoil","bucketPlantoil",  "bottleEthanol","bucketEthanol", "bottleBiodiesel","bucketBiodiesel")
		{
			@Override
			public boolean hasContainerItem(ItemStack stack)
			{
				return true;
			}
			@Override
			public ItemStack getContainerItem(ItemStack stack)
			{
				return stack.getItemDamage()%2==0?new ItemStack(Items.GLASS_BOTTLE): new ItemStack(Items.BUCKET);
			}
			@Override
			public int getItemStackLimit(ItemStack stack)
			{
				return stack.getItemDamage()%2==0?16:1;
			}
			@Override
			public boolean isMetaHidden(int meta)
			{
				return meta%2==1;
			}
		};
		itemWireCoil = new ItemWireCoil();
		WireType.ieWireCoil = itemWireCoil;
		itemSeeds = new ItemIESeed(blockCrop,"hemp");
		MinecraftForge.addGrassSeed(new ItemStack(itemSeeds), 5);
		itemDrill = new ItemDrill();
		itemDrillhead = new ItemDrillhead();
		itemJerrycan = new ItemJerrycan();
		itemMold = new ItemIEBase("mold", 1, "plate","gear","rod","bulletCasing","wire").setMetaHidden(1);
		itemBlueprint = new ItemEngineersBlueprint().setRegisterSubModels(false);
		itemRevolver = new ItemRevolver();
		itemBullet = new ItemBullet();
		itemChemthrower = new ItemChemthrower();
		itemRailgun = new ItemRailgun();
		itemSkyhook = new ItemSkyhook();
		itemToolUpgrades = new ItemToolUpgrade();
		itemShader = new ItemShader();
		itemShaderBag = new ItemShaderBag();
		itemEarmuffs = new ItemEarmuffs();
		itemCoresample = new ItemCoresample();
		itemGraphiteElectrode = new ItemGraphiteElectrode();
		ItemFaradaySuit.mat = EnumHelper.addArmorMaterial("faradayChains", "immersiveEngineering:faradaySuit", 1, new int[]{1, 3, 2, 1}, 0, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0);
		for(int i = 0;i<itemsFaradaySuit.length;i++)
			itemsFaradaySuit[i] = new ItemFaradaySuit(EntityEquipmentSlot.values()[2+i]);
		itemFluorescentTube = new ItemFluorescentTube();

		itemFakeIcons = new ItemIEBase("fakeIcon", 1, "birthday","lucky")
		{
			@Override
			public void getSubItems(Item item, CreativeTabs tab, List list)
			{
			}
		};

		//		blockMetalDevice = new BlockMetalDevices();
		//		blockMetalDevice2 = new BlockMetalDevices2();
		//		blockMetalDecoration = new BlockMetalDecoration();
		//		blockMetalMultiblocks = new BlockMetalMultiblocks();
		//		blockWoodenDevice = new BlockWoodenDevices().setFlammable(true);
		//		blockWoodenDecoration = new BlockWoodenDecoration().setFlammable(true);
		//		blockStoneDevice = new BlockStoneDevices();
		//		blockStoneDecoration = new BlockStoneDecoration();
		//		blockConcreteStair = new BlockIEStairs("concreteStairs",blockStoneDecoration,4);
		//		blockConcreteTileStair = new BlockIEStairs("concreteTileStairs",blockStoneDecoration,5);
		//		blockClothDevice = new BlockClothDevices();
		//

		fluidCreosote = new Fluid("creosote", new ResourceLocation("immersiveengineering:blocks/fluid/creosote_still"), new ResourceLocation("immersiveengineering:blocks/fluid/creosote_flow")).setDensity(800).setViscosity(3000);
		FluidRegistry.registerFluid(fluidCreosote);
		FluidRegistry.addBucketForFluid(fluidCreosote);
		fluidPlantoil = new Fluid("plantoil", new ResourceLocation("immersiveengineering:blocks/fluid/plantoil_still"), new ResourceLocation("immersiveengineering:blocks/fluid/plantoil_flow")).setDensity(925).setViscosity(2000);
		FluidRegistry.registerFluid(fluidPlantoil);
		FluidRegistry.addBucketForFluid(fluidPlantoil);
		fluidEthanol = new Fluid("ethanol", new ResourceLocation("immersiveengineering:blocks/fluid/ethanol_still"), new ResourceLocation("immersiveengineering:blocks/fluid/ethanol_flow")).setDensity(789).setViscosity(1000);
		FluidRegistry.registerFluid(fluidEthanol);
		FluidRegistry.addBucketForFluid(fluidEthanol);
		fluidBiodiesel = new Fluid("biodiesel", new ResourceLocation("immersiveengineering:blocks/fluid/biodiesel_still"), new ResourceLocation("immersiveengineering:blocks/fluid/biodiesel_flow")).setDensity(789).setViscosity(1000);
		FluidRegistry.registerFluid(fluidBiodiesel);
		FluidRegistry.addBucketForFluid(fluidBiodiesel);

		//Ore Dict
		registerToOreDict("ore", blockOre);
		registerToOreDict("block", blockStorage);
		registerToOreDict("slab", blockStorageSlabs);
		registerToOreDict("blockSheetmetal", blockSheetmetal);
		registerToOreDict("slabSheetmetal", blockSheetmetalSlabs);
		registerToOreDict("", itemMetal);
		//		registerOre("Cupronickel",	null,new ItemStack(itemMetal,1,5),new ItemStack(itemMetal,1,15),new ItemStack(blockStorage,1,5),new ItemStack(itemMetal,1,27));
		//
		//		OreDictionary.registerOre("seedIndustrialHemp", new ItemStack(itemSeeds));
		OreDictionary.registerOre("stickTreatedWood", new ItemStack(itemMaterial,1,0));
		OreDictionary.registerOre("stickIron", new ItemStack(itemMaterial,1,1));
		OreDictionary.registerOre("stickSteel", new ItemStack(itemMaterial,1,2));
		OreDictionary.registerOre("stickAluminum", new ItemStack(itemMaterial,1,3));
		OreDictionary.registerOre("fabricHemp", new ItemStack(itemMaterial,1,5));
		OreDictionary.registerOre("fuelCoke", new ItemStack(itemMaterial,1,6));
		OreDictionary.registerOre("itemSlag", new ItemStack(itemMaterial,1,7));
		OreDictionary.registerOre("dustCoke", new ItemStack(itemMaterial,1,17));
		OreDictionary.registerOre("dustHOPGraphite", new ItemStack(itemMaterial,1,18));
		OreDictionary.registerOre("ingotHOPGraphite", new ItemStack(itemMaterial,1,19));
		OreDictionary.registerOre("wireCopper", new ItemStack(itemMaterial,1,20));
		OreDictionary.registerOre("wireElectrum", new ItemStack(itemMaterial,1,21));
		OreDictionary.registerOre("wireAluminum", new ItemStack(itemMaterial,1,22));
		OreDictionary.registerOre("wireSteel", new ItemStack(itemMaterial,1,23));

		OreDictionary.registerOre("plankTreatedWood", new ItemStack(blockTreatedWood,1,OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("slabTreatedWood", new ItemStack(blockTreatedWoodSlabs,1,OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("fenceTreatedWood", new ItemStack(blockWoodenDecoration,1,BlockTypes_WoodenDecoration.FENCE.getMeta()));
		OreDictionary.registerOre("scaffoldingTreatedWood", new ItemStack(blockWoodenDecoration,1,BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta()));
		OreDictionary.registerOre("blockFuelCoke", new ItemStack(blockStoneDecoration,1,BlockTypes_StoneDecoration.COKE.getMeta()));
		OreDictionary.registerOre("concrete", new ItemStack(blockStoneDecoration,1,BlockTypes_StoneDecoration.CONCRETE.getMeta()));
		OreDictionary.registerOre("concrete", new ItemStack(blockStoneDecoration,1,BlockTypes_StoneDecoration.CONCRETE_TILE.getMeta()));
		OreDictionary.registerOre("fenceSteel", new ItemStack(blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta()));
		OreDictionary.registerOre("fenceAluminum", new ItemStack(blockMetalDecoration1,1,BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta()));
		OreDictionary.registerOre("scaffoldingSteel", new ItemStack(blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()));
		OreDictionary.registerOre("scaffoldingSteel", new ItemStack(blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta()));
		OreDictionary.registerOre("scaffoldingSteel", new ItemStack(blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_2.getMeta()));
		OreDictionary.registerOre("scaffoldingAluminum", new ItemStack(blockMetalDecoration1,1,BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta()));
		OreDictionary.registerOre("scaffoldingAluminum", new ItemStack(blockMetalDecoration1,1,BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_1.getMeta()));
		OreDictionary.registerOre("scaffoldingAluminum", new ItemStack(blockMetalDecoration1,1,BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_2.getMeta()));
		//Vanilla OreDict
		OreDictionary.registerOre("bricksStone", new ItemStack(Blocks.STONEBRICK));
		OreDictionary.registerOre("blockIce", new ItemStack(Blocks.ICE));
//		OreDictionary.registerOre("blockFrostedIce", new ItemStack(Blocks.FROSTED_ICE));
		OreDictionary.registerOre("blockPackedIce", new ItemStack(Blocks.PACKED_ICE));
		OreDictionary.registerOre("craftingTableWood", new ItemStack(Blocks.CRAFTING_TABLE));
		OreDictionary.registerOre("rodBlaze", new ItemStack(Items.BLAZE_ROD));
		OreDictionary.registerOre("charcoal", new ItemStack(Items.COAL,1,1));
		//Fluid Containers
//		FluidContainerRegistry.registerFluidContainer(fluidCreosote, new ItemStack(itemFluidContainers,1,0), new ItemStack(Items.GLASS_BOTTLE));
//		FluidContainerRegistry.registerFluidContainer(fluidCreosote, new ItemStack(itemFluidContainers,1,1), new ItemStack(Items.BUCKET));
//		FluidContainerRegistry.registerFluidContainer(fluidPlantoil, new ItemStack(itemFluidContainers,1,2), new ItemStack(Items.GLASS_BOTTLE));
//		FluidContainerRegistry.registerFluidContainer(fluidPlantoil, new ItemStack(itemFluidContainers,1,3), new ItemStack(Items.BUCKET));
//		FluidContainerRegistry.registerFluidContainer(fluidEthanol, new ItemStack(itemFluidContainers,1,4), new ItemStack(Items.GLASS_BOTTLE));
//		FluidContainerRegistry.registerFluidContainer(fluidEthanol, new ItemStack(itemFluidContainers,1,5), new ItemStack(Items.BUCKET));
//		FluidContainerRegistry.registerFluidContainer(fluidBiodiesel, new ItemStack(itemFluidContainers,1,6), new ItemStack(Items.GLASS_BOTTLE));
//		FluidContainerRegistry.registerFluidContainer(fluidBiodiesel, new ItemStack(itemFluidContainers,1,7), new ItemStack(Items.BUCKET));
		//		//Mining
		blockOre.setHarvestLevel("pickaxe", 1, blockOre.getStateFromMeta(BlockTypes_Ore.COPPER.getMeta()));
		blockOre.setHarvestLevel("pickaxe", 1, blockOre.getStateFromMeta(BlockTypes_Ore.ALUMINUM.getMeta()));
		blockOre.setHarvestLevel("pickaxe", 2, blockOre.getStateFromMeta(BlockTypes_Ore.LEAD.getMeta()));
		blockOre.setHarvestLevel("pickaxe", 2, blockOre.getStateFromMeta(BlockTypes_Ore.SILVER.getMeta()));
		blockOre.setHarvestLevel("pickaxe", 2, blockOre.getStateFromMeta(BlockTypes_Ore.NICKEL.getMeta()));
		blockOre.setHarvestLevel("pickaxe", 2, blockOre.getStateFromMeta(BlockTypes_Ore.URANIUM.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 1, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.COPPER.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 1, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.ALUMINUM.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.LEAD.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.SILVER.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.NICKEL.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.URANIUM.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.CONSTANTAN.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.ELECTRUM.getMeta()));
		blockStorage.setHarvestLevel("pickaxe", 2, blockStorage.getStateFromMeta(BlockTypes_MetalsIE.STEEL.getMeta()));

		addConfiguredWorldgen(blockOre.getStateFromMeta(0), "copper");
		addConfiguredWorldgen(blockOre.getStateFromMeta(1), "bauxite");
		addConfiguredWorldgen(blockOre.getStateFromMeta(2), "lead");
		addConfiguredWorldgen(blockOre.getStateFromMeta(3), "silver");
		addConfiguredWorldgen(blockOre.getStateFromMeta(4), "nickel");
		addConfiguredWorldgen(blockOre.getStateFromMeta(5), "uranium");
	}

	public static void init()
	{
		/**TILEENTITIES*/
		registerTile(TileEntityIESlab.class);

		registerTile(TileEntityBalloon.class);

		registerTile(TileEntityCokeOven.class);
		registerTile(TileEntityBlastFurnace.class);
		registerTile(TileEntityBlastFurnaceAdvanced.class);

		registerTile(TileEntityWoodenCrate.class);
		registerTile(TileEntityWoodenBarrel.class);
		registerTile(TileEntityModWorkbench.class);
		registerTile(TileEntitySorter.class);
		registerTile(TileEntityWatermill.class);
		registerTile(TileEntityWindmill.class);
		registerTile(TileEntityWindmillAdvanced.class);
		registerTile(TileEntityWoodenPost.class);
		registerTile(TileEntityWallmount.class);

		registerTile(TileEntityLantern.class);

		registerTile(TileEntityConnectorLV.class);
		registerTile(TileEntityRelayLV.class);
		registerTile(TileEntityConnectorMV.class);
		registerTile(TileEntityRelayMV.class);
		registerTile(TileEntityConnectorHV.class);
		registerTile(TileEntityRelayHV.class);
		registerTile(TileEntityConnectorStructural.class);
		registerTile(TileEntityTransformer.class);
		registerTile(TileEntityTransformerHV.class);
		registerTile(TileEntityBreakerSwitch.class);
		registerTile(TileEntityRedstoneBreaker.class);
		registerTile(TileEntityEnergyMeter.class);

		registerTile(TileEntityCapacitorLV.class);
		registerTile(TileEntityCapacitorMV.class);
		registerTile(TileEntityCapacitorHV.class);
		registerTile(TileEntityCapacitorCreative.class);
		registerTile(TileEntityMetalBarrel.class);
		registerTile(TileEntityFluidPump.class);

		registerTile(TileEntityBlastFurnacePreheater.class);
		registerTile(TileEntityFurnaceHeater.class);
		registerTile(TileEntityDynamo.class);
		registerTile(TileEntityThermoelectricGen.class);
		registerTile(TileEntityElectricLantern.class);
		registerTile(TileEntityChargingStation.class);
		registerTile(TileEntityFluidPipe.class);
		registerTile(TileEntitySampleDrill.class);
		registerTile(TileEntityTeslaCoil.class);
		registerTile(TileEntityFloodlight.class);

		registerTile(TileEntityConveyorBelt.class);
		registerTile(TileEntityConveyorVertical.class);

		registerTile(TileEntityMetalPress.class);
		registerTile(TileEntityCrusher.class);
		registerTile(TileEntitySheetmetalTank.class);
		registerTile(TileEntitySilo.class);
		registerTile(TileEntityAssembler.class);
		//		registerTile(TileEntityAutoWorkbench.class);
		//		registerTile(TileEntityBottlingMachine.class);
		//		registerTile(TileEntityLightningRod.class);
		registerTile(TileEntitySqueezer.class);
		registerTile(TileEntityFermenter.class);
		registerTile(TileEntityRefinery.class);
		registerTile(TileEntityDieselGenerator.class);
		registerTile(TileEntityBucketWheel.class);
		registerTile(TileEntityExcavator.class);
		registerTile(TileEntityArcFurnace.class);
		//
		//		registerTile(TileEntitySkycrateDispenser.class);
		//		registerTile(TileEntityFloodlight.class);
		//
		registerTile(TileEntityFakeLight.class);



		/**ENTITIES*/
		int i = 0;
		EntityRegistry.registerModEntity(EntityRevolvershot.class, "revolverShot", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntitySkylineHook.class, "skylineHook", i++, ImmersiveEngineering.instance, 64, 1, true);
		//EntityRegistry.registerModEntity(EntitySkycrate.class, "skylineCrate", 2, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntityRevolvershotHoming.class, "revolverShotHoming", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntityWolfpackShot.class, "revolverShotWolfpack", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntityChemthrowerShot.class, "chemthrowerShot", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntityRailgunShot.class, "railgunShot", i++, ImmersiveEngineering.instance, 64, 5, true);
		EntityRegistry.registerModEntity(EntityRevolvershotFlare.class, "revolverShotFlare", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntityIEExplosive.class, "explosive", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(EntityFluorescentTube.class, "fluorescentTube", i++, ImmersiveEngineering.instance, 64, 1, true);

		/**SMELTING*/
		IERecipes.initFurnaceRecipes();

		/**CRAFTING*/
		IERecipes.initCraftingRecipes();

		/**BLUEPRINTS*/
		IERecipes.initBlueprintRecipes();

		/**POTIONS*/
		IEPotions.init();

		CokeOvenRecipe.addRecipe(new ItemStack(itemMaterial,1,6), new ItemStack(Items.COAL), 1800, 500);
		CokeOvenRecipe.addRecipe(new ItemStack(blockStoneDecoration,1,3), "blockCoal", 1800*9, 5000);
		CokeOvenRecipe.addRecipe(new ItemStack(Items.COAL,1,1), "logWood", 900, 250);
		BlastFurnaceRecipe.addRecipe(new ItemStack(itemMetal,1,8), "ingotIron", 1200, new ItemStack(itemMaterial,1,7));
		BlastFurnaceRecipe.addRecipe(new ItemStack(blockStorage,1,8), "blockIron", 1200*9, new ItemStack(itemMaterial,9,7));

		BlastFurnaceRecipe.addBlastFuel("fuelCoke", 1200);
		BlastFurnaceRecipe.addBlastFuel("blockFuelCoke", 1200*10);
		BlastFurnaceRecipe.addBlastFuel("charcoal", 300);
		BlastFurnaceRecipe.addBlastFuel("blockCharcoal", 300*10);
		GameRegistry.registerFuelHandler(new IEFuelHandler());

		IERecipes.initCrusherRecipes();

		IERecipes.initArcSmeltingRecipes();

		ItemStack shoddyElectrode = new ItemStack(itemGraphiteElectrode);
		shoddyElectrode.setItemDamage(ItemGraphiteElectrode.electrodeMaxDamage/2);
		MetalPressRecipe.addRecipe(shoddyElectrode, "ingotHOPGraphite", new ItemStack(IEContent.itemMold,1,2), 4800).setInputSize(4);

		DieselHandler.registerFuel(fluidBiodiesel, 125);
		DieselHandler.registerFuel(FluidRegistry.getFluid("fuel"), 375);
		DieselHandler.registerFuel(FluidRegistry.getFluid("diesel"), 175);
		DieselHandler.registerDrillFuel(fluidBiodiesel);
		DieselHandler.registerDrillFuel(FluidRegistry.getFluid("fuel"));
		DieselHandler.registerDrillFuel(FluidRegistry.getFluid("diesel"));

		ChemthrowerHandler.registerEffect(FluidRegistry.WATER, new ChemthrowerEffect_Extinguish());
		ChemthrowerHandler.registerEffect(fluidCreosote, new ChemthrowerEffect_Potion(null,0, IEPotions.flammable,140,0));
		ChemthrowerHandler.registerFlammable(fluidCreosote);
		ChemthrowerHandler.registerEffect(fluidBiodiesel, new ChemthrowerEffect_Potion(null,0, IEPotions.flammable,140,1));
		ChemthrowerHandler.registerFlammable(fluidBiodiesel);
		ChemthrowerHandler.registerFlammable(fluidEthanol);
		ChemthrowerHandler.registerEffect("oil", new ChemthrowerEffect_Potion(null,0, new PotionEffect(IEPotions.flammable,140,0),new PotionEffect(Potion.getPotionFromResourceLocation("blindness"),80,1)));
		ChemthrowerHandler.registerFlammable("oil");
		ChemthrowerHandler.registerEffect("fuel", new ChemthrowerEffect_Potion(null,0, IEPotions.flammable,100,1));
		ChemthrowerHandler.registerFlammable("fuel");
		ChemthrowerHandler.registerEffect("diesel", new ChemthrowerEffect_Potion(null,0, IEPotions.flammable,140,1));
		ChemthrowerHandler.registerFlammable("diesel");
		ChemthrowerHandler.registerEffect("kerosene", new ChemthrowerEffect_Potion(null,0, IEPotions.flammable,100,1));
		ChemthrowerHandler.registerFlammable("kerosene");
		ChemthrowerHandler.registerEffect("biofuel", new ChemthrowerEffect_Potion(null,0, IEPotions.flammable,140,1));
		ChemthrowerHandler.registerFlammable("biofuel");
		ChemthrowerHandler.registerEffect("rocket_fuel", new ChemthrowerEffect_Potion(null,0, IEPotions.flammable,60,2));
		ChemthrowerHandler.registerFlammable("rocket_fuel");

		RailgunHandler.registerProjectileProperties(new ComparableItemStack("stickIron"), 10, 1.25).setColourMap(new int[][]{{0xd8d8d8,0xd8d8d8,0xd8d8d8,0xa8a8a8,0x686868,0x686868}});
		RailgunHandler.registerProjectileProperties(new ComparableItemStack("stickAluminum"), 9, 1.05).setColourMap(new int[][]{{0xd8d8d8,0xd8d8d8,0xd8d8d8,0xa8a8a8,0x686868,0x686868}});
		RailgunHandler.registerProjectileProperties(new ComparableItemStack("stickSteel"), 12, 1.25).setColourMap(new int[][]{{0xb4b4b4,0xb4b4b4,0xb4b4b4,0x7a7a7a,0x555555,0x555555}});
		RailgunHandler.registerProjectileProperties(new ComparableItemStack(new ItemStack(itemGraphiteElectrode)), 16, .9).setColourMap(new int[][]{{0x242424,0x242424,0x242424,0x171717,0x171717,0x0a0a0a}});

		ExternalHeaterHandler.defaultFurnaceEnergyCost = Config.getInt("heater_consumption");
		ExternalHeaterHandler.defaultFurnaceSpeedupCost= Config.getInt("heater_speedupConsumption");
		ExternalHeaterHandler.registerHeatableAdapter(TileEntityFurnace.class, new DefaultFurnaceAdapter());

		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 80), null, Items.WHEAT_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 80), null, Items.PUMPKIN_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 80), null, Items.MELON_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 120), null, itemSeeds, 6400);
		SqueezerRecipe.addRecipe(null, new ItemStack(itemMaterial,1,18), new ItemStack(itemMaterial,8,17), 19200);

		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol,80), null, Items.REEDS, 6400);
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol,80), null, Items.MELON, 6400);
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol,80), null, Items.APPLE, 6400);
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol,80), null, "cropPotato", 6400);

		RefineryRecipe.addRecipe(new FluidStack(fluidBiodiesel,16), new FluidStack(fluidPlantoil,8),new FluidStack(fluidEthanol,8),80);

		ThermoelectricHandler.registerSourceInKelvin("blockIce", 273);
		ThermoelectricHandler.registerSourceInKelvin("blockPackedIce", 200);
		ThermoelectricHandler.registerSourceInKelvin("blockUranium", 2000);
		ThermoelectricHandler.registerSourceInKelvin("blockYellorium", 2000);
		ThermoelectricHandler.registerSourceInKelvin("blockPlutonium", 4000);
		ThermoelectricHandler.registerSourceInKelvin("blockBlutonium", 4000);

		ExcavatorHandler.mineralVeinCapacity = Config.getInt("excavator_depletion");
		ExcavatorHandler.mineralChance = Config.getDouble("excavator_chance");
		ExcavatorHandler.defaultDimensionBlacklist = Config.getIntArray("excavator_dimBlacklist");
		ExcavatorHandler.addMineral("Iron", 25, .1f, new String[]{"oreIron","oreNickel","oreTin","denseoreIron"}, new float[]{.5f,.25f,.20f,.05f});
		ExcavatorHandler.addMineral("Magnetite", 25, .1f, new String[]{"oreIron","oreGold"}, new float[]{.85f,.15f});
		ExcavatorHandler.addMineral("Pyrite", 20, .1f, new String[]{"oreIron","oreSulfur"}, new float[]{.5f,.5f});
		ExcavatorHandler.addMineral("Bauxite", 20, .2f, new String[]{"oreAluminum","oreTitanium","denseoreAluminum"}, new float[]{.90f,.05f,.05f});
		ExcavatorHandler.addMineral("Copper", 30, .2f, new String[]{"oreCopper","oreGold","oreNickel","denseoreCopper"}, new float[]{.65f,.25f,.05f,.05f});
		if(OreDictionary.doesOreNameExist("oreTin"))
			ExcavatorHandler.addMineral("Cassiterite", 15, .2f, new String[]{"oreTin","denseoreTin"}, new float[]{.95f,.05f});
		ExcavatorHandler.addMineral("Gold", 20, .3f, new String[]{"oreGold","oreCopper","oreNickel","denseoreGold"}, new float[]{.65f,.25f,.05f,.05f});
		ExcavatorHandler.addMineral("Nickel", 20, .3f, new String[]{"oreNickel","orePlatinum","oreIron","denseoreNickel"}, new float[]{.85f,.05f,.05f,.05f});
		ExcavatorHandler.addMineral("Platinum", 5, .35f, new String[]{"orePlatinum","oreNickel","","oreIridium","denseorePlatinum"}, new float[]{.40f,.30f,.15f,.1f,.05f});
		if(OreDictionary.doesOreNameExist("oreUranium")||OreDictionary.doesOreNameExist("oreYellorium"))
			ExcavatorHandler.addMineral("Uranium", 10, .35f, new String[]{"oreUranium","oreLead","orePlutonium","denseoreUranium"}, new float[]{.55f,.3f,.1f,.05f}).addReplacement("oreUranium", "oreYellorium");
		ExcavatorHandler.addMineral("Quartzite", 5, .3f, new String[]{"oreQuartz","oreCertusQuartz"}, new float[]{.6f,.4f});
		ExcavatorHandler.addMineral("Galena", 15, .2f, new String[]{"oreLead","oreSilver","oreSulfur","denseoreLead","denseoreSilver"}, new float[]{.40f,.40f,.1f,.05f,.05f});
		ExcavatorHandler.addMineral("Lead", 10, .15f, new String[]{"oreLead","oreSilver","denseoreLead"}, new float[]{.55f,.4f,.05f});
		ExcavatorHandler.addMineral("Silver", 10, .2f, new String[]{"oreSilver","oreLead","denseoreSilver"}, new float[]{.55f,.4f,.05f});
		ExcavatorHandler.addMineral("Lapis", 10, .2f, new String[]{"oreLapis","oreIron","oreSulfur","denseoreLapis"}, new float[]{.65f,.275f,.025f,.05f});
		ExcavatorHandler.addMineral("Coal", 25, .1f, new String[]{"oreCoal","denseoreCoal","oreDiamond","oreEmerald"}, new float[]{.92f,.1f,.015f,.015f});

		/**MULTIBLOCKS*/
		MultiblockHandler.registerMultiblock(MultiblockCokeOven.instance);
		MultiblockHandler.registerMultiblock(MultiblockBlastFurnace.instance);
		MultiblockHandler.registerMultiblock(MultiblockBlastFurnaceAdvanced.instance);
		MultiblockHandler.registerMultiblock(MultiblockMetalPress.instance);
		MultiblockHandler.registerMultiblock(MultiblockCrusher.instance);
		MultiblockHandler.registerMultiblock(MultiblockSheetmetalTank.instance);
		MultiblockHandler.registerMultiblock(MultiblockSilo.instance);
		MultiblockHandler.registerMultiblock(MultiblockAssembler.instance);
		//		MultiblockHandler.registerMultiblock(MultiblockBottlingMachine.instance);
		MultiblockHandler.registerMultiblock(MultiblockSqueezer.instance);
		MultiblockHandler.registerMultiblock(MultiblockFermenter.instance);
		MultiblockHandler.registerMultiblock(MultiblockRefinery.instance);
		MultiblockHandler.registerMultiblock(MultiblockDieselGenerator.instance);
		//		MultiblockHandler.registerMultiblock(MultiblockLightningRod.instance);
		MultiblockHandler.registerMultiblock(MultiblockExcavator.instance);
		MultiblockHandler.registerMultiblock(MultiblockBucketWheel.instance);
		MultiblockHandler.registerMultiblock(MultiblockArcFurnace.instance);

		/**ACHIEVEMENTS*/
		IEAchievements.init();

		/**VILLAGE*/
		VillagerRegistry villageRegistry = VillagerRegistry.instance();
		villageRegistry.registerVillageCreationHandler(new VillageEngineersHouse.VillageManager());
		MapGenStructureIO.registerStructureComponent(VillageEngineersHouse.class, ImmersiveEngineering.MODID+":EngineersHouse");
		villagerProfession_engineer = new VillagerRegistry.VillagerProfession(ImmersiveEngineering.MODID+":engineer", "immersiveengineering:textures/models/villager_engineer.png","immersiveengineering:textures/models/villager_engineer_zombie.png");
		villageRegistry.register(villagerProfession_engineer);

		VillagerRegistry.VillagerCareer career_engineer = new VillagerRegistry.VillagerCareer(villagerProfession_engineer, ImmersiveEngineering.MODID+".engineer");
		career_engineer.addTrade(1,
				new IEVillagerTrades.EmeraldForItemstack(new ItemStack(itemMaterial,1,0), new EntityVillager.PriceInfo(8, 16)),
				new ListItemForEmeralds(new ItemStack(blockWoodenDecoration,1,1), new EntityVillager.PriceInfo(-10, -6)),
				new ListItemForEmeralds(new ItemStack(blockClothDevice,1,1), new EntityVillager.PriceInfo(-3, -1))
		);
		career_engineer.addTrade(2,
				new IEVillagerTrades.EmeraldForItemstack(new ItemStack(itemMaterial,1,1), new EntityVillager.PriceInfo(2, 6)),
				new ListItemForEmeralds(new ItemStack(blockMetalDecoration1,1,1), new EntityVillager.PriceInfo(-8, -4)),
				new ListItemForEmeralds(new ItemStack(blockMetalDecoration1,1,5), new EntityVillager.PriceInfo(-8, -4))
		);
		career_engineer.addTrade(3,
				new IEVillagerTrades.EmeraldForItemstack(new ItemStack(itemMaterial,1,2), new EntityVillager.PriceInfo(2, 6)),
				new IEVillagerTrades.EmeraldForItemstack(new ItemStack(itemMaterial,1,7), new EntityVillager.PriceInfo(4, 8)),
				new ListItemForEmeralds(new ItemStack(blockStoneDecoration,1,5), new EntityVillager.PriceInfo(-6, -2))
		);

		VillagerRegistry.VillagerCareer career_machinist = new VillagerRegistry.VillagerCareer(villagerProfession_engineer, ImmersiveEngineering.MODID+".machinist");
		career_machinist.addTrade(1,
				new IEVillagerTrades.EmeraldForItemstack(new ItemStack(itemMaterial,1,6), new EntityVillager.PriceInfo(8, 16)),
				new ListItemForEmeralds(new ItemStack(itemTool,1,0), new EntityVillager.PriceInfo(4, 7))
		);
		career_machinist.addTrade(2,
				new IEVillagerTrades.EmeraldForItemstack(new ItemStack(itemMetal,1,0), new EntityVillager.PriceInfo(4, 6)),
				new IEVillagerTrades.EmeraldForItemstack(new ItemStack(itemMetal,1,1), new EntityVillager.PriceInfo(4, 6)),
				new ListItemForEmeralds(new ItemStack(itemMaterial,1,9), new EntityVillager.PriceInfo(1, 3))
		);
		career_machinist.addTrade(3,
				new ListItemForEmeralds(new ItemStack(itemToolbox,1,0), new EntityVillager.PriceInfo(6, 8)),
				new ListItemForEmeralds(new ItemStack(itemMaterial,1,10), new EntityVillager.PriceInfo(1, 3)),
				new ListItemForEmeralds(ItemEngineersBlueprint.getTypedBlueprint("specialBullet"), new EntityVillager.PriceInfo(5, 9))
		);
		career_machinist.addTrade(4,
				new ListItemForEmeralds(new ItemStack(itemDrillhead,1,0), new EntityVillager.PriceInfo(28, 40)),
				new ListItemForEmeralds(itemEarmuffs, new EntityVillager.PriceInfo(4, 9))
		);
		career_machinist.addTrade(5,
				new ListItemForEmeralds(new ItemStack(itemDrillhead,1,1), new EntityVillager.PriceInfo(32, 48)),
				new ListItemForEmeralds(ItemEngineersBlueprint.getTypedBlueprint("electrode"), new EntityVillager.PriceInfo(12, 24))
		);

		VillagerRegistry.VillagerCareer career_electrician = new VillagerRegistry.VillagerCareer(villagerProfession_engineer, ImmersiveEngineering.MODID+".electrician");
		career_electrician.addTrade(1,
				new IEVillagerTrades.EmeraldForItemstack(new ItemStack(itemMaterial,1,20), new EntityVillager.PriceInfo(8, 16)),
				new ListItemForEmeralds(new ItemStack(itemTool,1,1), new EntityVillager.PriceInfo(4, 7)),
				new ListItemForEmeralds(new ItemStack(itemWireCoil,1,0), new EntityVillager.PriceInfo(-4, -2))
		);
		career_electrician.addTrade(2,
				new IEVillagerTrades.EmeraldForItemstack(new ItemStack(itemMaterial,1,21), new EntityVillager.PriceInfo(6, 12)),
				new ListItemForEmeralds(new ItemStack(itemTool,1,2), new EntityVillager.PriceInfo(4, 7)),
				new ListItemForEmeralds(new ItemStack(itemWireCoil,1,1), new EntityVillager.PriceInfo(-4, -1))
		);
		career_electrician.addTrade(3,
				new IEVillagerTrades.EmeraldForItemstack(new ItemStack(itemMaterial,1,22), new EntityVillager.PriceInfo(4, 8)),
				new ListItemForEmeralds(new ItemStack(itemWireCoil,1,1), new EntityVillager.PriceInfo(-2, -1)),
				new ListItemForEmeralds(new ItemStack(itemToolUpgrades,1,6), new EntityVillager.PriceInfo(8, 12))
		);
		career_electrician.addTrade(4,
				new ListItemForEmeralds(new ItemStack(itemToolUpgrades,1,9), new EntityVillager.PriceInfo(8, 12)),
				new ListItemForEmeralds(new ItemStack(itemFluorescentTube), new EntityVillager.PriceInfo(8, 12)),
				new ListItemForEmeralds(new ItemStack(itemsFaradaySuit[0]), new EntityVillager.PriceInfo(5,7)),
				new ListItemForEmeralds(new ItemStack(itemsFaradaySuit[1]), new EntityVillager.PriceInfo(9,11)),
				new ListItemForEmeralds(new ItemStack(itemsFaradaySuit[2]), new EntityVillager.PriceInfo(5,7)),
				new ListItemForEmeralds(new ItemStack(itemsFaradaySuit[3]), new EntityVillager.PriceInfo(11,15))
		);


		/**LOOT*/
		LootTableList.register(VillageEngineersHouse.woodenCrateLoot);
		for(ResourceLocation rl : EventHandler.lootInjections)
			LootTableList.register(rl);

		//		//Railcraft Compat
		//		if(Loader.isModLoaded("Railcraft"))
		//		{
		//			Block rcCube = GameRegistry.findBlock("Railcraft", "cube");
		//			if(rcCube!=null)
		//				OreDictionary.registerOre("blockFuelCoke", new ItemStack(rcCube,1,0));
		//		}
	}

	public static void postInit()
	{
		IERecipes.postInitOreDictRecipes();
	}

	public static void registerToOreDict(String type, ItemIEBase item, int... metas)
	{
		if(metas==null||metas.length<1)
		{
			for(int meta=0; meta<item.getSubNames().length; meta++)
				if(!item.isMetaHidden(meta))
				{
					String name = item.getSubNames()[meta];
					if(type!=null&&!type.isEmpty())
						name = name.substring(0,1).toUpperCase()+name.substring(1);
					OreDictionary.registerOre(type+name, new ItemStack(item,1,meta));
				}
		}
		else
		{
			for(int meta: metas)
				if(!item.isMetaHidden(meta))
				{
					String name = item.getSubNames()[meta];
					if(type!=null&&!type.isEmpty())
						name = name.substring(0,1).toUpperCase()+name.substring(1);
					OreDictionary.registerOre(type+name, new ItemStack(item,1,meta));
				}
		}
	}
	public static void registerToOreDict(String type, BlockIEBase item, int... metas)
	{
		if(metas==null||metas.length<1)
		{
			for(int meta=0; meta<item.getMetaEnums().length; meta++)
				if(!item.isMetaHidden(meta))
				{
					String name = item.getMetaEnums()[meta].toString();
					if(type!=null&&!type.isEmpty())
						name = name.substring(0,1).toUpperCase()+name.substring(1).toLowerCase();
					OreDictionary.registerOre(type+name, new ItemStack(item,1,meta));
				}
		}
		else
		{
			for(int meta: metas)
				if(!item.isMetaHidden(meta))
				{
					String name = item.getMetaEnums()[meta].toString();
					if(type!=null&&!type.isEmpty())
						name = name.substring(0,1).toUpperCase()+name.substring(1).toLowerCase();
					OreDictionary.registerOre(type+name, new ItemStack(item,1,meta));
				}
		}
	}
	public static void registerOre(String type, ItemStack ore, ItemStack ingot, ItemStack dust, ItemStack block, ItemStack nugget)
	{
		//		if(ore!=null)
		//			OreDictionary.registerOre("ore"+type, ore);
		//		if(ingot!=null)
		//			OreDictionary.registerOre("ingot"+type, ingot);
		//		if(dust!=null)
		//			OreDictionary.registerOre("dust"+type, dust);
		//		if(block!=null)
		//			OreDictionary.registerOre("block"+type, block);
		//		if(nugget!=null)
		//			OreDictionary.registerOre("nugget"+type, nugget);
	}

	public static void registerTile(Class<? extends TileEntity> tile)
	{
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity")+"TileEntity".length());
		GameRegistry.registerTileEntity(tile, ImmersiveEngineering.MODID+":"+ s);
	}

	public static void addConfiguredWorldgen(IBlockState state, String config)
	{
		int[] values = Config.getIntArray("ore_"+config);
		if(values!=null && values.length>=5 && values[0]>0)
			IEWorldGen.addOreGen(config, state, values[0],values[1],values[2], values[3],values[4]);
	}
}
