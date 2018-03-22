//package blusunrize.immersiveengineering.common.util;
//
//import blusunrize.immersiveengineering.ImmersiveEngineering;
//import blusunrize.immersiveengineering.api.tool.BulletHandler;
//import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
//import blusunrize.immersiveengineering.common.IEContent;
//import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0;
//import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
//import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
//import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDevices;
//import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDevice0;
//import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDevice1;
//import blusunrize.immersiveengineering.common.items.ItemDrill;
//import blusunrize.immersiveengineering.common.items.ItemRevolver;
//import com.google.common.collect.Maps;
//import net.minecraft.advancements.Advancement;
//import net.minecraft.advancements.AdvancementRewards;
//import net.minecraft.advancements.DisplayInfo;
//import net.minecraft.advancements.FrameType;
//import net.minecraft.block.Block;
//import net.minecraft.init.Enchantments;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.stats.Achievement;
//import net.minecraft.stats.StatBase;
//import net.minecraft.stats.StatList;
//import net.minecraft.util.FrameTimer;
//import net.minecraft.util.NonNullList;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.text.TextComponentTranslation;
//import net.minecraftforge.common.AchievementPage;
//import net.minecraftforge.fml.common.registry.ForgeRegistries;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class IEAchievements
//{
//	public static AchievementPage ieAchievementPage;
//
//	public static Achievement openManual;//0,0
//	public static Achievement craftHammer;//3,1
//	public static Achievement connectWire;//0,-2
//	public static Achievement blastfurnace;//2,0
//	public static Achievement makeSteel;//2,-2
//
//	public static Achievement placeConveyor;//1,2
//	public static Achievement placeWindmill;//1,-1
//	public static Achievement craftHeater;//2,-2
//	public static Achievement craftPump;//2,3
//	public static Achievement placeFloodlight;//2,3
//
//	public static Achievement craftWorkbench;
//	public static Achievement craftRevolver;
//	public static Achievement upgradeRevolver;
//	public static Achievement craftDrill;
//	public static Achievement upgradeDrill;
//	public static Achievement craftSkyhook;
//	public static Achievement skyhookPro;
//	public static Achievement craftManeuverGear;
//	public static Achievement craftChemthrower;
//	public static Achievement craftRailgun;
//	public static Achievement craftWolfPack;
//
//	public static Achievement mbImprovedBlastFurnace;
//	public static Achievement mbMetalPress;
//	public static Achievement mbSilo;
//	public static Achievement mbCrusher;
//	public static Achievement mbDieselGen;
//	public static Achievement mbExcavator;
//	public static Achievement mbArcFurnace;
//
//	public static Achievement secret_birthdayParty;
//	public static Achievement secret_luckOfTheDraw;
//
//	public static StatBase statDistanceSkyhook;
//
//	public static void init()
//	{
//		openManual = new AchievementIE("openManual", 0, 1, new ItemStack(IEItems.Tool,1,3), null);
//		craftHammer = new AchievementIE("craftHammer", 3, 2, IEItems.Tool, openManual).setNormalCrafting();
//		connectWire = new AchievementIE("connectWire", 0, -1, IEItems.WireCoil, openManual);
//		blastfurnace = new AchievementIE("blastfurnace", 2, 1, new ItemStack(IEContent.blockStoneDevice,1,BlockTypes_StoneDevices.BLAST_FURNACE.getMeta()), craftHammer);
//		makeSteel = new AchievementIE("makeSteel", 2, -1, new ItemStack(IEItems.Metal,1,8), blastfurnace);
//
//		placeConveyor = new AchievementIE("placeConveyor", 1, 3, ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID + ":conveyor"), openManual).setPlacement(new ItemStack(IEContent.blockConveyor));
//		placeWindmill = new AchievementIE("placeWindmill", -1, 3, new ItemStack(IEContent.blockWoodenDevice1,1,BlockTypes_WoodenDevice1.WINDMILL.getMeta()), openManual).setPlacement(new ItemStack(IEContent.blockWoodenDevice1,1,BlockTypes_WoodenDevice1.WINDMILL.getMeta()));
//		craftHeater = new AchievementIE("craftHeater", -2, 4, new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FURNACE_HEATER.getMeta()), openManual).setNormalCrafting();
//		craftPump = new AchievementIE("craftPump", 2, 4, new ItemStack(IEContent.blockMetalDevice0, 1, BlockTypes_MetalDevice0.FLUID_PUMP.getMeta()), openManual).setNormalCrafting();
//		placeFloodlight = new AchievementIE("placeFloodlight", -1, 5, new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.FLOODLIGHT.getMeta()), openManual).setPlacement();
//		craftWorkbench = new AchievementIE("craftWorkbench", 1, 5, new ItemStack(IEContent.blockWoodenDevice0,1,BlockTypes_WoodenDevice0.WORKBENCH.getMeta()), openManual).setNormalCrafting();
//
//		mbImprovedBlastFurnace = new AchievementIE("mbImprovedBlastFurnace", 6, -3, new ItemStack(IEContent.blockStoneDevice,1,BlockTypes_StoneDevices.BLAST_FURNACE_ADVANCED.getMeta()), makeSteel).setSpecial();
//		mbMetalPress = new AchievementIE("mbMetalPress", 6, -2, new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.METAL_PRESS.getMeta()), makeSteel).setSpecial();
//		mbCrusher = new AchievementIE("mbCrusher", 6, -1, new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.CRUSHER.getMeta()), makeSteel).setSpecial();
//		mbSilo = new AchievementIE("mbSilo", 7, -2, new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.SILO.getMeta()), craftHammer).setSpecial();
//		mbDieselGen = new AchievementIE("mbDieselGen", 6, 0, new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.DIESEL_GENERATOR.getMeta()), craftHammer).setSpecial();
//		mbExcavator = new AchievementIE("mbExcavator", 6, 1, new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.EXCAVATOR.getMeta()), craftHammer).setSpecial();
//		mbArcFurnace = new AchievementIE("mbArcFurnace", 6, 2, new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.ARC_FURNACE.getMeta()), craftHammer).setSpecial();
//
//		craftRevolver = new AchievementIE("craftRevolver", 3, -6, IEItems.Revolver, makeSteel).setNormalCrafting();
//		NonNullList<ItemStack> upgrades = NonNullList.withSize(20, ItemStack.EMPTY);
//		upgrades.set(18, new ItemStack(IEItems.ToolUpgrades,1,5));
//		upgrades.set(19, new ItemStack(IEItems.ToolUpgrades,1,6));
//		ItemStack revolver = new ItemStack(IEItems.Revolver);
//		((ItemRevolver)IEItems.Revolver).setContainedItems(revolver, upgrades);
//		((ItemRevolver)IEItems.Revolver).recalculateUpgrades(revolver);
//		upgradeRevolver = new AchievementIE("upgradeRevolver", 4, -6, revolver, craftRevolver);
//		if(!BulletHandler.homingCartridges.isEmpty())
//			craftWolfPack = new AchievementIE("craftWolfPack", 4, -7, BulletHandler.getBulletStack("wolfpack"), craftRevolver).setCheckNBT(true).setBlueprintCrafting().setSpecial();
//
//		ItemStack drill = new ItemStack(IEItems.Drill);
//		((ItemDrill)IEItems.Drill).setHead(drill, new ItemStack(IEItems.Drillhead));
//		craftDrill = new AchievementIE("craftDrill", 1, -6, drill, makeSteel).setNormalCrafting();
//		upgrades = NonNullList.withSize(4, ItemStack.EMPTY);
//		upgrades.set(0, new ItemStack(IEItems.Drillhead));
//		upgrades.set(1, new ItemStack(IEItems.ToolUpgrades,1,0));
//		upgrades.set(2, new ItemStack(IEItems.ToolUpgrades,1,1));
//		upgrades.set(3, new ItemStack(IEItems.ToolUpgrades,3,2));
//		ItemStack drill2 = drill.copy();
//		((ItemDrill)IEItems.Drill).setContainedItems(drill2, upgrades);
//		((ItemDrill)IEItems.Drill).recalculateUpgrades(drill2);
//		upgradeDrill = new AchievementIE("upgradeDrill", 0, -6, drill2, craftDrill);
//
//		craftSkyhook = new AchievementIE("craftSkyhook", 1, -5, IEItems.Skyhook, makeSteel).setNormalCrafting();
//		ItemStack hook = new ItemStack(IEItems.Skyhook);
//		hook.addEnchantment(Enchantments.UNBREAKING, 1);
//		//skyhookPro = new AchievementIE("skyhookPro", 0, -5, hook, craftSkyhook);
//
//
//		craftChemthrower = new AchievementIE("craftChemthrower", 3, -5, IEItems.Chemthrower, makeSteel).setNormalCrafting();
//
//		craftRailgun = new AchievementIE("craftRailgun", 1, -7, IEItems.Railgun, makeSteel).setNormalCrafting();
//
//		secret_birthdayParty = new AchievementIE("secret_birthdayParty", -4,-1, new ItemStack(IEItems.FakeIcons,1,0), null).setSpecial();
//		secret_luckOfTheDraw = new AchievementIE("secret_luckOfTheDraw", -4, 1, new ItemStack(IEItems.FakeIcons,1,1), null).setSpecial();
//
//		ieAchievementPage = new AchievementPage(ImmersiveEngineering.MODNAME, AchievementIE.achievements.toArray(new Achievement[AchievementIE.achievements.size()]));
//		AchievementPage.registerAchievementPage(ieAchievementPage);
//
//		statDistanceSkyhook = new StatBase("stat.skyhookOneCm", new TextComponentTranslation("stat.skyhookOneCm", new Object[0]), StatBase.distanceStatType)
//		{
//			@Override
//			public StatBase registerStat()
//			{
//				super.registerStat();
//				StatList.BASIC_STATS.add(12,this);
//				return this;
//			}
//		}.initIndependentStat().registerStat();
//
//	}
//
//	public static class AchievementIE extends Advancement
//	{
//		public static List<Advancement> achievements = new ArrayList();
//		private final static String TRANSLATION_KEY = "achievement.immersiveengineering.";
//		public AchievementIE(String name, Object icon, Advancement parent)
//		{
//			super(new ResourceLocation(ImmersiveEngineering.MODID,name), parent, displayInfo(name, icon, FrameType.TASK), AdvancementRewards.EMPTY, Maps.newHashMap(), new String[0][0]);
//			achievements.add(this);
//			ForgeRegistries.
//		}
//
//		private static DisplayInfo displayInfo(String name, Object icon, FrameType frame)
//		{
//			if(icon instanceof ItemStack)
//				return new DisplayInfo((ItemStack)icon, new TextComponentTranslation(TRANSLATION_KEY+name), new TextComponentTranslation(TRANSLATION_KEY+name+".desc"), null, frame, true,true,false);
//			else if(icon instanceof Item)
//				return new DisplayInfo(new ItemStack((Item)icon), new TextComponentTranslation(TRANSLATION_KEY+name), new TextComponentTranslation(TRANSLATION_KEY+name+".desc"), null, frame, true,true,false);
//			else if(icon instanceof Block)
//				return new DisplayInfo(new ItemStack((Block)icon), new TextComponentTranslation(TRANSLATION_KEY+name), new TextComponentTranslation(TRANSLATION_KEY+name+".desc"), null, frame, true,true,false);
//			return new DisplayInfo(ItemStack.EMPTY, new TextComponentTranslation(TRANSLATION_KEY+name), new TextComponentTranslation(TRANSLATION_KEY+name+".desc"), null, frame, true,true,false);
//		}
//
//		public ItemStack[] triggerItems;
//		public boolean checkNBT = false;
//		public AchievementIE setCheckNBT(boolean checkNBT)
//		{
//			this.checkNBT = checkNBT;
//			return this;
//		}
//		public AchievementIE setNormalCrafting(ItemStack... triggerItems)
//		{
//			this.triggerItems = triggerItems;
//			normalCraftingAchievements.add(this);
//			return this;
//		}
//		public AchievementIE setBlueprintCrafting(ItemStack... triggerItems)
//		{
//			this.triggerItems = triggerItems;
//			blueprintCraftingAchievements.add(this);
//			return this;
//		}
//		public AchievementIE setPlacement(ItemStack... triggerItems)
//		{
//			this.triggerItems = triggerItems;
//			placementAchievements.add(this);
//			return this;
//		}
//	}
//
//	public static ArrayList<AchievementIE> normalCraftingAchievements = new ArrayList();
//	public static ArrayList<AchievementIE> blueprintCraftingAchievements = new ArrayList();
//	public static ArrayList<AchievementIE> placementAchievements = new ArrayList();
//}