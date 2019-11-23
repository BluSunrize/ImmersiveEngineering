/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class IEItems
{
	private IEItems()
	{
	}

	public static final class Molds
	{
		public static Item moldPlate;
		public static Item moldGear;
		public static Item moldRod;
		public static Item moldBulletCasing;
		public static Item moldWire;
		public static Item moldPacking4;
		public static Item moldPacking9;
		public static Item moldUnpacking;
	}

	public static final class Ingredients
	{
		public static Item stickTreated;
		public static Item stickIron;
		public static Item stickSteel;
		public static Item stickAluminum;
		public static Item hempFiber;
		public static Item hempFabric;
		public static Item coalCoke;
		public static Item slag;
		public static Item componentIron;
		public static Item componentSteel;
		public static Item waterwheelSegment;
		public static Item windmillBlade;
		public static Item windmillSail;
		public static Item woodenGrip;
		public static Item gunpartBarrel;
		public static Item gunpartDrum;
		public static Item gunpartHammer;
		public static Item dustCoke;
		public static Item dustHopGraphite;
		public static Item ingotHopGraphite;
		public static Item wireCopper;
		public static Item wireElectrum;
		public static Item wireAluminum;
		public static Item wireSteel;
		public static Item dustSaltpeter;
		public static Item dustSulfur;
		public static Item electronTube;
		public static Item circuitBoard;
		public static Item emptyCasing;
		public static Item emptyShell;
	}

	public static final class Metals
	{
		public static Map<EnumMetals, Item> ingots = new EnumMap<>(EnumMetals.class);
		public static Map<EnumMetals, Item> nuggets = new EnumMap<>(EnumMetals.class);
		public static Map<EnumMetals, Item> dusts = new EnumMap<>(EnumMetals.class);
		public static Map<EnumMetals, Item> plates = new EnumMap<>(EnumMetals.class);
	}

	public static final class Tools
	{
		public static Item hammer;
		public static Item wirecutter;
		public static Item manual;
		public static Item voltmeter;

		public static Item steelPick;
		public static Item steelShovel;
		public static Item steelAxe;
		public static Item steelSword;
		public static Item toolbox;

		public static Item drill;
		public static Item drillheadSteel;
		public static Item drillheadIron;

	}

	public static final class Weapons
	{
		public static Item revolver;
		public static Item speedloader;
		public static Map<IBullet, Item> bullets = new IdentityHashMap<>();
		public static Item chemthrower;
		public static Item railgun;

	}

	//TODO move all of these somewhere else
	public static final class Misc
	{
		public static Map<WireType, Item> wireCoils = new LinkedHashMap<>();
		public static Map<ToolUpgrade, Item> toolUpgrades = new EnumMap<>(ToolUpgrade.class);

		public static Item hempSeeds;
		public static Item jerrycan;
		public static Item blueprint;
		public static Item skyhook;
		public static Item shader;
		public static Map<Rarity, Item> shaderBag = new EnumMap<>(Rarity.class);
		public static Item earmuffs;
		public static Item coresample;
		public static Item graphiteElectrode;
		public static Item[] faradaySuit = new ItemFaradaySuit[4];
		public static Item fluorescentTube;
		public static Item powerpack;
		public static Item shield;
		public static Item maintenanceKit;

		public static Item iconBirthday;
		public static Item iconLucky;
		public static Item iconDrillbreak;

	}
}
