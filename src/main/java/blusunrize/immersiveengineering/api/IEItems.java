/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.tool.ToolUpgrades;
import blusunrize.immersiveengineering.common.items.ItemFaradaySuit;
import blusunrize.immersiveengineering.common.items.ItemIEBase;
import blusunrize.immersiveengineering.common.items.ItemMaterial;
import net.minecraft.item.Item;

import java.util.EnumMap;
import java.util.Map;

public class IEItems
{
	//Components and materials
	public static ItemIEBase stickTreated;
	public static ItemIEBase stickIron;
	public static ItemIEBase stickSteel;
	public static ItemIEBase stickAluminum;
	public static ItemIEBase hempFiber;
	public static ItemIEBase hempFabric;
	public static ItemIEBase coalCoke;
	public static ItemIEBase slag;
	public static ItemIEBase componentIron;
	public static ItemIEBase componentSteel;
	public static ItemIEBase waterwheelSegment;
	public static ItemIEBase windmillBlade;
	public static ItemIEBase windmillSail;
	public static ItemIEBase woodenGrip;
	public static ItemIEBase gunpartBarrel;
	public static ItemIEBase gunpartDrum;
	public static ItemIEBase gunpartHammer;
	public static ItemIEBase dustCoke;
	public static ItemIEBase dustHopGraphite;
	public static ItemIEBase ingotHopGraphite;
	public static ItemIEBase wireCopper;
	public static ItemIEBase wireElectrum;
	public static ItemIEBase wireAluminum;
	public static ItemIEBase wireSteel;
	public static ItemIEBase dustSaltpeter;
	public static ItemIEBase dustSulfur;
	public static ItemIEBase electronTube;
	public static ItemIEBase circuitBoard;
	
	//Metal
	public static ItemIEBase ingotCopper;
	public static ItemIEBase ingotAluminum;
	public static ItemIEBase ingotLead;
	public static ItemIEBase ingotSilver;
	public static ItemIEBase ingotNickel;
	public static ItemIEBase ingotUranium;
	public static ItemIEBase ingotConstantan;
	public static ItemIEBase ingotElectrum;
	public static ItemIEBase ingotSteel;
	public static ItemIEBase dustCopper;
	public static ItemIEBase dustAluminum;
	public static ItemIEBase dustLead;
	public static ItemIEBase dustSilver;
	public static ItemIEBase dustNickel;
	public static ItemIEBase dustUranium;
	public static ItemIEBase dustConstantan;
	public static ItemIEBase dustElectrum;
	public static ItemIEBase dustSteel;
	public static ItemIEBase dustIron;
	public static ItemIEBase dustGold;
	public static ItemIEBase nuggetCopper;
	public static ItemIEBase nuggetAluminum;
	public static ItemIEBase nuggetLead;
	public static ItemIEBase nuggetSilver;
	public static ItemIEBase nuggetNickel;
	public static ItemIEBase nuggetUranium;
	public static ItemIEBase nuggetConstantan;
	public static ItemIEBase nuggetElectrum;
	public static ItemIEBase nuggetSteel;
	public static ItemIEBase nuggetIron;
	public static ItemIEBase plateCopper;
	public static ItemIEBase plateAluminum;
	public static ItemIEBase plateLead;
	public static ItemIEBase plateSilver;
	public static ItemIEBase plateNickel;
	public static ItemIEBase plateUranium;
	public static ItemIEBase plateConstantan;
	public static ItemIEBase plateElectrum;
	public static ItemIEBase plateSteel;
	public static ItemIEBase plateIron;
	public static ItemIEBase plateGold;

	public static ItemIEBase manual;
	public static ItemIEBase wirecutter;
	public static ItemIEBase voltmeter;
	public static ItemIEBase hammer;
	public static ItemIEBase toolbox;
	public static ItemIEBase wireCoilCopper;
	public static ItemIEBase wireCoilElectrum;
	public static ItemIEBase wireCoilHV;
	public static ItemIEBase wireCoilStructual;
	public static ItemIEBase wireCoilRope;
	public static ItemIEBase wireCoilRedstone;
	public static ItemIEBase wireCoilCopperInsulated;
	public static ItemIEBase wireCoilElectrumInsulated;
	public static ItemIEBase hempSeeds;
	public static ItemIEBase drill;
	public static ItemIEBase drillheadIron;
	public static ItemIEBase drillheadSteel;
	public static ItemIEBase jerrycan;
	public static ItemIEBase moldPlate;
	public static ItemIEBase moldGear;
	public static ItemIEBase moldRod;
	public static ItemIEBase moldBulletCasing;
	public static ItemIEBase moldWire;
	public static ItemIEBase moldPacking4;
	public static ItemIEBase moldPacking9;
	public static ItemIEBase moldUnpacking;
	public static ItemIEBase blueprint;
	public static ItemIEBase revolver;
	public static ItemIEBase speedloader;
	public static ItemIEBase casing;
	public static ItemIEBase shell;
	public static ItemIEBase bullet;//TODO!!!!
	public static ItemIEBase chemthrower;
	public static ItemIEBase railgun;
	public static ItemIEBase skyhook;
	public static Map<ToolUpgrades, ItemIEBase> toolUpgrades = new EnumMap<>(ToolUpgrades.class);
	public static ItemIEBase shader;
	public static ItemIEBase shaderBag;
	public static Item earmuffs;
	public static ItemIEBase coresample;
	public static ItemIEBase graphiteElectrode;
	public static ItemFaradaySuit[] faradaySuit = new ItemFaradaySuit[4];
	public static ItemIEBase fluorescentTube;
	public static Item powerpack;
	public static ItemIEBase shield;

	//Fake items for achievement labels
	public static ItemIEBase fakeBirthday;
	public static ItemIEBase fakeLucky;
}