/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualPages;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class GeneralComputerHelper
{
	private static boolean added = false;

	public static void addComputerManualContent()
	{
		if(added)
			return;
		added = true;
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
		{
			ManualHelper.getManual().addEntry("computer.general", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.general0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.general1"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.general2"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.general3"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.general4"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.general5"));
			ManualHelper.getManual().addEntry("computer.arcfurnace", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.arcFurnace0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.arcFurnace1"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.arcFurnace2"));
			ManualHelper.getManual().addEntry("computer.bottlingmachine", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.bottlingmachine0"), new ManualPages.Text(ManualHelper.getManual(), "computer.bottlingmachine1"));
			ManualHelper.getManual().addEntry("computer.sampleDrill", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.sampleDrill0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.sampleDrill1"));
			ManualHelper.getManual().addEntry("computer.crusher", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.crusher0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.crusher1"));
			ManualHelper.getManual().addEntry("computer.dieselgen", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.dieselgen0"));
			ManualHelper.getManual().addEntry("computer.energymeter", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.energymeter0"));
			ManualHelper.getManual().addEntry("computer.excavator", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.excavator0"));
			ManualHelper.getManual().addEntry("computer.squeezerAndFermenter", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.squeezerAndFermenter0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.squeezerAndFermenter1"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.squeezerAndFermenter2"));
			ManualHelper.getManual().addEntry("computer.floodlight", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.floodlight0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.floodlight1"));
			ManualHelper.getManual().addEntry("computer.refinery", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.refinery0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.refinery1"));
			ManualHelper.getManual().addEntry("computer.assembler", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.assembler0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.assembler1"));
			ManualHelper.getManual().addEntry("computer.mixer", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.mixer0"), new ManualPages.Text(ManualHelper.getManual(), "computer.mixer1"));
//			ManualHelper.getManual().addEntry("computer.bottlingMachine", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.bottlingMachine0"),
//					new ManualPages.Text(ManualHelper.getManual(), "computer.bottlingMachine1"),
//					new ManualPages.Text(ManualHelper.getManual(), "computer.bottlingMachine2"));
			ManualHelper.getManual().addEntry("computer.capacitor", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.capacitor0"));
			ManualHelper.getManual().addEntry("computer.teslaCoil", "computers", new ManualPages.Text(ManualHelper.getManual(), "computer.teslaCoil0"));
		}
	}
}
