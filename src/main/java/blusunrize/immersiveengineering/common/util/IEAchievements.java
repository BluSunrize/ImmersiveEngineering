package blusunrize.immersiveengineering.common.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.AchievementPage;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import cpw.mods.fml.common.Loader;

public class IEAchievements
{
	public static AchievementPage ieAchievementPage;

	public static Achievement openManual;
	public static Achievement craftHammer;
	public static Achievement connectWire;
	public static Achievement blastfurnace;
	public static Achievement makeSteel;

	public static Achievement makeRevolver;
	public static Achievement upgradeRevolver;
	public static Achievement makeDrill;
	public static Achievement upgradeDrill;
	public static Achievement makeSkyhook;
	public static Achievement skyhookPro;
	public static Achievement makeWolfPack;

	public static Achievement mbSilo;
	public static Achievement mbCrusher;
	public static Achievement mbDieselGen;
	public static Achievement mbExcavator;
	public static Achievement mbArcFurnace;
	
	public static Achievement birthdayParty;
	
	public static StatBase statDistanceSkyhook;

	public static void init()
	{
		openManual = new AchievementIE("openManual", 0, 0, new ItemStack(IEContent.itemTool,1,3), null);
		craftHammer = new AchievementIE("craftHammer", 3, 1, IEContent.itemTool, openManual);
		connectWire = new AchievementIE("connectWire", 0, -2, IEContent.itemWireCoil, openManual);
		blastfurnace = new AchievementIE("blastfurnace", 2, 0, new ItemStack(IEContent.blockStoneDevice,1,2), craftHammer);
		makeSteel = new AchievementIE("makeSteel", 2, -2, new ItemStack(IEContent.itemMetal,1,7), blastfurnace);
		
		//		openManual = new AchievementIE("", 0, 0, new ItemStack(IEContent.itemTool,1,3), null);
		//		openManual = new AchievementIE("", 0, 0, new ItemStack(IEContent.itemTool,1,3), null);
		//		openManual = new AchievementIE("", 0, 0, new ItemStack(IEContent.itemTool,1,3), null);
		mbSilo = new AchievementIE("mbSilo", 6, -3, new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_silo), craftHammer).setSpecial();
		mbCrusher = new AchievementIE("mbCrusher", 6, -2, new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_crusher), craftHammer).setSpecial();
		mbDieselGen = new AchievementIE("mbDieselGen", 6, -1, new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_dieselGenerator), craftHammer).setSpecial();
		mbExcavator = new AchievementIE("mbExcavator", 6, 0, new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_excavator), craftHammer).setSpecial();
		mbArcFurnace = new AchievementIE("mbArcFurnace", 6, 1, new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_arcFurnace), craftHammer).setSpecial();
	
		makeRevolver = new AchievementIE("makeRevolver", 3, -5, IEContent.itemRevolver, makeSteel);
		ItemStack[] upgrades = new ItemStack[20];
		upgrades[18]=new ItemStack(IEContent.itemToolUpgrades,1,5);
		upgrades[19]=new ItemStack(IEContent.itemToolUpgrades,1,6);
		ItemStack revolver = new ItemStack(IEContent.itemRevolver);
		((ItemRevolver)IEContent.itemRevolver).setContainedItems(revolver, upgrades);
		((ItemRevolver)IEContent.itemRevolver).recalculateUpgrades(revolver);
		upgradeRevolver = new AchievementIE("upgradeRevolver", 4, -5, revolver, makeRevolver);
		if(Loader.isModLoaded("Botania"))
			makeWolfPack = new AchievementIE("makeWolfPack", 4, -6, new ItemStack(IEContent.itemBullet,1,8), makeRevolver).setSpecial();
		
		ItemStack drill = new ItemStack(IEContent.itemDrill);
		((ItemDrill)IEContent.itemDrill).setHead(drill, new ItemStack(IEContent.itemDrillhead));
		makeDrill = new AchievementIE("makeDrill", 1, -5, drill, makeSteel);
		upgrades = new ItemStack[4];
		upgrades[0]=new ItemStack(IEContent.itemDrillhead);
		upgrades[1]=new ItemStack(IEContent.itemToolUpgrades,1,0);
		upgrades[2]=new ItemStack(IEContent.itemToolUpgrades,1,1);
		upgrades[3]=new ItemStack(IEContent.itemToolUpgrades,3,2);
		ItemStack drill2 = drill.copy();
		((ItemDrill)IEContent.itemDrill).setContainedItems(drill2, upgrades);
		((ItemDrill)IEContent.itemDrill).recalculateUpgrades(drill2);
		upgradeDrill = new AchievementIE("upgradeDrill", 0, -5, drill2, makeDrill);
		
		makeSkyhook = new AchievementIE("makeSkyhook", 1, -4, IEContent.itemSkyhook, makeSteel);
		ItemStack hook = new ItemStack(IEContent.itemSkyhook);
		hook.addEnchantment(Enchantment.unbreaking, 1);
		skyhookPro = new AchievementIE("skyhookPro", 0, -4, hook, makeSkyhook);
		
		birthdayParty = new AchievementIE("secret_birthdayParty", -4,-2, IEContent.itemFakeIcons, null).setSpecial();
		
		ieAchievementPage = new AchievementPage(ImmersiveEngineering.MODNAME, AchievementIE.achievements.toArray(new Achievement[AchievementIE.achievements.size()]));
		AchievementPage.registerAchievementPage(ieAchievementPage);
	
		statDistanceSkyhook = new StatBase("stat.skyhookOneCm", new ChatComponentTranslation("stat.skyhookOneCm", new Object[0]), StatBase.distanceStatType)
		{
			@Override
		    public StatBase registerStat()
		    {
		        super.registerStat();
		        StatList.generalStats.add(12,this);
		        return this;
		    }
		}.initIndependentStat().registerStat();
	    
	}


	public static class AchievementIE extends Achievement
	{
		public static List<Achievement> achievements = new ArrayList();
		public AchievementIE(String name, int x, int y, ItemStack icon, Achievement parent)
		{
			super("achievement.immersiveengineering."+name, "immersiveengineering." + name, x, y, icon, parent);
			achievements.add(this);
			registerStat();
		}
		public AchievementIE(String name, int x, int y, Item icon, Achievement parent)
		{
			this(name, x, y, new ItemStack(icon), parent);
		}
		public AchievementIE(String name, int x, int y, Block icon, Achievement parent)
		{
			this(name, x, y, new ItemStack(icon), parent);
		}
	}
}