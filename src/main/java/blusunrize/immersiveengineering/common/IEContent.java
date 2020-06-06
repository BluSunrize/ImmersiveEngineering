/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.*;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.IRecipeAdapter;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.RecipeQuery;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Extinguish;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler.DefaultFurnaceAdapter;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.*;
import blusunrize.immersiveengineering.common.blocks.BlockFakeLight.TileEntityFakeLight;
import blusunrize.immersiveengineering.common.blocks.cloth.*;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.*;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorChute.ConveyorChuteAluminum;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorChute.ConveyorChuteCopper;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorChute.ConveyorChuteIron;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorChute.ConveyorChuteSteel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.*;
import blusunrize.immersiveengineering.common.blocks.plant.BlockIECrop;
import blusunrize.immersiveengineering.common.blocks.plant.BlockTypes_Hemp;
import blusunrize.immersiveengineering.common.blocks.stone.*;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.crafting.*;
import blusunrize.immersiveengineering.common.datafixers.IEDataFixers;
import blusunrize.immersiveengineering.common.entities.*;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.ItemBullet.WolfpackBullet;
import blusunrize.immersiveengineering.common.items.ItemBullet.WolfpackPartBullet;
import blusunrize.immersiveengineering.common.items.tools.*;
import blusunrize.immersiveengineering.common.util.IEFluid;
import blusunrize.immersiveengineering.common.util.IEFluid.FluidPotion;
import blusunrize.immersiveengineering.common.util.IELootFunctions;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.IEVillagerHandler;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import blusunrize.immersiveengineering.common.world.VillageEngineersHouse;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.potion.*;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.AbstractBrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IRegistryDelegate;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

@Mod.EventBusSubscriber
public class IEContent
{
	public static ArrayList<Block> registeredIEBlocks = new ArrayList<Block>();
	public static ArrayList<Item> registeredIEItems = new ArrayList<Item>();
	public static List<Class<? extends TileEntity>> registeredIETiles = new ArrayList<>();

	public static BlockIEBase<BlockTypes_MetalsIE> blockOre;
	public static BlockIEBase<BlockTypes_MetalsIE> blockStorage;
	public static BlockIESlab blockStorageSlabs;
	public static BlockIEBase<BlockTypes_StoneDecoration> blockStoneDecoration;
	public static BlockIEBase<BlockTypes_StoneDecoration> blockStoneDecorationSlabs;
	public static Block blockStoneStair_hempcrete;
	public static Block blockStoneStair_concrete0;
	public static Block blockStoneStair_concrete1;
	public static Block blockStoneStair_concrete2;
	public static BlockIEBase<BlockTypes_StoneDevices> blockStoneDevice;

	public static BlockIEBase<BlockTypes_TreatedWood> blockTreatedWood;
	public static BlockIEBase<BlockTypes_TreatedWood> blockTreatedWoodSlabs;
	public static Block blockWoodenStair;
	public static Block blockWoodenStair1;
	public static Block blockWoodenStair2;
	public static BlockIEBase<BlockTypes_WoodenDecoration> blockWoodenDecoration;
	public static BlockIEBase<BlockTypes_WoodenDevice0> blockWoodenDevice0;
	public static BlockIEBase<BlockTypes_WoodenDevice1> blockWoodenDevice1;
	public static Block blockCrop;
	public static BlockIEBase<BlockTypes_ClothDevice> blockClothDevice;
	public static Block blockFakeLight;

	public static BlockIEBase<BlockTypes_MetalsAll> blockSheetmetal;
	public static BlockIEBase<BlockTypes_MetalsAll> blockSheetmetalSlabs;
	public static BlockIEBase<BlockTypes_MetalDecoration0> blockMetalDecoration0;
	public static BlockIEBase<BlockTypes_MetalDecoration1> blockMetalDecoration1;
	public static BlockIEBase<BlockTypes_MetalDecoration2> blockMetalDecoration2;
	public static BlockIEBase<BlockTypes_MetalDecoration1> blockMetalDecorationSlabs1;
	public static Block blockSteelScaffoldingStair;
	public static Block blockSteelScaffoldingStair1;
	public static Block blockSteelScaffoldingStair2;
	public static Block blockAluminumScaffoldingStair;
	public static Block blockAluminumScaffoldingStair1;
	public static Block blockAluminumScaffoldingStair2;
	public static Block blockMetalLadder;
	public static BlockIEBase<BlockTypes_Connector> blockConnectors;
	public static BlockIEBase<BlockTypes_MetalDevice0> blockMetalDevice0;
	public static BlockIEBase<BlockTypes_MetalDevice1> blockMetalDevice1;
	public static BlockIEBase<BlockTypes_Conveyor> blockConveyor;
	public static BlockIEBase<BlockTypes_MetalMultiblock> blockMetalMultiblock;
	public static BlockIEFluid blockFluidCreosote;
	public static BlockIEFluid blockFluidPlantoil;
	public static BlockIEFluid blockFluidEthanol;
	public static BlockIEFluid blockFluidBiodiesel;
	public static BlockIEFluid blockFluidConcrete;

	public static ItemIEBase itemMaterial;
	public static ItemIEBase itemMetal;
	public static ItemIEBase itemTool;
	public static ItemToolBase itemSteelPick;
	public static ItemToolBase itemSteelShovel;
	public static ItemToolBase itemSteelAxe;
	public static ItemIEHoe itemSteelHoe;
	public static ItemIESword itemSteelSword;
	public static ItemIEBase itemToolbox;
	public static ItemIEBase itemWireCoil;
	public static ItemIEBase itemSeeds;
	public static ItemIEBase itemDrill;
	public static ItemIEBase itemDrillhead;
	public static ItemIEBase itemJerrycan;
	public static ItemIEBase itemMold;
	public static ItemIEBase itemBlueprint;
	public static ItemIEBase itemRevolver;
	public static ItemIEBase itemSpeedloader;
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
	public static Item itemPowerpack;
	public static ItemIEBase itemShield;
	public static ItemIEBase itemMaintenanceKit;
	public static ItemSteelArmor[] itemsSteelArmor = new ItemSteelArmor[4];

	public static ItemIEBase itemFakeIcons;

	//	public static BlockIEBase<BlockTypes_> blockClothDevice;
	public static Fluid fluidCreosote;
	public static Fluid fluidPlantoil;
	public static Fluid fluidEthanol;
	public static Fluid fluidBiodiesel;
	public static Fluid fluidConcrete;

	public static Fluid fluidPotion;

	static
	{
		fluidCreosote = setupFluid(new Fluid("creosote", new ResourceLocation("immersiveengineering:blocks/fluid/creosote_still"), new ResourceLocation("immersiveengineering:blocks/fluid/creosote_flow")).setDensity(1100).setViscosity(3000));
		fluidPlantoil = setupFluid(new Fluid("plantoil", new ResourceLocation("immersiveengineering:blocks/fluid/plantoil_still"), new ResourceLocation("immersiveengineering:blocks/fluid/plantoil_flow")).setDensity(925).setViscosity(2000));
		fluidEthanol = setupFluid(new Fluid("ethanol", new ResourceLocation("immersiveengineering:blocks/fluid/ethanol_still"), new ResourceLocation("immersiveengineering:blocks/fluid/ethanol_flow")).setDensity(789).setViscosity(1000));
		fluidBiodiesel = setupFluid(new Fluid("biodiesel", new ResourceLocation("immersiveengineering:blocks/fluid/biodiesel_still"), new ResourceLocation("immersiveengineering:blocks/fluid/biodiesel_flow")).setDensity(789).setViscosity(1000));
		fluidConcrete = setupFluid(new Fluid("concrete", new ResourceLocation("immersiveengineering:blocks/fluid/concrete_still"), new ResourceLocation("immersiveengineering:blocks/fluid/concrete_flow")).setDensity(2400).setViscosity(4000));
		fluidPotion = setupFluid(new FluidPotion("potion", new ResourceLocation("immersiveengineering:blocks/fluid/potion_still"), new ResourceLocation("immersiveengineering:blocks/fluid/potion_flow")));

		blockOre = (BlockIEBase)new BlockIEBase("ore", Material.ROCK, PropertyEnum.create("type", BlockTypes_Ore.class), ItemBlockIEBase.class).setOpaque(true).setHardness(3.0F).setResistance(5.0F);
		blockStorage = (BlockIEBase)new BlockIEBase("storage", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalsIE.class), ItemBlockIEBase.class).setOpaque(true).setHardness(5.0F).setResistance(10.0F);
		blockStorageSlabs = (BlockIESlab)new BlockIESlab("storage_slab", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalsIE.class)).setHardness(5.0F).setResistance(10.0F);
		blockStoneDecoration = (BlockIEBase)new BlockIEBase("stone_decoration", Material.ROCK, PropertyEnum.create("type", BlockTypes_StoneDecoration.class), ItemBlockIEBase.class)
		{
			@Override
			public int quantityDropped(IBlockState state, int fortune, Random random)
			{
				if(getMetaFromState(state)==BlockTypes_StoneDecoration.CONCRETE_SPRAYED.getMeta())
					return 0;
				return super.quantityDropped(state, fortune, random);
			}
		}.setMetaExplosionResistance(BlockTypes_StoneDecoration.CONCRETE_LEADED.getMeta(), 180).setHardness(2.0F).setResistance(10.0F);
		//Insulated Glass + Sprayed concrete are special
		int insGlassMeta = BlockTypes_StoneDecoration.INSULATING_GLASS.getMeta();
		blockStoneDecoration.setMetaBlockLayer(insGlassMeta, BlockRenderLayer.TRANSLUCENT).setMetaLightOpacity(insGlassMeta, 0).setNotNormalBlock(insGlassMeta);
		int sprConcreteMeta = BlockTypes_StoneDecoration.CONCRETE_SPRAYED.getMeta();
		blockStoneDecoration.setMetaHidden(sprConcreteMeta).setMetaBlockLayer(sprConcreteMeta, BlockRenderLayer.CUTOUT).setMetaLightOpacity(sprConcreteMeta, 0).setNotNormalBlock(sprConcreteMeta).setMetaHardness(sprConcreteMeta, .2f).setMetaHammerHarvest(sprConcreteMeta);

		blockStoneDecorationSlabs = (BlockIEBase)new BlockIESlab("stone_decoration_slab", Material.ROCK, PropertyEnum.create("type", BlockTypes_StoneDecoration.class)).setMetaHidden(3, 8, sprConcreteMeta).setMetaExplosionResistance(BlockTypes_StoneDecoration.CONCRETE_LEADED.getMeta(), 180).setHardness(2.0F).setResistance(10.0F);
		blockStoneStair_hempcrete = new BlockIEStairs("stone_decoration_stairs_hempcrete", blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.HEMPCRETE.getMeta()));
		blockStoneStair_concrete0 = new BlockIEStairs("stone_decoration_stairs_concrete", blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE.getMeta()));
		blockStoneStair_concrete1 = new BlockIEStairs("stone_decoration_stairs_concrete_tile", blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE_TILE.getMeta()));
		blockStoneStair_concrete2 = new BlockIEStairs("stone_decoration_stairs_concrete_leaded", blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE_LEADED.getMeta())).setExplosionResistance(180f);

		blockStoneDevice = new BlockStoneDevice();

		blockTreatedWood = (BlockIEBase)new BlockIEBase("treated_wood", Material.WOOD, PropertyEnum.create("type", BlockTypes_TreatedWood.class), ItemBlockIEBase.class).setOpaque(true).setHasFlavour().setHardness(2.0F).setResistance(5.0F);
		blockTreatedWoodSlabs = (BlockIESlab)new BlockIESlab("treated_wood_slab", Material.WOOD, PropertyEnum.create("type", BlockTypes_TreatedWood.class)).setHasFlavour().setHardness(2.0F).setResistance(5.0F);
		blockWoodenStair = new BlockIEStairs("treated_wood_stairs0", blockTreatedWood.getStateFromMeta(0)).setHasFlavour(true);
		blockWoodenStair1 = new BlockIEStairs("treated_wood_stairs1", blockTreatedWood.getStateFromMeta(1)).setHasFlavour(true);
		blockWoodenStair2 = new BlockIEStairs("treated_wood_stairs2", blockTreatedWood.getStateFromMeta(2)).setHasFlavour(true);

		blockWoodenDecoration = new BlockWoodenDecoration();
		blockWoodenDevice0 = new BlockWoodenDevice0();
		blockWoodenDevice1 = new BlockWoodenDevice1().setMetaHidden(BlockTypes_WoodenDevice1.WINDMILL_ADVANCED.getMeta());
		blockCrop = new BlockIECrop("hemp", PropertyEnum.create("type", BlockTypes_Hemp.class));
		blockClothDevice = new BlockClothDevice();
		blockFakeLight = new BlockFakeLight();

		blockSheetmetal = (BlockIEBase)new BlockIEBase("sheetmetal", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalsAll.class), ItemBlockIEBase.class).setOpaque(true).setHardness(3.0F).setResistance(10.0F);
		blockSheetmetalSlabs = (BlockIESlab)new BlockIESlab("sheetmetal_slab", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalsAll.class)).setHardness(3.0F).setResistance(10.0F);

		blockMetalDecoration0 = new BlockMetalDecoration0();
		blockMetalDecoration1 = new BlockMetalDecoration1();
		blockMetalDecoration2 = new BlockMetalDecoration2();
		blockMetalDecorationSlabs1 = (BlockIESlab)new BlockIEScaffoldSlab("metal_decoration1_slab", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalDecoration1.class)).setMetaHidden(0, 4).setHardness(3.0F).setResistance(15.0F);
		blockSteelScaffoldingStair = new BlockIEStairs("steel_scaffolding_stairs0", blockMetalDecoration1.getStateFromMeta(1)).setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);
		blockSteelScaffoldingStair1 = new BlockIEStairs("steel_scaffolding_stairs1", blockMetalDecoration1.getStateFromMeta(2)).setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);
		blockSteelScaffoldingStair2 = new BlockIEStairs("steel_scaffolding_stairs2", blockMetalDecoration1.getStateFromMeta(3)).setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);
		blockAluminumScaffoldingStair = new BlockIEStairs("aluminum_scaffolding_stairs0", blockMetalDecoration1.getStateFromMeta(5)).setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);
		blockAluminumScaffoldingStair1 = new BlockIEStairs("aluminum_scaffolding_stairs1", blockMetalDecoration1.getStateFromMeta(6)).setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);
		blockAluminumScaffoldingStair2 = new BlockIEStairs("aluminum_scaffolding_stairs2", blockMetalDecoration1.getStateFromMeta(7)).setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);
		blockMetalLadder = new BlockMetalLadder();

		blockConnectors = new BlockConnector();
		blockMetalDevice0 = new BlockMetalDevice0();
		blockMetalDevice1 = new BlockMetalDevice1();
		blockConveyor = new BlockConveyor();
		blockMetalMultiblock = new BlockMetalMultiblocks();

		blockFluidCreosote = new BlockIEFluid("fluidCreosote", fluidCreosote, Material.WATER).setFlammability(40, 400);
		blockFluidPlantoil = new BlockIEFluid("fluidPlantoil", fluidPlantoil, Material.WATER);
		blockFluidEthanol = new BlockIEFluid("fluidEthanol", fluidEthanol, Material.WATER).setFlammability(60, 600);
		blockFluidBiodiesel = new BlockIEFluid("fluidBiodiesel", fluidBiodiesel, Material.WATER).setFlammability(60, 200);
		blockFluidConcrete = new BlockIEFluidConcrete("fluidConcrete", fluidConcrete, Material.WATER);

		itemMaterial = new ItemMaterial();
		itemMetal = new ItemIEBase("metal", 64,
				"ingot_copper", "ingot_aluminum", "ingot_lead", "ingot_silver", "ingot_nickel", "ingot_uranium", "ingot_constantan", "ingot_electrum", "ingot_steel",
				"dust_copper", "dust_aluminum", "dust_lead", "dust_silver", "dust_nickel", "dust_uranium", "dust_constantan", "dust_electrum", "dust_steel", "dust_iron", "dust_gold",
				"nugget_copper", "nugget_aluminum", "nugget_lead", "nugget_silver", "nugget_nickel", "nugget_uranium", "nugget_constantan", "nugget_electrum", "nugget_steel", "nugget_iron",
				"plate_copper", "plate_aluminum", "plate_lead", "plate_silver", "plate_nickel", "plate_uranium", "plate_constantan", "plate_electrum", "plate_steel", "plate_iron", "plate_gold");
		itemTool = new ItemIETool();
		itemSteelPick = new ItemIEPickaxe(Lib.MATERIAL_Steel, "pickaxe_steel", "pickaxe", "ingotSteel");
		itemSteelShovel = new ItemIEShovel(Lib.MATERIAL_Steel, "shovel_steel", "shovel", "ingotSteel");
		itemSteelAxe = new ItemIEAxe(Lib.MATERIAL_Steel, "axe_steel", "axe", "ingotSteel");
		itemSteelHoe = new ItemIEHoe(Lib.MATERIAL_Steel, "hoe_steel", "ingotSteel");
		itemSteelSword = new ItemIESword(Lib.MATERIAL_Steel, "sword_steel", "ingotSteel");
		itemToolbox = new ItemToolbox();
		itemWireCoil = new ItemWireCoil();
		WireType.ieWireCoil = itemWireCoil;
		itemSeeds = new ItemIESeed(blockCrop, "hemp");
		if(Config.IEConfig.hempSeedWeight > 0)
			MinecraftForge.addGrassSeed(new ItemStack(itemSeeds), Config.IEConfig.hempSeedWeight);
		itemDrill = new ItemDrill();
		itemDrillhead = new ItemDrillhead();
		itemJerrycan = new ItemJerrycan();
		itemMold = new ItemIEBase("mold", 1, "plate", "gear", "rod", "bullet_casing", "wire", "packing4", "packing9", "unpacking");
		itemBlueprint = new ItemEngineersBlueprint().setRegisterSubModels(false);
		BlueprintCraftingRecipe.itemBlueprint = itemBlueprint;
		itemRevolver = new ItemRevolver();
		itemSpeedloader = new ItemSpeedloader();
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
		ItemFaradaySuit.mat = EnumHelper.addArmorMaterial("IMMERSIVEENGINEERING:FARADAY", "immersiveengineering:faradaySuit", 1, new int[]{1, 3, 2, 1}, 0, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0);
		for(int i = 0; i < itemsFaradaySuit.length; i++)
			itemsFaradaySuit[i] = new ItemFaradaySuit(EntityEquipmentSlot.values()[2+i]);
		itemFluorescentTube = new ItemFluorescentTube();
		itemPowerpack = new ItemPowerpack();
		itemShield = new ItemIEShield();
		itemMaintenanceKit = new ItemMaintenanceKit();
		ItemSteelArmor.mat = EnumHelper.addArmorMaterial("IMMERSIVEENGINEERING:STEEL", "immersiveengineering:steelArmor", 21, new int[]{2, 6, 7, 2}, 10, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0f).setRepairItem(new ItemStack(itemMetal,1,8));
		for(int i = 0; i < itemsSteelArmor.length; i++)
			itemsSteelArmor[i] = new ItemSteelArmor(EntityEquipmentSlot.values()[2+i]);

		itemFakeIcons = new ItemIEBase("fake_icon", 1, "birthday", "lucky", "drillbreak")
		{
			@Override
			public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
			{
			}
		};
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		for(Block block : registeredIEBlocks)
			event.getRegistry().register(block.setRegistryName(createRegistryName(block.getTranslationKey())));
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		for(Item item : registeredIEItems)
			event.getRegistry().register(item.setRegistryName(createRegistryName(item.getTranslationKey())));

		registerOres();
	}

	@SubscribeEvent
	public static void missingItems(RegistryEvent.MissingMappings<Item> event)
	{
		Set<String> knownMissing = ImmutableSet.of(
				"fluidethanol",
				"fluidconcrete",
				"fluidbiodiesel",
				"fluidplantoil",
				"fluidcreosote"
		);
		for(Mapping<Item> missing : event.getMappings())
			if(knownMissing.contains(missing.key.getPath()))
				missing.ignore();
	}


	@SubscribeEvent
	public static void registerPotions(RegistryEvent.Register<Potion> event)
	{
		/*POTIONS*/
		IEPotions.init();
	}

	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
	{
		/*CRAFTING*/
		IERecipes.initCraftingRecipes(event.getRegistry());

		/*FURNACE*/
		IERecipes.initFurnaceRecipes();

		/*BLUEPRINTS*/
		IERecipes.initBlueprintRecipes();

		/*BELLJAR*/
		BelljarHandler.init();

		/*EXCAVATOR*/
		ExcavatorHandler.mineralVeinCapacity = IEConfig.Machines.excavator_depletion;
		ExcavatorHandler.mineralChance = IEConfig.Machines.excavator_chance;
		ExcavatorHandler.defaultDimensionBlacklist = IEConfig.Machines.excavator_dimBlacklist;
		String sulfur = OreDictionary.doesOreNameExist("oreSulfur")?"oreSulfur": "dustSulfur";
		ExcavatorHandler.addMineral("Iron", 25, .1f, new String[]{"oreIron", "oreNickel", "oreTin", "denseoreIron"}, new float[]{.5f, .25f, .20f, .05f});
		ExcavatorHandler.addMineral("Magnetite", 25, .1f, new String[]{"oreIron", "oreGold"}, new float[]{.85f, .15f});
		ExcavatorHandler.addMineral("Pyrite", 20, .1f, new String[]{"oreIron", sulfur}, new float[]{.5f, .5f});
		ExcavatorHandler.addMineral("Bauxite", 20, .2f, new String[]{"oreAluminum", "oreTitanium", "denseoreAluminum"}, new float[]{.90f, .05f, .05f});
		ExcavatorHandler.addMineral("Copper", 30, .2f, new String[]{"oreCopper", "oreGold", "oreNickel", "denseoreCopper"}, new float[]{.65f, .25f, .05f, .05f});
		if(OreDictionary.doesOreNameExist("oreTin"))
			ExcavatorHandler.addMineral("Cassiterite", 15, .2f, new String[]{"oreTin", "denseoreTin"}, new float[]{.95f, .05f});
		ExcavatorHandler.addMineral("Gold", 20, .3f, new String[]{"oreGold", "oreCopper", "oreNickel", "denseoreGold"}, new float[]{.65f, .25f, .05f, .05f});
		ExcavatorHandler.addMineral("Nickel", 20, .3f, new String[]{"oreNickel", "orePlatinum", "oreIron", "denseoreNickel"}, new float[]{.85f, .05f, .05f, .05f});
		if(OreDictionary.doesOreNameExist("orePlatinum"))
			ExcavatorHandler.addMineral("Platinum", 5, .35f, new String[]{"orePlatinum", "oreNickel", "", "oreIridium", "denseorePlatinum"}, new float[]{.40f, .30f, .15f, .1f, .05f});
		ExcavatorHandler.addMineral("Uranium", 10, .35f, new String[]{"oreUranium", "oreLead", "orePlutonium", "denseoreUranium"}, new float[]{.55f, .3f, .1f, .05f}).addReplacement("oreUranium", "oreYellorium");
		ExcavatorHandler.addMineral("Quartzite", 5, .3f, new String[]{"oreQuartz", "oreCertusQuartz"}, new float[]{.6f, .4f});
		ExcavatorHandler.addMineral("Galena", 15, .2f, new String[]{"oreLead", "oreSilver", "oreSulfur", "denseoreLead", "denseoreSilver"}, new float[]{.40f, .40f, .1f, .05f, .05f});
		ExcavatorHandler.addMineral("Lead", 10, .15f, new String[]{"oreLead", "oreSilver", "denseoreLead"}, new float[]{.55f, .4f, .05f});
		ExcavatorHandler.addMineral("Silver", 10, .2f, new String[]{"oreSilver", "oreLead", "denseoreSilver"}, new float[]{.55f, .4f, .05f});
		ExcavatorHandler.addMineral("Lapis", 10, .2f, new String[]{"oreLapis", "oreIron", sulfur, "denseoreLapis"}, new float[]{.65f, .275f, .025f, .05f});
		ExcavatorHandler.addMineral("Cinnabar", 15, .1f, new String[]{"oreRedstone", "denseoreRedstone", "oreRuby", "oreCinnabar", sulfur}, new float[]{.75f, .05f, .05f, .1f, .05f});
		ExcavatorHandler.addMineral("Coal", 25, .1f, new String[]{"oreCoal", "denseoreCoal", "oreDiamond", "oreEmerald"}, new float[]{.92f, .1f, .015f, .015f});
		ExcavatorHandler.addMineral("Silt", 25, .05f, new String[]{"blockClay", "sand", "gravel"}, new float[]{.5f, .3f, .2f});

		/*MULTIBLOCK RECIPES*/
		CokeOvenRecipe.addRecipe(new ItemStack(itemMaterial, 1, 6), new ItemStack(Items.COAL), 1800, 500);
		CokeOvenRecipe.addRecipe(new ItemStack(blockStoneDecoration, 1, 3), "blockCoal", 1800*9, 5000);
		CokeOvenRecipe.addRecipe(new ItemStack(Items.COAL, 1, 1), "logWood", 900, 250);

		IERecipes.initBlastFurnaceRecipes();

		IERecipes.initMetalPressRecipes();

		IERecipes.initAlloySmeltingRecipes();

		IERecipes.initCrusherRecipes();

		IERecipes.initArcSmeltingRecipes();

		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 80), ItemStack.EMPTY, Items.WHEAT_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 60), ItemStack.EMPTY, Items.BEETROOT_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 40), ItemStack.EMPTY, Items.PUMPKIN_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 20), ItemStack.EMPTY, Items.MELON_SEEDS, 6400);
		SqueezerRecipe.addRecipe(new FluidStack(fluidPlantoil, 120), ItemStack.EMPTY, itemSeeds, 6400);
		SqueezerRecipe.addRecipe(null, new ItemStack(itemMaterial, 1, 18), new ItemStack(itemMaterial, 8, 17), 19200);
		Fluid fluidBlood = FluidRegistry.getFluid("blood");
		if(fluidBlood!=null)
			SqueezerRecipe.addRecipe(new FluidStack(fluidBlood, 5), new ItemStack(Items.LEATHER), new ItemStack(Items.ROTTEN_FLESH), 6400);

		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol, 80), ItemStack.EMPTY, Items.REEDS, 6400);
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol, 80), ItemStack.EMPTY, Items.MELON, 6400);
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol, 80), ItemStack.EMPTY, Items.APPLE, 6400);
		FermenterRecipe.addRecipe(new FluidStack(fluidEthanol, 80), ItemStack.EMPTY, "cropPotato", 6400);

		RefineryRecipe.addRecipe(new FluidStack(fluidBiodiesel, 16), new FluidStack(fluidPlantoil, 8), new FluidStack(fluidEthanol, 8), 80);

		MixerRecipe.addRecipe(new FluidStack(fluidConcrete, 500), new FluidStack(FluidRegistry.WATER, 500), new Object[]{"sand", "sand", Items.CLAY_BALL, "gravel"}, 3200);

		BottlingMachineRecipe.addRecipe(new ItemStack(Blocks.SPONGE, 1, 1), new ItemStack(Blocks.SPONGE, 1, 0), new FluidStack(FluidRegistry.WATER, 1000));

		IECompatModule.doModulesRecipes();

		/*ORE DICT CRAWLING*/
		IERecipes.postInitOreDictRecipes();
	}

	private static ResourceLocation createRegistryName(String unlocalized)
	{
		unlocalized = unlocalized.substring(unlocalized.indexOf("immersive"));
		unlocalized = unlocalized.replaceFirst("\\.", ":");
		return new ResourceLocation(unlocalized);
	}

	public static Fluid setupFluid(Fluid fluid)
	{
		FluidRegistry.addBucketForFluid(fluid);
		if(!FluidRegistry.registerFluid(fluid))
			return FluidRegistry.getFluid(fluid.getName());
		return fluid;
	}

	public static void preInit()
	{
		WireType.init();
		/*CONVEYORS*/
		ConveyorHandler.registerMagnetSupression((entity, iConveyorTile) -> {
			NBTTagCompound data = entity.getEntityData();
			if(!data.getBoolean(Lib.MAGNET_PREVENT_NBT))
				data.setBoolean(Lib.MAGNET_PREVENT_NBT, true);
		}, (entity, iConveyorTile) -> {
			entity.getEntityData().removeTag(Lib.MAGNET_PREVENT_NBT);
		});
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "conveyor"), ConveyorBasic.class, (tileEntity) -> new ConveyorBasic());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "uncontrolled"), ConveyorUncontrolled.class, (tileEntity) -> new ConveyorUncontrolled());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "dropper"), ConveyorDrop.class, (tileEntity) -> new ConveyorDrop());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "vertical"), ConveyorVertical.class, (tileEntity) -> new ConveyorVertical());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "splitter"), ConveyorSplit.class, (tileEntity) -> new ConveyorSplit(tileEntity instanceof IConveyorTile?((IConveyorTile)tileEntity).getFacing(): EnumFacing.NORTH));
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "extract"), ConveyorExtract.class, (tileEntity) -> new ConveyorExtract(tileEntity instanceof IConveyorTile?((IConveyorTile)tileEntity).getFacing(): EnumFacing.NORTH));
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "covered"), ConveyorCovered.class, (tileEntity) -> new ConveyorCovered());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "droppercovered"), ConveyorDropCovered.class, (tileEntity) -> new ConveyorDropCovered());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "verticalcovered"), ConveyorVerticalCovered.class, (tileEntity) -> new ConveyorVerticalCovered());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "extractcovered"), ConveyorExtractCovered.class, (tileEntity) -> new ConveyorExtractCovered(tileEntity instanceof IConveyorTile?((IConveyorTile)tileEntity).getFacing(): EnumFacing.NORTH));
		ConveyorHandler.registerSubstitute(new ResourceLocation(ImmersiveEngineering.MODID, "conveyor"), new ResourceLocation(ImmersiveEngineering.MODID, "uncontrolled"));
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "chute_"+BlockTypes_MetalsAll.IRON.getName()), ConveyorChuteIron.class, (tileEntity) -> new ConveyorChuteIron());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "chute_"+BlockTypes_MetalsAll.STEEL.getName()), ConveyorChuteSteel.class, (tileEntity) -> new ConveyorChuteSteel());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "chute_"+BlockTypes_MetalsAll.ALUMINUM.getName()), ConveyorChuteAluminum.class, (tileEntity) -> new ConveyorChuteAluminum());
		ConveyorHandler.registerConveyorHandler(new ResourceLocation(ImmersiveEngineering.MODID, "chute_"+BlockTypes_MetalsAll.COPPER.getName()), ConveyorChuteCopper.class, (tileEntity) -> new ConveyorChuteCopper());

		/*BULLETS*/
		ItemBullet.initBullets();

		DataSerializers.registerSerializer(IEFluid.OPTIONAL_FLUID_STACK);

		IELootFunctions.preInit();
	}

	public static void preInitEnd()
	{
		/*WOLFPACK BULLETS*/
		if(!BulletHandler.homingCartridges.isEmpty())
		{
			BulletHandler.registerBullet("wolfpack", new WolfpackBullet());
			BulletHandler.registerBullet("wolfpackPart", new WolfpackPartBullet());
		}
	}

	public static void registerOres()
	{
		/*ORE DICTIONARY*/
		registerToOreDict("ore", blockOre);
		registerToOreDict("block", blockStorage);
		registerToOreDict("slab", blockStorageSlabs);
		registerToOreDict("blockSheetmetal", blockSheetmetal);
		registerToOreDict("slabSheetmetal", blockSheetmetalSlabs);
		registerToOreDict("", itemMetal);
		OreDictionary.registerOre("stickTreatedWood", new ItemStack(itemMaterial, 1, 0));
		OreDictionary.registerOre("stickIron", new ItemStack(itemMaterial, 1, 1));
		OreDictionary.registerOre("stickSteel", new ItemStack(itemMaterial, 1, 2));
		OreDictionary.registerOre("stickAluminum", new ItemStack(itemMaterial, 1, 3));
		OreDictionary.registerOre("fiberHemp", new ItemStack(itemMaterial, 1, 4));
		OreDictionary.registerOre("fabricHemp", new ItemStack(itemMaterial, 1, 5));
		OreDictionary.registerOre("fuelCoke", new ItemStack(itemMaterial, 1, 6));
		OreDictionary.registerOre("itemSlag", new ItemStack(itemMaterial, 1, 7));
		OreDictionary.registerOre("dustCoke", new ItemStack(itemMaterial, 1, 17));
		OreDictionary.registerOre("dustHOPGraphite", new ItemStack(itemMaterial, 1, 18));
		OreDictionary.registerOre("ingotHOPGraphite", new ItemStack(itemMaterial, 1, 19));
		OreDictionary.registerOre("wireCopper", new ItemStack(itemMaterial, 1, 20));
		OreDictionary.registerOre("wireElectrum", new ItemStack(itemMaterial, 1, 21));
		OreDictionary.registerOre("wireAluminum", new ItemStack(itemMaterial, 1, 22));
		OreDictionary.registerOre("wireSteel", new ItemStack(itemMaterial, 1, 23));
		OreDictionary.registerOre("dustSaltpeter", new ItemStack(itemMaterial, 1, 24));
		OreDictionary.registerOre("dustSulfur", new ItemStack(itemMaterial, 1, 25));
		OreDictionary.registerOre("electronTube", new ItemStack(itemMaterial, 1, 26));

		OreDictionary.registerOre("plankTreatedWood", new ItemStack(blockTreatedWood, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("slabTreatedWood", new ItemStack(blockTreatedWoodSlabs, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("fenceTreatedWood", new ItemStack(blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.FENCE.getMeta()));
		OreDictionary.registerOre("scaffoldingTreatedWood", new ItemStack(blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta()));
		OreDictionary.registerOre("blockFuelCoke", new ItemStack(blockStoneDecoration, 1, BlockTypes_StoneDecoration.COKE.getMeta()));
		OreDictionary.registerOre("concrete", new ItemStack(blockStoneDecoration, 1, BlockTypes_StoneDecoration.CONCRETE.getMeta()));
		OreDictionary.registerOre("concrete", new ItemStack(blockStoneDecoration, 1, BlockTypes_StoneDecoration.CONCRETE_TILE.getMeta()));
		OreDictionary.registerOre("fenceSteel", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta()));
		OreDictionary.registerOre("fenceAluminum", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta()));
		OreDictionary.registerOre("scaffoldingSteel", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()));
		OreDictionary.registerOre("scaffoldingSteel", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta()));
		OreDictionary.registerOre("scaffoldingSteel", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_2.getMeta()));
		OreDictionary.registerOre("scaffoldingAluminum", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta()));
		OreDictionary.registerOre("scaffoldingAluminum", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_1.getMeta()));
		OreDictionary.registerOre("scaffoldingAluminum", new ItemStack(blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_2.getMeta()));
		//Vanilla OreDict
		OreDictionary.registerOre("blockClay", new ItemStack(Blocks.CLAY));
		OreDictionary.registerOre("bricksStone", new ItemStack(Blocks.STONEBRICK));
		OreDictionary.registerOre("blockIce", new ItemStack(Blocks.ICE));
		OreDictionary.registerOre("blockPackedIce", new ItemStack(Blocks.PACKED_ICE));
		OreDictionary.registerOre("craftingTableWood", new ItemStack(Blocks.CRAFTING_TABLE));
		OreDictionary.registerOre("rodBlaze", new ItemStack(Items.BLAZE_ROD));
		OreDictionary.registerOre("charcoal", new ItemStack(Items.COAL, 1, 1));
	}

	private static ArcRecyclingThreadHandler arcRecycleThread;

	public static void init()
	{

		/*ARC FURNACE RECYCLING*/
		if(IEConfig.Machines.arcfurnace_recycle)
		{
			arcRecycleThread = new ArcRecyclingThreadHandler();
			arcRecycleThread.start();
		}

		/*MINING LEVELS*/
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

		/*WORLDGEN*/
		addConfiguredWorldgen(blockOre.getStateFromMeta(0), "copper", IEConfig.Ores.ore_copper);
		addConfiguredWorldgen(blockOre.getStateFromMeta(1), "bauxite", IEConfig.Ores.ore_bauxite);
		addConfiguredWorldgen(blockOre.getStateFromMeta(2), "lead", IEConfig.Ores.ore_lead);
		addConfiguredWorldgen(blockOre.getStateFromMeta(3), "silver", IEConfig.Ores.ore_silver);
		addConfiguredWorldgen(blockOre.getStateFromMeta(4), "nickel", IEConfig.Ores.ore_nickel);
		addConfiguredWorldgen(blockOre.getStateFromMeta(5), "uranium", IEConfig.Ores.ore_uranium);

		/*TILEENTITIES*/
		registerTile(TileEntityIESlab.class);

		registerTile(TileEntityBalloon.class);
		registerTile(TileEntityStripCurtain.class);
		registerTile(TileEntityShaderBanner.class);

		registerTile(TileEntityCokeOven.class);
		registerTile(TileEntityBlastFurnace.class);
		registerTile(TileEntityBlastFurnaceAdvanced.class);
		registerTile(TileEntityCoresample.class);
		registerTile(TileEntityAlloySmelter.class);

		registerTile(TileEntityWoodenCrate.class);
		registerTile(TileEntityWoodenBarrel.class);
		registerTile(TileEntityModWorkbench.class);
		registerTile(TileEntitySorter.class);
		registerTile(TileEntityTurntable.class);
		registerTile(TileEntityFluidSorter.class);
		registerTile(TileEntityWatermill.class);
		registerTile(TileEntityWindmill.class);
		registerTile(TileEntityWoodenPost.class);
		registerTile(TileEntityWallmount.class);

		registerTile(TileEntityLadder.class);
		registerTile(TileEntityLantern.class);
		registerTile(TileEntityRazorWire.class);
		registerTile(TileEntityToolbox.class);
		registerTile(TileEntityStructuralArm.class);

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
		registerTile(TileEntityConnectorRedstone.class);
		registerTile(TileEntityConnectorProbe.class);
		registerTile(TileEntityFeedthrough.class);

		registerTile(TileEntityCapacitorLV.class);
		registerTile(TileEntityCapacitorMV.class);
		registerTile(TileEntityCapacitorHV.class);
		registerTile(TileEntityCapacitorCreative.class);
		registerTile(TileEntityMetalBarrel.class);
		registerTile(TileEntityFluidPump.class);
		registerTile(TileEntityFluidPlacer.class);

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
		registerTile(TileEntityTurret.class);
		registerTile(TileEntityTurretChem.class);
		registerTile(TileEntityTurretGun.class);
		registerTile(TileEntityBelljar.class);

		registerTile(TileEntityConveyorBelt.class);
		registerTile(TileEntityConveyorVertical.class);

		registerTile(TileEntityMetalPress.class);
		registerTile(TileEntityCrusher.class);
		registerTile(TileEntitySheetmetalTank.class);
		registerTile(TileEntitySilo.class);
		registerTile(TileEntityAssembler.class);
		registerTile(TileEntityAutoWorkbench.class);
		registerTile(TileEntityBottlingMachine.class);
		registerTile(TileEntitySqueezer.class);
		registerTile(TileEntityFermenter.class);
		registerTile(TileEntityRefinery.class);
		registerTile(TileEntityDieselGenerator.class);
		registerTile(TileEntityBucketWheel.class);
		registerTile(TileEntityExcavator.class);
		registerTile(TileEntityArcFurnace.class);
		registerTile(TileEntityLightningrod.class);
		registerTile(TileEntityMixer.class);
		//		registerTile(TileEntitySkycrateDispenser.class);
		registerTile(TileEntityFakeLight.class);



		/*ENTITIES*/
		int i = 0;
		EntityRegistry.registerModEntity(new ResourceLocation(ImmersiveEngineering.MODID, "revolverShot"), EntityRevolvershot.class, "revolverShot", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImmersiveEngineering.MODID, "skylineHook"), EntitySkylineHook.class, "skylineHook", i++, ImmersiveEngineering.instance, 64, 1, true);
		//EntityRegistry.registerModEntity(EntitySkycrate.class, "skylineCrate", 2, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImmersiveEngineering.MODID, "revolverShotHoming"), EntityRevolvershotHoming.class, "revolverShotHoming", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImmersiveEngineering.MODID, "revolverShotWolfpack"), EntityWolfpackShot.class, "revolverShotWolfpack", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImmersiveEngineering.MODID, "chemthrowerShot"), EntityChemthrowerShot.class, "chemthrowerShot", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImmersiveEngineering.MODID, "railgunShot"), EntityRailgunShot.class, "railgunShot", i++, ImmersiveEngineering.instance, 64, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImmersiveEngineering.MODID, "revolverShotFlare"), EntityRevolvershotFlare.class, "revolverShotFlare", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImmersiveEngineering.MODID, "explosive"), EntityIEExplosive.class, "explosive", i++, ImmersiveEngineering.instance, 64, 1, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ImmersiveEngineering.MODID, "fluorescentTube"), EntityFluorescentTube.class, "fluorescentTube", i++, ImmersiveEngineering.instance, 64, 1, true);
		CapabilityShader.register();
		CapabilitySkyhookData.register();
		ShaderRegistry.itemShader = IEContent.itemShader;
		ShaderRegistry.itemShaderBag = IEContent.itemShaderBag;
		ShaderRegistry.itemExamples.add(new ItemStack(IEContent.itemRevolver));
		ShaderRegistry.itemExamples.add(new ItemStack(IEContent.itemDrill));
		ShaderRegistry.itemExamples.add(new ItemStack(IEContent.itemChemthrower));
		ShaderRegistry.itemExamples.add(new ItemStack(IEContent.itemRailgun));

		/*SMELTING*/
		itemMaterial.setBurnTime(6, 3200);
		Item itemBlockStoneDecoration = Item.getItemFromBlock(blockStoneDecoration);
		if(itemBlockStoneDecoration instanceof ItemBlockIEBase)
			((ItemBlockIEBase)itemBlockStoneDecoration).setBurnTime(3, 3200*10);

		/*BANNERS*/
		addBanner("hammer", "hmr", new ItemStack(itemTool, 1, 0));
		addBanner("bevels", "bvl", "plateIron");
		addBanner("ornate", "orn", "dustSilver");
		addBanner("treated_wood", "twd", "plankTreatedWood");
		addBanner("windmill", "wnd", new ItemStack[]{new ItemStack(blockWoodenDevice1, 1, BlockTypes_WoodenDevice1.WINDMILL.getMeta())});
		if(!BulletHandler.homingCartridges.isEmpty())
		{
			ItemStack wolfpackCartridge = BulletHandler.getBulletStack("wolfpack");
			addBanner("wolf_r", "wlfr", wolfpackCartridge, 1);
			addBanner("wolf_l", "wlfl", wolfpackCartridge, -1);
			addBanner("wolf", "wlf", wolfpackCartridge, 0, 0);
		}

		/*ASSEMBLER RECIPE ADAPTERS*/
		//Fluid Ingredients
		AssemblerHandler.registerSpecialQueryConverters((o) ->
		{
			if(o instanceof IngredientFluidStack)
				return new RecipeQuery(((IngredientFluidStack)o).getFluid(), ((IngredientFluidStack)o).getFluid().amount);
			else return null;
		});
		//Potion bullets
		AssemblerHandler.registerRecipeAdapter(RecipePotionBullets.class, new IRecipeAdapter<RecipePotionBullets>()
		{
			@Nullable
			@Override
			public RecipeQuery[] getQueriedInputs(RecipePotionBullets recipe, NonNullList<ItemStack> input)
			{
				RecipeQuery bullet = null;
				RecipeQuery potion = null;
				for(int i = 0; i < input.size()-1; ++i)
				{
					ItemStack s = input.get(i);
					if(!s.isEmpty())
					{
						if(bullet==null&&RecipePotionBullets.isPotionBullet(s))
							bullet = AssemblerHandler.createQueryFromItemStack(s);
						else if(potion==null&&s.getItem() instanceof ItemPotion)
							potion = AssemblerHandler.createQuery(
									MixerPotionHelper.getFluidStackForType(PotionUtils.getPotionFromItem(s), 250));
						else
							return null;
					}
				}
				if(bullet==null||potion==null)
					return null;
				return new RecipeQuery[]{bullet, potion};
			}
		});

		DieselHandler.registerFuel(fluidBiodiesel, 125);
		DieselHandler.registerFuel(FluidRegistry.getFluid("fuel"), 375);
		DieselHandler.registerFuel(FluidRegistry.getFluid("diesel"), 175);
		DieselHandler.registerDrillFuel(fluidBiodiesel);
		DieselHandler.registerDrillFuel(FluidRegistry.getFluid("fuel"));
		DieselHandler.registerDrillFuel(FluidRegistry.getFluid("diesel"));

		blockFluidCreosote.setPotionEffects(new PotionEffect(IEPotions.flammable, 100, 0));
		blockFluidEthanol.setPotionEffects(new PotionEffect(MobEffects.NAUSEA, 40, 0));
		blockFluidBiodiesel.setPotionEffects(new PotionEffect(IEPotions.flammable, 100, 1));
		blockFluidConcrete.setPotionEffects(new PotionEffect(MobEffects.SLOWNESS, 20, 3, false, false));

		ChemthrowerHandler.registerEffect(FluidRegistry.WATER, new ChemthrowerEffect_Extinguish());

		ChemthrowerHandler.registerEffect(fluidPotion, new ChemthrowerEffect()
		{
			@Override
			public void applyToEntity(EntityLivingBase target, @Nullable EntityPlayer shooter, ItemStack thrower, FluidStack fluid)
			{
				if(fluid.tag!=null)
				{
					List<PotionEffect> effects = PotionUtils.getEffectsFromTag(fluid.tag);
					for(PotionEffect e : effects)
					{
						PotionEffect newEffect = new PotionEffect(e.getPotion(), (int)Math.ceil(e.getDuration()*.05), e.getAmplifier());
						newEffect.setCurativeItems(new ArrayList(e.getCurativeItems()));
						target.addPotionEffect(newEffect);
					}
				}
			}

			@Override
			public void applyToEntity(EntityLivingBase target, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid)
			{
			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable EntityPlayer shooter, ItemStack thrower, FluidStack fluid)
			{

			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid)
			{
			}
		});

		ChemthrowerHandler.registerEffect(fluidConcrete, new ChemthrowerEffect()
		{
			@Override
			public void applyToEntity(EntityLivingBase target, @Nullable EntityPlayer shooter, ItemStack thrower, FluidStack fluid)
			{
				hit(target.world, target.getPosition(), EnumFacing.UP);
			}

			@Override
			public void applyToEntity(EntityLivingBase target, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid)
			{
			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable EntityPlayer shooter, ItemStack thrower, FluidStack fluid)
			{
				IBlockState hit = world.getBlockState(mop.getBlockPos());
				if(hit.getBlock()!=blockStoneDecoration||hit.getBlock().getMetaFromState(hit)!=BlockTypes_StoneDecoration.CONCRETE_SPRAYED.getMeta())
				{
					BlockPos pos = mop.getBlockPos().offset(mop.sideHit);
					if(!world.isAirBlock(pos))
						return;
					AxisAlignedBB aabb = new AxisAlignedBB(pos);
					List<EntityChemthrowerShot> otherProjectiles = world.getEntitiesWithinAABB(EntityChemthrowerShot.class, aabb);
					if(otherProjectiles.size() >= 8)
						hit(world, pos, mop.sideHit);
				}
			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid)
			{
			}

			private void hit(World world, BlockPos pos, EnumFacing side)
			{
				AxisAlignedBB aabb = new AxisAlignedBB(pos);
				List<EntityChemthrowerShot> otherProjectiles = world.getEntitiesWithinAABB(EntityChemthrowerShot.class, aabb);
				for(EntityChemthrowerShot shot : otherProjectiles)
					shot.setDead();
				world.setBlockState(pos, blockStoneDecoration.getStateFromMeta(BlockTypes_StoneDecoration.CONCRETE_SPRAYED.getMeta()));
				for(EntityLivingBase living : world.getEntitiesWithinAABB(EntityLivingBase.class, aabb))
					living.addPotionEffect(new PotionEffect(IEPotions.concreteFeet, Integer.MAX_VALUE));
			}
		});

		ChemthrowerHandler.registerEffect(fluidCreosote, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 140, 0));
		ChemthrowerHandler.registerFlammable(fluidCreosote);
		ChemthrowerHandler.registerEffect(fluidBiodiesel, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 140, 1));
		ChemthrowerHandler.registerFlammable(fluidBiodiesel);
		ChemthrowerHandler.registerFlammable(fluidEthanol);
		ChemthrowerHandler.registerEffect("oil", new ChemthrowerEffect_Potion(null, 0, new PotionEffect(IEPotions.flammable, 140, 0), new PotionEffect(MobEffects.BLINDNESS, 80, 1)));
		ChemthrowerHandler.registerFlammable("oil");
		ChemthrowerHandler.registerEffect("fuel", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 100, 1));
		ChemthrowerHandler.registerFlammable("fuel");
		ChemthrowerHandler.registerEffect("diesel", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 140, 1));
		ChemthrowerHandler.registerFlammable("diesel");
		ChemthrowerHandler.registerEffect("kerosene", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 100, 1));
		ChemthrowerHandler.registerFlammable("kerosene");
		ChemthrowerHandler.registerEffect("biofuel", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 140, 1));
		ChemthrowerHandler.registerFlammable("biofuel");
		ChemthrowerHandler.registerEffect("rocket_fuel", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 60, 2));
		ChemthrowerHandler.registerFlammable("rocket_fuel");

		RailgunHandler.registerProjectileProperties(new IngredientStack("stickIron"), 15, 1.25).setColourMap(new int[][]{{0xd8d8d8, 0xd8d8d8, 0xd8d8d8, 0xa8a8a8, 0x686868, 0x686868}});
		RailgunHandler.registerProjectileProperties(new IngredientStack("stickAluminum"), 13, 1.05).setColourMap(new int[][]{{0xd8d8d8, 0xd8d8d8, 0xd8d8d8, 0xa8a8a8, 0x686868, 0x686868}});
		RailgunHandler.registerProjectileProperties(new IngredientStack("stickSteel"), 18, 1.25).setColourMap(new int[][]{{0xb4b4b4, 0xb4b4b4, 0xb4b4b4, 0x7a7a7a, 0x555555, 0x555555}});
		RailgunHandler.registerProjectileProperties(new ItemStack(itemGraphiteElectrode), 24, .9).setColourMap(new int[][]{{0x242424, 0x242424, 0x242424, 0x171717, 0x171717, 0x0a0a0a}});

		ExternalHeaterHandler.defaultFurnaceEnergyCost = IEConfig.Machines.heater_consumption;
		ExternalHeaterHandler.defaultFurnaceSpeedupCost = IEConfig.Machines.heater_speedupConsumption;
		ExternalHeaterHandler.registerHeatableAdapter(TileEntityFurnace.class, new DefaultFurnaceAdapter());

		BelljarHandler.DefaultPlantHandler hempBelljarHandler = new BelljarHandler.DefaultPlantHandler()
		{
			private HashSet<ComparableItemStack> validSeeds = new HashSet<>();

			@Override
			protected HashSet<ComparableItemStack> getSeedSet()
			{
				return validSeeds;
			}

			@Override
			@SideOnly(Side.CLIENT)
			public IBlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
			{
				int age = Math.min(4, Math.round(growth*4));
				if(age==4)
					return new IBlockState[]{blockCrop.getStateFromMeta(age), blockCrop.getStateFromMeta(age+1)};
				return new IBlockState[]{blockCrop.getStateFromMeta(age)};
			}

			@Override
			@SideOnly(Side.CLIENT)
			public float getRenderSize(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
			{
				return .6875f;
			}
		};
		BelljarHandler.registerHandler(hempBelljarHandler);
		hempBelljarHandler.register(new ItemStack(itemSeeds), new ItemStack[]{new ItemStack(itemMaterial, 4, 4), new ItemStack(itemSeeds, 2)}, new ItemStack(Blocks.DIRT), blockCrop.getDefaultState());

		ThermoelectricHandler.registerSource(new IngredientStack(new ItemStack(Blocks.MAGMA)), 1300);
		ThermoelectricHandler.registerSourceInKelvin("blockIce", 273);
		ThermoelectricHandler.registerSourceInKelvin("blockPackedIce", 200);
		ThermoelectricHandler.registerSourceInKelvin("blockUranium", 2000);
		ThermoelectricHandler.registerSourceInKelvin("blockYellorium", 2000);
		ThermoelectricHandler.registerSourceInKelvin("blockPlutonium", 4000);
		ThermoelectricHandler.registerSourceInKelvin("blockBlutonium", 4000);

		/*MULTIBLOCKS*/
		MultiblockHandler.registerMultiblock(MultiblockCokeOven.instance);
		MultiblockHandler.registerMultiblock(MultiblockAlloySmelter.instance);
		MultiblockHandler.registerMultiblock(MultiblockBlastFurnace.instance);
		MultiblockHandler.registerMultiblock(MultiblockBlastFurnaceAdvanced.instance);
		MultiblockHandler.registerMultiblock(MultiblockMetalPress.instance);
		MultiblockHandler.registerMultiblock(MultiblockCrusher.instance);
		MultiblockHandler.registerMultiblock(MultiblockSheetmetalTank.instance);
		MultiblockHandler.registerMultiblock(MultiblockSilo.instance);
		MultiblockHandler.registerMultiblock(MultiblockAssembler.instance);
		MultiblockHandler.registerMultiblock(MultiblockAutoWorkbench.instance);
		MultiblockHandler.registerMultiblock(MultiblockBottlingMachine.instance);
		MultiblockHandler.registerMultiblock(MultiblockSqueezer.instance);
		MultiblockHandler.registerMultiblock(MultiblockFermenter.instance);
		MultiblockHandler.registerMultiblock(MultiblockRefinery.instance);
		MultiblockHandler.registerMultiblock(MultiblockDieselGenerator.instance);
		MultiblockHandler.registerMultiblock(MultiblockExcavator.instance);
		MultiblockHandler.registerMultiblock(MultiblockBucketWheel.instance);
		MultiblockHandler.registerMultiblock(MultiblockArcFurnace.instance);
		MultiblockHandler.registerMultiblock(MultiblockLightningrod.instance);
		MultiblockHandler.registerMultiblock(MultiblockMixer.instance);
		MultiblockHandler.registerMultiblock(MultiblockFeedthrough.instance);

		/*VILLAGE*/
		IEVillagerHandler.initIEVillagerHouse();
		IEVillagerHandler.initIEVillagerTrades();

		/*LOOT*/
		if(IEConfig.villagerHouse)
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

		/*BLOCK ITEMS FROM CRATES*/
		IEApi.forbiddenInCrates.add((stack) -> {
			if(stack.getItem()==IEContent.itemToolbox)
				return true;
			if(stack.getItem()==IEContent.itemToolbox)
				return true;
			if(OreDictionary.itemMatches(new ItemStack(IEContent.blockWoodenDevice0, 1, 0), stack, true))
				return true;
			if(OreDictionary.itemMatches(new ItemStack(IEContent.blockWoodenDevice0, 1, 5), stack, true))
				return true;
			return stack.getItem() instanceof ItemShulkerBox;
		});

		TileEntityFluidPipe.initCovers();
		IEDataFixers.register();
	}

	public static void postInit()
	{
		/*POTIONS*/
		try
		{
			//Blame Forge for this mess. They stopped ATs from working on MixPredicate and its fields by modifying them with patches
			//without providing a usable way to look up the vanilla potion recipes
			String mixPredicateName = "net.minecraft.potion.PotionHelper$MixPredicate";
			Class<?> mixPredicateClass = Class.forName(mixPredicateName);
			Field output = ReflectionHelper.findField(mixPredicateClass,
					ObfuscationReflectionHelper.remapFieldNames(mixPredicateName, "field_185200_c"));
			Field reagent = ReflectionHelper.findField(mixPredicateClass,
					ObfuscationReflectionHelper.remapFieldNames(mixPredicateName, "field_185199_b"));
			Field input = ReflectionHelper.findField(mixPredicateClass,
					ObfuscationReflectionHelper.remapFieldNames(mixPredicateName, "field_185198_a"));
			output.setAccessible(true);
			reagent.setAccessible(true);
			input.setAccessible(true);
			for(Object mixPredicate : PotionHelper.POTION_TYPE_CONVERSIONS)
				//noinspection unchecked
				MixerPotionHelper.registerPotionRecipe(((IRegistryDelegate<PotionType>)output.get(mixPredicate)).get(),
						((IRegistryDelegate<PotionType>)input.get(mixPredicate)).get(),
						ApiUtils.createIngredientStack(reagent.get(mixPredicate)));
		} catch(Exception x)
		{
			x.printStackTrace();
		}
		for(IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes())
			if(recipe instanceof AbstractBrewingRecipe)
			{
				IngredientStack ingredientStack = ApiUtils.createIngredientStack(((AbstractBrewingRecipe)recipe).getIngredient());
				ItemStack input = ((AbstractBrewingRecipe)recipe).getInput();
				ItemStack output = ((AbstractBrewingRecipe)recipe).getOutput();
				if(input.getItem()==Items.POTIONITEM&&output.getItem()==Items.POTIONITEM)
					MixerPotionHelper.registerPotionRecipe(PotionUtils.getPotionFromItem(output), PotionUtils.getPotionFromItem(input), ingredientStack);
			}
		if(arcRecycleThread!=null)
		{
			try
			{
				arcRecycleThread.join();
				arcRecycleThread.finishUp();
			} catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void refreshFluidReferences()
	{
		fluidCreosote = FluidRegistry.getFluid("creosote");
		fluidPlantoil = FluidRegistry.getFluid("plantoil");
		fluidEthanol = FluidRegistry.getFluid("ethanol");
		fluidBiodiesel = FluidRegistry.getFluid("biodiesel");
		fluidConcrete = FluidRegistry.getFluid("concrete");
		fluidPotion = FluidRegistry.getFluid("potion");
	}

	public static void registerToOreDict(String type, ItemIEBase item, int... metas)
	{
		if(metas==null||metas.length < 1)
		{
			for(int meta = 0; meta < item.getSubNames().length; meta++)
				if(!item.isMetaHidden(meta))
				{
					String name = item.getSubNames()[meta];
					name = createOreDictName(name);
					if(type!=null&&!type.isEmpty())
						name = name.substring(0, 1).toUpperCase()+name.substring(1);
					OreDictionary.registerOre(type+name, new ItemStack(item, 1, meta));
				}
		}
		else
		{
			for(int meta : metas)
				if(!item.isMetaHidden(meta))
				{
					String name = item.getSubNames()[meta];
					name = createOreDictName(name);
					if(type!=null&&!type.isEmpty())
						name = name.substring(0, 1).toUpperCase()+name.substring(1);
					OreDictionary.registerOre(type+name, new ItemStack(item, 1, meta));
				}
		}
	}

	private static String createOreDictName(String name)
	{
		String upperName = name.toUpperCase();
		StringBuilder sb = new StringBuilder();
		boolean nextCapital = false;
		for(int i = 0; i < name.length(); i++)
		{
			if(name.charAt(i)=='_')
			{
				nextCapital = true;
			}
			else
			{
				char nextChar = name.charAt(i);
				if(nextCapital)
				{
					nextChar = upperName.charAt(i);
					nextCapital = false;
				}
				sb.append(nextChar);
			}
		}
		return sb.toString();
	}

	public static void registerToOreDict(String type, BlockIEBase item, int... metas)
	{
		if(metas==null||metas.length < 1)
		{
			for(int meta = 0; meta < item.getMetaEnums().length; meta++)
				if(!item.isMetaHidden(meta))
				{
					String name = item.getMetaEnums()[meta].toString();
					if(type!=null&&!type.isEmpty())
						name = name.substring(0, 1).toUpperCase(Locale.ENGLISH)+name.substring(1).toLowerCase(Locale.ENGLISH);
					OreDictionary.registerOre(type+name, new ItemStack(item, 1, meta));
				}
		}
		else
		{
			for(int meta : metas)
				if(!item.isMetaHidden(meta))
				{
					String name = item.getMetaEnums()[meta].toString();
					if(type!=null&&!type.isEmpty())
						name = name.substring(0, 1).toUpperCase(Locale.ENGLISH)+name.substring(1).toLowerCase(Locale.ENGLISH);
					OreDictionary.registerOre(type+name, new ItemStack(item, 1, meta));
				}
		}
	}

	public static void registerOre(String type, ItemStack ore, ItemStack ingot, ItemStack dust, ItemStack nugget, ItemStack plate, ItemStack block, ItemStack slab, ItemStack sheet, ItemStack slabSheet)
	{
		if(!ore.isEmpty())
			OreDictionary.registerOre("ore"+type, ore);
		if(!ingot.isEmpty())
			OreDictionary.registerOre("ingot"+type, ingot);
		if(!dust.isEmpty())
			OreDictionary.registerOre("dust"+type, dust);
		if(!nugget.isEmpty())
			OreDictionary.registerOre("nugget"+type, nugget);
		if(!plate.isEmpty())
			OreDictionary.registerOre("plate"+type, plate);
		if(!block.isEmpty())
			OreDictionary.registerOre("block"+type, block);
		if(!slab.isEmpty())
			OreDictionary.registerOre("slab"+type, slab);
		if(!sheet.isEmpty())
			OreDictionary.registerOre("blockSheetmetal"+type, sheet);
		if(!slabSheet.isEmpty())
			OreDictionary.registerOre("slabSheetmetal"+type, slabSheet);
	}

	public static void registerTile(Class<? extends TileEntity> tile)
	{
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity")+"TileEntity".length());
		GameRegistry.registerTileEntity(tile, ImmersiveEngineering.MODID+":"+s);
		registeredIETiles.add(tile);
	}

	public static void addConfiguredWorldgen(IBlockState state, String name, int[] config)
	{
		if(config!=null&&config.length >= 5&&config[0] > 0)
			IEWorldGen.addOreGen(name, state, config[0], config[1], config[2], config[3], config[4]);
	}

	public static void addBanner(String name, String id, Object item, int... offset)
	{
		name = ImmersiveEngineering.MODID+"_"+name;
		id = "ie_"+id;
		ItemStack craftingStack = ItemStack.EMPTY;
		if(item instanceof ItemStack&&(offset==null||offset.length < 1))
			craftingStack = (ItemStack)item;
		BannerPattern e = EnumHelper.addEnum(BannerPattern.class, name.toUpperCase(), new Class[]{String.class, String.class, ItemStack.class}, name, id, craftingStack);
		if(craftingStack.isEmpty())
			RecipeBannerAdvanced.addAdvancedPatternRecipe(e, ApiUtils.createIngredientStack(item), offset);
	}
}
